package com.example.controller;

import com.example.domain.Anime;
import com.example.service.AnimeService;
import com.example.util.AnimeCreator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

@ExtendWith(SpringExtension.class)
class AnimeControllerTest {

    @InjectMocks
    private AnimeController controller;

    @Mock
    private AnimeService service;

    private final Anime anime = AnimeCreator.createValidAnime();

    @BeforeAll
    public static void blockhoundSetUp(){
        BlockHound.install();
    }

    @BeforeEach
    public void setUpMock(){
        BDDMockito.when(service.getAllAnimes())
                .thenReturn(Flux.just(anime));

        BDDMockito.when(service.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.just(anime));

        BDDMockito.when(service.save(AnimeCreator.createAnimeToBeSaved()))
                .thenReturn(Mono.just(anime));

        BDDMockito.when(service.delete(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.empty());

        BDDMockito.when(service.update(AnimeCreator.createValidAnime()))
                .thenReturn(Mono.empty());
    }

    @Test
    public void BlockHoundWorks(){
        try {
            FutureTask<?> task = new FutureTask<>(() -> {
                Thread.sleep(0); //NOSONAR
                return "";
            });
            Schedulers.parallel().schedule(task);

            task.get(10, TimeUnit.SECONDS);
            Assertions.fail("should fail");

        } catch (Exception e) {
            Assertions.assertTrue(e.getCause() instanceof BlockingOperationError);
        }

    }

    @Test
    @DisplayName("listAllAnime returns a flux of anime")
    public void listAllAnime_ReturnFlux_OfAnime_WhenSuccessful(){
        StepVerifier.create(controller.listAll())
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("findById returns a Mono of anime when it exists")
    public void findById_ReturnMono_OfAnime_When_ItExists(){
        StepVerifier.create(controller.getAnimeById(1))
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("save creates an anime when successful")
    public void save_Creates_AnAnime_WhenSuccessful(){
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        StepVerifier.create(controller.saveAnime(animeToBeSaved))
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("delete removes an anime when successful")
    public void delete_Removes_AnAnime_whenSuccessful(){
        StepVerifier.create(controller.deleteAnime(1))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    @DisplayName("save updated anime and returns empty mono when successful")
    public void saveUpdatedAnime_WhenSuccessful(){
        Anime validAnime = AnimeCreator.createValidAnime();
        StepVerifier.create(controller.updateAnime(AnimeCreator.createValidAnime()))
                .expectSubscription()
                .verifyComplete();
    }
}