package com.desafio.votacao.service;

import com.desafio.votacao.client.CpfValidationClient;
import com.desafio.votacao.client.CpfValidationResult;
import com.desafio.votacao.dto.ResultadoResponse;
import com.desafio.votacao.dto.VotoContagem;
import com.desafio.votacao.dto.VotoRequest;
import com.desafio.votacao.entity.Pauta;
import com.desafio.votacao.entity.SessaoVotacao;
import com.desafio.votacao.entity.Voto;
import com.desafio.votacao.enums.OpcaoVoto;
import com.desafio.votacao.exception.AssociadoNaoAptoException;
import com.desafio.votacao.exception.SessaoEncerradaException;
import com.desafio.votacao.exception.VotoDuplicadoException;
import com.desafio.votacao.repository.VotoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VotoService {

    private static final Logger log = LoggerFactory.getLogger(VotoService.class);

    @Autowired
    VotoRepository votoRepository;

    @Autowired
    SessaoService sessaoService;

    @Autowired
    PautaService pautaService;

    @Autowired
    CpfValidationClient cpfValidationClient;

    public void votar(Long sessaoId, VotoRequest voto) {
        CpfValidationResult statusCpf = cpfValidationClient.validar(voto.getAssociadoId());
        if (statusCpf.equals(CpfValidationResult.UNABLE_TO_VOTE)) {
            log.warn("Voto rejeitado (associado não apto): sessaoId={} associadoId={}", sessaoId, mascarar(voto.getAssociadoId()));
            throw new AssociadoNaoAptoException();
        }
        SessaoVotacao sessaoVotacao = sessaoService.buscarPorId(sessaoId);
        if (!sessaoVotacao.isAberta()) {
            log.warn("Voto rejeitado (sessão encerrada): sessaoId={}", sessaoId);
            throw new SessaoEncerradaException();
        }
        if (votoRepository.existsBySessaoIdAndAssociadoId(sessaoId, voto.getAssociadoId())) {
            log.warn("Voto rejeitado (duplicado): sessaoId={} associadoId={}", sessaoId, mascarar(voto.getAssociadoId()));
            throw new VotoDuplicadoException();
        }
        Voto novoVoto = new Voto(sessaoVotacao, voto.getAssociadoId(), voto.getOpcao(), LocalDateTime.now());
        votoRepository.save(novoVoto);
        log.info("Voto registrado: sessaoId={} associadoId={} opcao={}", sessaoId, mascarar(voto.getAssociadoId()), voto.getOpcao());
    }

    public ResultadoResponse apurar(Long pautaId) {
        Pauta pauta = pautaService.getPauta(pautaId);
        SessaoVotacao sessao = sessaoService.buscarPorPautaId(pautaId);
        List<VotoContagem> votoContagens = votoRepository.contarPorSessao(sessao.getId());
        long totalSim = 0, totalNao = 0;
        for (VotoContagem votoContagem : votoContagens) {
            if (votoContagem.getOpcao().equals(OpcaoVoto.SIM)) {
                totalSim = votoContagem.getTotal();
            } else {
                totalNao = votoContagem.getTotal();
            }
        }
        String resultado = totalSim > totalNao ? "APROVADA" : totalNao > totalSim ? "REJEITADA" : "EMPATE";
        log.info("Resultado apurado: pautaId={} totalSim={} totalNao={} resultado={}", pautaId, totalSim, totalNao, resultado);
        return new ResultadoResponse(pautaId, pauta.getTitulo(), totalSim, totalNao, resultado);
    }

    private String mascarar(String associadoId) {
        if (associadoId == null || associadoId.length() < 3) {
            return "***";
        }
        return "***" + associadoId.substring(associadoId.length() - 3);
    }

}
