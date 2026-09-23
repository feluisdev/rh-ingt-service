package cv.igrp.RH_Service.parametrizacoes.domain.models;

import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.LeaveTypeId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;

import java.util.Objects;

@Getter
public class LeaveType {

    private LeaveTypeId id;
    private String code;
    private String description;
    private boolean deductsBalance;
    private boolean requiresApproval;
    private Integer maxDaysPerYear;
    /**
     * Limite por ACONTECIMENTO (V53). O art. 15.o n.o 1 do DL n.o 3/2010 quase so fala assim --
     * 6 dias por ocasiao do casamento, 8 por falecimento do conjuge, 2 por cada prova. Nulo quer
     * dizer que a lei nao poe limite desta natureza, que e o caso da maioria das alineas.
     */
    private Integer maxDaysPerOccurrence;
    /** Limite por mes civil (V53): art. 15.o n.o 1 al. o) e al. q). */
    private Integer maxDaysPerMonth;
    private String category;
    private boolean active;
    /**
     * Regime legal do DL n.o 3/2010 (V49). O motivo e o nome sao da instituicao; o regime e da
     * lei, e e por ele -- nunca pelo codigo -- que se sabe quais destas linhas sao ferias.
     * Por omissao FALTA, que e o regime que nao produz efeitos automaticos.
     */
    private RegimeAusencia regime;
    /**
     * O que a ausencia faz a remuneracao (art. 16.o; V54). Nao se calcula nada com isto: e
     * informacao para o sistema que processa vencimentos. Por omissao SEM_PERDA, que e a
     * direccao segura -- afirma que nao ha perda em vez de a provocar.
     */
    private EfeitoNaRemuneracao efeitoRemuneracao;
    /**
     * Como se contam os dias (art. 76.o; V56). A regra da lei e DIAS_SEGUIDOS, mas por omissao
     * fica DIAS_UTEIS: e como sempre se contou, e mudar a contagem muda os numeros dos pedidos --
     * isso decide-se classificando a linha, nao por omissao.
     */
    private ContagemDias contagem;

    private LeaveType() {}

    private LeaveType(LeaveTypeId id, String code, String description, boolean deductsBalance,
                      boolean requiresApproval, Integer maxDaysPerYear, Integer maxDaysPerOccurrence,
                      Integer maxDaysPerMonth, String category, boolean active,
                      RegimeAusencia regime, EfeitoNaRemuneracao efeitoRemuneracao,
                      ContagemDias contagem) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.deductsBalance = deductsBalance;
        this.requiresApproval = requiresApproval;
        this.maxDaysPerYear = maxDaysPerYear;
        this.maxDaysPerOccurrence = maxDaysPerOccurrence;
        this.maxDaysPerMonth = maxDaysPerMonth;
        this.category = category;
        this.active = active;
        this.regime = regime != null ? regime : RegimeAusencia.FALTA;
        this.efeitoRemuneracao = efeitoRemuneracao != null
                ? efeitoRemuneracao : EfeitoNaRemuneracao.SEM_PERDA;
        this.contagem = contagem != null ? contagem : ContagemDias.DIAS_UTEIS;
    }

    public static LeaveType criar(String code, String description, boolean deductsBalance,
                                  boolean requiresApproval, Integer maxDaysPerYear,
                                  Integer maxDaysPerOccurrence, Integer maxDaysPerMonth,
                                  String category, RegimeAusencia regime,
                                  EfeitoNaRemuneracao efeitoRemuneracao, ContagemDias contagem) {
        Objects.requireNonNull(code, "code não pode ser nulo");
        validarLimite("maxDaysPerYear", maxDaysPerYear);
        validarLimite("maxDaysPerOccurrence", maxDaysPerOccurrence);
        validarLimite("maxDaysPerMonth", maxDaysPerMonth);
        return new LeaveType(LeaveTypeId.gerarNovo(), code, description, deductsBalance,
                requiresApproval, maxDaysPerYear, maxDaysPerOccurrence, maxDaysPerMonth,
                category, true, regime, efeitoRemuneracao, contagem);
    }

    /**
     * Um limite de zero dias não é um limite: é um tipo que ninguém pode pedir, e isso diz-se
     * desactivando a linha. Nulo continua a valer — é o «sem limite desta natureza».
     */
    private static void validarLimite(String campo, Integer valor) {
        if (valor != null && valor <= 0)
            throw IgrpResponseStatusException.badRequest(
                    campo + " tem de ser maior do que zero; para impedir os pedidos, desactive o tipo.");
    }

    public static LeaveType reconstruir(LeaveTypeId id, String code, String description, boolean deductsBalance,
                                        boolean requiresApproval, Integer maxDaysPerYear,
                                        Integer maxDaysPerOccurrence, Integer maxDaysPerMonth,
                                        String category, boolean active, RegimeAusencia regime,
                                        EfeitoNaRemuneracao efeitoRemuneracao, ContagemDias contagem) {
        return new LeaveType(id, code, description, deductsBalance, requiresApproval,
                maxDaysPerYear, maxDaysPerOccurrence, maxDaysPerMonth, category, active, regime,
                efeitoRemuneracao, contagem);
    }

    public void atualizar(String description, boolean deductsBalance, boolean requiresApproval,
                          Integer maxDaysPerYear, Integer maxDaysPerOccurrence,
                          Integer maxDaysPerMonth, String category, RegimeAusencia regime,
                          EfeitoNaRemuneracao efeitoRemuneracao, ContagemDias contagem) {
        validarLimite("maxDaysPerYear", maxDaysPerYear);
        validarLimite("maxDaysPerOccurrence", maxDaysPerOccurrence);
        validarLimite("maxDaysPerMonth", maxDaysPerMonth);
        this.description = description;
        this.deductsBalance = deductsBalance;
        this.requiresApproval = requiresApproval;
        this.maxDaysPerYear = maxDaysPerYear;
        this.maxDaysPerOccurrence = maxDaysPerOccurrence;
        this.maxDaysPerMonth = maxDaysPerMonth;
        this.category = category;
        // Nao se apaga uma classificacao por o pedido vir sem ela: a omissao mantem a que esta.
        if (regime != null) this.regime = regime;
        if (efeitoRemuneracao != null) this.efeitoRemuneracao = efeitoRemuneracao;
        if (contagem != null) this.contagem = contagem;
    }

    public void desativar() {
        if (!this.active) throw IgrpResponseStatusException.conflict("Tipo de licença já está inactivo.");
        this.active = false;
    }

    public void reativar() {
        if (this.active) throw IgrpResponseStatusException.conflict("Tipo de licença já está activo.");
        this.active = true;
    }
}
