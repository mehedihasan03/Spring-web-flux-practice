package com.example.service;

import com.example.domain.Anime;
import com.example.repository.AnimeRepository;
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
class AnimeServiceTest {

    @InjectMocks
    private AnimeService service;

    @Mock
    private AnimeRepository repository;

    private final Anime anime = AnimeCreator.createValidAnime();

    @BeforeAll
    public static void blockhoundSetUp(){
        BlockHound.install();
    }

    @BeforeEach
    public void setUpMock(){
        BDDMockito.when(repository.findAll())
                .thenReturn(Flux.just(anime));

        BDDMockito.when(repository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.just(anime));

        BDDMockito.when(repository.save(AnimeCreator.createAnimeToBeSaved()))
                .thenReturn(Mono.just(anime));

        BDDMockito.when(repository.delete(ArgumentMatchers.any(Anime.class)))
                .thenReturn(Mono.empty());

        BDDMockito.when(repository.save(AnimeCreator.createValidAnime()))
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
    @DisplayName("getAllAnimes returns a flux of anime")
    public void getAllAnimes_ReturnFlux_OfAnime_WhenSuccessful(){
        StepVerifier.create(service.getAllAnimes())
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("findById returns a Mono of anime when it exists")
    public void findById_ReturnMono_OfAnime_When_ItExists(){
        StepVerifier.create(service.findById(1))
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("findById returns a Mono error when it doesn't exists")
    public void findById_ReturnMono_Error_When_EmptyMonoReturns(){
        BDDMockito.when(repository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.empty());

        StepVerifier.create(service.findById(1))
                .expectSubscription()
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    @DisplayName("save creates an anime when successful")
    public void save_Creates_AnAnime_WhenSuccessful(){
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        StepVerifier.create(service.save(animeToBeSaved))
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("delete removes an anime when successful")
    public void delete_Removes_AnAnime_whenSuccessful(){
        StepVerifier.create(service.delete(1))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    @DisplayName("delete returns a mono error when anime doesn't exists")
    public void delete_ReturnMono_Error_When_EmptyMonoReturns(){
        BDDMockito.when(repository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.empty());

        StepVerifier.create(service.delete(1))
                .expectSubscription()
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    @DisplayName("save updated anime and returns empty mono when successful")
    public void saveUpdatedAnime_WhenSuccessful(){
        StepVerifier.create(service.update(AnimeCreator.createValidAnime()))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    @DisplayName("update returns a mono error when anime dose not exists")
    public void update_ReturnMonoError_When_EmptyMonoReturn(){
        BDDMockito.when(repository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.empty());

        StepVerifier.create(service.update(AnimeCreator.createValidAnime()))
                .expectSubscription()
                .expectError(ResponseStatusException.class)
                .verify();
    }
}