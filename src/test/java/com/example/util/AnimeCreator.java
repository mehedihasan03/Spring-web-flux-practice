package com.example.util;

import com.example.domain.Anime;

public class AnimeCreator {
    public static Anime createAnimeToBeSaved(){
        return Anime.builder()
                .name("mehedi hasan")
                .build();
    }

    public static Anime createValidAnime(){
        return Anime.builder()
                .id(1)
                .name("mehedi hasan")
                .build();
    }

    public static Anime createdValidUpdateAnime(){
        return Anime.builder()
                .id(1)
                .name("mehedi hasan 1")
                .build();
    }
}
