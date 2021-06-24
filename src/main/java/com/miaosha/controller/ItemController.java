package com.miaosha.controller;


import com.miaosha.response.CommonReturnType;
import com.miaosha.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/item")
@RestController
@CrossOrigin(originPatterns = "*", allowCredentials = "true", allowedHeaders = "*")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @RequestMapping("/create")
    public CommonReturnType create(@RequestParam(name = "title") String title,
                                   @RequestParam(name = "price") Double price,
                                   @RequestParam(name = "description") String description,
                                   @RequestParam(name = "stock") Integer stock,
                                   @RequestParam(name = "imgUrl") String imgUrl) {
        return CommonReturnType.create(null);
    }

}
