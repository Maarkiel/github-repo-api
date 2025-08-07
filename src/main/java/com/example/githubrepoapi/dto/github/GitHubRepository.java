package com.example.githubrepoapi.dto.github;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubRepository(
    String name,
    GitHubOwner owner,
    @JsonProperty("fork") boolean isFork
) {
}

