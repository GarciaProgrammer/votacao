package com.desafio.votacao.service;

import com.desafio.votacao.client.CpfValidationClient;
import com.desafio.votacao.client.CpfValidationResult;
import com.desafio.votacao.dto.ResultadoResponse;
import com.desafio.votacao.dto.VotoContagem;
import com.desafio.votacao.dto.VotoRequest;
import com.desafio.votacao.entity.Pauta;
import com.desafio.votacao.entity.SessaoVotacao;
import com.desafio.votacao.enums.OpcaoVoto;
import com.desafio.votacao.exception.AssociadoNaoAptoException;
import com.desafio.votacao.exception.SessaoEncerradaException;
import com.desafio.votacao.exception.VotoDuplicadoException;
import com.desafio.votacao.repository.VotoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VotoServiceTest {

    @Mock
    private VotoRepository votoRepository;
    @Mock
    private SessaoService sessaoService;
    @Mock
    private PautaService pautaService;
    @Mock
    private CpfValidationClient cpfValidationClient;

    @InjectMocks
    private VotoService votoService;

    private static final String ASSOCIADO_ID = "12345678901";

    private SessaoVotacao sessaoAberta() {
        SessaoVotacao sessao = new SessaoVotacao();
        sessao.setId(1L);
        sessao.setAbertura(LocalDateTime.now());
        sessao.setFechamento(LocalDateTime.now().plusMinutes(1));
        return sessao;
    }

    private SessaoVotacao sessaoFechada() {
        SessaoVotacao sessao = new SessaoVotacao();
        sessao.setId(1L);
        sessao.setAbertura(LocalDateTime.now().minusMinutes(5));
        sessao.setFechamento(LocalDateTime.now().minusMinutes(1));
        return sessao;
    }

    private VotoRequest votoRequest() {
        VotoRequest request = new VotoRequest();
        request.setAssociadoId(ASSOCIADO_ID);
        request.setOpcao(OpcaoVoto.SIM);
        return request;
    }

    private VotoContagem contagem(OpcaoVoto opcao, Long total) {
        return new VotoContagem() {
            @Override
            public OpcaoVoto getOpcao() {
                return opcao;
            }

            @Override
            public Long getTotal() {
                return total;
            }
        };
    }

    @Test
    void votar_deveSalvarQuandoTudoValido() {
        when(cpfValidationClient.validar(ASSOCIADO_ID)).thenReturn(CpfValidationResult.ABLE_TO_VOTE);
        when(sessaoService.buscarPorId(1L)).thenReturn(sessaoAberta());
        when(votoRepository.existsBySessaoIdAndAssociadoId(1L, ASSOCIADO_ID)).thenReturn(false);

        votoService.votar(1L, votoRequest());

        verify(votoRepository).save(any());
    }

    @Test
    void votar_deveLancarExceptionQuandoAssociadoNaoApto() {
        when(cpfValidationClient.validar(ASSOCIADO_ID)).thenReturn(CpfValidationResult.UNABLE_TO_VOTE);

        assertThatThrownBy(() -> votoService.votar(1L, votoRequest()))
                .isInstanceOf(AssociadoNaoAptoException.class);

        verify(votoRepository, never()).save(any());
    }

    @Test
    void votar_deveLancarExceptionQuandoSessaoEncerrada() {
        when(cpfValidationClient.validar(ASSOCIADO_ID)).thenReturn(CpfValidationResult.ABLE_TO_VOTE);
        when(sessaoService.buscarPorId(1L)).thenReturn(sessaoFechada());

        assertThatThrownBy(() -> votoService.votar(1L, votoRequest()))
                .isInstanceOf(SessaoEncerradaException.class);

        verify(votoRepository, never()).save(any());
    }

    @Test
    void votar_deveLancarExceptionQuandoVotoDuplicado() {
        when(cpfValidationClient.validar(ASSOCIADO_ID)).thenReturn(CpfValidationResult.ABLE_TO_VOTE);
        when(sessaoService.buscarPorId(1L)).thenReturn(sessaoAberta());
        when(votoRepository.existsBySessaoIdAndAssociadoId(1L, ASSOCIADO_ID)).thenReturn(true);

        assertThatThrownBy(() -> votoService.votar(1L, votoRequest()))
                .isInstanceOf(VotoDuplicadoException.class);

        verify(votoRepository, never()).save(any());
    }

    @Test
    void apurar_deveContarVotosEDecidirAprovada() {
        Pauta pauta = new Pauta();
        pauta.setId(1L);
        pauta.setTitulo("Pauta teste");
        SessaoVotacao sessao = sessaoAberta();

        when(pautaService.getPauta(1L)).thenReturn(pauta);
        when(sessaoService.buscarPorPautaId(1L)).thenReturn(sessao);
        when(votoRepository.contarPorSessao(sessao.getId())).thenReturn(List.of(
                contagem(OpcaoVoto.SIM, 7L),
                contagem(OpcaoVoto.NAO, 3L)
        ));

        ResultadoResponse resultado = votoService.apurar(1L);

        assertThat(resultado.getTotalSim()).isEqualTo(7L);
        assertThat(resultado.getTotalNao()).isEqualTo(3L);
        assertThat(resultado.getResultado()).isEqualTo("APROVADA");
    }

    @Test
    void apurar_deveDecidirEmpateQuandoContagemIgual() {
        Pauta pauta = new Pauta();
        pauta.setId(1L);
        pauta.setTitulo("Pauta teste");
        SessaoVotacao sessao = sessaoAberta();

        when(pautaService.getPauta(1L)).thenReturn(pauta);
        when(sessaoService.buscarPorPautaId(1L)).thenReturn(sessao);
        when(votoRepository.contarPorSessao(sessao.getId())).thenReturn(List.of(
                contagem(OpcaoVoto.SIM, 5L),
                contagem(OpcaoVoto.NAO, 5L)
        ));

        ResultadoResponse resultado = votoService.apurar(1L);

        assertThat(resultado.getResultado()).isEqualTo("EMPATE");
    }

    @Test
    void apurar_deveDecidirRejeitadaQuandoNaoMaiorQueSim() {
        Pauta pauta = new Pauta();
        pauta.setId(1L);
        pauta.setTitulo("Pauta teste");
        SessaoVotacao sessao = sessaoAberta();

        when(pautaService.getPauta(1L)).thenReturn(pauta);
        when(sessaoService.buscarPorPautaId(1L)).thenReturn(sessao);
        when(votoRepository.contarPorSessao(sessao.getId())).thenReturn(List.of(
                contagem(OpcaoVoto.SIM, 2L),
                contagem(OpcaoVoto.NAO, 9L)
        ));

        ResultadoResponse resultado = votoService.apurar(1L);

        assertThat(resultado.getResultado()).isEqualTo("REJEITADA");
    }
}
