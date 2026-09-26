package com.desafio.votacao.service;

import com.desafio.votacao.dto.CriarPautaRequest;
import com.desafio.votacao.dto.PautaResponse;
import com.desafio.votacao.entity.Pauta;
import com.desafio.votacao.exception.PautaNotFoundException;
import com.desafio.votacao.repository.PautaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PautaServiceTest {

    @Mock
    private PautaRepository pautaRepository;

    @InjectMocks
    private PautaService pautaService;

    @Test
    void save_devePersistirEDevolverDto() {
        CriarPautaRequest request = new CriarPautaRequest();
        request.setTitulo("Reforma do estatuto");
        request.setDescricao("Votação da nova redação");

        Pauta pautaSalva = new Pauta();
        pautaSalva.setId(1L);
        pautaSalva.setTitulo("Reforma do estatuto");
        pautaSalva.setDescricao("Votação da nova redação");
        pautaSalva.setCriadaEm(LocalDateTime.now());

        when(pautaRepository.save(any(Pauta.class))).thenReturn(pautaSalva);

        PautaResponse resposta = pautaService.save(request);

        assertThat(resposta.getId()).isEqualTo(1L);
        assertThat(resposta.getTitulo()).isEqualTo("Reforma do estatuto");
        assertThat(resposta.getDescricao()).isEqualTo("Votação da nova redação");
    }

    @Test
    void getPauta_deveLancarExceptionQuandoNaoExiste() {
        when(pautaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pautaService.getPauta(99L))
                .isInstanceOf(PautaNotFoundException.class);
    }

    @Test
    void getPauta_deveRetornarEntityQuandoExiste() {
        Pauta pauta = new Pauta();
        pauta.setId(1L);
        pauta.setTitulo("Pauta existente");
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));

        Pauta resultado = pautaService.getPauta(1L);

        assertThat(resultado.getTitulo()).isEqualTo("Pauta existente");
    }

    @Test
    void getPautaDto_deveConverterEntityParaDto() {
        Pauta pauta = new Pauta();
        pauta.setId(2L);
        pauta.setTitulo("Outra pauta");
        pauta.setDescricao("Descrição");
        pauta.setCriadaEm(LocalDateTime.now());
        when(pautaRepository.findById(2L)).thenReturn(Optional.of(pauta));

        PautaResponse resposta = pautaService.getPautaDto(2L);

        assertThat(resposta.getId()).isEqualTo(2L);
        assertThat(resposta.getTitulo()).isEqualTo("Outra pauta");
        assertThat(resposta.getDescricao()).isEqualTo("Descrição");
    }
}
