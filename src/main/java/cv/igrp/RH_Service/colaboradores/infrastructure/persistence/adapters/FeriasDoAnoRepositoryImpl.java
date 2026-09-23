package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.models.AlteracaoMarcacaoFerias;
import cv.igrp.RH_Service.colaboradores.domain.models.FeriasDoAno;
import cv.igrp.RH_Service.colaboradores.domain.models.MotivoAlteracaoMapaFerias;
import cv.igrp.RH_Service.colaboradores.domain.models.OrigemMarcacaoFerias;
import cv.igrp.RH_Service.colaboradores.domain.models.PeriodoFerias;
import cv.igrp.RH_Service.colaboradores.domain.repository.FeriasDoAnoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FeriasDoAnoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FeriasAlteracaoEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FeriasDoAnoEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FeriasPeriodoEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FuncionarioEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsFeriasDoAnoEntityRepository;
import cv.igrp.RH_Service.shared.infrastructure.persistence.JpaReferences;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FeriasDoAnoRepositoryImpl implements FeriasDoAnoRepository {

    private final ColabsFeriasDoAnoEntityRepository entityRepository;
    private final JpaReferences refs;

    @Transactional
    @Override
    public FeriasDoAno save(FeriasDoAno f) {
        FeriasDoAnoEntity e = entityRepository.findById(f.getId().getValor()).orElseGet(() -> {
            FeriasDoAnoEntity nova = new FeriasDoAnoEntity();
            nova.setId(f.getId().getValor());
            nova.setFuncionario(refs.ref(FuncionarioEntity.class, f.getFuncionarioId().getValor()));
            nova.setAno(f.getAno());
            return nova;
        });

        e.setPreferenciaIndicadaEm(f.getPreferenciaIndicadaEm());
        e.setPreferenciaForaDePrazo(f.isPreferenciaForaDePrazo());
        e.setPreferenciaObservacoes(f.getPreferenciaObservacoes());
        e.setOrigem(f.getOrigem() != null ? f.getOrigem().name() : null);
        e.setFundamentacao(f.getFundamentacao());
        e.setMarcadaEm(f.getMarcadaEm());

        // Os periodos substituem-se por inteiro: a preferencia e a marcacao sao conjuntos, e o
        // que interessa e o conjunto actual. O historico das marcacoes vive nas alteracoes.
        e.getPeriodos().clear();
        f.getPreferencia().forEach(p -> e.getPeriodos().add(periodo(e, FeriasPeriodoEntity.PREFERENCIA, p)));
        f.getMarcacao().forEach(p -> e.getPeriodos().add(periodo(e, FeriasPeriodoEntity.MARCACAO, p)));

        // As alteracoes so crescem: acrescenta-se o que o dominio tem a mais.
        List<AlteracaoMarcacaoFerias> alteracoes = f.getAlteracoes();
        for (int i = e.getAlteracoes().size(); i < alteracoes.size(); i++) {
            AlteracaoMarcacaoFerias a = alteracoes.get(i);
            FeriasAlteracaoEntity ae = new FeriasAlteracaoEntity();
            ae.setId(UUID.randomUUID());
            ae.setFeriasAno(e);
            ae.setMotivo(a.motivo().name());
            ae.setFundamentacao(a.fundamentacao());
            ae.setPeriodosAnteriores(a.periodosAnteriores());
            ae.setPeriodosNovos(a.periodosNovos());
            ae.setAlteradaEm(a.alteradaEm());
            e.getAlteracoes().add(ae);
        }

        return toDomain(entityRepository.save(e));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<FeriasDoAno> findByFuncionarioIdAndAno(FuncionarioId funcionarioId, int ano) {
        return entityRepository.findByFuncionarioIdAndAno(funcionarioId.getValor(), ano).map(this::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<FeriasDoAno> findAllComMarcacaoByAno(int ano) {
        return entityRepository.findAllComMarcacaoByAno(ano).stream().map(this::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<UUID> findFuncionariosActivosSemMarcacao(int ano) {
        return entityRepository.findFuncionariosActivosSemMarcacao(ano);
    }

    @Transactional(readOnly = true)
    @Override
    public List<UUID> findFuncionariosComPreferencia(int ano) {
        return entityRepository.findFuncionariosComPreferencia(ano);
    }

    private static FeriasPeriodoEntity periodo(FeriasDoAnoEntity dono, String natureza, PeriodoFerias p) {
        FeriasPeriodoEntity pe = new FeriasPeriodoEntity();
        pe.setId(UUID.randomUUID());
        pe.setFeriasAno(dono);
        pe.setNatureza(natureza);
        pe.setDataInicio(p.inicio());
        pe.setDataFim(p.fim());
        pe.setDiasUteis(p.diasUteis());
        return pe;
    }

    private FeriasDoAno toDomain(FeriasDoAnoEntity e) {
        List<PeriodoFerias> preferencia = e.getPeriodos().stream()
                .filter(p -> FeriasPeriodoEntity.PREFERENCIA.equals(p.getNatureza()))
                .map(p -> new PeriodoFerias(p.getDataInicio(), p.getDataFim(), p.getDiasUteis())).toList();
        List<PeriodoFerias> marcacao = e.getPeriodos().stream()
                .filter(p -> FeriasPeriodoEntity.MARCACAO.equals(p.getNatureza()))
                .map(p -> new PeriodoFerias(p.getDataInicio(), p.getDataFim(), p.getDiasUteis())).toList();
        List<AlteracaoMarcacaoFerias> alteracoes = e.getAlteracoes().stream()
                .map(a -> new AlteracaoMarcacaoFerias(MotivoAlteracaoMapaFerias.valueOf(a.getMotivo()),
                        a.getFundamentacao(), a.getPeriodosAnteriores(), a.getPeriodosNovos(), a.getAlteradaEm()))
                .toList();
        return FeriasDoAno.reconstituir(
                FeriasDoAnoId.from(e.getId()),
                FuncionarioId.from(refs.idOf(e.getFuncionario(), FuncionarioEntity::getId)),
                e.getAno(),
                preferencia, e.getPreferenciaIndicadaEm(), e.isPreferenciaForaDePrazo(), e.getPreferenciaObservacoes(),
                marcacao, e.getOrigem() != null ? OrigemMarcacaoFerias.valueOf(e.getOrigem()) : null,
                e.getFundamentacao(), e.getMarcadaEm(),
                alteracoes);
    }
}
