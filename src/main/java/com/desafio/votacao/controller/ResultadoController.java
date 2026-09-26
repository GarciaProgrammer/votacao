package com.desafio.votacao.controller;

import com.desafio.votacao.dto.ResultadoResponse;
import com.desafio.votacao.service.VotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pautas/{pautaId}/resultado")
public class ResultadoController {

    @Autowired
    private VotoService votoService;

    @GetMapping
    public ResultadoResponse getResultado(@PathVariable Long pautaId) {
        return votoService.apurar(pautaId);
    }
}
