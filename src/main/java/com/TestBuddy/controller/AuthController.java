package com.TestBuddy.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthController {

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> req) {

        Map<String, String> res = new HashMap<>();

        if ("admin".equals(req.get("username")) &&
                "1234".equals(req.get("password"))) {
            res.put("status", "success");
        } else {
            res.put("status", "fail");
        }

        return res;
    }
}