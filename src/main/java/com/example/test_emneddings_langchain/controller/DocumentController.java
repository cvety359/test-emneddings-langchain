package com.example.test_emneddings_langchain.controller;

import com.example.test_emneddings_langchain.model.SetupResponse;
import com.example.test_emneddings_langchain.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/document")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/collection/create")
    public ResponseEntity<SetupResponse> createCollection() {
        log.info("Creating collection in Weaviate");

        try {
            documentService.createCollection();

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
        log.info("Importing data to Weaviate:");

        try {
            int count = documentService.importData();

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
}
