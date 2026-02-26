package com.cjenm.confluence.mcp;

import com.cjenm.confluence.mcp.tool.ConverterTool;
import com.cjenm.confluence.mcp.tool.TemplateTool;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class McpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }

    @Bean
    public ToolCallbackProvider converterTools(ConverterTool converterTool) {
        return MethodToolCallbackProvider.builder().toolObjects(converterTool).build();
    }

    @Bean
    public ToolCallbackProvider templateTools(TemplateTool templateTool) {
        return MethodToolCallbackProvider.builder().toolObjects(templateTool).build();
    }
}
