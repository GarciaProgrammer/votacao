package com.desafio.votacao.dto;

import com.desafio.votacao.enums.OpcaoVoto;

public interface VotoContagem {

    OpcaoVoto getOpcao();

    Long getTotal();
}
