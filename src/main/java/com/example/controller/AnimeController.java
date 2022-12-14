package com.example.controller;

import com.example.domain.Anime;
import com.example.repository.AnimeRepository;
import com.example.service.AnimeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/anime")
@Slf4j
@RequiredArgsConstructor
public class AnimeController {
    @Autowired
    private final AnimeService service;

    @GetMapping
    public Flux<Anime> listAll(){
        return service.getAllAnimes()
                .log();
    }

    @GetMapping(path = "{id}")
    public Mono<Anime> getAnimeById(@PathVariable long id){
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Anime> saveAnime(@Valid @RequestBody Anime anime){
        return service.save(anime);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> updateAnime(@Valid @RequestBody Anime anime){
        return service.update(anime);
    }

    @DeleteMapping(path = "{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteAnime(@PathVariable long id){
        return service.delete(id);
    }

}
