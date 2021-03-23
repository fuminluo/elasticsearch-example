package com.example.elasticsearch.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * @Author LuoFuMin
 * @DATE 2021/3/16 10:20
 */
@Log4j2
@RestController
public class LogController {

    @GetMapping("log")
    public void testLog(String msg) {
        log.info(">>>日志信息 : {}", msg);
    }
}
