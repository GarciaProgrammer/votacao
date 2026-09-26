package com.desafio.votacao.service;

import com.desafio.votacao.entity.Pauta;
import com.desafio.votacao.entity.SessaoVotacao;
import com.desafio.votacao.exception.SessaoJaExisteException;
import com.desafio.votacao.exception.SessaoNotFoundException;
import com.desafio.votacao.repository.SessaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SessaoService {

    private static final Logger log = LoggerFactory.getLogger(SessaoService.class);

    @Autowired
    private SessaoRepository sessaoRepository;

    @Autowired
    private PautaService pautaService;

    public SessaoVotacao createSessao(Long pautaId, Integer duracao) {
        Pauta pauta = pautaService.getPauta(pautaId);
        sessaoRepository.findByPautaId(pautaId).ifPresent(s -> {
            throw new SessaoJaExisteException();
        });

        LocalDateTime abertura = LocalDateTime.now();
        LocalDateTime fechada = abertura.plusMinutes(duracao != null ? duracao : 1);

        SessaoVotacao sessaoVotacao = new SessaoVotacao();
        sessaoVotacao.setPauta(pauta);
        sessaoVotacao.setAbertura(abertura);
        sessaoVotacao.setFechamento(fechada);
        SessaoVotacao salva = sessaoRepository.save(sessaoVotacao);
        log.info("Sessão aberta: sessaoId={} pautaId={} fechamento={}", salva.getId(), pautaId, fechada);
        return salva;
    }

    public SessaoVotacao buscarPorId(Long id) {
        return sessaoRepository.findById(id).orElseThrow(SessaoNotFoundException::new);
    }

    public SessaoVotacao buscarPorPautaId(Long pautaId) {
        return sessaoRepository.findByPautaId(pautaId).orElseThrow(SessaoNotFoundException::new);
    }

}
