package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.models.EstadoProcessoAposentacao;
import cv.igrp.RH_Service.colaboradores.domain.models.ModalidadeAposentacao;
import cv.igrp.RH_Service.colaboradores.domain.models.ProcessoAposentacao;
import cv.igrp.RH_Service.colaboradores.domain.models.ProrrogacaoPermanencia;
import cv.igrp.RH_Service.colaboradores.domain.repository.AposentacaoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ProcessoAposentacaoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ProrrogacaoPermanenciaId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FuncionarioEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.ProcessoAposentacaoEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.ProrrogacaoPermanenciaEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsProcessoAposentacaoEntityRepository;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsProrrogacaoPermanenciaEntityRepository;
import cv.igrp.RH_Service.shared.infrastructure.persistence.JpaReferences;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AposentacaoRepositoryImpl implements AposentacaoRepository {

    private final ColabsProcessoAposentacaoEntityRepository processos;
    private final ColabsProrrogacaoPermanenciaEntityRepository prorrogacoes;
    private final JpaReferences refs;

    @Transactional
    @Override
    public ProcessoAposentacao save(ProcessoAposentacao p) {
        ProcessoAposentacaoEntity e = processos.findById(p.getId().getValor()).orElseGet(() -> {
            var n = new ProcessoAposentacaoEntity();
            n.setId(p.getId().getValor());
            n.setFuncionario(refs.ref(FuncionarioEntity.class, p.getFuncionarioId().getValor()));
            n.setModalidade(p.getModalidade().name());
            n.setIniciativa(p.getIniciativa().name());
            n.setDataPedido(p.getDataPedido());
            n.setFundamentacao(p.getFundamentacao());
            n.setAcordoFuncionario(p.isAcordoFuncionario());
            return n;
        });
        e.setEstado(p.getEstado().name());
        e.setDataPrevista(p.getDataPrevista());
        e.setDespachoNumero(p.getDespachoNumero());
        e.setDespachoData(p.getDespachoData());
        e.setMotivoIndeferimento(p.getMotivoIndeferimento());
        e.setDataDesligacao(p.getDataDesligacao());
        e.setPercentagemPrestacao(p.getPercentagemPrestacao());
        e.setDataAposentacao(p.getDataAposentacao());
        e.setMotivoCancelamento(p.getMotivoCancelamento());
        return toDomain(processos.save(e));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<ProcessoAposentacao> findProcesso(ProcessoAposentacaoId id) {
        return processos.findById(id.getValor()).map(this::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProcessoAposentacao> findProcessos(FuncionarioId funcionarioId) {
        return processos.findDoFuncionario(funcionarioId.getValor()).stream().map(this::toDomain).toList();
    }

    @Transactional
    @Override
    public ProrrogacaoPermanencia save(ProrrogacaoPermanencia p) {
        ProrrogacaoPermanenciaEntity e = prorrogacoes.findById(p.getId().getValor()).orElseGet(() -> {
            var n = new ProrrogacaoPermanenciaEntity();
            n.setId(p.getId().getValor());
            n.setFuncionario(refs.ref(FuncionarioEntity.class, p.getFuncionarioId().getValor()));
            n.setDataPedido(p.getDataPedido());
            n.setPropostaFundamentada(p.getPropostaFundamentada());
            n.setValidaAte(p.getValidaAte());
            return n;
        });
        e.setEstado(p.getEstado().name());
        e.setDespachoNumero(p.getDespachoNumero());
        e.setDespachoData(p.getDespachoData());
        e.setMotivoIndeferimento(p.getMotivoIndeferimento());
        return toDomain(prorrogacoes.save(e));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<ProrrogacaoPermanencia> findProrrogacao(ProrrogacaoPermanenciaId id) {
        return prorrogacoes.findById(id.getValor()).map(this::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProrrogacaoPermanencia> findProrrogacoes(FuncionarioId funcionarioId) {
        return prorrogacoes.findDoFuncionario(funcionarioId.getValor()).stream().map(this::toDomain).toList();
    }

    private ProcessoAposentacao toDomain(ProcessoAposentacaoEntity e) {
        return ProcessoAposentacao.reconstruir(ProcessoAposentacaoId.from(e.getId()),
                FuncionarioId.from(refs.idOf(e.getFuncionario(), FuncionarioEntity::getId)),
                ModalidadeAposentacao.valueOf(e.getModalidade()), EstadoProcessoAposentacao.valueOf(e.getEstado()),
                ProcessoAposentacao.Iniciativa.valueOf(e.getIniciativa()), e.getDataPedido(), e.getDataPrevista(),
                e.getFundamentacao(), Boolean.TRUE.equals(e.getAcordoFuncionario()), e.getDespachoNumero(),
                e.getDespachoData(), e.getMotivoIndeferimento(), e.getDataDesligacao(), e.getPercentagemPrestacao(),
                e.getDataAposentacao(), e.getMotivoCancelamento());
    }

    private ProrrogacaoPermanencia toDomain(ProrrogacaoPermanenciaEntity e) {
        return ProrrogacaoPermanencia.reconstruir(ProrrogacaoPermanenciaId.from(e.getId()),
                FuncionarioId.from(refs.idOf(e.getFuncionario(), FuncionarioEntity::getId)),
                ProrrogacaoPermanencia.Estado.valueOf(e.getEstado()), e.getDataPedido(), e.getPropostaFundamentada(),
                e.getValidaAte(), e.getDespachoNumero(), e.getDespachoData(), e.getMotivoIndeferimento());
    }
}
