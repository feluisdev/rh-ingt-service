package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.models.AcidenteServico;
import cv.igrp.RH_Service.colaboradores.domain.repository.AcidenteServicoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.AcidenteServicoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.AcidenteIncapacidadeEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.AcidenteServicoEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FuncionarioEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsAcidenteServicoEntityRepository;
import cv.igrp.RH_Service.shared.infrastructure.persistence.JpaReferences;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class AcidenteServicoRepositoryImpl implements AcidenteServicoRepository {

    private final ColabsAcidenteServicoEntityRepository entityRepository;
    private final JpaReferences refs;

    @Transactional
    @Override
    public AcidenteServico save(AcidenteServico a) {
        var e = entityRepository.findById(a.getId().getValor()).orElseGet(() -> {
            var n = new AcidenteServicoEntity();
            n.setId(a.getId().getValor());
            n.setFuncionario(refs.ref(FuncionarioEntity.class, a.getFuncionarioId().getValor()));
            return n;
        });
        e.setTipo(a.getTipo().name());
        e.setDataHora(a.getDataHora());
        e.setLocal(a.getLocal());
        e.setDescricao(a.getDescricao());
        e.setTestemunhas(a.getTestemunhas());
        e.setDataParticipacao(a.getDataParticipacao());
        e.setParticipadoPeloProprio(a.isParticipadoPeloProprio());
        e.setEstado(a.getEstado().name());
        e.setDespacho(a.getDespacho());
        e.setMotivo(a.getMotivo());
        e.setSeguradora(a.getSeguradora());
        e.setApolice(a.getApolice());
        e.setParticipacaoSeguradora(a.getParticipacaoSeguradora());
        e.setAlta(a.getAlta());
        e.setIncapacidadePermanente(a.getIncapacidadePermanente());
        e.setIncapacidadeAbsoluta(a.isIncapacidadeAbsoluta());
        Map<UUID, AcidenteIncapacidadeEntity> existentes = e.getIncapacidades().stream()
                .collect(Collectors.toMap(AcidenteIncapacidadeEntity::getId, Function.identity()));
        for (var i : a.getIncapacidades()) {
            var x = existentes.get(i.id());
            if (x == null) {
                x = new AcidenteIncapacidadeEntity();
                x.setId(i.id());
                x.setAcidente(e);
                e.getIncapacidades().add(x);
            }
            x.setTipo(i.tipo().name());
            x.setInicio(i.inicio());
            x.setFim(i.fim());
        }
        return toDomain(entityRepository.save(e));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<AcidenteServico> findById(AcidenteServicoId id) {
        return entityRepository.findById(id.getValor()).map(this::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<AcidenteServico> find(AcidenteServico.Estado estado) {
        return entityRepository.find(estado != null ? estado.name() : null).stream().map(this::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<AcidenteServico> findByFuncionario(FuncionarioId funcionarioId) {
        return entityRepository.findDoFuncionario(funcionarioId.getValor()).stream().map(this::toDomain).toList();
    }

    private AcidenteServico toDomain(AcidenteServicoEntity e) {
        var incapacidades = e.getIncapacidades().stream().map(x -> new AcidenteServico.Incapacidade(x.getId(),
                AcidenteServico.TipoIncapacidade.valueOf(x.getTipo()), x.getInicio(), x.getFim())).toList();
        return AcidenteServico.reconstruir(AcidenteServicoId.from(e.getId()), FuncionarioId.from(refs.idOf(e.getFuncionario(), FuncionarioEntity::getId)),
                AcidenteServico.Tipo.valueOf(e.getTipo()), e.getDataHora(), e.getLocal(), e.getDescricao(), e.getTestemunhas(), e.getDataParticipacao(),
                Boolean.TRUE.equals(e.getParticipadoPeloProprio()), AcidenteServico.Estado.valueOf(e.getEstado()), e.getDespacho(), e.getMotivo(),
                e.getSeguradora(), e.getApolice(), e.getParticipacaoSeguradora(), incapacidades, e.getAlta(), e.getIncapacidadePermanente(),
                Boolean.TRUE.equals(e.getIncapacidadeAbsoluta()));
    }
}
