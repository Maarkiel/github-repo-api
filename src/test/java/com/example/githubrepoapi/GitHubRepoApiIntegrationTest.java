package com.example.githubrepoapi;

import com.example.githubrepoapi.dto.BranchInfo;
import com.example.githubrepoapi.dto.ErrorResponse;
import com.example.githubrepoapi.dto.RepositoryResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class GitHubRepoApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnUserRepositoriesWithBranchesForExistingUser() throws Exception {
        // Given: A known GitHub user with public repositories
        String username = "octocat";

        // When: Making a request to get user repositories
        MvcResult result = mockMvc.perform(get("/api/users/{username}/repos", username))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andReturn();

        // Then: Response should contain repositories with required information
        String responseContent = result.getResponse().getContentAsString();
        List<RepositoryResponse> repositories = objectMapper.readValue(
                responseContent, 
                new TypeReference<List<RepositoryResponse>>() {}
        );

        // Verify response structure and content
        assertNotNull(repositories);
        assertFalse(repositories.isEmpty(), "User should have at least one non-fork repository");

        // Verify each repository has required fields
        for (RepositoryResponse repo : repositories) {
            assertNotNull(repo.repositoryName(), "Repository name should not be null");
            assertFalse(repo.repositoryName().isEmpty(), "Repository name should not be empty");
            
            assertNotNull(repo.ownerLogin(), "Owner login should not be null");
            assertEquals(username, repo.ownerLogin(), "Owner login should match requested username");
            
            assertNotNull(repo.branches(), "Branches list should not be null");
            assertFalse(repo.branches().isEmpty(), "Repository should have at least one branch");

            // Verify each branch has required information
            for (BranchInfo branch : repo.branches()) {
                assertNotNull(branch.name(), "Branch name should not be null");
                assertFalse(branch.name().isEmpty(), "Branch name should not be empty");
                
                assertNotNull(branch.lastCommitSha(), "Last commit SHA should not be null");
                assertFalse(branch.lastCommitSha().isEmpty(), "Last commit SHA should not be empty");
                assertTrue(branch.lastCommitSha().matches("[a-f0-9]{40}"), 
                    "Last commit SHA should be a valid 40-character hex string");
            }
        }

        // Verify that at least one repository has a main/master branch
        boolean hasMainBranch = repositories.stream()
                .flatMap(repo -> repo.branches().stream())
                .anyMatch(branch -> "main".equals(branch.name()) || "master".equals(branch.name()));
        assertTrue(hasMainBranch, "At least one repository should have a main or master branch");
    }

    @Test
    void shouldReturn404ForNonExistingUser() throws Exception {
        // Given: A non-existing GitHub username
        String nonExistingUsername = "this-user-definitely-does-not-exist-12345";

        // When: Making a request to get user repositories
        MvcResult result = mockMvc.perform(get("/api/users/{username}/repos", nonExistingUsername))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/json"))
                .andReturn();

        // Then: Response should contain error information in required format
        String responseContent = result.getResponse().getContentAsString();
        ErrorResponse errorResponse = objectMapper.readValue(responseContent, ErrorResponse.class);

        assertNotNull(errorResponse);
        assertEquals(404, errorResponse.status());
        assertNotNull(errorResponse.message());
        assertTrue(errorResponse.message().contains(nonExistingUsername), 
            "Error message should contain the username that was not found");
    }
}

