package com.example.test_emneddings_langchain.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "langchain4j.open-ai.embeddings-model")
@Getter
@Setter
public class EmbeddingModelProperties {

    private String baseUrl;

    private String apiKey;

    private String openAiApiKey;

    private String embeddingModelName;
}
