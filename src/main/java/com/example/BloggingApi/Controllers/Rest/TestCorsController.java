package com.example.BloggingApi.Controllers.Rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class TestCorsController {

    @GetMapping("/test")
    public String returnString()
    {

        return "Hello, text received";
    }


}
