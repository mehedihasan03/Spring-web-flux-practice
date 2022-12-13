package com.example.repository;

import com.example.domain.Anime;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface AnimeRepository extends ReactiveCrudRepository<Anime, Long> {

    Mono<Anime> findById(long id);
}
