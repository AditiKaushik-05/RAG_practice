package com.QnA.Backend.Repository;

import com.QnA.Backend.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface userRepo extends JpaRepository<User, UUID> {
    Optional<User> findByGithubId(Long githubId);
}
