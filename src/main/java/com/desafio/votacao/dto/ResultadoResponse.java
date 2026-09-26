package com.desafio.votacao.dto;

public class ResultadoResponse {
    private Long pautaId;
    private String titulo;
    private Long totalSim;
    private Long totalNao;
    private String resultado;

    public ResultadoResponse() {
    }

    public ResultadoResponse(Long pautaId, String titulo, Long totalSim, Long totalNao, String resultado) {
        this.pautaId = pautaId;
        this.titulo = titulo;
        this.totalSim = totalSim;
        this.totalNao = totalNao;
        this.resultado = resultado;
    }

    public Long getPautaId() {
        return pautaId;
    }

    public void setPautaId(Long pautaId) {
        this.pautaId = pautaId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Long getTotalSim() {
        return totalSim;
    }

    public void setTotalSim(Long totalSim) {
        this.totalSim = totalSim;
    }

    public Long getTotalNao() {
        return totalNao;
    }

    public void setTotalNao(Long totalNao) {
        this.totalNao = totalNao;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }
}
