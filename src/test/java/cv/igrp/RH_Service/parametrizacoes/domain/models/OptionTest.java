package cv.igrp.RH_Service.parametrizacoes.domain.models;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OptionTest {

    @Test
    void criar_comCcodeValido_deveCriarComActiveTrue() {
        Option option = Option.criar("MARITAL_STATUS", "SINGLE", "Solteiro(a)", "pt-CV", 1, null);

        assertNotNull(option.getId());
        assertEquals("MARITAL_STATUS", option.getCcode());
        assertEquals("SINGLE", option.getCkey());
        assertEquals("Solteiro(a)", option.getCvalue());
        assertEquals("pt-CV", option.getLocale());
        assertTrue(option.isActive());
    }

    @Test
    void criar_comLocaleNulo_deveUsarDefaultPtCV() {
        Option option = Option.criar("SEX", "M", "Masculino", null, null, null);

        assertEquals("pt-CV", option.getLocale());
        assertEquals(0, option.getSortOrder());
    }

    @Test
    void criar_comCcodeInvalido_deveLancarBadRequest() {
        assertThrows(IgrpResponseStatusException.class, () ->
            Option.criar("INVALID_CODE", "KEY", "Value", "pt-CV", 1, null));
    }

    @Test
    void desativar_quandoActiva_deveDesativar() {
        Option option = Option.criar("SEX", "M", "Masculino", "pt-CV", 1, null);
        assertTrue(option.isActive());

        option.desativar();

        assertFalse(option.isActive());
    }

    @Test
    void desativar_quandoJaInactiva_deveLancarConflict() {
        Option option = Option.criar("SEX", "F", "Feminino", "pt-CV", 1, null);
        option.desativar();

        assertThrows(IgrpResponseStatusException.class, option::desativar);
    }

    @Test
    void reativar_quandoInactiva_deveReativar() {
        Option option = Option.criar("SEX", "M", "Masculino", "pt-CV", 1, null);
        option.desativar();

        option.reativar();

        assertTrue(option.isActive());
    }

    @Test
    void reativar_quandoJaActiva_deveLancarConflict() {
        Option option = Option.criar("SEX", "M", "Masculino", "pt-CV", 1, null);

        assertThrows(IgrpResponseStatusException.class, option::reativar);
    }

    @Test
    void atualizar_deveAlterarApenasCamposEditaveis() {
        Option option = Option.criar("NATIONALITY", "CV", "Cabo-verdiana", "pt-CV", 1, null);
        String originalCcode = option.getCcode();
        String originalCkey = option.getCkey();

        option.atualizar("Cabo-verdiana (actualizado)", 5, "Descrição nova");

        assertEquals("Cabo-verdiana (actualizado)", option.getCvalue());
        assertEquals(5, option.getSortOrder());
        assertEquals("Descrição nova", option.getDescription());
        assertEquals(originalCcode, option.getCcode());
        assertEquals(originalCkey, option.getCkey());
    }
}
