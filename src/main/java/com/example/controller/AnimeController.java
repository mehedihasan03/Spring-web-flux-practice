package com.example.controller;

import com.example.domain.Anime;
import com.example.repository.AnimeRepository;
import com.example.service.AnimeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/anime")
@Slf4j
@RequiredArgsConstructor
@SecurityScheme(name = "Basic Authentication", type = SecuritySchemeType.HTTP, scheme = "Basic")
public class AnimeController {
    @Autowired
    private final AnimeService service;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List of all Anime", tags = {"anime"}, security = @SecurityRequirement(name = "Basic Authentication"))
    public Flux<Anime> listAll(){
        return service.getAllAnimes();
    }

    @GetMapping(path = "{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Anime> getAnimeById(@PathVariable long id){
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Anime> saveAnime(@Valid @RequestBody Anime anime){
        return service.save(anime);
    }

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public Flux<Anime> BatchSave(@RequestBody List<Anime> animes){
        return service.saveAllAnime(animes);
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
