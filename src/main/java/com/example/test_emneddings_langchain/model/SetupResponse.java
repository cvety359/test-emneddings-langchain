package com.example.test_emneddings_langchain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetupResponse {
    private boolean success;
    private String message;
    private Object data;

    public SetupResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
