package com.miaosha.service;

import com.miaosha.service.model.ItemModel;
import com.miaosha.service.model.PromoModel;

public interface PromoService {

    PromoModel getPromoByItemId(Integer itemId);

    void getPromoById(Integer id);
}
