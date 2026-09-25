package cv.igrp.RH_Service.shared.application.services.scheduler;

import cv.igrp.RH_Service.shared.application.constants.FrequenciaScheduler;
import cv.igrp.RH_Service.shared.application.dto.AtualizarSchedulerRequestDTO;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** As partes puras do {@link SchedulerService}: cron, validação e o instante de cada disparo. */
class SchedulerServiceTest {

    private static ScheduledJob job(List<JobParametro> parametros) {
        return new ScheduledJob() {
            @Override public String getChave()       { return "JOB_X"; }
            @Override public String getNomeLegivel() { return "Vencimento do direito a férias"; }
            @Override public String getCronPadrao()  { return "0 5 0 * * *"; }
            @Override public List<JobParametro> getParametros() { return parametros; }
            @Override public JobResult executar(JobContext ctx) { return JobResult.vazio(); }
        };
    }

    private static AtualizarSchedulerRequestDTO dto(String freq, Integer dia, String diaSemana, Integer mes, int h, int m) {
        return new AtualizarSchedulerRequestDTO(freq, dia, diaSemana, mes, h, m, null);
    }

    @Nested
    class Cron {

        @Test
        void buildCron_cadaFrequencia() {
            assertEquals("0 5 0 * * *", SchedulerService.buildCron(FrequenciaScheduler.DIARIO, dto("DIARIO", null, null, null, 0, 5)));
            assertEquals("0 30 9 * * MON", SchedulerService.buildCron(FrequenciaScheduler.SEMANAL, dto("SEMANAL", null, "MON", null, 9, 30)));
            assertEquals("0 0 2 1,15 * *", SchedulerService.buildCron(FrequenciaScheduler.QUINZENAL, dto("QUINZENAL", null, null, null, 2, 0)));
            assertEquals("0 0 2 5 * *", SchedulerService.buildCron(FrequenciaScheduler.MENSAL, dto("MENSAL", 5, null, null, 2, 0)));
            assertEquals("0 0 2 5 1,4,7,10 *", SchedulerService.buildCron(FrequenciaScheduler.TRIMESTRAL, dto("TRIMESTRAL", 5, null, null, 2, 0)));
            assertEquals("0 0 2 5 1,7 *", SchedulerService.buildCron(FrequenciaScheduler.SEMESTRAL, dto("SEMESTRAL", 5, null, null, 2, 0)));
            assertEquals("0 0 2 5 6 *", SchedulerService.buildCron(FrequenciaScheduler.ANUAL, dto("ANUAL", 5, null, 6, 2, 0)));
        }

        @Test
        @DisplayName("o '?' do cron vale '*' — um job diário semeado com '?' não passa por semanal")
        void descreverCron_interrogacaoEDiario() {
            var desc = SchedulerService.descreverCron(job(List.of()), "0 15 0 * * ?", null);
            assertEquals("DIARIO", desc.getFrequencia());
            assertEquals("Diário às 00:15", desc.getDescricao());
        }

        @Test
        void descreverCron_anualEmPortugues() {
            var desc = SchedulerService.descreverCron(job(List.of()), "0 0 2 5 6 *", null);
            assertEquals("ANUAL", desc.getFrequencia());
            assertEquals("junho", desc.getMesDesc());
        }

        @Test
        @DisplayName("um cron com hora ou minuto não fixos não passa por diário")
        void descreverCron_foraDoPadrao() {
            var desc = SchedulerService.descreverCron(job(List.of()), "0 */5 * * * *", null);
            assertEquals("Agendamento personalizado", desc.getDescricao());
            assertEquals(null, desc.getFrequencia());
        }
    }

    @Nested
    class Validacao {

        @Test
        void campos_foraDoIntervalo_recusados() {
            assertThrows(IgrpResponseStatusException.class,
                    () -> SchedulerService.validarCampos(FrequenciaScheduler.DIARIO, dto("DIARIO", null, null, null, 24, 0)));
            assertThrows(IgrpResponseStatusException.class,
                    () -> SchedulerService.validarCampos(FrequenciaScheduler.SEMANAL, dto("SEMANAL", null, null, null, 1, 0)));
            assertThrows(IgrpResponseStatusException.class,
                    () -> SchedulerService.validarCampos(FrequenciaScheduler.MENSAL, dto("MENSAL", 31, null, null, 1, 0)));
            assertThrows(IgrpResponseStatusException.class,
                    () -> SchedulerService.validarCampos(FrequenciaScheduler.ANUAL, dto("ANUAL", 5, null, 13, 1, 0)));
        }

