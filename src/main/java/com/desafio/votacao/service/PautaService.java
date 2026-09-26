package com.desafio.votacao.service;

import com.desafio.votacao.dto.CriarPautaRequest;
import com.desafio.votacao.dto.PautaResponse;
import com.desafio.votacao.entity.Pauta;
import com.desafio.votacao.exception.PautaNotFoundException;
import com.desafio.votacao.repository.PautaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PautaService {

    private static final Logger log = LoggerFactory.getLogger(PautaService.class);

    @Autowired
    private PautaRepository pautaRepository;

    public PautaResponse save(CriarPautaRequest criarPautaRequest) {
        Pauta pauta = new Pauta();
        pauta.setTitulo(criarPautaRequest.getTitulo());
        pauta.setDescricao(criarPautaRequest.getDescricao());
        pauta.setCriadaEm(LocalDateTime.now());
        Pauta save = pautaRepository.save(pauta);
        log.info("Pauta criada: id={} titulo={}", save.getId(), save.getTitulo());
        return new PautaResponse(save.getId(), save.getTitulo(), save.getDescricao(), save.getCriadaEm());
    }

    public PautaResponse getPautaDto(Long id) {
        Pauta pauta = this.getPauta(id);
        return new PautaResponse(pauta.getId(), pauta.getTitulo(), pauta.getDescricao(), pauta.getCriadaEm());
    }

    public Pauta getPauta(Long id) {
        Optional<Pauta> pauta = pautaRepository.findById(id);
        return pauta.orElseThrow(PautaNotFoundException::new);
    }
}
