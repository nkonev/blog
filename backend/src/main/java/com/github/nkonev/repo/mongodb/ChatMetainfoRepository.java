package com.github.nkonev.repo.mongodb;

import com.github.nkonev.entity.mongodb.ChatInfo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.List;

@Repository
public interface ChatMetainfoRepository extends ReactiveMongoRepository<ChatInfo, String> {

    Flux<ChatInfo> findAllByParticipantsIn(List<Long> userIds);
}
