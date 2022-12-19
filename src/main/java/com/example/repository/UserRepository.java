package com.example.repository;

import com.example.domain.AnimeUser;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveCrudRepository<AnimeUser, Long> {
    Mono<AnimeUser> findByUsername(String username);
}
