package com.desafio.votacao;

import com.desafio.votacao.client.CpfValidationClient;
import com.desafio.votacao.client.CpfValidationResult;
import com.desafio.votacao.dto.CriarPautaRequest;
import com.desafio.votacao.dto.VotoRequest;
import com.desafio.votacao.entity.SessaoVotacao;
import com.desafio.votacao.enums.OpcaoVoto;
import com.desafio.votacao.exception.VotoDuplicadoException;
import com.desafio.votacao.repository.VotoRepository;
import com.desafio.votacao.service.PautaService;
import com.desafio.votacao.service.SessaoService;
import com.desafio.votacao.service.VotoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class VotoConcorrenciaTest {

    private static final int TOTAL_THREADS = 20;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void configurarDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.hikari.maximum-pool-size", () -> String.valueOf(TOTAL_THREADS));
    }

    @TestConfiguration
    static class CpfSempreAptoConfig {
        @Bean
        @Primary
        CpfValidationClient cpfValidationClient() {
            return cpf -> CpfValidationResult.ABLE_TO_VOTE;
        }
    }

    @Autowired
    private PautaService pautaService;
    @Autowired
    private SessaoService sessaoService;
    @Autowired
    private VotoService votoService;
    @Autowired
    private VotoRepository votoRepository;

    @Test
    void votosSimultaneos_mesmoAssociado_apenasUmDevePersistir() throws InterruptedException {
        CriarPautaRequest criarPauta = new CriarPautaRequest();
        criarPauta.setTitulo("Pauta para teste de concorrência");
        Long pautaId = pautaService.save(criarPauta).getId();

        SessaoVotacao sessao = sessaoService.createSessao(pautaId, 1);

        ExecutorService executor = Executors.newFixedThreadPool(TOTAL_THREADS);
        CountDownLatch prontos = new CountDownLatch(TOTAL_THREADS);
        CountDownLatch largada = new CountDownLatch(1);
        CountDownLatch terminaram = new CountDownLatch(TOTAL_THREADS);
        AtomicInteger sucessos = new AtomicInteger();
        AtomicInteger duplicados = new AtomicInteger();

        for (int i = 0; i < TOTAL_THREADS; i++) {
            executor.submit(() -> {
                VotoRequest voto = new VotoRequest();
                voto.setAssociadoId("33333333333");
                voto.setOpcao(OpcaoVoto.SIM);
                prontos.countDown();
                try {
                    largada.await();
                    votoService.votar(sessao.getId(), voto);
                    sucessos.incrementAndGet();
                } catch (VotoDuplicadoException e) {
                    duplicados.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    terminaram.countDown();
                }
            });
        }

        prontos.await();
        largada.countDown();
        terminaram.await();
        executor.shutdown();

        assertThat(sucessos.get()).isEqualTo(1);
        assertThat(duplicados.get()).isEqualTo(TOTAL_THREADS - 1);
        assertThat(votoRepository.count()).isEqualTo(1);
    }
}
