package com.desafio.votacao.dto;

import com.desafio.votacao.enums.OpcaoVoto;

public class VotoRequest {
    private String associadoId;
    private OpcaoVoto opcao;


    public String getAssociadoId() {
        return associadoId;
    }

    public void setAssociadoId(String associadoId) {
        this.associadoId = associadoId;
    }

    public OpcaoVoto getOpcao() {
        return opcao;
    }

    public void setOpcao(OpcaoVoto opcao) {
        this.opcao = opcao;
    }
}
