package com.QnA.Backend.Services.github;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GithubApiClient {

    private static final String API_BASE = "https://api.github.com";

    private static final ParameterizedTypeReference<List<Map<String, Object>>> LIST_MAP = new ParameterizedTypeReference<>() {
    };
    private static final ParameterizedTypeReference<Map<String, Object>> MAP = new ParameterizedTypeReference<>() {
    };

    private final RestClient.Builder restClientBuilder;

    public List<Map<String, Object>> listUserRepos(String accessToken) {
        List<Map<String, Object>> all = new ArrayList<>();
        int page = 1;
        while (page <= 10) {
            final int currentPage = page;
            List<Map<String, Object>> pageRepos = client(accessToken)
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/user/repos")
                            .queryParam("affiliation", "owner,collaborator,organization_member")
                            .queryParam("sort", "updated")
                            .queryParam("per_page", 100)
                            .queryParam("page", currentPage)
                            .build())
                    .retrieve()
                    .body(LIST_MAP);
            if (pageRepos == null || pageRepos.isEmpty()) {
                break;
            }
            all.addAll(pageRepos);
            if (pageRepos.size() < 100) {
                break;
            }
            page++;
        }
        return all;
    }
    // example  output list of repos from github
    //[
    //  {
    //    "id": 101,
    //    "name": "QnA",
    //    "full_name": "aditi/QnA",
    //    "private": false
    //  },
    //  {
    //    "id": 102,
    //    "name": "MailMind",
    //    "full_name": "aditi/MailMind",
    //    "private": true
    //  }
    //]

    public Map<String, Object> getRepoTree(String accessToken, String owner, String repo, String branch) {
        return client(accessToken)
                .get()
                .uri("/repos/{owner}/{repo}/git/trees/{branch}?recursive=1", owner, repo, branch)
                .retrieve()
                .body(MAP);
    }
    //example output
    // repo tree
    // {
    //  "sha": "abc123",
    //  "tree": [
    //    {
    //      "path": "User.java",
    //      "type": "blob",
    //      "size": 1200
    //    },
    //    {
    //      "path": "UserService.java",
    //      "type": "blob",
    //      "size": 2000
    //    }
    //  ]
    //}

    public String getFileContent(String accessToken, String owner, String repo, String path) {
        Map<String, Object> body = client(accessToken)
                .get()
                .uri("/repos/{owner}/{repo}/contents/{path}", owner, repo, path)
                .retrieve()
                .body(MAP);
        if (body == null) {
            return null;
        }
        Object encoding = body.get("encoding");
        Object content = body.get("content");
        if (content == null) {
            return null;
        }
        if ("base64".equals(String.valueOf(encoding))) {
            String raw = String.valueOf(content).replaceAll("\\s", "");
            return new String(Base64.getDecoder().decode(raw), StandardCharsets.UTF_8);
        }
        return String.valueOf(content);
    }
// example output from github***************************************
    //{
//  "name": "User.java",
//  "path": "src/User.java",
//  "sha": "abc123...",
//  "size": 850,
//  "url": "...",
//  "html_url": "...",
//  "git_url": "...",
//  "download_url": "...",
//  "type": "file",
//  "content": "cHVibGljIGNsYXNzIFVzZXIgewo...",
//  "encoding": "base64"
//}
    private RestClient client(String accessToken){
        return restClientBuilder
                .baseUrl(API_BASE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .defaultHeader(HttpHeaders.USER_AGENT, "DevPilot")
                .build();
    }
}
