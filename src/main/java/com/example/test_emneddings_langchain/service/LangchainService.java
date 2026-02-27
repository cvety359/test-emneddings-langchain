package com.example.test_emneddings_langchain.service;

import com.example.test_emneddings_langchain.client.LangChainEmbeddingsClient;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.weaviate.WeaviateEmbeddingStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
public class LangchainService {

    private final LangChainEmbeddingsClient embeddingsClient;

    private final WeaviateEmbeddingStore weaviateEmbeddingStore;

    @Autowired
    public LangchainService(LangChainEmbeddingsClient embeddingsClient, WeaviateEmbeddingStore weaviateEmbeddingStore) {
        this.embeddingsClient = embeddingsClient;
        this.weaviateEmbeddingStore = weaviateEmbeddingStore;
    }

    public List<String> langchainResponse(List<String> inputs, List<String> prompts) {
        List<Embedding> embeddings = embeddingsClient.getEmbeddings(inputs, 500, 100);

        List<String> results = new ArrayList<>();
        for (int i = 0; i < prompts.size(); i++) {
            final int queryIndex = i;
            System.out.println("\nQUERY: " + prompts.get(queryIndex));

            List<Integer> ranked =
                    IntStream.range(0, inputs.size())
                            .boxed()
                            .filter(j -> j != queryIndex)
                            .sorted((a, b) ->
                                    Double.compare(
                                            cosine(
                                                    embeddings.get(queryIndex).vector(),
                                                    embeddings.get(b).vector()
                                            ),
                                            cosine(
                                                    embeddings.get(queryIndex).vector(),
                                                    embeddings.get(a).vector()
                                            )
                                    )
                            )
                            .toList();


            for (int k = 0; k < Math.min(3, ranked.size()); k++) {
                int j = ranked.get(k);
                results.add(String.format("%s%n  %.3f  %s%n",
                        "QUERY: " + prompts.get(queryIndex),
                        cosine(
                                embeddings.get(queryIndex).vector(),
                                embeddings.get(j).vector()
                        ),
                        inputs.get(j)));
            }
        }

        return results;
    }

    public List<Map<String, List<String>>> weaviateResponse(List<String> inputs, List<String> prompts) {
        List<TextSegment> segments = new ArrayList<>();
        for (String input : inputs) {
            List<TextSegment> chunks = DocumentSplitters.recursive(500, 100).split(Document.from(input));
            segments.addAll(chunks);
        }

        List<Embedding> embeddings = embeddingsClient.getEmbeddings(segments);

        List<String> strings = weaviateEmbeddingStore.addAll(embeddings, segments);

        List<Map<String, List<String>>> result = new ArrayList<>();
        for (String prompt : prompts) {
            System.out.println("\nQUERY: " + prompt);
            Embedding embedding = embeddingsClient.getEmbedding(prompt);
            EmbeddingSearchResult<TextSegment> results = weaviateEmbeddingStore.search(
                    EmbeddingSearchRequest.builder()
                            .queryEmbedding(embedding)
                            .maxResults(3)
                            .minScore(0.8)
                            .build()
            );

            Map<String, List<String>> resultMap = new HashMap<>();
            List<String> matchedSegments = new ArrayList<>();
            results.matches()
                    .forEach(entry ->
                            matchedSegments.add(String.format("%.3f  %s", entry.score(), entry.embedded()))
                    );
            resultMap.put(prompt, matchedSegments);
            result.add(resultMap);

//            results.matches()
//                    .forEach(
//                            entry -> System.out.printf(
//                                    "  %.3f  %s%n",
//                                    entry.score(),
//                                    entry.embedded().text()
//                            )
//                    );
        }

        return result;
    }

    double cosine(float[] a, float[] b) {
        double dot = 0.0, normA = 0.0, normB = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
