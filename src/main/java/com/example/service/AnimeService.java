package com.example.service;

import com.example.domain.Anime;
import com.example.repository.AnimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnimeService {
    public final AnimeRepository repository;

    public Flux<Anime> getAllAnimes() {
        return repository.findAll();
    }

    public Mono<Anime> findById(long id){
        return repository.findById(id)
                .switchIfEmpty(monoResponseStatusNotFoundException());
    }
    public <T> Mono<T> monoResponseStatusNotFoundException(){
        return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Anime not found"));
    }

    public Mono<Anime> save(Anime anime) {
        return repository.save(anime);
    }

    public Mono<Void> update(Anime anime) {
        return findById(anime.getId())
                .map(animeDb -> anime.withId(animeDb.getId()))
                .flatMap(repository::save)
                .thenEmpty(Mono.empty());
    }

    public Mono<Void> delete(long id) {
        return findById(id)
                .flatMap(repository::delete);
    }
}
