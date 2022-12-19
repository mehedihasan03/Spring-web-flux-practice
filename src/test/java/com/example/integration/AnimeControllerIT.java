package com.example.integration;

import com.example.domain.Anime;
import com.example.exception.CustomAttributes;
import com.example.repository.AnimeRepository;
import com.example.service.AnimeService;
import com.example.util.AnimeCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ExtendWith(SpringExtension.class)
@Import({AnimeService.class, CustomAttributes.class})
@AutoConfigureWebTestClient
public class AnimeControllerIT {

    private static final String ADMIN = "moon";
    private static final String USER = "prome";

    @Autowired
    private WebTestClient testClientUtil;

    @MockBean
    private AnimeRepository animeRepository;

    @Autowired
    private WebTestClient testClient;

    private Anime anime = AnimeCreator.createValidAnime();

//    @BeforeAll
//    public static void blockhoundSetUp(){
//        BlockHound.install();
//    }

    @BeforeEach
    public void setUpMock() {
        BDDMockito.when(animeRepository.findAll())
                .thenReturn(Flux.just(anime));

        BDDMockito.when(animeRepository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.just(anime));

        BDDMockito.when(animeRepository.save(AnimeCreator.createAnimeToBeSaved()))
                .thenReturn(Mono.just(anime));

        BDDMockito.when(animeRepository.saveAll(List.of(AnimeCreator.createAnimeToBeSaved(), AnimeCreator.createAnimeToBeSaved())))
                .thenReturn(Flux.just(anime, anime));

        BDDMockito.when(animeRepository.delete(ArgumentMatchers.any(Anime.class)))
                .thenReturn(Mono.empty());

        BDDMockito.when(animeRepository.save(AnimeCreator.createValidAnime()))
                .thenReturn(Mono.empty());
    }

//    @Test
//    public void BlockHoundWorks(){
//        try {
//            FutureTask<?> task = new FutureTask<>(() -> {
//                Thread.sleep(0); //NOSONAR
//                return "";
//            });
//            Schedulers.parallel().schedule(task);
//
//            task.get(10, TimeUnit.SECONDS);
//            Assertions.fail("should fail");
//
//        } catch (Exception e) {
//            Assertions.assertTrue(e.getCause() instanceof BlockingOperationError);
//        }
//
//    }

    @Test
    @DisplayName("listAllAnime returns a flux of anime")
    @WithUserDetails(ADMIN)
    public void listAllAnime_ReturnFlux_OfAnime_WhenSuccessful() {
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
    @WithUserDetails(ADMIN)
    public void listAllAnime_Use_isOk_ReturnFlux_OfAnime_WhenSuccessful() {
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
    @DisplayName("listAllAnime returns unauthorized when user unAthenticated")
    public void listAllAnime_ReturnUnauthorized_WhenUserUnauthenticated() {
        testClient.get()
                .uri("/anime")
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    @DisplayName("listAllAnime returns forbidden when user unthorized")
    @WithUserDetails(USER)
    public void listAllAnime_ReturnForbidden_WhenUserUnauthorised() {
        testClient.get()
                .uri("/anime")
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    @Test
    @DisplayName("findById returns a Mono of anime when it exists and has auth user")
//    @WithUserDetails(USER)
    @WithMockUser(roles = "USER") // Both are Working
    public void findById_ReturnMono_OfAnime_When_ItExists() {
        testClient.get()
                .uri("/anime/{id}", 1)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Anime.class)
                .isEqualTo(anime);
    }

    @Test
    @DisplayName("findById returns a Mono error when it doesn't exists and has auth user")
    @WithUserDetails(USER)
    public void findById_ReturnMono_Error_When_EmptyMonoReturns() {
        BDDMockito.when(animeRepository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.empty());

        testClient.get()
                .uri("/anime/{id}", 1)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.path").isEqualTo("/anime/" + anime.getId());
//              . Here I can use more condition of response.
    }

    @Test
    @DisplayName("save creates an anime when successful and has admin")
    @WithUserDetails(ADMIN)
    public void save_Creates_AnAnime_WhenSuccessful() {
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
    @DisplayName("BatchSave creates animes when successful")
    @WithUserDetails(ADMIN)
    public void batchSave_Creates_Animes_WhenSuccessful() {
        Anime animeSaved = AnimeCreator.createAnimeToBeSaved();

        testClient.post()
                .uri("/anime/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(List.of(animeSaved, animeSaved)))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBodyList(Anime.class)
                .hasSize(2)
                .contains(anime);
    }

    @Test
    @DisplayName("save return error when name is empty")
    @WithUserDetails(ADMIN)
    public void save_ReturnError_WhenNameIsEmpty() {
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
    @DisplayName("BatchSave returns error when empty object in the List")
    @WithUserDetails(ADMIN)
    public void batchSave_ReturnsError_WhenEmptyAnimes_Object() {
        Anime animeSaved = AnimeCreator.createAnimeToBeSaved();

        BDDMockito.when(animeRepository.saveAll(ArgumentMatchers.anyIterable()))
                .thenReturn(Flux.just(anime, anime.withName("")));

        testClient.post()
                .uri("/anime/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(List.of(animeSaved, animeSaved)))
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    @DisplayName("delete removes an anime when successful")
    @WithUserDetails(ADMIN)
    public void delete_Removes_AnAnime_whenSuccessful() {
        testClient.delete()
                .uri("/anime/{id}", 1)
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    @DisplayName("delete returns a mono error when anime doesn't exists")
//    @WithUserDetails(ADMIN)
    @WithMockUser(roles = "ADMIN") // Both are working
    public void delete_ReturnMono_Error_When_EmptyMonoReturns() {
        BDDMockito.when(animeRepository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.empty());

        testClient.delete()
                .uri("/anime/{id}", 1)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusErrorException Happened");
    }

    @Test
    @DisplayName("save updated anime and returns empty mono when successful")
    @WithUserDetails(ADMIN)
    public void saveUpdatedAnime_WhenSuccessful() {
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
    @WithUserDetails(ADMIN)
    public void update_ReturnMonoError_When_EmptyMonoReturn() {
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
                .jsonPath("$.status").isEqualTo(404);
    }
}
