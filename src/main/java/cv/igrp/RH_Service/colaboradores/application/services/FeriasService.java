package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.SaldoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.SaldoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.TipoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * <b>Vencimento do direito a férias</b> — DL n.º 3/2010, cap. II.
 *
 * <p>Até aqui o saldo de férias era criado <b>à mão</b>, por {@code POST /saldos-ausencia}. Quem
 * instalasse a aplicação julgava ter gestão de férias e tinha metade: o ciclo do saldo funcionava
 * (reservar, confirmar, libertar, devolver), mas o número de onde tudo parte era escrito por
 * alguém. O art. 2.º n.º 4 diz o contrário: <b>o direito vence-se a 1 de Janeiro</b>, sozinho.
 *
 * <p><b>Quantos dias.</b> O art. 2.º n.º 3 fixa 22 dias úteis por ano civil. O número não está
 * escrito aqui: vem do catálogo ({@code max_days_per_year} do tipo classificado como férias), com
 * o valor legal como recurso quando a instituição não o tiver preenchido. É a regra da casa —
 * parametrizar valores, não decisões: a instituição pode ter um valor diferente por diploma
 * próprio, mas não pode decidir que as férias não se vencem.
 *
 * <p><b>O ano de ingresso é proporcional</b> (art. 3.º): «a partir dos 90 dias de prestação
 * efectiva de serviço, o funcionário pode gozar antecipadamente 6 ou 5 dias úteis de férias, por
 * cada 3 meses completos de serviço até 31 de Dezembro desse ano». Os «6 ou 5» não são uma
 * escolha: são o que dá 22 repartido por quatro trimestres — 5,5 de cada vez. Arredondando cada
 * acumulado à unidade mais próxima saem 6, 11, 17 e 22 dias, que é a alternância que a lei
 * descreve. Fica assim derivado do direito anual em vez de escrito à mão, e continua certo se a
 * instituição tiver um direito anual diferente de 22.
 *
 * <p><b>O que este serviço não faz.</b> Não acumula férias de um ano para o outro (art. 7.º n.º 1,
 * quando por motivo de serviço não puderam ser gozadas) nem trata da compensação na cessação
 * (art. 12.º), que depende de remuneração — e remuneração não existe nesta aplicação. São os
 * pontos seguintes do plano.
 */
