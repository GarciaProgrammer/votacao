package com.desafio.votacao.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class SessaoVotacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "pauta_id", nullable = false, unique = true)
    private Pauta pauta;

    private LocalDateTime abertura;
    private LocalDateTime fechamento;

    public boolean isAberta() {
        return LocalDateTime.now().isBefore(this.fechamento);
    }

    public SessaoVotacao() {
    }

    public SessaoVotacao(Long id, Pauta pauta, LocalDateTime abertura, LocalDateTime fechamento) {
        this.id = id;
        this.pauta = pauta;
        this.abertura = abertura;
        this.fechamento = fechamento;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pauta getPauta() {
        return pauta;
    }

    public void setPauta(Pauta pauta) {
        this.pauta = pauta;
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
