package com.bajaj.IPMS.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/secure/hello")
    public String secureHello() {
        return "Hello, you have accessed a secure endpoint!";
    }
}
