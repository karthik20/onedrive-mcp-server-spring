package com.karthik.ai.mcpserver.onedrive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

class OnedriveServiceTest {

    private RestClient restClient;
    private RestClient.ResponseSpec responseSpec;
    private OnedriveService onedriveService;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        restClient = mock(RestClient.class);
        responseSpec = mock(RestClient.ResponseSpec.class);
        onedriveService = new OnedriveService();
        
        ReflectionTestUtils.setField(onedriveService, "restClient", restClient);
        ReflectionTestUtils.setField(onedriveService, "accessToken", "test-token");
        
        // Mock the chain using a single mock and type casting
        var requestSpec = mock(RestClient.RequestHeadersUriSpec.class);
        
        when(restClient.get()).thenReturn((RestClient.RequestHeadersUriSpec) requestSpec);
        when(requestSpec.uri(anyString())).thenReturn((RestClient.RequestHeadersSpec) requestSpec);
        when(requestSpec.header(anyString(), anyString())).thenReturn((RestClient.RequestHeadersSpec) requestSpec);
        when(requestSpec.accept(any())).thenReturn((RestClient.RequestHeadersSpec) requestSpec);
        when(requestSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void searchFiles_WithSinglePattern_Success() {
        // Given
        String expectedResponse = """
            {
                "value": [
                    {
                        "name": "resume.pdf",
                        "webUrl": "https://example.com/resume.pdf"
                    }
                ]
            }
            """;
        when(responseSpec.body(String.class)).thenReturn(expectedResponse);

        // When
        String result = onedriveService.searchFiles("resume", "Documents");

        // Then
        assertTrue(result.contains("resume.pdf"));
        assertTrue(result.contains("https://example.com/resume.pdf"));
    }

    @Test
    void searchFiles_WithMultiplePatterns_Success() {
        String expectedResponse = """
            {
                "value": [
                    {
                        "name": "resume.pdf",
                        "webUrl": "https://example.com/resume.pdf"
                    },
                    {
                        "name": "ats.doc",
                        "webUrl": "https://example.com/ats.doc"
                    }
                ]
            }
            """;
        when(responseSpec.body(String.class)).thenReturn(expectedResponse);

        String result = onedriveService.searchFiles("resume ats", "Documents");

        assertTrue(result.contains("resume.pdf"));
        assertTrue(result.contains("ats.doc"));
        assertTrue(result.contains("https://example.com/resume.pdf"));
        assertTrue(result.contains("https://example.com/ats.doc"));
    }

    @Test
    void searchFiles_WithNoResults_ReturnsEmptyList() {
        // Given
        String expectedResponse = """
            {
                "value": []
            }
            """;
        when(responseSpec.body(String.class)).thenReturn(expectedResponse);

        // When
        String result = onedriveService.searchFiles("nonexistent", "Documents");

        // Then
        assertEquals("[]", result);
    }

    @Test
    void searchFiles_WithoutFolder_UsesRootSearch() {
        // Given
        String expectedResponse = """
            {
                "value": [
                    {
                        "name": "resume.pdf",
                        "webUrl": "https://example.com/resume.pdf"
                    }
                ]
            }
            """;
        when(responseSpec.body(String.class)).thenReturn(expectedResponse);

        // When
        String result = onedriveService.searchFiles("resume", null);

        // Then
        assertTrue(result.contains("resume.pdf"));
    }

    @Test
    void searchFiles_WithApiError_ReturnsErrorMessage() {
        // Given
        when(responseSpec.body(String.class)).thenThrow(new RuntimeException("API Error"));

        // When
        String result = onedriveService.searchFiles("resume", "Documents");

        // Then
        assertTrue(result.contains("Error searching files:"));
        assertTrue(result.contains("API Error"));
    }

    @Test
    void searchFiles_WithInvalidJsonResponse_HandlesError() {
        // Given
        when(responseSpec.body(String.class)).thenReturn("invalid json");

        // When
        String result = onedriveService.searchFiles("resume", "Documents");

        // Then
        assertTrue(result.contains("Error processing search results:"));
    }
}