package com.desafio.votacao.client;

import com.desafio.votacao.exception.CpfInvalidoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class FakeCpfValidationClient implements CpfValidationClient {

    private static final Logger log = LoggerFactory.getLogger(FakeCpfValidationClient.class);
    private static final int CHANCE_CPF_INVALIDO_PERCENT = 20;

    private final Random random = new Random();

    public CpfValidationResult validar(String cpf) {
        if (random.nextInt(100) < CHANCE_CPF_INVALIDO_PERCENT) {
            log.debug("CPF sorteado como inválido");
            throw new CpfInvalidoException();
        }
        CpfValidationResult resultado = random.nextBoolean() ? CpfValidationResult.ABLE_TO_VOTE : CpfValidationResult.UNABLE_TO_VOTE;
        log.debug("CPF válido, resultado sorteado: {}", resultado);
        return resultado;
    }
}
