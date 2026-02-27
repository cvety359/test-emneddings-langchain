package com.example.test_emneddings_langchain.config;

import dev.langchain4j.store.embedding.weaviate.WeaviateEmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbeddingStoreConfig {

    @Bean
    public WeaviateEmbeddingStore weaviateEmbeddingStore() {
        // Initialize WeaviateEmbeddingStore with required parameters
        // Replace with actual initialization as per your needs
        return WeaviateEmbeddingStore.builder()
                .scheme("http")
                .host("localhost")
                .port(8080)
                .build();
    }
}
