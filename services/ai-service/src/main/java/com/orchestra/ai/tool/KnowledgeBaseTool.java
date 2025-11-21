package com.orchestra.ai.tool;

import com.orchestra.ai.service.KnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeBaseTool {

    private final KnowledgeBaseService knowledgeBaseService;

    @Tool(description = "Search the knowledge base for relevant context, examples, or business rules.")
    public String searchKnowledgeBase(@ToolParam(description = "Search query") String query) {
        log.info("Tool: Searching knowledge base for '{}'", query);
        List<Document> docs = knowledgeBaseService.search(query);
        if (docs.isEmpty()) {
            return "No relevant information found in knowledge base.";
        }
        return docs.stream().map(Document::getText).collect(Collectors.joining("\n---\n"));
    }
}

