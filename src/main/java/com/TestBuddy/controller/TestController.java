package com.TestBuddy.controller;

import com.TestBuddy.Service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/test")
@CrossOrigin(origins="*")
public class TestController {

    @Autowired
    private TestService testService;

    @PostMapping("/run")
    public List<Map<String, String>> runTests(
            @RequestBody List<Map<String, Object>> testCases) {

        return testService.runTests(testCases);
    }
}