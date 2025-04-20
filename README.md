# OneDrive MCP Server

A Model Context Protocol (MCP) server implementation that enables AI agents to search and navigate files and folders in Microsoft OneDrive. Built with Spring Boot and Spring AI.

## Features

- 🔍 Search files in OneDrive with optional folder filtering
- 📁 Search and navigate folder structures
- 🔗 Get direct web URLs to files and folders
- 🤖 MCP-compatible interface for AI agent integration
- 🔒 Secure access using Microsoft Graph API

## Prerequisites

- Java 21 or higher
- Maven 3.6+
- Docker (optional, for containerized deployment)
- Microsoft OneDrive account and access token

## Getting Microsoft OneDrive Access Token

You can obtain the access token in two ways:

### Option 1: Using Microsoft Graph Explorer (Quick Method)
1. Visit [Microsoft Graph Explorer](https://developer.microsoft.com/en-us/graph/graph-explorer)
2. Sign in with your Microsoft account
3. Go to the "Access token" tab
4. Make sure you have the following permissions enabled:
   - Files.Read
   - Files.Read.All
5. Copy the access token from the token field

Note: This token is temporary and will expire after a few hours. It's good for testing but not recommended for production use.

### Option 2: Register Your Own Application (Recommended for Production)
1. Register your application in the [Microsoft Azure Portal](https://portal.azure.com/#blade/Microsoft_AAD_RegisteredApps/ApplicationsListBlade):
   - Click "New Registration"
   - Give your application a name
   - For "Supported account types" select "Accounts in any organizational directory and personal Microsoft accounts"
   - For "Redirect URI", select "Public client/native (mobile & desktop)" and enter "https://login.microsoftonline.com/common/oauth2/nativeclient"
   - Click "Register"

2. Note down these values from your registered application:
   - Application (client) ID
   - Redirect URI

3. Configure API Permissions:
   - Go to "API Permissions"
   - Click "Add a permission"
   - Select "Microsoft Graph"
   - Choose "Delegated permissions"
   - Add the following permissions:
     - Files.Read
     - Files.Read.All

4. Get your access token:
   - Visit this URL in your browser (replace YOUR_CLIENT_ID):
   ```
   https://login.microsoftonline.com/common/oauth2/v2.0/authorize?client_id=YOUR_CLIENT_ID&scope=files.read&response_type=token&redirect_uri=https://login.microsoftonline.com/common/oauth2/nativeclient
   ```
   - Sign in with your Microsoft account
   - After authentication, you'll be redirected to a URL containing your access token
   - Copy the access token value from the URL parameter `access_token=`

This token is valid for several hours and needs to be refreshed periodically. For production use, implement proper OAuth2 token refresh flow.

## Configuration

1. Set your OneDrive access token:

The application expects the access token to be provided via the `MICROSOFT_ONEDRIVE_ACCESS_TOKEN` environment variable, which is referenced in `application.yaml`:
```yaml
onedrive:
    access-token: ${MICROSOFT_ONEDRIVE_ACCESS_TOKEN}
```

2. VS Code MCP Configuration:

You can configure the MCP server in VS Code either using the local JAR file or using Docker. Create or update `.vscode/mcp.json` in your VS Code workspace:

### Option 1: Using Local JAR
```json
{
    "servers": {
        "onedrive-mcpserver": {
            "type": "stdio",
            "command": "java",
            "args": [
                "-jar",
                "~/onedrive-mcp-server/target/onedrive-mcp-server-0.0.1.jar"
            ],
            "env": {
                "MICROSOFT_ONEDRIVE_ACCESS_TOKEN": "<INSERT TOKEN HERE>"
            }
        }
    }
}
```

### Option 2: Using Docker
```json
{
    "servers": {
        "onedrive-mcpserver-docker": {
            "type": "stdio",
            "command": "docker",
            "args": [
                "run",
                "--rm",
                "-i",
                "-e",
                "MICROSOFT_ONEDRIVE_ACCESS_TOKEN",
                "karthik20/onedrive-mcp-server:0.0.1"
            ],
            "env": {
                "MICROSOFT_ONEDRIVE_ACCESS_TOKEN": "<INSERT TOKEN HERE>"
            }
        }
    }
}
```

Replace `<INSERT TOKEN HERE>` with your Microsoft OneDrive access token. For the local JAR option, make sure to update the jar path to match your local setup.

## Building and Running

### Local Development

1. Build the project:
```bash
mvn clean package
```

2. Run the server:
```bash
java -jar target/onedrive-mcp-server-0.0.1.jar
```

### Docker Deployment

1. Build the Docker image:
```bash
mvn spring-boot:build-image
```

This will create an image named: `karthik20/onedrive-mcp-server:0.0.1`

2. Run the container:
```bash
docker run -p 8080:8080 -e ONEDRIVE_ACCESS_TOKEN=your-token karthik20/onedrive-mcp-server:0.0.1
```

## Testing with MCP Clients

### GitHub Copilot

1. Install GitHub Copilot in VS Code
2. Configure your AI settings to use the MCP server:
```json
{
    "mcp.server.url": "http://localhost:8080"
}
```

### Claude Desktop

1. Open Claude Desktop settings
2. Add a new MCP server with URL: `http://localhost:8080`
3. Use the built-in file browser to access OneDrive files

## Available MCP Tools

### 1. Search Files
Search for files in OneDrive with optional folder filtering:
```typescript
searchFiles(fileName: string, folder?: string): string
```

### 2. Search Folders
Navigate and search folder structures:
```typescript
searchFolderPath(folderName: string): string
```

## Testing

Run the test suite:
```bash
mvn test
```

## Built With

- Spring Boot 3.4.4
- Spring AI 1.0.0-M7
- Microsoft Graph API
- JUnit 5 & Mockito

## License

This project is licensed under the MIT License - see the LICENSE file for details.