package com.desafio.votacao.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PautaNotFoundException.class)
    public ResponseEntity<?> handlePautaNotFoundException(PautaNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Pauta não encontrada");
    }

    @ExceptionHandler(SessaoNotFoundException.class)
    public ResponseEntity<?> handleSessaoNotFoundException(SessaoNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sessão não encontrada");
    }

    @ExceptionHandler(SessaoJaExisteException.class)
    public ResponseEntity<?> handleSessaoJaExisteException(SessaoJaExisteException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Sessão já existe");
    }

    @ExceptionHandler(SessaoEncerradaException.class)
    public ResponseEntity<?> handleSessaoEncerradaException(SessaoEncerradaException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Sessão já encerrada");
    }

    @ExceptionHandler(VotoDuplicadoException.class)
    public ResponseEntity<?> handleVotoDuplicadoException(VotoDuplicadoException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Voto duplicado");
    }

    @ExceptionHandler(CpfInvalidoException.class)
    public ResponseEntity<?> handleCpfInvalidoException(CpfInvalidoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cpf Invalido");
    }

    @ExceptionHandler(AssociadoNaoAptoException.class)
    public ResponseEntity<?> handleAssociadoNaoAptoException(AssociadoNaoAptoException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Associado não apto a votar");
    }


}
