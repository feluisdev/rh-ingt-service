package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import cv.igrp.RH_Service.colaboradores.domain.models.FormaPrestacaoMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.models.LicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.models.SubtipoLicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.SubtipoLicencaMobilidadeId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.LocalDate;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Prova da forma de prestação da mobilidade — Lei n.º 20/X/2023, art. 134.º n.º 2.
 *
 * <p>A lei classifica a mobilidade geral <b>quanto à forma de prestação</b>: a tempo inteiro, em
 * exclusividade, ou <i>«em regime de acumulação, quando o funcionário passa a exercer funções
 * noutro serviço, em acumulação com as do serviço de origem»</i>.
 *
 * <p>O que estes testes fixam, e que é o erro que o modelo antes tinha: a acumulação é uma forma
 * de prestar a <b>mobilidade</b>, não um título para ocupar um segundo Lugar. Uma mobilidade em
 * acumulação continua a não criar afectação nenhuma, porque a mobilidade transitória é sem
 * ocupação do lugar do quadro (art. 135.º n.º 7).
 */
@ExtendWith(MockitoExtension.class)
class FormaPrestacaoMobilidadeTest {

    @Mock private cv.igrp.RH_Service.colaboradores.domain.repository.SubtipoLicencaMobilidadeRepository subtipoRepository;
    @Mock private cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository unidadeRepository;
    @Mock private cv.igrp.RH_Service.colaboradores.domain.repository.LicencaMobilidadeRepository licencaRepository;

    @InjectMocks private MobilidadeService service;

    private static SubtipoLicencaMobilidade subtipo(String recordType) {
        return SubtipoLicencaMobilidade.reconstituir(
                SubtipoLicencaMobilidadeId.gerarNovo(), "Subtipo de teste", "COD",
                recordType, false, true, false, true, 365, 1, "MANTEM", null, "REGRESSA_LUGAR");
    }

    private static LicencaMobilidade nova() {
        return LicencaMobilidade.criar(FuncionarioId.gerarNovo(), SubtipoLicencaMobilidadeId.gerarNovo(),
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30),
                null, null, null, null, null, null, null);
    }

    @Nested
    class OValorLido {

        /** A exclusividade é a regra (art. 20.º), logo é o que o silêncio significa. */
        @Test
        void vazioOuNuloValeTempoInteiro() {
            assertEquals(FormaPrestacaoMobilidade.TEMPO_INTEIRO, FormaPrestacaoMobilidade.de(null));
            assertEquals(FormaPrestacaoMobilidade.TEMPO_INTEIRO, FormaPrestacaoMobilidade.de("  "));
        }

        @Test
        void toleraEspacosEMinusculas() {
            assertEquals(FormaPrestacaoMobilidade.ACUMULACAO, FormaPrestacaoMobilidade.de(" acumulacao "));
        }

        @Test
        void recusaValorForaDaLista() {
            var erro = assertThrows(IgrpResponseStatusException.class,
                    () -> FormaPrestacaoMobilidade.de("MEIO_TEMPO"));
            assertEquals(422, erro.getStatusCode().value());
        }

        @Test
        void saoDuas() {
            assertEquals("TEMPO_INTEIRO, ACUMULACAO", FormaPrestacaoMobilidade.codigosValidos());
            assertTrue(FormaPrestacaoMobilidade.ACUMULACAO.isAcumulacao());
            assertFalse(FormaPrestacaoMobilidade.TEMPO_INTEIRO.isAcumulacao());
        }
    }

    @Nested
    class OQueOProcessoGuarda {

        @Test
        void umProcessoNasceATempoInteiro() {
            assertEquals(FormaPrestacaoMobilidade.TEMPO_INTEIRO, nova().getFormaPrestacao());
        }

        @Test
        void aFormaFixaSeEnquantoEstaPorDecidir() {
            LicencaMobilidade l = nova();
            l.definirFormaPrestacao(FormaPrestacaoMobilidade.ACUMULACAO);
            assertEquals(FormaPrestacaoMobilidade.ACUMULACAO, l.getFormaPrestacao());
        }

        @Test
        void nulaVoltaAValerTempoInteiro() {
            LicencaMobilidade l = nova();
            l.definirFormaPrestacao(null);
            assertEquals(FormaPrestacaoMobilidade.TEMPO_INTEIRO, l.getFormaPrestacao());
        }

        /** Depois do despacho, passar de exclusividade a acumulação é outro despacho. */
        @Test
        void depoisDeDecididoJaNaoSeMuda() {
            LicencaMobilidade l = nova();
            l.aprovar();

            var erro = assertThrows(IgrpResponseStatusException.class,
                    () -> l.definirFormaPrestacao(FormaPrestacaoMobilidade.ACUMULACAO));

            assertEquals(409, erro.getStatusCode().value());
        }
    }

    @Nested
    class OndeAAcumulacaoFazSentido {

        @Test
        void umaMobilidadePodeSerPrestadaEmAcumulacao() {
            service.validarFormaPrestacao(subtipo("MOBILIDADE"), FormaPrestacaoMobilidade.ACUMULACAO);
        }

        /** Quem está de licença não exerce funções em serviço nenhum — não há nada a acumular. */
        @Test
        void umaLicencaNaoSePrestaEmAcumulacao() {
            var erro = assertThrows(IgrpResponseStatusException.class,
                    () -> service.validarFormaPrestacao(subtipo("LICENCA"), FormaPrestacaoMobilidade.ACUMULACAO));

            assertEquals(422, erro.getStatusCode().value());
        }

        @Test
        void umaLicencaATempoInteiroNaoIncomodaNinguem() {
            service.validarFormaPrestacao(subtipo("LICENCA"), FormaPrestacaoMobilidade.TEMPO_INTEIRO);
            service.validarFormaPrestacao(subtipo("LICENCA"), null);
        }
    }
}
