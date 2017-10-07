package com.github.nkonev.repo.jpa;

import com.github.nkonev.entity.jpa.UserAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByUsername(String username);

    Optional<UserAccount> findById(Long id);

    Optional<UserAccount> findByEmail(String email);

    Page<UserAccount> findByUsernameContains(Pageable springDataPage, String login);
}
