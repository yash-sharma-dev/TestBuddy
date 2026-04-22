package com.TestBuddy.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class TestService {

    private static final Logger log = LoggerFactory.getLogger(TestService.class);

    public List<Map<String, String>> runTests(List<Map<String, Object>> testCases) {

        List<Map<String, String>> results = new ArrayList<>();
        RestTemplate restTemplate = new RestTemplate();

        for (Map<String, Object> testCase : testCases) {

            String name = (String) testCase.get("name");
            String endpoint = (String) testCase.get("endpoint");
            String method = (String) testCase.get("method");

            String baseUrl = (String) testCase.get("baseUrl");
            if (baseUrl == null) baseUrl = "http://localhost:8080";

            String url = baseUrl + endpoint;

            Map<String, Object> expected =
                    (Map<String, Object>) testCase.get("expected");

            Map<String, Object> actual = null;

            String result = "FAIL";

            log.info("Running test: {}", name);
            log.info("Endpoint: {}, Method: {}", endpoint, method);

            try {

                // 🔹 POST
                if ("POST".equalsIgnoreCase(method)) {

                    Map<String, Object> body =
                            (Map<String, Object>) testCase.get("body");

                    log.info("Request Body: {}", body);

                    ResponseEntity<Map> response =
                            restTemplate.postForEntity(url, body, Map.class);

                    actual = response.getBody();

                    log.info("Response: {}", actual);
                }

                // 🔹 GET
                else if ("GET".equalsIgnoreCase(method)) {

                    ResponseEntity<List> response =
                            restTemplate.getForEntity(url, List.class);

                    List data = response.getBody();

                    log.info("Response: {}", data);

                    if (data != null && !data.isEmpty()) {
                        result = "PASS";
                    }
                }

                // 🔹 Compare for POST
                if (actual != null && expected != null) {

                    Object expectedStatus = expected.get("status");
                    Object actualStatus = actual.get("status");

                    if (expectedStatus != null &&
                            expectedStatus.equals(actualStatus)) {
                        result = "PASS";
                    }
                }

                log.info("Final Result: {}", result);
                log.info("-----------------------------");

                Map<String, String> res = new LinkedHashMap<>();
                res.put("test", name);
                res.put("result", result);

                results.add(res);

            } catch (Exception e) {

                log.error("Error while running test: {}", name);
                e.printStackTrace();

                Map<String, String> res = new LinkedHashMap<>();
                res.put("test", name);
                res.put("result", "ERROR");

                results.add(res);
            }
        }

        return results;
    }
}