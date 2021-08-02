package com.miaosha.service.impl;

import com.miaosha.dao.PromoDOMapper;
import com.miaosha.dataobject.PromoDO;
import com.miaosha.service.ItemService;
import com.miaosha.service.model.ItemModel;
import com.miaosha.service.model.PromoModel;
import com.miaosha.service.PromoService;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PromoServiceImpl implements PromoService {

    @Autowired
    private PromoDOMapper promoDOMapper;

    @Autowired
    private ItemService itemService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public PromoModel getPromoByItemId(Integer itemId) {
        PromoDO promoDO = promoDOMapper.getPromoByItemId(itemId);
        PromoModel promoModel = convertToModel(promoDO);
        if(promoModel == null ) return null;

        if(promoModel.getEndDate().isBeforeNow()) {
            promoModel.setStatus(3); //结束秒杀
        }else if(promoModel.getStartDate().isAfterNow()) {
            promoModel.setStatus(1);  //未开始
        }else {
            promoModel.setStatus(2);
        }
        return promoModel;

    }

    //这个是活动开始前运营加上去的
    public void getPromoById(Integer id) {
        PromoDO promoDO = promoDOMapper.selectByPrimaryKey(id);
//        return convertToModel(promoDO);

        if(promoDO.getItemId() == null || promoDO.getItemId().intValue() == 0) {
            return ;
        }
        ItemModel itemModel = itemService.getItemDetail(promoDO.getItemId());
//        return itemModel;
        redisTemplate.opsForValue().set("promo_item_stock_" + itemModel.getId(), itemModel.getStock());
        return;
    }


    private PromoModel convertToModel(PromoDO promoDO) {
        if(promoDO == null) return null;
        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promoDO, promoModel);
        promoModel.setPromoItemPrice(new BigDecimal(promoDO.getPromoItemPrice()));
        promoModel.setStartDate(new DateTime(promoDO.getStartDate()));
        promoModel.setEndDate(new DateTime(promoDO.getEndDate()));
        return promoModel;
    }
}
