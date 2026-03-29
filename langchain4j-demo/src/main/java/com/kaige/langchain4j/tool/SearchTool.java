package com.kaige.langchain4j.tool;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;

import java.util.stream.Collectors;

/**
 * 搜索工具
 */
public class SearchTool {

    private final ContentRetriever contentRetriever;

    public SearchTool(ContentRetriever contentRetriever) {
        this.contentRetriever = contentRetriever;
    }

    @Tool("Search for technical information about LangChain4j and RAG configurations")
    public String search(String query) {
        // This logic is only executed when the LLM determines retrieval is necessary
        return contentRetriever.retrieve(new Query(query)).stream()
                .map(content -> content.textSegment().text())
                .collect(Collectors.joining("\n\n"));
    }

}
