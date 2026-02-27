package com.example.test_emneddings_langchain.service;

import com.example.test_emneddings_langchain.config.EmbeddingModelProperties;
import com.example.test_emneddings_langchain.model.Document;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.client6.v1.api.collections.CollectionHandle;
import io.weaviate.client6.v1.api.collections.Property;
import io.weaviate.client6.v1.api.collections.VectorConfig;
import io.weaviate.client6.v1.api.collections.query.QueryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static io.weaviate.client6.v1.api.collections.vectorizers.Text2VecOpenAiVectorizer.TEXT_EMBEDDING_3_SMALL;

@Slf4j
@Service
public class WeaviateService {

    private static final String COLLECTION_NAME = "Document";
    private final Map<String, WeaviateClient> clientCache = new HashMap<>();

    private final EmbeddingModelProperties properties;

    public WeaviateService(EmbeddingModelProperties properties) {
        this.properties = properties;
    }

    /**
     * Test connection to Weaviate instance
     */
    public boolean testConnection(String url, String apiKey) {
        try {
            WeaviateClient testClient = createClient(url, apiKey);

            // Try to check if the server is ready
            var meta = testClient.meta();

            if (meta != null) {
                log.info("Successfully connected to Weaviate at {}", url);
                return true;
            }

            return false;
        } catch (Exception e) {
            log.error("Failed to connect to Weaviate: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Create the collection
     */
    public void createCollection(String url, String apiKey) throws IOException {
        WeaviateClient client = createClient(url, apiKey);
        boolean isCloud = url != null && url.contains("weaviate.cloud");

        log.info("Creating Weaviate collection {} with vectorizer: {}",
                COLLECTION_NAME, "text2vec-openai");

        // Delete if exists
        if (client.collections.exists(COLLECTION_NAME)) {
            client.collections.delete(COLLECTION_NAME);
            log.info("Deleted existing collection {}", COLLECTION_NAME);
        }

        Map.Entry<String, VectorConfig> vectorConfig = VectorConfig.text2vecOpenAi(config -> config
                .model("text-embedding-3-small")
        );

        client.collections.create(COLLECTION_NAME, col -> col
                .description("E-commerce document collection")
                .vectorConfig(vectorConfig)
                .properties(
                        Property.text("identifier", p -> p.description("Identifier of the document")),
                        Property.text("text", p -> p.description("Document text content")),
                        Property.text("metadata", p -> p.description("Document metadata"))
                )
        );

        log.info("Collection created successfully");
    }

    public void createCollectionWithDial(String url, String apiKey) throws IOException {
        WeaviateClient client = createDialClient(url, apiKey);
        boolean isCloud = url != null && url.contains("weaviate.cloud");

        log.info("Creating Weaviate collection {} with vectorizer: {}",
                "Document2", "text2vec-openai");

        // Delete if exists
        if (client.collections.exists("Document2")) {
            client.collections.delete("Document2");
            log.info("Deleted existing collection {}", "Document2");
        }

        Map.Entry<String, VectorConfig> vectorConfig = VectorConfig.text2vecOpenAi(config -> config
                .baseUrl("https://ai-proxy.lab.epam.com/openai/deployments/")
                .model(TEXT_EMBEDDING_3_SMALL)
        );

        client.collections.create("Document2", col -> col
                .description("E-commerce document collection")
                .vectorConfig(vectorConfig)
                .properties(
                        Property.text("identifier", p -> p.description("Identifier of the document")),
                        Property.text("text", p -> p.description("Document text content")),
                        Property.text("metadata", p -> p.description("Document metadata"))
                )
        );

        log.info("Collection created successfully");
    }

    /**
     * Import data
     */
    public int importData(String url, String apiKey) throws IOException {
        WeaviateClient client = createClient(url, apiKey);

        log.info("Loading documents from JSON file...");

        ObjectMapper objectMapper = new ObjectMapper();
        ClassPathResource resource = new ClassPathResource("documents.json");

        List<Document> documents = objectMapper.readValue(
                resource.getInputStream(),
                new TypeReference<>() {}
        );

        log.info("Loaded {} documents, starting batch import...", documents.size());

        // Get collection handle
        CollectionHandle<Map<String, Object>> collection = client.collections.use(COLLECTION_NAME);

        // Prepare batch data
        List<Map<String, Object>> dataObjects = new ArrayList<>();

        for (Document document : documents) {
            Map<String, Object> properties = new HashMap<>();
            properties.put("identifier", document.getId());
            properties.put("text", document.getText());
//            properties.put("metadata", document.metadata());

            dataObjects.add(properties);
        }

        // Batch insert
        var response = collection.data.insertMany(dataObjects.toArray(new Map[0]));

        if (!response.errors().isEmpty()) {
            log.error("Batch import had {} errors", response.errors().size());
            response.errors().forEach(error -> log.error("Import error: {}", error));
            throw new RuntimeException("Batch import failed with " + response.errors().size() + " errors");
        }

        log.info("Successfully imported {} documents to Weaviate", documents.size());
        return documents.size();
    }

    public int importDataWithDial(String url, String apiKey) throws IOException {
        WeaviateClient client = createDialClient(url, apiKey);

        log.info("Loading documents from JSON file...");

        ObjectMapper objectMapper = new ObjectMapper();
        ClassPathResource resource = new ClassPathResource("documents.json");

        List<Document> documents = objectMapper.readValue(
                resource.getInputStream(),
                new TypeReference<>() {}
        );

        log.info("Loaded {} documents, starting batch import...", documents.size());

        // Get collection handle
        CollectionHandle<Map<String, Object>> collection = client.collections.use(COLLECTION_NAME);

        // Prepare batch data
        List<Map<String, Object>> dataObjects = new ArrayList<>();

        for (Document document : documents) {
            Map<String, Object> properties = new HashMap<>();
            properties.put("identifier", document.getId());
            properties.put("text", document.getText());
//            properties.put("metadata", document.metadata());

            dataObjects.add(properties);
        }

        // Batch insert
        var response = collection.data.insertMany(dataObjects.toArray(new Map[0]));

        if (!response.errors().isEmpty()) {
            log.error("Batch import had {} errors", response.errors().size());
            response.errors().forEach(error -> log.error("Import error: {}", error));
            throw new RuntimeException("Batch import failed with " + response.errors().size() + " errors");
        }

        log.info("Successfully imported {} documents to Weaviate", documents.size());
        return documents.size();
    }

    /**
     * Check if collection exists
     */
    public boolean collectionExists(String url, String apiKey) {
        try {
            WeaviateClient client = createClient(url, apiKey);
            return client.collections.exists(COLLECTION_NAME);
        } catch (Exception e) {
            log.error("Error checking collection existence: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get or create a Weaviate client with the provided credentials
     */
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

    private WeaviateClient createDialClient(String url, String apiKey) {
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
                        .setHeaders(Map.of("Api-Key", properties.getApiKey(),
                                "X-Openai-Api-Key", properties.getApiKey()))
                );
            }
        });
    }

    public List<Document> searchByHybrid(String query) {
        log.info("Searching by hybrid: {}", query);

        try {
            WeaviateClient client = createClient("localhost:8080", "");
            CollectionHandle<Map<String, Object>> collection = client.collections.use(COLLECTION_NAME);

            var response = collection.query.hybrid(query, q -> q
                    .alpha(0.5f)
                    .maxVectorDistance(0.8f)
                    .limit(20));

            return extractDocuments(response);
        } catch (Exception e) {
            log.error("Error in hybrid search", e);
            return Collections.emptyList();
        }
    }

    private List<Document> extractDocuments(QueryResponse<Map<String, Object>> response) {
        try {
            return response.objects().stream()
                    .map(obj -> {
                        Document document = new Document();

                        // Extract UUID from metadata
                        if (obj != null && obj.uuid() != null) {
                            document.setId(obj.uuid());
                        }

                        // Extract properties
                        Map<String, Object> props = obj.properties();
                        if (props != null) {
                            document.setText((String) props.get("text"));
                        }

                        return document;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error extracting documents from response", e);
            return Collections.emptyList();
        }
    }
}
