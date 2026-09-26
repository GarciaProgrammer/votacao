package com.desafio.votacao.dto;

import java.time.LocalDateTime;

public class PautaResponse {
    private Long id;
    private String titulo;
    private String descricao;
    private LocalDateTime criadaEm;

    public PautaResponse(Long id, String titulo, String descricao, LocalDateTime criadaEm) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.criadaEm = criadaEm;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public void setCriadaEm(LocalDateTime criadaEm) {
        this.criadaEm = criadaEm;
    }
}
