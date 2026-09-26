package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.models.AcumulacaoFuncoes;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.repository.AcumulacaoFuncoesRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.AcumulacaoFuncoesId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.application.services.notificacoes.Notificador;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.notificacoes.TipoNotificacao;
import cv.igrp.RH_Service.shared.domain.service.Datas;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * <b>Acumulação de funções</b> (Lei n.º 20/X/2023, arts. 20.º–24.º; BR-ACU-01..08): o pedido (pelo próprio ou pelo RH), o
 * despacho de quem a lei manda, a cessação e a caducidade no fim do período.
 */
@Service
@RequiredArgsConstructor
public class AcumulacaoFuncoesService {

    static final String RECURSO = "ACUMULACAO_FUNCOES";
    /** Com que antecedência se avisa do fim do período [ind.]. */
    static final int DIAS_AVISO = 30;

    public record Dados(AcumulacaoFuncoes.Tipo tipo, AcumulacaoFuncoes.CasoPublico caso, boolean remunerada, String entidade, String funcoes,
                        String horario, Integer horasSemanais, LocalDate inicio, LocalDate fim, boolean declaracaoSemConflito) {}

    private final AcumulacaoFuncoesRepository repository;
    private final FuncionarioRepository funcionarioRepository;
    private final HorarioColaboradorService horarioService;
    private final ChefiaService chefiaService;
    private final Notificador notificador;

    @Transactional
    public AcumulacaoFuncoes pedir(FuncionarioId funcionarioId, Dados d, boolean peloProprio) {
        var f = funcionario(funcionarioId);
        if (!Boolean.TRUE.equals(f.getIsActive())) throw invalido("Este colaborador já não está ao serviço.");
        var a = repository.save(AcumulacaoFuncoes.pedir(funcionarioId, d.tipo(), d.caso(), d.remunerada(), d.entidade(), d.funcoes(), d.horario(),
                d.horasSemanais(), d.inicio(), d.fim(), d.declaracaoSemConflito(), minutosSemanais(funcionarioId, d.inicio())));
        if (peloProprio) {
            String titulo = f.getNomeCompleto() + " pede autorização para acumular funções " + (a.getTipo() == AcumulacaoFuncoes.Tipo.PUBLICA
                    ? "públicas" : "privadas") + " em " + a.getEntidade();
            notificador.paraRh().tipo(TipoNotificacao.ACUMULACAO_FUNCOES).titulo(titulo).recurso(RECURSO, a.getId().getStringValor()).enviar();
            notificador.para(chefiaService.chefeDirecto(funcionarioId)).tipo(TipoNotificacao.ACUMULACAO_FUNCOES).titulo(titulo)
                    .recurso(RECURSO, a.getId().getStringValor()).enviar();
        }
        return a;
    }

    @Transactional
    public AcumulacaoFuncoes autorizar(FuncionarioId funcionarioId, AcumulacaoFuncoesId id, String despacho, LocalDate data) {
        var a = acumulacao(funcionarioId, id);
        a.autorizar(despacho, data != null ? data : hoje());
        avisar(a, "O seu pedido de acumulação de funções em " + a.getEntidade() + " foi autorizado");
        return repository.save(a);
    }

    @Transactional
    public AcumulacaoFuncoes indeferir(FuncionarioId funcionarioId, AcumulacaoFuncoesId id, String motivo, LocalDate data) {
        var a = acumulacao(funcionarioId, id);
        a.indeferir(motivo, data != null ? data : hoje());
        avisar(a, "O seu pedido de acumulação de funções em " + a.getEntidade() + " foi indeferido");
        return repository.save(a);
    }

    @Transactional
    public AcumulacaoFuncoes cessar(FuncionarioId funcionarioId, AcumulacaoFuncoesId id, LocalDate data, String motivo) {
        var a = acumulacao(funcionarioId, id);
        a.cessar(data != null ? data : hoje(), motivo);
        return repository.save(a);
    }

    /** Para o job: caducam as que terminaram; avisa-se 30 dias antes do fim (o próprio e o RH). */
    @Transactional
    public int processar(LocalDate dia) {
        int n = 0;
        for (var a : repository.findAutorizadasComFimAte(dia.minusDays(1)))
            if (a.caducarSeTerminou(dia)) {
                repository.save(a);
                n++;
            }
        LocalDate alvo = dia.plusDays(DIAS_AVISO);
        for (var a : repository.findAutorizadasComFimAte(alvo)) {
            if (!alvo.equals(a.getFim())) continue;
            String titulo = "A acumulação de funções de " + nome(a.getFuncionarioId()) + " em " + a.getEntidade() + " termina a " + Datas.pt(a.getFim());
            notificador.paraRh().tipo(TipoNotificacao.ACUMULACAO_FUNCOES).titulo(titulo).recurso(RECURSO, a.getId().getStringValor()).enviar();
            avisar(a, "A sua acumulação de funções em " + a.getEntidade() + " termina a " + Datas.pt(a.getFim()) + ": peça a renovação se precisar");
            n++;
        }
        return n;
    }

    @Transactional(readOnly = true)
    public List<AcumulacaoFuncoes> listar(AcumulacaoFuncoes.Estado estado) {
        return repository.find(estado);
    }

    @Transactional(readOnly = true)
    public List<AcumulacaoFuncoes> doFuncionario(FuncionarioId funcionarioId) {
        return repository.findByFuncionario(funcionarioId);
    }

    public String nome(FuncionarioId id) {
        return funcionarioRepository.findById(id).map(Funcionario::getNomeCompleto).orElse("um colaborador");
    }

    /** O horário semanal da função principal, se se sabe (para o terço da docência). */
    private Integer minutosSemanais(FuncionarioId funcionarioId, LocalDate data) {
        try {
            var v = horarioService.vigente(funcionarioId, data != null ? data : hoje());
            return v != null && v.horario() != null ? v.horario().minutosSemanais() : null;
        } catch (RuntimeException e) {
            return null;
        }
    }

    private void avisar(AcumulacaoFuncoes a, String titulo) {
        notificador.para(a.getFuncionarioId()).tipo(TipoNotificacao.ACUMULACAO_FUNCOES).titulo(titulo).texto(a.getMotivo())
                .recurso(RECURSO, a.getId().getStringValor()).enviar();
    }

    private AcumulacaoFuncoes acumulacao(FuncionarioId funcionarioId, AcumulacaoFuncoesId id) {
        return repository.findById(id).filter(a -> a.getFuncionarioId().equals(funcionarioId))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Acumulação de funções não encontrada."));
    }

    private Funcionario funcionario(FuncionarioId id) {
        return funcionarioRepository.findById(id).orElseThrow(() -> IgrpResponseStatusException.notFound("Colaborador não encontrado."));
    }

    LocalDate hoje() { return LocalDate.now(); }

    private static IgrpResponseStatusException invalido(String m) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, m);
    }
}
