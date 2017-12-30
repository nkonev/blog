package com.github.nkonev.repo.redis;

import com.github.nkonev.entity.redis.UserConfirmationToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserConfirmationTokenRepository extends CrudRepository<UserConfirmationToken, String> {
}
