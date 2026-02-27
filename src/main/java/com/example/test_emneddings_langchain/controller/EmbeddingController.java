package com.example.test_emneddings_langchain.controller;

import com.example.test_emneddings_langchain.service.LangchainService;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/embedding")
public class EmbeddingController {

    private final LangchainService langchainService;

    public EmbeddingController(LangchainService langchainService) {
        this.langchainService = langchainService;
    }

    @GetMapping
    public List<Map<String, List<String>>> embedding() {
        // JIRA
        List<String> jiraTickets = List.of(
                """
                        JIRA-101: Data storage strategies: \
                         Description: Investigate and test storing strategies:
                        
                        * Embedding models
                        * Store data - only links or store data text""",
                """
                        JIRA-102: Git integration for log analysis agent \
                        Description: Investigate and implement git integration for the needs of the log analysis agent. Currently identified needs are:
                        
                        * Fetch branch info (owner, commits, descriptions of those commits)
                        * Fetch logs for a GH workflow execution (job_id)""",
                "JIRA-103: Authentication service overview and token lifecycle",
                "JIRA-104: User cannot reset password via email",
                "JIRA-105: Update UI color palette",
                "JIRA-106: Quarterly billing reconciliation process",
                "JIRA-107: Fix session timeout calculation on iOS devices"
        );

        // GIT
        List<String> gitPRs = List.of(
                "Change data storage strategy to include text snippets " +
                        "Description: Updated the data storage strategy to store text snippets alongside links. This change aims to improve retrieval efficiency and context relevance when accessing stored data. The new approach leverages embedding models to enhance search capabilities.",
                "Implement git integration for log analysis agent " +
                        "Description: Added git integration to the log analysis agent. The agent can now fetch branch information including owner details, commit history, and descriptions. Additionally, it can retrieve logs for specific GitHub workflow executions using job IDs, enhancing its diagnostic capabilities.",
                "Fix session timeout calculation on iOS devices " +
                        "Description: Resolved an issue with session timeout calculations on iOS devices. The fix ensures that user sessions now expire correctly after the designated period, improving security and user experience.",
                "Improve authentication service token lifecycle management " +
                        "Description: Enhanced the token lifecycle management in the authentication service. Implemented new strategies for token issuance, renewal, and revocation to bolster security and streamline user access."
        );

        // Confluence
        List<String> confluencePage = List.of(
                "Confluence-201: Data Storage Strategies Overview: " +
                        "This page provides an overview of various data storage strategies, including the use of embedding models and the pros and cons of storing data as links versus text snippets. It aims to guide decision-making for optimal data management.",
                "Confluence-202: Git Integration for Log Analysis Agent: " +
                        "This document outlines the integration of git functionalities into the log analysis agent. It details how to fetch branch information, commit histories, and logs from GitHub workflow executions to enhance the agent's capabilities."
        );

        List<String> inputs = Stream.of(jiraTickets, gitPRs, confluencePage)
                .flatMap(List::stream)
                .toList();

        List<String> promts = List.of(
                "How to store data using embedding models?",
                "Git integration for log analysis agent",
                "Fix session timeout calculation"
        );

        return langchainService.weaviateResponse(inputs, promts);
    }
}
