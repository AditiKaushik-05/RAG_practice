package com.QnA.Backend.Security;
import com.QnA.Backend.Entity.User;
import  com.QnA.Backend.Services.userService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GithubOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final userService userService;
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    @Override
    //GitHub → Authorization code → Spring Security exchanges it for an access token → userRequest → Your code extracts the token.
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User githubUser = delegate.loadUser(userRequest);

        String accessToken = userRequest.getAccessToken().getTokenValue();
        String scopes = userRequest.getAccessToken().getScopes() != null
                ? String.join(",", userRequest.getAccessToken().getScopes())
                : "read:user,repo";

        User user = userService.upsertFromGitHub(githubUser.getAttributes() , accessToken , scopes);
        return new AppUserPrincipal(user , githubUser.getAttributes());
    }
}
