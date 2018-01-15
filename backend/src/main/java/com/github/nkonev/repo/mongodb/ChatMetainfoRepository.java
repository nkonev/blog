package com.github.nkonev.repo.mongodb;

import com.github.nkonev.dto.ChatInfoDto;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatMetainfoRepository extends MongoRepository<ChatInfoDto, String> {
}
