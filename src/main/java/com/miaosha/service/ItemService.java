package com.miaosha.service;

import com.miaosha.error.BusinessException;
import com.miaosha.service.model.ItemModel;

import java.util.List;

public interface ItemService {

    //创建商品
    public ItemModel create(ItemModel itemModel) throws BusinessException;

    //商品列表浏览
    public List<ItemModel> getItems();

    //浏览商品详情
    public ItemModel getItemDetail(int id);
}
