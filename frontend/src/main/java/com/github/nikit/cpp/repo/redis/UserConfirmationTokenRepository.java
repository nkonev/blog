package com.github.nikit.cpp.repo.redis;

import com.github.nikit.cpp.entity.redis.UserConfirmationToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface UserConfirmationTokenRepository extends CrudRepository<UserConfirmationToken, String> {
}
