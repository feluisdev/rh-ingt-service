package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.models.DiaAssiduidade;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.MarcacaoAssiduidade;
import cv.igrp.RH_Service.colaboradores.domain.models.OrigemMarcacao;
import cv.igrp.RH_Service.colaboradores.domain.models.SentidoMarcacao;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.MarcacaoAssiduidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.PedidoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.MarcacaoAssiduidadeId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <b>Registo diário de assiduidade</b> — Lei n.º 20/X/2023, art. 164.º n.º 3: o serviço mantém um
 * registo que permita apurar as horas por dia e por semana, com início, termo e intervalos.
 *
 * <p>Guardam-se as <b>marcações</b> (a prova), e o dia calcula-se a partir delas. Várias formas de
 * registar convivem, e cada marcação diz a sua origem: o relógio (importação), o RH (excepções e
 * correcções) e, no passo seguinte, o próprio. Débito, faltas (DL n.º 3/2010, art. 13.º) e o fecho do
 * mês (art. 75.º) são os passos seguintes: aqui só se regista e se lê.
 */
@Service
@RequiredArgsConstructor
public class AssiduidadeService {

    /** Uma leitura cobre no máximo dois meses: um mês com folga para as semanas das pontas. */
    static final int DIAS_MAXIMOS_CONSULTA = 62;
    /** Um lote de importação razoável: um mês de picagens de uma instituição média. */
    static final int MAXIMO_IMPORTACAO = 5000;

    public record Resultado(MarcacaoAssiduidade marcacao, List<String> alertas) {}

    public record Picagem(String funcionarioId, String numeroFuncionario, LocalDateTime momento,
                          SentidoMarcacao sentido, String referenciaExterna) {}

    public record Rejeicao(String referenciaExterna, String motivo) {}

    public record Relatorio(int importadas, int duplicadas, List<Rejeicao> rejeitadas) {}

    public record DiaConsulta(DiaAssiduidade dia, int minutosEsperados, String horarioNome, boolean feriado,
                              List<MarcacaoAssiduidade> marcacoes) {}

    public record Semana(int ano, int semana, LocalDate inicio, int minutosTrabalhados, int minutosEsperados) {}

    public record Consulta(List<DiaConsulta> dias, List<Semana> semanas) {}

    private final MarcacaoAssiduidadeRepository marcacaoRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final PedidoAusenciaRepository pedidoAusenciaRepository;
    private final CalendarioFeriadosService calendarioFeriadosService;
    private final HorarioColaboradorService horarioColaboradorService;

    /**
     * Lançamento pelo RH. Num dia que já tem marcações é uma correcção, e exige motivo. Num dia de
     * ausência aprovada, feriado ou fim-de-semana, aceita-se com alerta: a classificação é de outro
     * passo (faltas, trabalho suplementar).
     */
    @Transactional
    public Resultado lancar(FuncionarioId funcionarioId, LocalDateTime momento, SentidoMarcacao sentido, String motivo) {
        funcionario(funcionarioId);
        var marcacao = MarcacaoAssiduidade.registar(funcionarioId, momento, sentido, OrigemMarcacao.MANUAL,
                motivo, null, agora());
        LocalDate data = marcacao.getMomento().toLocalDate();
        if (marcacao.getMotivo() == null && marcacaoRepository.existeValidaNoDia(funcionarioId, data))
            throw invalido("O dia " + data + " já tem marcações: lançar outra é uma correcção, e exige motivo.");

        var gravada = marcacaoRepository.save(marcacao);
        return new Resultado(gravada, alertas(funcionarioId, data));
    }

    /** A marcação fica, anulada, com o motivo. É prova do que foi picado. */
    @Transactional
    public MarcacaoAssiduidade anular(FuncionarioId funcionarioId, MarcacaoAssiduidadeId id, String motivo) {
        var marcacao = marcacaoRepository.findById(id)
                .filter(m -> m.getFuncionarioId().equals(funcionarioId))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Marcação não encontrada: " + id.getStringValor()));
        marcacao.anular(motivo, agora());
        return marcacaoRepository.save(marcacao);
    }

    /**
     * Picagens de um relógio. Cada uma traz a referência que tem no relógio: uma importação repetida
     * não duplica (conta como duplicada). Uma picagem que não se possa registar não trava as outras:
     * vai para o relatório, com o motivo.
     */
    @Transactional
    public Relatorio importar(List<Picagem> picagens) {
        if (picagens == null || picagens.isEmpty()) throw invalido("A importação não traz picagens.");
        if (picagens.size() > MAXIMO_IMPORTACAO)
            throw invalido("Uma importação tem no máximo " + MAXIMO_IMPORTACAO + " picagens; dividir o lote.");

        int importadas = 0, duplicadas = 0;
        List<Rejeicao> rejeitadas = new ArrayList<>();
        Set<String> vistasNoLote = new HashSet<>();
        LocalDateTime agora = agora();
        for (Picagem p : picagens) {
            String ref = p.referenciaExterna() == null ? null : p.referenciaExterna().trim();
            if (ref == null || ref.isEmpty()) {
                rejeitadas.add(new Rejeicao(null, "Sem referenciaExterna: sem ela a importação não é repetível."));
                continue;
            }
            if (!vistasNoLote.add(ref) || marcacaoRepository.existsByReferenciaExterna(ref)) {
                duplicadas++;
                continue;
            }
            try {
                Funcionario funcionario = doRelogio(p);
                marcacaoRepository.save(MarcacaoAssiduidade.registar(funcionario.getId(), p.momento(), p.sentido(),
                        OrigemMarcacao.IMPORTADO, null, ref, agora));
                importadas++;
            } catch (IgrpResponseStatusException e) {
                rejeitadas.add(new Rejeicao(ref, e.getBody().getTitle()));
            }
        }
        return new Relatorio(importadas, duplicadas, rejeitadas);
    }

