# GitHub Repository API

A Spring Boot REST API that provides information about GitHub user repositories, including branch details and commit information.

## Overview

This application exposes a REST API endpoint that allows users to retrieve information about GitHub repositories for a given username. The API returns only non-fork repositories along with their branch information and last commit SHA for each branch.

## Features

- List all non-fork repositories for a GitHub user
- Retrieve branch information for each repository
- Get last commit SHA for each branch
- Proper error handling for non-existing users
- Integration with GitHub API v3

## Requirements

- Java 21
- Maven 3.6+
- Internet connection (for GitHub API access)

## Getting Started

### Building the Application

```bash
mvn clean compile
```

### Running the Application

```bash
mvn spring-boot:run
```

The application will start on port 8080 by default.

### Running Tests

```bash
mvn test
```

## API Endpoints

### Get User Repositories

**Endpoint:** `GET /api/users/{username}/repos`

**Description:** Retrieves all non-fork repositories for the specified GitHub user, including branch information.

**Parameters:**
- `username` (path parameter) - GitHub username

**Success Response (200 OK):**
```json
[
  {
    "repositoryName": "Hello-World",
    "ownerLogin": "octocat",
    "branches": [
      {
        "name": "main",
        "lastCommitSha": "7fd1a60b01f91b314f59955a4e4d4e80d8edf11d"
      },
      {
        "name": "test",
        "lastCommitSha": "553c2077f0edc3d5dc5d17262f6aa498e69d6f8e"
      }
    ]
  }
]
```

**Error Response (404 Not Found):**
```json
{
  "status": 404,
  "message": "User 'nonexistentuser' not found"
}
```

### Example Usage

```bash
# Get repositories for user 'octocat'
curl http://localhost:8080/api/users/octocat/repos

# Get repositories for non-existing user (returns 404)
curl http://localhost:8080/api/users/nonexistentuser/repos
```

## Architecture

The application follows a layered architecture:

- **Controller Layer** (`GitHubController`) - Handles HTTP requests and responses
- **Service Layer** (`GitHubService`) - Contains business logic and GitHub API integration
- **DTO Layer** - Data Transfer Objects for API responses
- **Exception Handling** - Global exception handler for proper error responses

## Configuration

The application can be configured through `application.properties`:

```properties
server.port=8080
github.api.base-url=https://api.github.com
```

## Dependencies

- Spring Boot 3.3.5
- Spring Web
- Spring WebFlux (for WebClient)
- Jackson (for JSON processing)
- JUnit 5 (for testing)

## Testing

The application includes integration tests that verify:
- Successful retrieval of repository information for existing users
- Proper error handling for non-existing users
- Correct response format and data structure

Tests use real GitHub API calls to ensure proper integration without mocking external dependencies.

## Error Handling

The application provides proper error handling for various scenarios:
- **404 Not Found** - When the specified GitHub user does not exist
- **Network Issues** - Graceful handling of GitHub API connectivity problems
- **Rate Limiting** - Proper handling of GitHub API rate limits

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## License

This project is created for recruitment purposes and is not intended for production use.

