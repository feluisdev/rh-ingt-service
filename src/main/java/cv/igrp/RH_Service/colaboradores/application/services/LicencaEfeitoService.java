package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.models.LicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.repository.LicencaMobilidadeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Aplica os efeitos de uma licença/mobilidade <b>na data em que são devidos</b>, e regista que
 * foram aplicados.
 *
 * <p><b>Porque é que isto não vive no handler da aprovação.</b> Aprovar é deferir um pedido
 * (art. 44.º n.º 2 do DL n.º 3/2010) e pode acontecer meses antes de a licença começar. Até à
 * V48 os efeitos eram aplicados na aprovação: deferir em Setembro uma licença de Outubro abria a
 * vaga e mudava o estado do trabalhador logo em Setembro. O efeito pertence ao <b>período</b>
 * (art. 44.º n.º 1), não ao despacho.
 *
 * <p><b>Dois chamadores, um só caminho.</b> O handler da aprovação chama isto para o caso comum
 * de a licença começar hoje — ninguém fica à espera da meia-noite. Para tudo o resto chama o job
 * diário. Como a decisão de aplicar ou não está aqui dentro e assenta nas marcas do agregado,
 * os dois caminhos não se pisam: o que já foi aplicado não volta a ser.
 *
 * <p><b>Idempotência.</b> É o que torna o job seguro de repetir e um dia falhado inofensivo.
 * Sem ela, uma segunda passagem encerraria outra vez uma afectação e escreveria outro registo no
 * histórico de estados.
 */
@Service
@RequiredArgsConstructor
public class LicencaEfeitoService {

    private final LicencaMobilidadeRepository licencaRepository;
    private final MobilidadeService mobilidadeService;
    private final LicencaService licencaService;
    private final SubstituicaoService substituicaoService;

    /** O que a passagem produziu, para quem precise de o dizer na resposta. */
    public record Efeito(boolean aplicado, UUID afectacaoEncerradaId, UUID estadoAtribuidoId) {
        public static Efeito nenhum() { return new Efeito(false, null, null); }
    }

    /**
     * Entrada em vigor, se for devida nesta data: a licença está deferida, o início já chegou e
     * os efeitos ainda não foram aplicados. Fora disso não faz nada e diz que não fez.
     *
     * <p>Se o subtipo já não existir no catálogo, não se aplica nada e <b>não se marca</b>: a
     * configuração pode voltar, e marcar aqui perderia o efeito para sempre em silêncio.
     */
    @Transactional
    public Efeito aplicarEntradaSeDevida(LicencaMobilidade licenca, LocalDate hoje) {
        if (!licenca.carecedeEfeitoEntrada(hoje)) return Efeito.nenhum();

        Optional<LicencaService.EfeitoAplicado> aplicado = mobilidadeService.subtipoSeExistir(licenca)
                .map(subtipo -> licencaService.aplicarEntradaEmVigor(licenca, subtipo));
        if (aplicado.isEmpty()) return Efeito.nenhum();

        licenca.marcarEfeitoEntradaAplicado(LocalDateTime.now());
        licencaRepository.save(licenca);

        return new Efeito(true, aplicado.get().afectacaoEncerradaId(), aplicado.get().estadoAtribuidoId());
    }

    /**
     * Regresso, se for devido nesta data: o período já terminou e o regresso ainda não foi
     * aplicado. É o «caduca automaticamente» do art. 46.º n.º 3 — ninguém tem de carregar em nada
     * para a licença acabar no dia em que acaba.
     *
     * <p>Acabado o impedimento, quem estava a substituir o titular sai (art. 77.º n.º 2). A data
     * que conta é a do <b>fim da licença</b>, não a do dia em que o job passou: se a aplicação
     * esteve em baixo três dias, a substituição encerra na data certa à mesma.
     */
    @Transactional
    public Efeito aplicarRegressoSeDevido(LicencaMobilidade licenca, LocalDate hoje) {
        if (!licenca.carecedeEfeitoRegresso(hoje)) return Efeito.nenhum();
        return aplicarRegresso(licenca);
    }

    /**
     * Os efeitos do regresso, sem perguntar se a data já chegou — para quem já sabe que chegou.
     * É o caso do regresso antecipado: o funcionário está à frente de quem o regista, e esperar
     * pelo job da noite deixaria o substituto no Lugar mais um dia.
     *
     * <p>Continua a marcar, portanto o job não repete o que aqui foi feito. A data que conta é a
     * do <b>fim da licença</b> e não a de hoje: é dela que dependem as contagens de dias.
     */
    @Transactional
    public Efeito aplicarRegresso(LicencaMobilidade licenca) {
        if (licenca.getEfeitoRegressoAplicadoEm() != null) return Efeito.nenhum();

        LocalDate dataRegresso = licenca.getDataFim();

        UUID estadoAtribuidoId = mobilidadeService.subtipoSeExistir(licenca)
                .flatMap(subtipo -> licencaService.aplicarRegresso(licenca, subtipo, dataRegresso))
                .orElse(null);

        substituicaoService.encerrarPorRegressoDoTitular(licenca.getFuncionarioId(), dataRegresso);

        licenca.marcarEfeitoRegressoAplicado(LocalDateTime.now());
        licencaRepository.save(licenca);

        return new Efeito(true, null, estadoAtribuidoId);
    }
}
