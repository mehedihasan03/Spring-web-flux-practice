package com.example.controller;

import com.example.domain.Anime;
import com.example.repository.AnimeRepository;
import com.example.service.AnimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

    @GetMapping("/")
    public Flux<Anime> listAll(){
        return service.getAnimes()
                .log();
    }

    @GetMapping("/{id}")
    public Mono<Anime> getAnimeById(@PathVariable long id){
        return service.findById(id);
    }

}
