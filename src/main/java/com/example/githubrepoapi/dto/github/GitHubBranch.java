package com.example.githubrepoapi.dto.github;

public record GitHubBranch(
    String name,
    GitHubCommit commit
) {
}

