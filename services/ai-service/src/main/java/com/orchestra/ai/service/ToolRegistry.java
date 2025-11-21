package com.orchestra.ai.service;

import com.orchestra.ai.tool.DictionaryLookupTool;
import com.orchestra.ai.tool.KnowledgeBaseTool;
import com.orchestra.ai.tool.SchemaLookupTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ToolRegistry {

    private final SchemaLookupTool schemaLookupTool;
    private final DictionaryLookupTool dictionaryLookupTool;
    private final KnowledgeBaseTool knowledgeBaseTool;

    public Object[] getGlobalTools() {
        // Возвращаем бины инструментов, которые Spring AI превратит в ToolCallback
        // благодаря аннотации @Tool на их методах.
        log.info("Registering global tools: SchemaLookupTool, DictionaryLookupTool, KnowledgeBaseTool");
        return new Object[]{schemaLookupTool, dictionaryLookupTool, knowledgeBaseTool};
    }
}

