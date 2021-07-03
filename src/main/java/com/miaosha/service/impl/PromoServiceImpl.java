package com.miaosha.service.impl;

import com.miaosha.dao.PromoDOMapper;
import com.miaosha.dataobject.PromoDO;
import com.miaosha.service.model.PromoModel;
import com.miaosha.service.PromoService;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PromoServiceImpl implements PromoService {

    @Autowired
    private PromoDOMapper promoDOMapper;

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
