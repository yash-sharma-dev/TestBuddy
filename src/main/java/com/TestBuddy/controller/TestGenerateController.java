package com.TestBuddy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@CrossOrigin(origins = "*")
public class TestGenerateController {

    //  Store uploaded test cases in memory
    private List<Map<String, Object>> storedTests = new ArrayList<>();

    @PostMapping("/api/spec/upload")
    public Map<String, Object> uploadSpec(@RequestParam("file") MultipartFile file) throws IOException {

        String content = new String(file.getBytes());

        ObjectMapper mapper = new ObjectMapper();
        storedTests = mapper.readValue(content, List.class);

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);

        Map<String, Object> data = new HashMap<>();
        data.put("runId", UUID.randomUUID().toString());
        data.put("title", "Uploaded JSON");
        data.put("endpointCount", storedTests.size());

        // extract endpoints
        List<String> endpoints = new ArrayList<>();
        for (Map<String, Object> tc : storedTests) {
            endpoints.add(tc.get("endpoint").toString());
        }
        data.put("endpoints", endpoints);

        res.put("data", data);

        return res;
    }


    @PostMapping("/api/tests/generate")
    public Map<String, Object> generateTests(@RequestBody Map<String, Object> body) {

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("data", storedTests); // 🔥 return uploaded tests

        return res;
    }
}