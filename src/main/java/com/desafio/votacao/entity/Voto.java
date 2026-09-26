package com.desafio.votacao.entity;

import com.desafio.votacao.enums.OpcaoVoto;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"sessao_id", "associado_id"}))
@Entity
public class Voto {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sessao_id")
    private SessaoVotacao sessao;

    private String associadoId;

    @Enumerated(EnumType.STRING)
    private OpcaoVoto voto;

    private LocalDateTime criadoEm;


    public Voto() {
    }

    public Voto(SessaoVotacao sessao, String associadoId, OpcaoVoto voto, LocalDateTime criadoEm) {
        this.sessao = sessao;
        this.associadoId = associadoId;
        this.voto = voto;
        this.criadoEm = criadoEm;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SessaoVotacao getSessao() {
        return sessao;
    }

    public void setSessao(SessaoVotacao sessao) {
        this.sessao = sessao;
    }

    public String getAssociadoId() {
        return associadoId;
    }

    public void setAssociadoId(String associadoId) {
        this.associadoId = associadoId;
    }

    public OpcaoVoto getVoto() {
        return voto;
    }

    public void setVoto(OpcaoVoto voto) {
        this.voto = voto;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
}
