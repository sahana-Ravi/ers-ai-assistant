package com.volvotrucks.ers_ai_assistant.controller;


import org.springframework.ai.chat.cache.semantic.SemanticCacheAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.ai.tool.ToolCallbackProvider;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatClient chatClient;

    public final VectorStore vectorStore;

    private final ToolCallbackProvider mcpTools;

    @Value("classpath:/promptTemplates/systemPromptTemplates.st")
    Resource promptTemplate;

    public ChatController(
            ChatClient.Builder chatClientBuilder, ChatMemory chatMemory, VectorStore vectorStore, SemanticCacheAdvisor semanticCacheAdvisor, ToolCallbackProvider mcpTools) {
        this.mcpTools = mcpTools;
        Advisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();

        this.chatClient = chatClientBuilder.defaultAdvisors(List.of(memoryAdvisor, semanticCacheAdvisor, new SimpleLoggerAdvisor())).build();
        this.vectorStore = vectorStore;
    }

    @PostMapping
    public String chat(@RequestParam String message) {
        SearchRequest sr = SearchRequest.builder().query(message).topK(3).similarityThreshold(0.5).build();
        List<Document> docs = vectorStore.similaritySearch(sr);
        String similarContext =  docs.stream().map(Document::getText).collect(Collectors.joining(System.lineSeparator()));

        return chatClient
                .prompt(message)
                .tools(mcpTools)
                .system(promptSystemSpec -> promptSystemSpec.text(promptTemplate).param("documents", similarContext))
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID,"default")).call().content();
    }
    @GetMapping("/tools")
    public List<String> tools() {
        return Arrays.stream(mcpTools.getToolCallbacks())
                .map(tool -> tool.getToolDefinition().name())
                .toList();
    }
}