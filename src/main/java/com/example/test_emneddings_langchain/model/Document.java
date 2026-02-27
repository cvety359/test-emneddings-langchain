package com.example.test_emneddings_langchain.model;

import lombok.Data;

@Data
public class Document {

    private String id;
    private String text;
    private Metadata metadata;
}