        @Test
        void timezone_omissaMantemAActual_invalidaRecusada() {
            assertEquals("Atlantic/Cape_Verde", SchedulerService.validarTimezone(null, "Atlantic/Cape_Verde"));
            assertEquals("Europe/Lisbon", SchedulerService.validarTimezone("Europe/Lisbon", "Atlantic/Cape_Verde"));
            assertThrows(IgrpResponseStatusException.class, () -> SchedulerService.validarTimezone("Marte/Olympus", null));
        }

        @Test
        @DisplayName("os parâmetros do disparo manual saem normalizados: data ISO, ano inteiro, vazios fora")
        void parametros_normalizados() {
            var declarados = List.of(JobParametro.dataReferencia(), JobParametro.ano("ano", "Ano"));
            var recebidos = new HashMap<String, Object>();
            recebidos.put("data", "20/09/2026");
            recebidos.put("ano", " ");

            var normalizados = SchedulerService.validarParametros(job(declarados), recebidos);

            assertEquals(Map.of("data", "2026-09-20"), normalizados);
        }

        @Test
        @DisplayName("um parâmetro que o job não declara é recusado com o nome da tarefa, não a chave")
        void parametro_desconhecido_recusado() {
            var erro = assertThrows(IgrpResponseStatusException.class,
                    () -> SchedulerService.validarParametros(job(List.of()), Map.of("mes", "202607")));
            assertTrue(erro.getBody().getTitle().contains("Vencimento do direito a férias"));
            assertFalse(erro.getBody().getTitle().contains("JOB_X"));
        }

        @Test
        void parametro_invalido_ouObrigatorioEmFalta_recusado() {
            assertThrows(IgrpResponseStatusException.class, () -> SchedulerService.validarParametros(
                    job(List.of(JobParametro.dataReferencia())), Map.of("data", "31/02/2026")));
            assertThrows(IgrpResponseStatusException.class, () -> SchedulerService.validarParametros(
                    job(List.of(JobParametro.ano("ano", "Ano"))), Map.of("ano", "26")));
            assertThrows(IgrpResponseStatusException.class, () -> SchedulerService.validarParametros(
                    job(List.of(JobParametro.texto("motivo", "Motivo").obrigatorio())), Map.of()));
        }
    }

    @Nested
    class InstanteDoDisparo {

        private final LocalDateTime agora = LocalDateTime.of(2026, 9, 25, 0, 5, 0, 200_000_000);

        @Test
        void previsaoCerta_usaAPrevisao() {
            var previsto = LocalDateTime.of(2026, 9, 25, 0, 5);
            assertEquals(previsto, SchedulerService.instanteDoDisparo(previsto, agora));
        }

        @Test
        @DisplayName("previsão já avançada por outra réplica: usa a hora ao minuto, igual à da outra")
        void previsaoJaAvancada_usaAHoraAoMinuto() {
            assertEquals(LocalDateTime.of(2026, 9, 25, 0, 5),
                    SchedulerService.instanteDoDisparo(LocalDateTime.of(2026, 9, 26, 0, 5), agora));
        }

        @Test
        @DisplayName("previsão antiga que o sweeper ainda não tratou: não atribui este disparo a outro dia")
        void previsaoAntiga_usaAHoraAoMinuto() {
            assertEquals(LocalDateTime.of(2026, 9, 25, 0, 5),
                    SchedulerService.instanteDoDisparo(LocalDateTime.of(2026, 9, 22, 0, 5), agora));
        }

        @Test
        void semPrevisao_usaAHoraAoMinuto() {
            assertEquals(LocalDateTime.of(2026, 9, 25, 0, 5), SchedulerService.instanteDoDisparo(null, agora));
        }
    }
}
