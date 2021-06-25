package com.miaosha.service.impl;

import com.miaosha.dao.ItemDOMapper;
import com.miaosha.dao.ItemStockDOMapper;
import com.miaosha.dataobject.ItemDO;
import com.miaosha.dataobject.ItemStockDO;
import com.miaosha.error.BusinessException;
import com.miaosha.error.EmBusinessError;
import com.miaosha.service.ItemService;
import com.miaosha.service.model.ItemModel;
import com.miaosha.validator.ValidationResult;
import com.miaosha.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemDOMapper itemDOMapper;

    @Autowired
    private ItemStockDOMapper itemStockDOMapper;

    @Autowired
    private ValidatorImpl validator;

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

        return itemModel;

//        return null;
    }
}
