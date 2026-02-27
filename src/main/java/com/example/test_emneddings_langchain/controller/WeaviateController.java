package com.example.test_emneddings_langchain.controller;

import com.example.test_emneddings_langchain.model.Document;
import com.example.test_emneddings_langchain.model.SetupResponse;
import com.example.test_emneddings_langchain.service.WeaviateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/setup")
@RequiredArgsConstructor
public class WeaviateController {

    private static final String WEAVIATE_URL = "localhost:8080";

    private final WeaviateService weaviateService;

    @PostMapping("/connect")
    public ResponseEntity<SetupResponse> testConnection() {
        log.info("Testing connection to Weaviate: {}", WEAVIATE_URL);

        try {
            boolean connected = weaviateService.testConnection(
                    WEAVIATE_URL, ""
            );

            if (connected) {
                return ResponseEntity.ok(new SetupResponse(
                        true,
                        "Successfully connected to Weaviate!"
                ));
            } else {
                return ResponseEntity.ok(new SetupResponse(
                        false,
                        "Failed to connect to Weaviate. Please check your URL and API key."
                ));
            }
        } catch (Exception e) {
            log.error("Error testing connection", e);
            return ResponseEntity.ok(new SetupResponse(
                    false,
                    "Connection error: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/collection/create")
    public ResponseEntity<SetupResponse> createCollection() {
        log.info("Creating collection in Weaviate: {}", WEAVIATE_URL);

        try {
            weaviateService.createCollection(
                    WEAVIATE_URL, ""
            );

            return ResponseEntity.ok(new SetupResponse(
                    true,
                    "Document collection created successfully!"
            ));
        } catch (Exception e) {
            log.error("Error creating collection", e);
            return ResponseEntity.ok(new SetupResponse(
                    false,
                    "Failed to create collection: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/collection/dial/create")
    public ResponseEntity<SetupResponse> createCollectionWithDial() {
        log.info("Creating collection in Weaviate: {}", WEAVIATE_URL);

        try {
            weaviateService.createCollectionWithDial(
                    WEAVIATE_URL, ""
            );

            return ResponseEntity.ok(new SetupResponse(
                    true,
                    "Document collection created successfully!"
            ));
        } catch (Exception e) {
            log.error("Error creating collection", e);
            return ResponseEntity.ok(new SetupResponse(
                    false,
                    "Failed to create collection: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/data/import")
    public ResponseEntity<SetupResponse> importData() {
        log.info("Importing data to Weaviate: {}", WEAVIATE_URL);

        try {
            int count = weaviateService.importData(
                    WEAVIATE_URL, ""
            );

            return ResponseEntity.ok(new SetupResponse(
                    true,
                    "Successfully imported " + count + " documents!"
            ));
        } catch (Exception e) {
            log.error("Error importing data", e);
            return ResponseEntity.ok(new SetupResponse(
                    false,
                    "Failed to import data: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/data/dial/import")
    public ResponseEntity<SetupResponse> importDataWithDial() {
        log.info("Importing data to Weaviate: {}", WEAVIATE_URL);

        try {
            int count = weaviateService.importDataWithDial(
                    WEAVIATE_URL, ""
            );

            return ResponseEntity.ok(new SetupResponse(
                    true,
                    "Successfully imported " + count + " documents!"
            ));
        } catch (Exception e) {
            log.error("Error importing data", e);
            return ResponseEntity.ok(new SetupResponse(
                    false,
                    "Failed to import data: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/hybrid")
    public ResponseEntity<List<Document>> searchByHybrid(
            @RequestParam String query) {
        log.info("Received hybrid search request: {}", query);
        List<Document> results = weaviateService.searchByHybrid(query);
        return ResponseEntity.ok(results);
    }
}
