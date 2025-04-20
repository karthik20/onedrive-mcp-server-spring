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
        
        var requestSpec = mock(RestClient.RequestHeadersUriSpec.class);
        
        when(restClient.get()).thenReturn(requestSpec);
        when(requestSpec.uri(anyString())).thenReturn((RestClient.RequestHeadersSpec<?>) requestSpec);
        when(requestSpec.header(anyString(), anyString())).thenReturn((RestClient.RequestHeadersSpec<?>) requestSpec);
        when(requestSpec.accept(any())).thenReturn((RestClient.RequestHeadersSpec<?>) requestSpec);
        when(requestSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void searchFiles_WithSinglePattern_Success() {
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

        String result = onedriveService.searchFiles("resume", "Documents");

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
        String expectedResponse = """
            {
                "value": []
            }
            """;
        when(responseSpec.body(String.class)).thenReturn(expectedResponse);

        String result = onedriveService.searchFiles("nonexistent", "Documents");

        assertEquals("[]", result);
    }

    @Test
    void searchFiles_WithoutFolder_UsesRootSearch() {
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

        String result = onedriveService.searchFiles("resume", null);

        assertTrue(result.contains("resume.pdf"));
    }

    @Test
    void searchFiles_WithApiError_ReturnsErrorMessage() {
        when(responseSpec.body(String.class)).thenThrow(new RuntimeException("API Error"));

        String result = onedriveService.searchFiles("resume", "Documents");

        assertTrue(result.contains("Error searching files:"));
        assertTrue(result.contains("API Error"));
    }

    @Test
    void searchFiles_WithInvalidJsonResponse_HandlesError() {
        when(responseSpec.body(String.class)).thenReturn("invalid json");

        String result = onedriveService.searchFiles("resume", "Documents");

        assertTrue(result.contains("Error processing search results:"));
    }

    @Test
    void searchFolderPath_WithValidFolder_Success() {
        String expectedResponse = """
            {
                "value": [
                    {
                        "name": "Resume",
                        "webUrl": "https://example.com/folders/Resume",
                        "parentReference": {
                            "path": "/drive/root:/Documents"
                        },
                        "folder": {
                            "childCount": 5
                        }
                    }
                ]
            }
            """;
        when(responseSpec.body(String.class)).thenReturn(expectedResponse);

        String result = onedriveService.searchFolderPath("Resume");

        assertTrue(result.contains("Resume"));
        assertTrue(result.contains("https://example.com/folders/Resume"));
        assertTrue(result.contains("/drive/root:/Documents"));
        assertTrue(result.contains("5"));
    }

    @Test
    void searchFolderPath_WithNoResults_ReturnsEmptyList() {
        String expectedResponse = """
            {
                "value": []
            }
            """;
        when(responseSpec.body(String.class)).thenReturn(expectedResponse);

        String result = onedriveService.searchFolderPath("NonexistentFolder");

        assertEquals("[]", result);
    }

    @Test
    void searchFolderPath_WithApiError_ReturnsErrorMessage() {
        when(responseSpec.body(String.class)).thenThrow(new RuntimeException("API Error"));

        String result = onedriveService.searchFolderPath("Resume");

        assertTrue(result.contains("Error searching folders:"));
        assertTrue(result.contains("API Error"));
    }

    @Test
    void searchFolderPath_WithInvalidJsonResponse_HandlesError() {
        when(responseSpec.body(String.class)).thenReturn("invalid json");

        String result = onedriveService.searchFolderPath("Resume");

        assertTrue(result.contains("Error processing folder search results:"));
    }

    @Test
    void searchFolderPath_WithMultipleFolders_Success() {
        String expectedResponse = """
            {
                "value": [
                    {
                        "name": "Resume",
                        "webUrl": "https://example.com/folders/Resume1",
                        "parentReference": {
                            "path": "/drive/root:/Documents"
                        },
                        "folder": {
                            "childCount": 5
                        }
                    },
                    {
                        "name": "Resume Templates",
                        "webUrl": "https://example.com/folders/Resume2",
                        "parentReference": {
                            "path": "/drive/root:/Templates"
                        },
                        "folder": {
                            "childCount": 3
                        }
                    }
                ]
            }
            """;
        when(responseSpec.body(String.class)).thenReturn(expectedResponse);

        String result = onedriveService.searchFolderPath("Resume");

        assertTrue(result.contains("Resume"));
        assertTrue(result.contains("Resume Templates"));
        assertTrue(result.contains("/drive/root:/Documents"));
        assertTrue(result.contains("/drive/root:/Templates"));
        assertTrue(result.contains("https://example.com/folders/Resume1"));
        assertTrue(result.contains("https://example.com/folders/Resume2"));
    }

    @Test
    void searchFolderPath_WithSpecialCharacters_Success() {
        String expectedResponse = """
            {
                "value": [
                    {
                        "name": "Resume & CV's",
                        "webUrl": "https://example.com/folders/special",
                        "parentReference": {
                            "path": "/drive/root:/Special Characters"
                        },
                        "folder": {
                            "childCount": 2
                        }
                    }
                ]
            }
            """;
        when(responseSpec.body(String.class)).thenReturn(expectedResponse);

        String result = onedriveService.searchFolderPath("Resume & CV's");

        assertTrue(result.contains("Resume & CV's"));
        assertTrue(result.contains("/drive/root:/Special Characters"));
        assertTrue(result.contains("https://example.com/folders/special"));
        assertTrue(result.contains("2")); // childCount
    }

    @Test
    void listFolderContents_WithValidPath_Success() {
        String expectedResponse = """
            {
                "value": [
                    {
                        "name": "resume.pdf",
                        "webUrl": "https://example.com/resume.pdf",
                        "file": {},
                        "parentReference": {
                            "path": "/drive/root:/Documents"
                        }
                    },
                    {
                        "name": "Templates",
                        "webUrl": "https://example.com/folders/templates",
                        "folder": {
                            "childCount": 3
                        },
                        "parentReference": {
                            "path": "/drive/root:/Documents"
                        }
                    }
                ]
            }
            """;
        when(responseSpec.body(String.class)).thenReturn(expectedResponse);

        String result = onedriveService.listFolderContents("Documents");

        assertTrue(result.contains("resume.pdf"));
        assertTrue(result.contains("Templates"));
        assertTrue(result.contains("https://example.com/resume.pdf"));
        assertTrue(result.contains("https://example.com/folders/templates"));
        assertTrue(result.contains("/drive/root:/Documents"));
        assertTrue(result.contains("3")); // childCount for Templates folder
    }

    @Test
    void listFolderContents_WithNestedPath_Success() {
        String expectedResponse = """
            {
                "value": [
                    {
                        "name": "technical.pdf",
                        "webUrl": "https://example.com/technical.pdf",
                        "file": {},
                        "parentReference": {
                            "path": "/drive/root:/Documents/Resume"
                        }
                    }
                ]
            }
            """;
        when(responseSpec.body(String.class)).thenReturn(expectedResponse);

        String result = onedriveService.listFolderContents("Documents/Resume");

        assertTrue(result.contains("technical.pdf"));
        assertTrue(result.contains("https://example.com/technical.pdf"));
        assertTrue(result.contains("/drive/root:/Documents/Resume"));
    }

    @Test
    void listFolderContents_WithEmptyFolder_ReturnsEmptyList() {
        String expectedResponse = """
            {
                "value": []
            }
            """;
        when(responseSpec.body(String.class)).thenReturn(expectedResponse);

        String result = onedriveService.listFolderContents("EmptyFolder");

        assertEquals("[]", result);
    }

    @Test
    void listFolderContents_WithRootPath_Success() {
        String expectedResponse = """
            {
                "value": [
                    {
                        "name": "Documents",
                        "webUrl": "https://example.com/folders/documents",
                        "folder": {
                            "childCount": 5
                        },
                        "parentReference": {
                            "path": "/drive/root:"
                        }
                    }
                ]
            }
            """;
        when(responseSpec.body(String.class)).thenReturn(expectedResponse);

        String result = onedriveService.listFolderContents(null);

        assertTrue(result.contains("Documents"));
        assertTrue(result.contains("https://example.com/folders/documents"));
        assertTrue(result.contains("/drive/root:"));
        assertTrue(result.contains("5")); // childCount
    }

    @Test
    void listFolderContents_WithApiError_ReturnsErrorMessage() {
        when(responseSpec.body(String.class)).thenThrow(new RuntimeException("API Error"));

        String result = onedriveService.listFolderContents("Documents");

        assertTrue(result.contains("Error listing folder contents:"));
        assertTrue(result.contains("API Error"));
    }
}