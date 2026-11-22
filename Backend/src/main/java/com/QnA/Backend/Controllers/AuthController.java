package com.QnA.Backend.Controllers;
import com.QnA.Backend.Entity.User;
import com.QnA.Backend.Security.AppUserPrincipal;
import com.QnA.Backend.dto.UserResponse;
import org.springframework.http.ResponseEntity;
import com.QnA.Backend.Security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final CurrentUser currentUser;
    @GetMapping("/login-url")
    public Map<String, String> loginUrl() {
        return Map.of("url", "/oauth2/authorization/github");
    }
    @GetMapping("/login-success")
    public ResponseEntity<String> loginSuccess() {
        return ResponseEntity.ok("GitHub Login Successful!");
    }
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me() {
        AppUserPrincipal principal = currentUser.require();
        User user = principal.getUser();
        return ResponseEntity.ok(new UserResponse(
                user.getId(),
                user.getGithubId(),
                user.getGithubUsername(),
                user.getDisplayName(),
                user.getAvatarUrl()));
    }

}
