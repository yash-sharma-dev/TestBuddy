package com.testai.ai_api_tester.controller;

import com.testai.ai_api_tester.dto.ApiResponse;
import com.testai.ai_api_tester.model.User;
import com.testai.ai_api_tester.model.TestRun;
import com.testai.ai_api_tester.repository.TestRunRepository;
import com.testai.ai_api_tester.repository.UserRepository;
import com.testai.ai_api_tester.service.SpecParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/spec")
@RequiredArgsConstructor
public class SpecController {

    private final SpecParserService specParserService;
    private final TestRunRepository testRunRepository;
    private final UserRepository userRepository;

    @PostMapping("/upload")
    public ApiResponse<Map<String, Object>> uploadSpec(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "environment", defaultValue = "dev") String environment
    ) {
        log.info("Uploading spec file: {} ({} bytes), environment: {}",
                file.getOriginalFilename(), file.getSize(), environment);

        Map<String, Object> parsedSpec = specParserService.parseSpec(file);

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = userRepository.findByEmail(email).map(User::getId).orElse(null);

        TestRun testRun = TestRun.builder()
                .specFilename(file.getOriginalFilename())
                .environment(environment)
                .status("SPEC_UPLOADED")
                .userId(userId)
                .build();
        testRun = testRunRepository.save(testRun);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("runId", testRun.getId().toString());
        response.putAll(parsedSpec);

        log.info("Spec uploaded successfully. RunId={}, endpoints={}",
                testRun.getId(), parsedSpec.get("endpointCount"));

        return ApiResponse.ok(response);
    }
}
