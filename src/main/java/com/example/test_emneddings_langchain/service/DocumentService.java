package com.example.test_emneddings_langchain.service;

import com.example.test_emneddings_langchain.config.EmbeddingModelProperties;
import com.example.test_emneddings_langchain.model.Document;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.client6.v1.api.collections.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private static final String COLLECTION_NAME = "Document";
    private final Map<String, WeaviateClient> clientCache = new HashMap<>();

    private final EmbeddingModelProperties properties;

    public void createCollection() {
        try (WeaviateClient client = createClient("localhost:8080", null)) {

            Map.Entry<String, VectorConfig> vectorConfig = VectorConfig.text2vecOpenAi(config -> config
                    .model("text-embedding-3-small")
            );

            client.collections.create(COLLECTION_NAME, col -> col
                    .vectorConfig(vectorConfig)
                    .properties(
                            Property.text("text", p -> p.description("Content of the document")),
                            Property.object("metadata", p -> p.description("Additional metadata for the document")
                                    .nestedProperties(
                                            Property.text("url", sp -> sp.description("URL of the document")),
                                            Property.text("source", sp -> sp.description("Source of the document")),
                                            Property.text("author", sp -> sp.description("Author of the document")),
                                            Property.text("publishedDate", sp -> sp.description("Published date of the document"))
                                    ).skipVectorization(true)
                            )
                    )
            );
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }

        log.info("Collection created successfully");
    }

    public int importData() {
        List<Document> documents;
        CollectionHandle<Map<String, Object>> collection;
        try (WeaviateClient client = createClient("localhost:8080", null)) {

            log.info("Loading documents from JSON file...");

            ObjectMapper objectMapper = new ObjectMapper();
            ClassPathResource resource = new ClassPathResource("documents.json");

            documents = objectMapper.readValue(
                    resource.getInputStream(),
                    new TypeReference<>() {
                    }
            );

            log.info("Loaded {} documents, starting batch import...", documents.size());

            // Get collection handle
            collection = client.collections.use(COLLECTION_NAME);


        // Prepare batch data
        List<Map<String, Object>> dataObjects = new ArrayList<>();

        for (Document document : documents) {
            Map<String, Object> properties = new HashMap<>();
            properties.put("text", document.getText());

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("url", document.getMetadata().getUrl());
            metadata.put("source", document.getMetadata().getSource());
            metadata.put("author", document.getMetadata().getAuthor());
            metadata.put("publishedDate", document.getMetadata().getPublishedDate());

            properties.put("metadata", metadata);

            dataObjects.add(properties);
        }

        // Batch insert
        var response = collection.data.insertMany(dataObjects.toArray(new Map[0]));

        if (!response.errors().isEmpty()) {
            log.error("Batch import had {} errors", response.errors().size());
            response.errors().forEach(error -> log.error("Import error: {}", error));
            throw new RuntimeException("Batch import failed with " + response.errors().size() + " errors");
        }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }

        log.info("Successfully imported {} documents to Weaviate", documents.size());
        return documents.size();
    }

    private WeaviateClient createClient(String url, String apiKey) {
        String cacheKey = url + "|" + (apiKey != null ? apiKey : "");

        return clientCache.computeIfAbsent(cacheKey, key -> {
            log.info("Creating new Weaviate client for: {}", url);

            // Parse URL to extract host and port
            String cleanUrl = url.replace("http://", "").replace("https://", "");
            String[] parts = cleanUrl.split(":");
            String host = parts[0];
            int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 8080;

            if (apiKey != null && !apiKey.trim().isEmpty()) {
                // Cloud connection with API key
                return WeaviateClient.connectToWeaviateCloud(
                        url,
                        apiKey,
                        config -> config
                );
            } else {
                // Local connection
                return WeaviateClient.connectToLocal(config -> config
                        .host(host)
                        .port(port)
                        .grpcPort(50051)
                        .setHeaders(Map.of("X-Openai-Api-Key", properties.getOpenAiApiKey()))
                );
            }
        });
    }
}
