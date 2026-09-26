package com.desafio.votacao;

import com.desafio.votacao.client.CpfValidationClient;
import com.desafio.votacao.client.CpfValidationResult;
import com.desafio.votacao.dto.CriarPautaRequest;
import com.desafio.votacao.dto.PautaResponse;
import com.desafio.votacao.dto.SessaoResponse;
import com.desafio.votacao.dto.VotoRequest;
import com.desafio.votacao.enums.OpcaoVoto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class VotacaoIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void configurarDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @TestConfiguration
    static class CpfSempreAptoConfig {
        @Bean
        @Primary
        CpfValidationClient cpfValidationClient() {
            return cpf -> CpfValidationResult.ABLE_TO_VOTE;
        }
    }

    @LocalServerPort
    private int porta;

    @Autowired
    private TestRestTemplate restTemplate;

    private String url(String path) {
        return "http://localhost:" + porta + path;
    }

    @Test
    void fluxoCompleto_deveCadastrarAbrirVotarEApurar() {
        CriarPautaRequest criarPauta = new CriarPautaRequest();
        criarPauta.setTitulo("Aprovar novo estatuto");
        criarPauta.setDescricao("Votação sobre o novo estatuto social");

        ResponseEntity<PautaResponse> respostaPauta =
                restTemplate.postForEntity(url("/api/v1/pautas"), criarPauta, PautaResponse.class);
        assertThat(respostaPauta.getStatusCode().is2xxSuccessful()).isTrue();
        Long pautaId = respostaPauta.getBody().getId();

        ResponseEntity<SessaoResponse> respostaSessao = restTemplate.postForEntity(
                url("/api/v1/sessoes/" + pautaId + "/create/1"), null, SessaoResponse.class);
        assertThat(respostaSessao.getStatusCode().is2xxSuccessful()).isTrue();
        Long sessaoId = respostaSessao.getBody().getId();

        VotoRequest voto = new VotoRequest();
        voto.setAssociadoId("11111111111");
        voto.setOpcao(OpcaoVoto.SIM);

        ResponseEntity<Void> respostaVoto =
                restTemplate.postForEntity(url("/api/v1/sessoes/" + sessaoId + "/votos"), voto, Void.class);
        assertThat(respostaVoto.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<String> respostaDuplicado =
                restTemplate.postForEntity(url("/api/v1/sessoes/" + sessaoId + "/votos"), voto, String.class);
        assertThat(respostaDuplicado.getStatusCode().value()).isEqualTo(409);

        VotoRequest votoContra = new VotoRequest();
        votoContra.setAssociadoId("22222222222");
        votoContra.setOpcao(OpcaoVoto.NAO);
        restTemplate.postForEntity(url("/api/v1/sessoes/" + sessaoId + "/votos"), votoContra, Void.class);

        ResponseEntity<com.desafio.votacao.dto.ResultadoResponse> respostaResultado = restTemplate.getForEntity(
                url("/api/v1/pautas/" + pautaId + "/resultado"), com.desafio.votacao.dto.ResultadoResponse.class);
        assertThat(respostaResultado.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(respostaResultado.getBody().getTotalSim()).isEqualTo(1L);
        assertThat(respostaResultado.getBody().getTotalNao()).isEqualTo(1L);
        assertThat(respostaResultado.getBody().getResultado()).isEqualTo("EMPATE");
    }

    @Test
    void criarPauta_semTitulo_deveRetornar400() {
        CriarPautaRequest semTitulo = new CriarPautaRequest();
        semTitulo.setDescricao("Sem título");

        ResponseEntity<String> resposta =
                restTemplate.postForEntity(url("/api/v1/pautas"), semTitulo, String.class);

        assertThat(resposta.getStatusCode().value()).isEqualTo(400);
    }
}
