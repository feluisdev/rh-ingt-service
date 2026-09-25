package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cv.igrp.RH_Service.sigdi.application.dto.CreateTacticalActivityDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Mesma família do A-135-2AA (2026-09-09): {@code UpdateTacticalActivityCommand} é o
 * próprio parâmetro {@code @Valid @RequestBody} de
 * {@code TaticalController.updateTacticalActivity}, que só chama {@code setId(id)}
 * DEPOIS de o Spring já ter corrido esta validação sobre o objeto vinculado do corpo.
 * Um {@code @NotBlank} em {@code id} não era um caso de 400 observável apenas porque o
 * único cliente real (functions/tactical.ts:updateActivity) sempre envia {@code id} no
 * corpo -- coincidência do payload, não garantia do servidor. Esta prova cobre o
 * caso que a coincidência escondia: um corpo sem {@code id}, como o contrato
 * documentado (id vem do caminho) permitiria a qualquer chamador direto do Gateway.
 *
 * <p><b>136-09 (D-52), acrescentado:</b> {@link #nestedInvalidJustificationWhyViolatesConstraint()}
 * e {@link #nestedValidJustificationWhyDoesNotViolateConstraint()} são o caso que dá valor à
 * Task 2a deste plano -- provam que o {@code @Valid} acrescentado ao campo
 * {@code tacticalActivity} faz a validação de bean cascatear para
 * {@link CreateTacticalActivityDTO}. Antes desta correção {@code tacticalActivity} não tinha
 * {@code @Valid}, e a alteração de atividade não tinha validação de campo nenhuma no servidor --
 * nem sequer o {@code @Size(min = 1)} fraco do caminho de criação. Sem estes dois testes, o
 * {@code @Valid} seria uma linha que ninguém verificou.
 */
class UpdateTacticalActivityCommandValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    private CreateTacticalActivityDTO validNestedDto() {
        CreateTacticalActivityDTO dto = new CreateTacticalActivityDTO();
        dto.setStrategicGoalId(UUID.randomUUID());
        dto.setOrganicUnitId(UUID.randomUUID());
        dto.setTitle("Atividade tática de teste");
        dto.setStartDate(LocalDate.of(2026, 1, 1));
        dto.setEndDate(LocalDate.of(2026, 12, 31));
        return dto;
    }

    @Test
    void nestedInvalidJustificationWhyViolatesConstraint() {
        CreateTacticalActivityDTO nested = validNestedDto();
        nested.setJustificationWhy("123456789"); // 9 caracteres -- abaixo do mínimo 10

        UpdateTacticalActivityCommand command = new UpdateTacticalActivityCommand();
        command.setId("994e1227-2974-4286-85ad-66115625bcda");
        command.setTacticalActivity(nested);

        Set<ConstraintViolation<UpdateTacticalActivityCommand>> violations =
                validator.validate(command);

        assertEquals(1, violations.size(),
                "Com @Valid em tacticalActivity, um justificationWhy de 9 caracteres embrulhado "
                        + "no comando de alteração tem de violar -- é a prova de que a cascata "
                        + "funciona e de que a alteração de atividade deixou de não ter validação "
                        + "de campo nenhuma (D-52)");
        assertEquals("tacticalActivity.justificationWhy",
                violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void nestedValidJustificationWhyDoesNotViolateConstraint() {
        CreateTacticalActivityDTO nested = validNestedDto();
        nested.setJustificationWhy("1234567890"); // 10 caracteres -- cumpre o mínimo

        UpdateTacticalActivityCommand command = new UpdateTacticalActivityCommand();
        command.setId("994e1227-2974-4286-85ad-66115625bcda");
        command.setTacticalActivity(nested);

        Set<ConstraintViolation<UpdateTacticalActivityCommand>> violations =
                validator.validate(command);

        assertTrue(violations.isEmpty(),
                "Com justificationWhy a cumprir o mínimo, o comando embrulhado não pode violar");
    }

    @Test
    void bodyWithoutIdDoesNotViolateConstraint() {
        UpdateTacticalActivityCommand command = new UpdateTacticalActivityCommand();
        // tacticalActivity fica null de propósito: mesmo com @Valid (136-09), a cascata do
        // Bean Validation não valida um campo nulo -- só o id, alvo deste teste, importa aqui.

        Set<ConstraintViolation<UpdateTacticalActivityCommand>> violations = validator.validate(command);

        assertTrue(violations.isEmpty(),
                "Um corpo sem id (id vem do caminho do URL) tem de passar a validação -- era o "
                        + "caso que só não dava 400 por coincidência do payload do único cliente real");
    }

    @Test
    void idSetAfterValidationStillDoesNotViolateConstraint() {
        UpdateTacticalActivityCommand command = new UpdateTacticalActivityCommand();
        command.setId("994e1227-2974-4286-85ad-66115625bcda");

        Set<ConstraintViolation<UpdateTacticalActivityCommand>> violations = validator.validate(command);

        assertTrue(violations.isEmpty(),
                "Com id preenchido (como o controlador faz a partir do {id} do caminho) continua "
                        + "sem violações");
    }
}
