package com.github.nkonev.blog.repo.jpa;

import com.github.nkonev.blog.entity.jpa.UserAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByUsername(String username);

    Optional<UserAccount> findById(Long id);

    Optional<UserAccount> findByEmail(String email);

    Page<UserAccount> findByUsernameContainsIgnoreCase(Pageable springDataPage, String login);

    Optional<UserAccount> findByFacebookId(String facebookId);
}
