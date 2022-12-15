package com.example.integration;

import com.example.domain.Anime;
import com.example.exception.CustomAttributes;
import com.example.repository.AnimeRepository;
import com.example.service.AnimeService;
import com.example.util.AnimeCreator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.server.ResponseStatusException;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ExtendWith(SpringExtension.class)
@Import({AnimeService.class, CustomAttributes.class})
public class AnimeControllerIT {

    @MockBean
    private AnimeRepository animeRepository;

    @Autowired
    private WebTestClient testClient;

    private Anime anime = AnimeCreator.createValidAnime();

    @BeforeAll
    public static void blockhoundSetUp(){
        BlockHound.install();
    }

    @BeforeEach
    public void setUpMock(){
        BDDMockito.when(animeRepository.findAll())
                .thenReturn(Flux.just(anime));

        BDDMockito.when(animeRepository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.just(anime));

        BDDMockito.when(animeRepository.save(AnimeCreator.createAnimeToBeSaved()))
                .thenReturn(Mono.just(anime));

        BDDMockito.when(animeRepository.delete(ArgumentMatchers.any(Anime.class)))
                .thenReturn(Mono.empty());

        BDDMockito.when(animeRepository.save(AnimeCreator.createValidAnime()))
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
        testClient.get()
                .uri("/anime")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.[0].id").isEqualTo(anime.getId())
                .jsonPath("$.[0].name").isEqualTo(anime.getName());

    }

    @Test
    @DisplayName("listAllAnime returns a flux of anime")
    public void listAllAnime_Use_isOk_ReturnFlux_OfAnime_WhenSuccessful(){
        testClient.get()
                .uri("/anime")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Anime.class)
                .hasSize(1)
                .contains(anime);
    }

    @Test
    @DisplayName("findById returns a Mono of anime when it exists")
    public void findById_ReturnMono_OfAnime_When_ItExists(){
        testClient.get()
                .uri("/anime/{id}",1)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Anime.class)
                .isEqualTo(anime);
    }

    @Test
    @DisplayName("findById returns a Mono error when it doesn't exists")
    public void findById_ReturnMono_Error_When_EmptyMonoReturns(){
        BDDMockito.when(animeRepository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.empty());

        testClient.get()
                .uri("/anime/{id}",1)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.path").isEqualTo("/anime/" + anime.getId());
//              . Here I can use more condition of response.
    }

    @Test
    @DisplayName("save creates an anime when successful")
    public void save_Creates_AnAnime_WhenSuccessful(){
        Anime animeSaved = AnimeCreator.createAnimeToBeSaved();

        testClient.post()
                .uri("/anime")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(animeSaved))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Anime.class)
                .isEqualTo(anime);
    }

    @Test
    @DisplayName("save return error when name is empty")
    public void save_ReturnError_WhenNameIsEmpty(){
        Anime animeSaved = AnimeCreator.createAnimeToBeSaved().withName("");

        testClient.post()
                .uri("/anime")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(animeSaved))
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    @DisplayName("delete removes an anime when successful")
    public void delete_Removes_AnAnime_whenSuccessful(){
        testClient.delete()
                .uri("/anime/{id}",1)
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    @DisplayName("delete returns a mono error when anime doesn't exists")
    public void delete_ReturnMono_Error_When_EmptyMonoReturns(){
        BDDMockito.when(animeRepository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.empty());

        testClient.delete()
                .uri("/anime/{id}",1)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusErrorException Happened");
    }

    @Test
    @DisplayName("save updated anime and returns empty mono when successful")
    public void saveUpdatedAnime_WhenSuccessful(){
        Anime animeSaved = AnimeCreator.createValidAnime();

        testClient.put()
                .uri("/anime")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(animeSaved))
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    @DisplayName("update returns a mono error when anime dose not exists")
    public void update_ReturnMonoError_When_EmptyMonoReturn(){
        BDDMockito.when(animeRepository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.empty());

        testClient.put()
                .uri("/anime")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(anime))
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.path").isEqualTo("/anime/" + anime.getId());
    }
}
