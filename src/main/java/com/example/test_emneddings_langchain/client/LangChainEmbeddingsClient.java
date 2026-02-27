package com.example.test_emneddings_langchain.client;

import com.example.test_emneddings_langchain.config.EmbeddingModelProperties;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class LangChainEmbeddingsClient {

    private final OpenAiEmbeddingModel embeddingModel;

    public LangChainEmbeddingsClient(EmbeddingModelProperties properties) {
        this.embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(properties.getApiKey())
                .baseUrl(String.format(properties.getBaseUrl(), properties.getEmbeddingModelName()))
                .customHeaders(Map.of("Api-Key", properties.getApiKey()))
                .build();
    }

    public Embedding getEmbedding(String input) {
        return embeddingModel.embed(input).content();
    }

    public List<Embedding> getEmbeddings(List<TextSegment> segments) {
        return embeddingModel.embedAll(segments).content();
    }

    public List<Embedding> getEmbeddings(List<String> inputs, int chunkSize, int overlap) {
        List<TextSegment> segments = new ArrayList<>();
        for (String input : inputs) {
            List<TextSegment> chunks = DocumentSplitters.recursive(chunkSize, overlap).split(Document.from(input));
            segments.addAll(chunks);
        }
        return getEmbeddings(segments);
    }

    public List<Embedding> getEmbeddingsWithChunks(List<String> inputs, int chunkSize, int overlap) {
        List<TextSegment> segments = new ArrayList<>();
        for (String input : inputs) {
            List<TextSegment> chunks = chunkText(input, chunkSize, overlap).stream()
                    .map(TextSegment::from)
                    .toList();
            segments.addAll(chunks);
        }
        return getEmbeddings(segments);
    }

    public Map<Integer, List<Float>> getEmbeddingsMap(List<TextSegment> inputs) {
        List<Embedding> embeddings = embeddingModel.embedAll(inputs).content();
        Map<Integer, List<Float>> result = new HashMap<>();
        for (int i = 0; i < embeddings.size(); i++) {
            result.put(i, embeddings.get(i).vectorAsList());
        }
        return result;
    }

    public List<String> chunkText(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        int length = text.length();
        int start = 0;
        while (start < length) {
            int end = Math.min(start + chunkSize, length);
            chunks.add(text.substring(start, end));
            if (end == length) break;
            start += chunkSize - overlap;
        }
        return chunks;
    }
}
