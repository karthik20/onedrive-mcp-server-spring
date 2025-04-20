package com.karthik.ai.mcpserver.onedrive;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class OnedriveService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${onedrive.access-token}")
    private String accessToken;

    public OnedriveService() {
        this.restClient = RestClient.builder()
                .baseUrl("https://graph.microsoft.com/v1.0")
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Tool(description = "Search for files in OneDrive using a query under an optional folder and returns file name and Web URL as link and its path")
    public String searchFiles(@ToolParam(description = "the search file name in the given query") String fileName,
            @ToolParam(description = "A folder name in the search query to search files under") String folder) {
        try {
            String encodedFolder = folder != null && !folder.isEmpty() 
                ? java.net.URLEncoder.encode(folder, StandardCharsets.UTF_8.toString())
                : "";

            // Microsoft Graph API requires single quotes around the search term
            String searchQuery = String.format("'%s'", fileName);
            
            String apiPath;
            if (!encodedFolder.isEmpty()) {
                apiPath = String.format("/me/drive/root:/%s:/search(q=%s)?$select=id,name,webUrl,file,parentReference",
                        encodedFolder, searchQuery);
            } else {
                apiPath = String.format("/me/drive/root/search(q=%s)?$select=name,id,webUrl,file,parentReference",
                        searchQuery);
            }

            String response = restClient.get()
                    .uri(apiPath)
                    .header("Authorization", "Bearer " + accessToken)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);

            return filterMatchingFiles(response);
        } catch (Exception e) {
            return "Error searching files: " + e.getMessage();
        }
    }

    private String filterMatchingFiles(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode value = root.get("value");

            List<FileInfo> results = value != null && value.isArray()
                    ? StreamSupport.stream(value.spliterator(), false)
                            .map(item -> new FileInfo(
                                    item.path("name").asText(),
                                    item.path("webUrl").asText(),
                                    item.path("file").asText(),
                                    item.path("parentReference").path("path").asText()
                                    ))
                            .collect(Collectors.toList())
                    : List.of();

            return objectMapper.writeValueAsString(results);
        } catch (Exception e) {
            return "Error processing search results: " + e.getMessage();
        }
    }

    private record FileInfo(String name, String webUrl, String fileType, String path) {
        
    }
}