    /** O que o art. 164.º n.º 3 pede: por dia, início, termo, intervalos e horas; por semana, os totais. */
    @Transactional(readOnly = true)
    public Consulta consultar(FuncionarioId funcionarioId, LocalDate de, LocalDate ate) {
        funcionario(funcionarioId);
        if (de == null || ate == null) throw invalido("O período é obrigatório: de e ate.");
        if (de.isAfter(ate)) throw invalido("O início do período (" + de + ") é depois do fim (" + ate + ").");
        if (ChronoUnit.DAYS.between(de, ate) + 1 > DIAS_MAXIMOS_CONSULTA)
            throw invalido("Uma consulta cobre no máximo " + DIAS_MAXIMOS_CONSULTA + " dias.");

        List<MarcacaoAssiduidade> todas = marcacaoRepository.findByFuncionarioEntre(funcionarioId, de, ate);
        Set<LocalDate> feriados = calendarioFeriadosService.feriadosDoColaborador(funcionarioId, de, ate);

        List<DiaConsulta> dias = new ArrayList<>();
        Map<String, int[]> porSemana = new LinkedHashMap<>();
        Map<String, LocalDate> inicioSemana = new LinkedHashMap<>();
        WeekFields iso = WeekFields.ISO;
        for (LocalDate d = de; !d.isAfter(ate); d = d.plusDays(1)) {
            final LocalDate data = d;
            List<MarcacaoAssiduidade> doDia = todas.stream().filter(m -> m.getMomento().toLocalDate().equals(data)).toList();
            DiaAssiduidade dia = DiaAssiduidade.calcular(data, doDia);
            boolean feriado = feriados.contains(data);
            var vigente = horarioColaboradorService.vigente(funcionarioId, data);
            int esperados = feriado || vigente.horario() == null ? 0 : vigente.horario().minutosNoDia(data.getDayOfWeek());
            dias.add(new DiaConsulta(dia, esperados, vigente.horario() != null ? vigente.horario().getNome() : null,
                    feriado, doDia));

            String chave = data.get(iso.weekBasedYear()) + "-" + data.get(iso.weekOfWeekBasedYear());
            porSemana.computeIfAbsent(chave, k -> new int[2]);
            porSemana.get(chave)[0] += dia.minutosTrabalhados();
            porSemana.get(chave)[1] += esperados;
            inicioSemana.putIfAbsent(chave, data.with(DayOfWeek.MONDAY));
        }
        List<Semana> semanas = porSemana.entrySet().stream().map(e -> {
            String[] partes = e.getKey().split("-");
            return new Semana(Integer.parseInt(partes[0]), Integer.parseInt(partes[1]), inicioSemana.get(e.getKey()),
                    e.getValue()[0], e.getValue()[1]);
        }).toList();
        return new Consulta(dias, semanas);
    }

    private List<String> alertas(FuncionarioId funcionarioId, LocalDate data) {
        List<String> alertas = new ArrayList<>();
        if (!pedidoAusenciaRepository.findAprovadosEntre(funcionarioId, data, data).isEmpty())
            alertas.add("Há uma ausência aprovada em " + data + ".");
        if (calendarioFeriadosService.feriadosDoColaborador(funcionarioId, data, data).contains(data))
            alertas.add(data + " é feriado para este colaborador.");
        if (data.getDayOfWeek() == DayOfWeek.SATURDAY || data.getDayOfWeek() == DayOfWeek.SUNDAY)
            alertas.add(data + " é fim-de-semana.");
        return alertas;
    }

    /** O relógio conhece o número de funcionário; o id interno também serve. */
    private Funcionario doRelogio(Picagem p) {
        if (p.funcionarioId() != null && !p.funcionarioId().isBlank()) {
            FuncionarioId id;
            try {
                id = FuncionarioId.from(p.funcionarioId().trim());
            } catch (IllegalArgumentException e) {
                throw invalido("funcionarioId inválido: " + p.funcionarioId() + ".");
            }
            return funcionarioRepository.findById(id)
                    .orElseThrow(() -> invalido("Funcionário não encontrado: " + p.funcionarioId() + "."));
        }
        if (p.numeroFuncionario() != null && !p.numeroFuncionario().isBlank())
            return funcionarioRepository.findByNumeroFuncionario(p.numeroFuncionario().trim())
                    .orElseThrow(() -> invalido("Número de funcionário desconhecido: " + p.numeroFuncionario() + "."));
        throw invalido("A picagem não diz de quem é: numeroFuncionario ou funcionarioId.");
    }

    private void funcionario(FuncionarioId funcionarioId) {
        funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Funcionário não encontrado: " + funcionarioId.getStringValor()));
    }

    LocalDateTime agora() { return LocalDateTime.now(); }

    private static IgrpResponseStatusException invalido(String mensagem) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, mensagem);
    }
}
