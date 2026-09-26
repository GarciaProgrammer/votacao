package com.desafio.votacao.dto;

import java.time.LocalDateTime;

public class SessaoResponse {

    private Long id;
    private Long pautaId;
    private LocalDateTime abertura;
    private LocalDateTime fechamento;

    public SessaoResponse() {
    }

    public SessaoResponse(Long id, Long pautaId, LocalDateTime abertura, LocalDateTime fechamento) {
        this.id = id;
        this.pautaId = pautaId;
        this.abertura = abertura;
        this.fechamento = fechamento;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPautaId() {
        return pautaId;
    }

    public void setPautaId(Long pautaId) {
        this.pautaId = pautaId;
    }

    public LocalDateTime getAbertura() {
        return abertura;
    }

    public void setAbertura(LocalDateTime abertura) {
        this.abertura = abertura;
    }

    public LocalDateTime getFechamento() {
        return fechamento;
    }

    public void setFechamento(LocalDateTime fechamento) {
        this.fechamento = fechamento;
    }
}
