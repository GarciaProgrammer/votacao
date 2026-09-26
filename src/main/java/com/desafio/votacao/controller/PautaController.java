package com.desafio.votacao.controller;

import com.desafio.votacao.dto.CriarPautaRequest;
import com.desafio.votacao.dto.PautaResponse;
import com.desafio.votacao.service.PautaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pautas")
public class PautaController {

    @Autowired
    private PautaService pautaService;

    @PostMapping
    public PautaResponse createPauta(@RequestBody @Valid CriarPautaRequest pauta) {
        return pautaService.save(pauta);
    }

    @GetMapping("/{id}")
    public PautaResponse getpauta(@PathVariable Long id) {
        return pautaService.getPautaDto(id);
    }


}
