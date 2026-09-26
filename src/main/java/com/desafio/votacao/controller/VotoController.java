package com.desafio.votacao.controller;

import com.desafio.votacao.dto.VotoRequest;
import com.desafio.votacao.service.VotoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sessoes/{sessaoId}/votos")
public class VotoController {

    @Autowired
    private VotoService votoService;

    @PostMapping
    public ResponseEntity<Void> votar(@PathVariable Long sessaoId, @RequestBody @Valid VotoRequest votarRequest) {
        votoService.votar(sessaoId, votarRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
