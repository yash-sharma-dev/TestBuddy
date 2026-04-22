package com.testai.ai_api_tester.service;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SpecParserService {

    /**
     * Parse an OpenAPI spec from a MultipartFile.
     */
    public Map<String, Object> parseSpec(MultipartFile file) {
        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            return parseSpecContent(content);
        } catch (Exception e) {
            log.error("Failed to read spec file: {}", e.getMessage());
            throw new RuntimeException("Failed to read spec file: " + e.getMessage());
        }
    }

    /**
     * Parse an OpenAPI spec from a raw string.
     */
    public Map<String, Object> parseSpecContent(String content) {
        ParseOptions parseOptions = new ParseOptions();
        parseOptions.setResolve(true);

        SwaggerParseResult result = new OpenAPIV3Parser().readContents(content, null, parseOptions);
        OpenAPI openAPI = result.getOpenAPI();

        if (openAPI == null) {
            String errors = result.getMessages() != null
                    ? String.join("; ", result.getMessages())
                    : "Unknown parse error";
            log.error("Failed to parse OpenAPI spec: {}", errors);
            throw new RuntimeException("Invalid OpenAPI spec: " + errors);
        }

        String title = openAPI.getInfo() != null ? openAPI.getInfo().getTitle() : "Untitled API";
        String version = openAPI.getInfo() != null ? openAPI.getInfo().getVersion() : "unknown";

        List<Map<String, Object>> endpoints = new ArrayList<>();

        if (openAPI.getPaths() != null) {
            for (Map.Entry<String, PathItem> pathEntry : openAPI.getPaths().entrySet()) {
                String path = pathEntry.getKey();
                PathItem pathItem = pathEntry.getValue();

                extractOperations(path, pathItem, endpoints);
            }
        }

        // Extract schema names from components
        List<String> schemaNames = new ArrayList<>();
        if (openAPI.getComponents() != null && openAPI.getComponents().getSchemas() != null) {
            schemaNames.addAll(openAPI.getComponents().getSchemas().keySet());
        }

        Map<String, Object> parsed = new LinkedHashMap<>();
        parsed.put("title", title);
        parsed.put("version", version);
        parsed.put("endpointCount", endpoints.size());
        parsed.put("endpoints", endpoints);
        parsed.put("schemas", schemaNames);

        log.info("Parsed spec: '{}' v{} — {} endpoints, {} schemas",
                title, version, endpoints.size(), schemaNames.size());

        return parsed;
    }

    private void extractOperations(String path, PathItem pathItem, List<Map<String, Object>> endpoints) {
        Map<String, Operation> operations = new LinkedHashMap<>();
        if (pathItem.getGet() != null) operations.put("GET", pathItem.getGet());
        if (pathItem.getPost() != null) operations.put("POST", pathItem.getPost());
        if (pathItem.getPut() != null) operations.put("PUT", pathItem.getPut());
        if (pathItem.getDelete() != null) operations.put("DELETE", pathItem.getDelete());
        if (pathItem.getPatch() != null) operations.put("PATCH", pathItem.getPatch());

        for (Map.Entry<String, Operation> opEntry : operations.entrySet()) {
            String method = opEntry.getKey();
            Operation op = opEntry.getValue();

            Map<String, Object> endpoint = new LinkedHashMap<>();
            endpoint.put("path", path);
            endpoint.put("method", method);
            endpoint.put("operationId", op.getOperationId());
            endpoint.put("summary", op.getSummary());

            // Parameters
            List<String> paramNames = new ArrayList<>();
            if (op.getParameters() != null) {
                paramNames = op.getParameters().stream()
                        .map(p -> p.getName() + " (" + p.getIn() + ")")
                        .collect(Collectors.toList());
            }
            endpoint.put("parameters", paramNames);

            // Request body
            boolean hasRequestBody = op.getRequestBody() != null;
            endpoint.put("hasRequestBody", hasRequestBody);

            List<String> contentTypes = new ArrayList<>();
            if (hasRequestBody && op.getRequestBody().getContent() != null) {
                contentTypes.addAll(op.getRequestBody().getContent().keySet());
            }
            endpoint.put("contentTypes", contentTypes);

            // Response codes
            List<String> responseCodes = new ArrayList<>();
            ApiResponses responses = op.getResponses();
            if (responses != null) {
                responseCodes.addAll(responses.keySet());
            }
            endpoint.put("responseCodes", responseCodes);

            endpoints.add(endpoint);
        }
    }
}
