package com.example.test_emneddings_langchain;

import com.example.test_emneddings_langchain.config.EmbeddingModelProperties;
import com.example.test_emneddings_langchain.service.LangchainService;
import dev.langchain4j.data.segment.TextSegment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

@SpringBootApplication
@EnableConfigurationProperties(EmbeddingModelProperties.class)
public class TestEmneddingsLangchainApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestEmneddingsLangchainApplication.class, args);
	}
}
