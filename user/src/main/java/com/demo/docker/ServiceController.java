package com.demo.docker;


import cn.hutool.core.map.MapUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ServiceController {

    @GetMapping("/")
    public Map<String,String> index(){
        return MapUtil.builder("hello","world").build();
    }
}
