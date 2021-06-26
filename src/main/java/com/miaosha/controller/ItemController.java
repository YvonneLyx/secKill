package com.miaosha.controller;


import com.miaosha.controller.Mode.ItemVO;
import com.miaosha.error.BusinessException;
import com.miaosha.error.EmBusinessError;
import com.miaosha.response.CommonReturnType;
import com.miaosha.service.ItemService;
import com.miaosha.service.model.ItemModel;
import com.miaosha.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/item")
@RestController("item")
@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
public class ItemController extends BaseController {

    @Autowired
    private ItemService itemService;

    @RequestMapping(value = "/create", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    public CommonReturnType create(@RequestParam(name = "title") String title,
                                   @RequestParam(name = "description") String description,
                                   @RequestParam(name = "price") BigDecimal price,
                                   @RequestParam(name = "imgUrl") String imgUrl,
                                   @RequestParam(name = "stock") Integer stock) throws BusinessException {


        ItemModel itemModel = new ItemModel();
        itemModel.setTitle(title);
        itemModel.setPrice(price);
        itemModel.setDescription(description);
        itemModel.setStock(stock);
        itemModel.setImgUrl(imgUrl);
        ItemModel itemModel1 = itemService.create(itemModel);
//        ItemModel itemModel = itemService.create();


        ItemVO itemVo = convertItemVOFromItemModel(itemModel);
        return CommonReturnType.create(itemVo);
    }

    private ItemVO convertItemVOFromItemModel(ItemModel itemModel) {
        if(itemModel == null) return null;
        ItemVO itemVO = new ItemVO();
        BeanUtils.copyProperties(itemModel, itemVO);
        return itemVO;
    }

    @RequestMapping(value = "/get_items", method = {RequestMethod.GET})
    public CommonReturnType getItems(){
        List<ItemVO> list = itemService.getItems().stream().map(itemModel -> {
            ItemVO itemVO = convertItemVOFromItemModel(itemModel);
            return  itemVO;
        }).collect(Collectors.toList());
        return CommonReturnType.create(list);
    }

    @RequestMapping(value = "/get_item_detail", method = {RequestMethod.GET})
    public CommonReturnType getItemDetail(@RequestParam(name = "id") Integer id) throws BusinessException {
        if(id == null) throw  new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        ItemModel itemModel = itemService.getItemDetail(id);
//        return CommonReturnType.create(itemModel);
        ItemVO itemVO = convertItemVOFromItemModel(itemModel);
        return CommonReturnType.create(itemVO);
    }

}
