package com.example.githubrepoapi.service;

import com.example.githubrepoapi.dto.BranchInfo;
import com.example.githubrepoapi.dto.RepositoryResponse;
import com.example.githubrepoapi.dto.github.GitHubBranch;
import com.example.githubrepoapi.dto.github.GitHubRepository;
import com.example.githubrepoapi.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class GitHubService {

    private final WebClient webClient;

    @Autowired
    public GitHubService(WebClient webClient) {
        this.webClient = webClient;
    }

    public List<RepositoryResponse> getUserRepositories(String username) {
        try {
            List<GitHubRepository> repositories = getUserRepositoriesFromGitHub(username);
            
            return repositories.stream()
                    .filter(repo -> !repo.isFork())
                    .map(repo -> {
                        List<BranchInfo> branches = getRepositoryBranches(username, repo.name());
                        return new RepositoryResponse(
                                repo.name(),
                                repo.owner().login(),
                                branches
                        );
                    })
                    .toList();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new UserNotFoundException(username);
            }
            throw e;
        }
    }

    private List<GitHubRepository> getUserRepositoriesFromGitHub(String username) {
        return webClient.get()
                .uri("/users/{username}/repos", username)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<GitHubRepository>>() {})
                .block();
    }

    private List<BranchInfo> getRepositoryBranches(String username, String repositoryName) {
        try {
            List<GitHubBranch> branches = webClient.get()
                    .uri("/repos/{username}/{repo}/branches", username, repositoryName)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<GitHubBranch>>() {})
                    .block();

            return branches.stream()
                    .map(branch -> new BranchInfo(
                            branch.name(),
                            branch.commit().sha()
                    ))
                    .toList();
        } catch (WebClientResponseException e) {
            // If we can't get branches, return empty list
            return List.of();
        }
    }
}

