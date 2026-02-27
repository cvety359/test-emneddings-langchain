package com.example.test_emneddings_langchain.client;

import com.example.test_emneddings_langchain.config.EmbeddingModelProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class DialEmbeddingClient {

    private static final String API_URL = "https://ai-proxy.lab.epam.com/openai/deployments/%s/embeddings";

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String endpoint;
    private final String apiKey;

    public DialEmbeddingClient(EmbeddingModelProperties properties) {
        if (properties.getApiKey() == null || properties.getApiKey().trim().isEmpty()) {
            throw new IllegalArgumentException("API key cannot be null or empty");
        }
        this.endpoint = String.format(properties.getBaseUrl(), properties.getEmbeddingModelName());
        this.apiKey = properties.getApiKey();
    }

    public List<double[]> embed(List<String> inputs) throws IOException {
        Map<String, Object> body = Map.of(
                "input", inputs
        );

        Request request = new Request.Builder()
                .url(endpoint)
                .addHeader("Api-Key", apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(
                        mapper.writeValueAsBytes(body),
                        MediaType.parse("application/json")
                ))
                .build();

        try (Response response = client.newCall(request).execute()) {
            JsonNode root = mapper.readTree(response.body().string());
            List<double[]> embeddings = new ArrayList<>();

            for (JsonNode item : root.get("data")) {
                double[] vector = new double[item.get("embedding").size()];
                for (int i = 0; i < vector.length; i++) {
                    vector[i] = item.get("embedding").get(i).asDouble();
                }
                embeddings.add(vector);
            }
            return embeddings;
        }
    }
}