@Service
@RequiredArgsConstructor
public class FeriasService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeriasService.class);

    /** Art. 2.º n.º 3. Só vale quando o catálogo nada disser. */
    static final int DIAS_UTEIS_POR_LEI = 22;

    /** Art. 3.º: «a partir dos 90 dias de prestação efectiva de serviço». */
    static final int DIAS_MINIMOS_NO_ANO_DE_INGRESSO = 90;

    private static final int TRIMESTRES_NO_ANO = 4;

    private final FuncionarioRepository funcionarioRepository;
    private final TipoAusenciaRepository tipoAusenciaRepository;
    private final SaldoAusenciaRepository saldoAusenciaRepository;

    /**
     * Dias de férias a que o funcionário tem direito nesse ano civil.
     *
     * <ul>
     *   <li><b>Antes do ano de admissão</b> — nenhum: não havia relação de emprego (art. 2.º n.º 1).</li>
     *   <li><b>No ano de admissão</b> — proporcional, e <b>zero</b> enquanto não perfizer 90 dias
     *       de serviço (art. 3.º).</li>
     *   <li><b>Nos anos seguintes</b> — o direito inteiro, vencido a 1 de Janeiro (art. 2.º n.º 4).</li>
     * </ul>
     *
     * <p>No ano de admissão a contagem vai até 31 de Dezembro, como manda o art. 3.º: o direito do
     * ano de ingresso não depende do dia em que a pergunta é feita. Quem quiser saber quanto já
     * pode gozar <i>hoje</i> tem essa resposta no saldo, que o job actualiza trimestre a trimestre.
     */
    public int direitoDoAno(Funcionario funcionario, TipoAusencia ferias, int ano) {
        int direitoAnual = direitoAnual(ferias);

        LocalDate admissao = funcionario.getDataAdmissao();
        if (admissao == null) return 0;
        if (ano < admissao.getYear()) return 0;
        if (ano > admissao.getYear()) return direitoAnual;

        return direitoNoAnoDeIngresso(admissao, direitoAnual);
    }

    /**
     * Art. 2.º n.º 3: os dias úteis de férias de um ano completo — do catálogo, com a lei por
     * recurso. É também o máximo que se goza seguidamente (art. 5.º n.º 1).
     */
    public int direitoAnual(TipoAusencia ferias) {
        return ferias.getMaxDaysPerYear() != null && ferias.getMaxDaysPerYear() > 0
                ? ferias.getMaxDaysPerYear()
                : DIAS_UTEIS_POR_LEI;
    }

    /**
     * Art. 3.º. Conta os <b>trimestres completos</b> de serviço até 31 de Dezembro do ano de
     * ingresso e reparte por eles o direito anual, arredondando o acumulado — daí saírem os «6 ou
     * 5 dias úteis» de que a lei fala. Abaixo dos 90 dias de serviço não há direito antecipado
     * nenhum.
     */
    private int direitoNoAnoDeIngresso(LocalDate admissao, int direitoAnual) {
        LocalDate fimDoAno = LocalDate.of(admissao.getYear(), 12, 31);

        long diasDeServico = ChronoUnit.DAYS.between(admissao, fimDoAno) + 1;
        if (diasDeServico < DIAS_MINIMOS_NO_ANO_DE_INGRESSO) return 0;

        long trimestres = ChronoUnit.MONTHS.between(admissao, fimDoAno.plusDays(1)) / 3;
        if (trimestres <= 0) return 0;
        if (trimestres >= TRIMESTRES_NO_ANO) return direitoAnual;

        // Arredondamento à unidade mais próxima, meio para cima: com 22 dias dá 6, 11 e 17.
        return (int) ((direitoAnual * trimestres * 2 + TRIMESTRES_NO_ANO) / (TRIMESTRES_NO_ANO * 2));
    }

    /**
     * <b>Acumulação para o ano seguinte</b> — art. 7.º n.º 1: «As férias devem ser gozadas no
     * decurso do ano civil em que se vencem, salvo se, por motivo de serviço, não puderem ser
     * gozadas nesse ano, caso em que pode haver acumulação de férias para o ano seguinte.»
     *
     * <p><b>Não é automático, de propósito.</b> A lei condiciona-a a «motivo de serviço», e por
     * isso isto é um acto justificado e não um job a arrastar dias em silêncio. Não se valida o
     * conteúdo do motivo — os motivos são da instituição —, mas exige-se que exista.
     *
     * <p><b>É para o ano imediatamente seguinte, e uma só vez.</b> O que se cede fica registado
     * na linha de origem, e os dias recebidos não voltam a ser cedíveis: o horizonte da lei é de
     * um ano (art. 8.º n.º 4 e art. 9.º mandam gozar o remanescente «até ao termo do ano civil
     * imediato»), não uma corrente que arrasta dias indefinidamente.
     *
     * <p>O saldo do ano de destino é criado se ainda não existir — a acumulação pode ser
     * autorizada em Dezembro, antes de o job do vencimento passar.
     *
     * @return o saldo do ano de destino, já com os dias recebidos
     */
    @Transactional
    public SaldoAusencia acumularParaOAnoSeguinte(SaldoAusencia origem, int dias, String motivo) {
        if (motivo == null || motivo.isBlank())
            throw IgrpResponseStatusException.badRequest(
                    "A acumulação de férias exige um motivo: a lei só a permite quando, por motivo "
                            + "de serviço, as férias não puderam ser gozadas no ano (art. 7.º n.º 1).");

        TipoAusencia tipo = tipoAusenciaRepository.findById(origem.getTipoAusenciaId())
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Tipo de ausência não encontrado: " + origem.getTipoAusenciaId().getStringValor()));

        if (!tipo.isFerias())
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "A acumulação para o ano seguinte é das férias (DL n.º 3/2010, cap. II). "
                            + "O tipo '" + tipo.getNome() + "' está classificado como " + tipo.getRegime() + ".");

        int anoDestino = origem.getAno() + 1;

        SaldoAusencia destino = saldoAusenciaRepository
                .findByFuncionarioIdAndTipoAusenciaIdAndAno(
                        origem.getFuncionarioId(), origem.getTipoAusenciaId(), anoDestino)
                .orElseGet(() -> garantirSaldoDoAno(origem.getFuncionarioId(), anoDestino)
                        .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                                "Não foi possível preparar o saldo de " + anoDestino + ".")));

        // A ordem importa: a origem valida quantos dias tem para ceder antes de o destino os receber.
        origem.transportarParaOAnoSeguinte(dias);
        destino.receberAcumulados(dias, motivo);

        saldoAusenciaRepository.save(origem);
        return saldoAusenciaRepository.save(destino);
    }

    /**
     * Garante que o saldo de férias do ano existe e diz o número certo. Idempotente: chamada
     * todos os dias pelo job, só escreve quando há algo a mudar.
     *
     * <p><b>O direito nunca encolhe abaixo do que já foi usado.</b> Se alguém tiver gozado ou
     * reservado mais dias do que o direito recalculado — por correcção da data de admissão, ou
     * por o catálogo ter mudado —, o saldo fica no que está usado em vez de passar a negativo.
     * Tirar a alguém dias já gozados não é coisa que um job deva fazer sozinho, e o art. 2.º n.º 5
     * diz que o direito é irrenunciável.
     *
     * @return o saldo, ou vazio se a instituição não tiver um tipo de férias no catálogo
     */
    @Transactional
    public Optional<SaldoAusencia> garantirSaldoDoAno(FuncionarioId funcionarioId, int ano) {
        Optional<TipoAusencia> ferias = tipoAusenciaRepository.findFerias();
        if (ferias.isEmpty()) return Optional.empty();

        Optional<Funcionario> funcionario = funcionarioRepository.findById(funcionarioId);
        if (funcionario.isEmpty()) return Optional.empty();

        int direito = direitoDoAno(funcionario.get(), ferias.get(), ano);

        Optional<SaldoAusencia> existente = saldoAusenciaRepository
                .findByFuncionarioIdAndTipoAusenciaIdAndAno(funcionarioId, ferias.get().getId(), ano);

        if (existente.isEmpty()) {
            SaldoAusencia novo = SaldoAusencia.criar(funcionarioId, ferias.get().getId(), ano, direito);
            LOGGER.debug("Saldo de férias criado: funcionário {}, ano {}, {} dias",
                    funcionarioId.getStringValor(), ano, direito);
            return Optional.of(saldoAusenciaRepository.save(novo));
        }

        SaldoAusencia saldo = existente.get();
        int usados = saldo.getDiasGozados() + saldo.getDiasPendentes();
        int novoDireito = Math.max(direito, usados);
        int direitoAnterior = saldo.getDiasDireito();

        if (novoDireito == direitoAnterior) return existente;

        saldo.atualizarDiasDireito(novoDireito);
        LOGGER.debug("Direito a férias actualizado: funcionário {}, ano {}, {} → {} dias",
                funcionarioId.getStringValor(), ano, direitoAnterior, novoDireito);
        return Optional.of(saldoAusenciaRepository.save(saldo));
    }
}
