package com.TestBuddy.controller;

import com.TestBuddy.Service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private TestService testService;

    @PostMapping("/run")
    public List<Map<String, String>> runTests(
            @RequestBody List<Map<String, Object>> testCases) {

        return testService.runTests(testCases);
    }
}