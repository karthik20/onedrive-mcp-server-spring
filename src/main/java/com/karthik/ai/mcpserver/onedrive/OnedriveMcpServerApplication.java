package com.karthik.ai.mcpserver.onedrive;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class OnedriveMcpServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(OnedriveMcpServerApplication.class, args);
	}

	@Bean
	public ToolCallbackProvider onedriveTools(OnedriveService onedriveService) {
		return MethodToolCallbackProvider
			.builder()
			.toolObjects(onedriveService)
			.build();
	}
}
