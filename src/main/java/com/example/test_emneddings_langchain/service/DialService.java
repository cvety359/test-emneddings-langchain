package com.example.test_emneddings_langchain.service;

import com.example.test_emneddings_langchain.client.DialEmbeddingClient;
import com.example.test_emneddings_langchain.client.LangChainEmbeddingsClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class DialService {

    private final DialEmbeddingClient embeddingsClient;

    @Autowired
    public DialService(DialEmbeddingClient embeddingsClient) {
        this.embeddingsClient = embeddingsClient;
    }

    void dialResponse(List<String> inputs, List<String> promts) throws IOException {
        List<double[]> vectors = embeddingsClient.embed(inputs);

        for (int i = 0; i < promts.size(); i++) {
            final int queryIndex = i;

            System.out.println("\nQUERY: " + promts.get(queryIndex));

            List<Integer> ranked = IntStream.range(0, inputs.size())
                    .boxed()
                    .filter(j -> j != queryIndex)
                    .sorted((a, b) ->
                            Double.compare(
                                    cosine(vectors.get(queryIndex), vectors.get(b)),
                                    cosine(vectors.get(queryIndex), vectors.get(a))
                            )
                    )
                    .toList();

            for (int k = 0; k < 3; k++) {
                int j = ranked.get(k);
                System.out.printf(
                        "  %.3f  %s%n",
                        cosine(vectors.get(queryIndex), vectors.get(j)),
                        inputs.get(j)
                );
            }
        }
    }

    double cosine(double[] a, double[] b) {
        double dot = 0.0, normA = 0.0, normB = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
