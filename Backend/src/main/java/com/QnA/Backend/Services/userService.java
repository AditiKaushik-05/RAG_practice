package com.QnA.Backend.Services;
import com.QnA.Backend.Entity.User;
import com.QnA.Backend.Repository.userRepo;
import com.QnA.Backend.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
//for specifically replace the contructor we used to inject interfaces..
// userService (userRepo userrepo,textEncrypor){
//this.userrepo=userrepo;.........like these
@RequiredArgsConstructor
public class userService {
    public final userRepo userrepo;
    public final TextEncryptor textEncryptor;

    @Transactional
    public User upsertFromGitHub(Map<String, Object> attributes, String accessToken, String scopes) {
        Long githubId = toLong(attributes.get("id"));
        String login = String.valueOf(attributes.get("login"));
        //to see attributes of github go to "https://api.github.com/users/{userName}"
        String name = attributes.get("name") != null
                ? String.valueOf(attributes.get("name"))
                : login;
        String avatarUrl = attributes.get("avatar_url") != null
                ? String.valueOf(attributes.get("avatar_url"))
                : null;

        String encryptedToken = textEncryptor.encrypt(accessToken);

        User user = userrepo.findByGithubId(githubId).orElseGet(User::new);
        user.setGithubId(githubId);
        user.setGithubUsername(login);
        user.setDisplayName(name);
        user.setAvatarUrl(avatarUrl);
        user.setAccessToken(encryptedToken);
        user.setTokenScopes(scopes);
        return userrepo.save(user);
    }
    public User findById(UUID id){
        return userrepo.findById(id).orElseThrow(()->new NotFoundException("Sorry this user is not found"));
    }
    @Transactional(readOnly = true)
    public String decryptAccessToken(User user){
        return textEncryptor.decrypt(user.getAccessToken());
    }
    private static Long toLong(Object value){
        if(value instanceof Number number){
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }
    @Transactional(readOnly = true)
    public User requiredById(UUID id) {
        return userrepo.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

}
