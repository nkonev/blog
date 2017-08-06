package com.github.nikit.cpp.repo.jpa;

import com.github.nikit.cpp.entity.jpa.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByUsername(String username);

    Optional<UserAccount> getById(Long id);

    Optional<UserAccount> findByEmail(String email);
}
