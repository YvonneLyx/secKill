package com.miaosha.service.impl;

import com.miaosha.dao.ItemDOMapper;
import com.miaosha.dao.ItemStockDOMapper;
import com.miaosha.dataobject.ItemDO;
import com.miaosha.dataobject.ItemStockDO;
import com.miaosha.error.BusinessException;
import com.miaosha.error.EmBusinessError;
import com.miaosha.mq.MqProducer;
import com.miaosha.service.ItemService;
import com.miaosha.service.PromoService;
import com.miaosha.service.model.ItemModel;
import com.miaosha.service.model.PromoModel;
import com.miaosha.validator.ValidationResult;
import com.miaosha.validator.ValidatorImpl;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private MqProducer mqProducer;

    @Autowired
    private ItemDOMapper itemDOMapper;

    @Autowired
    private ItemStockDOMapper itemStockDOMapper;

    @Autowired
    private ValidatorImpl validator;

    @Autowired
    private PromoService promoService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Transactional
    @Override
    public ItemModel create(ItemModel itemModel) throws BusinessException {
        ValidationResult result = validator.validate(itemModel);
        if(result.isHasErrors()){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, result.getErrMsg());
        }

        ItemDO itemDO = copyToItemDO(itemModel);
        itemDOMapper.insertSelective(itemDO);

        ItemStockDO itemStock = new ItemStockDO();
        itemStock.setItemId(itemDO.getId());
        itemStock.setStock(itemModel.getStock());
        itemStockDOMapper.insertSelective(itemStock);

//        return;
        //必须返回实体,返回创建完成的对象
        return getItemDetail(itemDO.getId());
//        return null;
    }

    private ItemDO copyToItemDO(ItemModel itemModel) {
        if(itemModel == null) return null;// 判断要加
        ItemDO item = new ItemDO();
        BeanUtils.copyProperties(itemModel, item);
        //因为price是bigDecimal，不能直接转，会有精度丢失
        item.setPrice(itemModel.getPrice().doubleValue());
        return item;
    }

    private ItemModel copyToItemModel(ItemDO itemDO, ItemStockDO itemStockDO) {
        if(itemDO == null) return null;
        ItemModel itemModel = new ItemModel();
        BeanUtils.copyProperties(itemDO, itemModel);

        itemModel.setPrice(new BigDecimal(itemDO.getId()));

        itemModel.setStock(itemStockDO.getStock());
        return itemModel;
    }

    @Override
    public List<ItemModel> getItems() {
//      先查询所有itemDo，遍历itemDO中的id，获取对应的itemStock，插入stock
        //可以用strea流直接映射
        List<ItemModel> list = itemDOMapper.selectList().stream().map(itemDO -> {
            ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
            ItemModel itemModel = copyToItemModel(itemDO, itemStockDO);
            return itemModel;
        }).collect(Collectors.toList());
        return list;
    }

    @Override
    public ItemModel getItemDetail(int id) {
        ItemDO itemDO = itemDOMapper.selectByPrimaryKey(id);

        ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());

        ItemModel itemModel = copyToItemModel(itemDO, itemStockDO);

        //获取活动商品信息
        PromoModel promoModel = promoService.getPromoByItemId(id);
//        System.out.println(promoModel);
        if(promoModel != null && promoModel.getStatus().intValue() != 3) {
            itemModel.setPromoModel(promoModel);
        }

        return itemModel;

//        return null;
    }

    public ItemModel getItemByIdInCache(Integer itemId) {
        ItemModel itemModel = (ItemModel) redisTemplate.opsForValue().get("item_validate_"+itemId);
        if(itemModel == null) {
            itemModel = this.getItemDetail(itemId);
            redisTemplate.opsForValue().set("item_validate_" + itemId, itemModel);
        }
        return itemModel;
    }


    @Transactional
    @Override
    public boolean decreaseStock(Integer itemId, Integer amount) {
        long i = redisTemplate.opsForValue().increment("promo_item_stock_" + itemId, amount.intValue() * -1);
//        int i = itemStockDOMapper.decreaseStock(itemId, amount);

        if(i >= 0) {
            boolean isSuccess = mqProducer.asyncReduceStock(itemId, amount);
            if(!isSuccess) {
                redisTemplate.opsForValue().increment("promo_item_stock_" + itemId, amount.intValue());
                return false;
            }else {
                return true;
            }
        }else {
            return false;
        }


    }

    @Override
    public void increaseSale(Integer id, int amount) {
        itemDOMapper.increaseSale(id, amount);
    }
}
