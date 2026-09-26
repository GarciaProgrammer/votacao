package com.desafio.votacao.repository;

import com.desafio.votacao.dto.VotoContagem;
import com.desafio.votacao.entity.Voto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VotoRepository extends JpaRepository<Voto, Long> {

    boolean existsBySessaoIdAndAssociadoId(Long sessaoId, String associadoId);

    @Query("SELECT v.voto AS opcao, COUNT(v) AS total FROM Voto v WHERE v.sessao.id = :sessaoId GROUP BY v.voto")
    List<VotoContagem> contarPorSessao(Long sessaoId);
}
