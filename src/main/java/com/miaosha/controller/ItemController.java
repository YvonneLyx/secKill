package com.miaosha.controller;


import com.miaosha.controller.Mode.ItemVO;
import com.miaosha.error.BusinessException;
import com.miaosha.error.EmBusinessError;
import com.miaosha.response.CommonReturnType;
import com.miaosha.service.CacheService;
import com.miaosha.service.ItemService;
import com.miaosha.service.PromoService;
import com.miaosha.service.model.ItemModel;
import com.miaosha.service.model.PromoModel;
import com.miaosha.validator.ValidatorImpl;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private PromoService promoService;

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

        if(itemModel.getPromoModel() != null) {
            //有正在进行或即将进行的秒杀活动
            itemVO.setPromoStatus(itemModel.getPromoModel().getStatus());
            itemVO.setStartDate(itemModel.getPromoModel().getStartDate().toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")));
            itemVO.setPromoId(itemModel.getPromoModel().getId());
            itemVO.setPromoPrice(itemModel.getPromoModel().getPromoItemPrice());
        }else {
            itemVO.setPromoStatus(0);
        }
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

    //运营加上去的
    @RequestMapping(value = "/publishpromo", method = {RequestMethod.GET})
    public CommonReturnType publishPromo(@RequestParam(name = "id") Integer id){
        promoService.getPromoById(id);
//        redisTemplate.opsForValue().set("promo_item_stock_" + itemModel.getId(), itemModel.getStock());

        return CommonReturnType.create(null);

    }

    @RequestMapping(value = "/get_item_detail", method = {RequestMethod.GET})
    public CommonReturnType getItemDetail(@RequestParam(name = "id") Integer id) throws BusinessException {
        if(id == null) throw  new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
//        ItemModel itemModel = itemService.getItemDetail(id);
//        return CommonReturnType.create(itemModel);

        ItemModel itemModel = null;

        itemModel = (ItemModel) cacheService.getFromCommonCache("item_" + id);

        if(itemModel == null) {

            //先去redis中获取itemModel，如果redis中没有，去下游service中获取，否则直接返回给前端。
            itemModel = (ItemModel) redisTemplate.opsForValue().get("item_" + id);

            if(itemModel == null) {
                itemModel = itemService.getItemDetail(id);
                redisTemplate.opsForValue().set("item_" + id, itemModel);
            }

            cacheService.setCommonCache("item_" + id, itemModel);

        }



        ItemVO itemVO = convertItemVOFromItemModel(itemModel);
        return CommonReturnType.create(itemVO);
    }

}
