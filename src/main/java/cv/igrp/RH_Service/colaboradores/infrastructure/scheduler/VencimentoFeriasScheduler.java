package cv.igrp.RH_Service.colaboradores.infrastructure.scheduler;

import cv.igrp.RH_Service.colaboradores.application.services.FeriasService;
import cv.igrp.RH_Service.colaboradores.domain.filter.FuncionarioFilter;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Faz <b>vencer o direito a férias</b> — DL n.º 3/2010, art. 2.º n.º 4: «O direito a férias vence
 * no dia 1 de Janeiro de cada ano».
 *
 * <p>Corre todos os dias, e não uma vez por ano, por duas razões: no <b>ano de ingresso</b> o
 * direito cresce a cada trimestre completo de serviço (art. 3.º), e uma execução falhada a 1 de
 * Janeiro não pode deixar uma instituição inteira sem férias até ao ano seguinte. A passagem é
 * idempotente — só escreve quando há algo a mudar —, por isso repetir não custa nada e um dia sem
 * execução apanha-se no dia seguinte.
 *
 * <p><b>Só colaboradores activos.</b> Quem cessou funções não vence férias novas; o que lhe era
 * devido à data da cessação é matéria do art. 12.º, que depende de remuneração e está fora do
 * âmbito desta aplicação.
 *
 * <p><b>Um erro num colaborador não derruba os outros</b>, no molde dos restantes jobs: a falha
 * fica no log com a identificação de quem falhou, e o lote segue.
 *
 * <p><b>Limitação conhecida, partilhada com os restantes jobs:</b> não há lock distribuído. Com
 * mais do que uma instância isto corre em todas; a idempotência limita o estrago, mas não é um
 * lock. É o que o framework de jobs do {@code inss_core_service} resolveria.
 */
@Component
public class VencimentoFeriasScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(VencimentoFeriasScheduler.class);
    private static final int TAMANHO_PAGINA = 100;

    private final FuncionarioRepository funcionarioRepository;
    private final FeriasService feriasService;

    public VencimentoFeriasScheduler(FuncionarioRepository funcionarioRepository,
                                     FeriasService feriasService) {
        this.funcionarioRepository = funcionarioRepository;
        this.feriasService = feriasService;
    }

    /**
     * Corre às 00:05 por omissão — antes do job dos efeitos das licenças, para que a 1 de Janeiro
     * o saldo já exista quando o resto do dia começar. Configurável por
     * {@code rh.ferias.vencimento.cron} (variável {@code RH_FERIAS_VENCIMENTO_CRON}).
     */
    @Scheduled(cron = "${rh.ferias.vencimento.cron:0 5 0 * * ?}")
    public void vencerDireitoAFerias() {
        int ano = LocalDate.now().getYear();
        LOGGER.info("Férias: a vencer o direito do ano {}...", ano);

        int pagina = 0;
        int tratados = 0;
        List<Funcionario> lote;
        do {
            lote = paginaDeActivos(pagina);
            for (Funcionario funcionario : lote) {
                try {
                    feriasService.garantirSaldoDoAno(funcionario.getId(), ano);
                    tratados++;
                } catch (Exception e) {
                    LOGGER.error("Falhou o vencimento de férias do colaborador {}",
                            funcionario.getId().getStringValor(), e);
                }
            }
            pagina++;
        } while (lote.size() == TAMANHO_PAGINA);

        LOGGER.info("Férias: {} colaboradores tratados para o ano {}.", tratados, ano);
    }

    private List<Funcionario> paginaDeActivos(int pagina) {
        FuncionarioFilter filtro = new FuncionarioFilter();
        filtro.setIsActive(true);
        filtro.setPage(pagina);
        filtro.setSize(TAMANHO_PAGINA);
        return funcionarioRepository.findAll(filtro);
    }
}
