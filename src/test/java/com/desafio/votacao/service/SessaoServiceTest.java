package com.desafio.votacao.service;

import com.desafio.votacao.entity.Pauta;
import com.desafio.votacao.entity.SessaoVotacao;
import com.desafio.votacao.exception.SessaoJaExisteException;
import com.desafio.votacao.exception.SessaoNotFoundException;
import com.desafio.votacao.repository.SessaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessaoServiceTest {

    @Mock
    private SessaoRepository sessaoRepository;

    @Mock
    private PautaService pautaService;

    @InjectMocks
    private SessaoService sessaoService;

    @Test
    void createSessao_semDuracao_deveUsarUmMinutoDefault() {
        Pauta pauta = new Pauta();
        pauta.setId(1L);
        when(pautaService.getPauta(1L)).thenReturn(pauta);
        when(sessaoRepository.findByPautaId(1L)).thenReturn(Optional.empty());
        when(sessaoRepository.save(any(SessaoVotacao.class))).thenAnswer(inv -> inv.getArgument(0));

        SessaoVotacao sessao = sessaoService.createSessao(1L, null);

        long minutos = ChronoUnit.MINUTES.between(sessao.getAbertura(), sessao.getFechamento());
        assertThat(minutos).isEqualTo(1);
    }

    @Test
    void createSessao_comDuracao_deveRespeitarValorInformado() {
        Pauta pauta = new Pauta();
        pauta.setId(1L);
        when(pautaService.getPauta(1L)).thenReturn(pauta);
        when(sessaoRepository.findByPautaId(1L)).thenReturn(Optional.empty());
        when(sessaoRepository.save(any(SessaoVotacao.class))).thenAnswer(inv -> inv.getArgument(0));

        SessaoVotacao sessao = sessaoService.createSessao(1L, 5);

        long minutos = ChronoUnit.MINUTES.between(sessao.getAbertura(), sessao.getFechamento());
        assertThat(minutos).isEqualTo(5);
    }

    @Test
    void createSessao_deveLancarExceptionQuandoJaExisteSessaoParaPauta() {
        Pauta pauta = new Pauta();
        pauta.setId(1L);
        when(pautaService.getPauta(1L)).thenReturn(pauta);
        when(sessaoRepository.findByPautaId(1L)).thenReturn(Optional.of(new SessaoVotacao()));

        assertThatThrownBy(() -> sessaoService.createSessao(1L, null))
                .isInstanceOf(SessaoJaExisteException.class);
    }

    @Test
    void buscarPorId_deveLancarExceptionQuandoNaoExiste() {
        when(sessaoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessaoService.buscarPorId(99L))
                .isInstanceOf(SessaoNotFoundException.class);
    }

    @Test
    void buscarPorPautaId_deveLancarExceptionQuandoPautaSemSessao() {
        when(sessaoRepository.findByPautaId(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessaoService.buscarPorPautaId(5L))
                .isInstanceOf(SessaoNotFoundException.class);
    }
}
