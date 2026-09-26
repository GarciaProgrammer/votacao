package com.desafio.votacao.controller;

import com.desafio.votacao.dto.SessaoResponse;
import com.desafio.votacao.entity.SessaoVotacao;
import com.desafio.votacao.service.SessaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sessoes")
public class SessaoController {

    @Autowired
    private SessaoService sessaoService;

    @PostMapping("/{pautaId}/create")
    public SessaoResponse createSession(@PathVariable Long pautaId, @RequestParam(required = false) Integer duracao) {
        SessaoVotacao sessao = sessaoService.createSessao(pautaId, duracao);
        return new SessaoResponse(sessao.getId(), sessao.getPauta().getId(), sessao.getAbertura(), sessao.getFechamento());
    }

}
