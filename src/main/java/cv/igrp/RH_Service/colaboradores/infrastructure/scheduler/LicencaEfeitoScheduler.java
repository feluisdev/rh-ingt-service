package cv.igrp.RH_Service.colaboradores.infrastructure.scheduler;

import cv.igrp.RH_Service.colaboradores.application.services.LicencaEfeitoService;
import cv.igrp.RH_Service.colaboradores.domain.models.LicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.repository.LicencaMobilidadeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Job diário que dá aos efeitos das licenças a sua <b>data efectiva</b>.
 *
 * <p>Uma licença deferida hoje para começar em Outubro só produz efeitos em Outubro; e uma
 * licença cujo fim passou termina nesse dia, sem esperar por ninguém — é o «caduca
 * automaticamente» do art. 46.º n.º 3 do DL n.º 3/2010. Antes da V48 nada disto acontecia
 * sozinho: os efeitos eram aplicados na aprovação, e o fim só chegava quando alguém carregasse
 * no {@code /close}. Uma licença esquecida ficava em vigor para sempre.
 *
 * <p><b>Repetir é seguro, e faltar um dia não perde nada.</b> A pergunta é feita ao estado actual
 * — o que está deferido, já começou e ainda não teve efeitos — e não a um intervalo desde a
 * última execução. Se a aplicação estiver em baixo três dias, a passagem seguinte apanha o
 * atraso; e o regresso é aplicado com a <b>data de fim da licença</b>, não com a data em que o
 * job correu, para que a contagem de dias não dependa de quando a máquina esteve de pé. Essa
 * contagem é a base do desconto na antiguidade e das férias proporcionais (art. 47.º n.os 1 a 3).
 *
 * <p><b>Um erro num registo não derruba os outros.</b> Cada licença é tratada por si, no molde
 * dos jobs do {@code sigdi}: a falha fica no log com a identificação do registo e o lote segue.
 *
 * <p><b>Limitação conhecida, partilhada com os cinco jobs do {@code sigdi}:</b> não há lock
 * distribuído. Com mais do que uma instância, isto corre em todas. As marcas de aplicação no
 * agregado limitam o estrago — quem chegar em segundo já não encontra nada por aplicar — mas não
 * são um lock: duas instâncias podem ler a mesma linha antes de qualquer uma gravar. O passo
 * seguinte, quando o serviço correr com réplicas, é o framework de jobs do {@code inss_core_service}.
 */
@Component
public class LicencaEfeitoScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(LicencaEfeitoScheduler.class);

    private final LicencaMobilidadeRepository licencaRepository;
    private final LicencaEfeitoService efeitoService;

    public LicencaEfeitoScheduler(LicencaMobilidadeRepository licencaRepository,
                                  LicencaEfeitoService efeitoService) {
        this.licencaRepository = licencaRepository;
        this.efeitoService = efeitoService;
    }

    /**
     * Corre às 00:15 por omissão, antes do movimento do dia. Configurável por
     * {@code rh.licencas.efeitos.cron} (variável {@code RH_LICENCAS_EFEITOS_CRON}), no mesmo
     * formato dos jobs do {@code sigdi}.
     */
    @Scheduled(cron = "${rh.licencas.efeitos.cron:0 15 0 * * ?}")
    public void aplicarEfeitosDevidos() {
        LocalDate hoje = LocalDate.now();
        LOGGER.info("Licenças/mobilidades: a aplicar os efeitos devidos a {}...", hoje);

        int entradas = aplicar(licencaRepository.findEntradaPorAplicar(hoje), hoje, true);
        int regressos = aplicar(licencaRepository.findRegressoPorAplicar(hoje), hoje, false);

        LOGGER.info("Licenças/mobilidades: {} entradas e {} regressos aplicados.", entradas, regressos);
    }

    private int aplicar(List<LicencaMobilidade> licencas, LocalDate hoje, boolean entrada) {
        int aplicados = 0;
        for (LicencaMobilidade licenca : licencas) {
            try {
                var efeito = entrada
                        ? efeitoService.aplicarEntradaSeDevida(licenca, hoje)
                        : efeitoService.aplicarRegressoSeDevido(licenca, hoje);
                if (efeito.aplicado()) aplicados++;
            } catch (Exception e) {
                LOGGER.error("Falhou {} da licença/mobilidade {}",
                        entrada ? "a entrada em vigor" : "o regresso",
                        licenca.getId().getStringValor(), e);
            }
        }
        return aplicados;
    }
}
