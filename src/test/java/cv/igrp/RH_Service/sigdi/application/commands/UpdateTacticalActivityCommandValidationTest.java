package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
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

    @Test
    void bodyWithoutIdDoesNotViolateConstraint() {
        UpdateTacticalActivityCommand command = new UpdateTacticalActivityCommand();
        // tacticalActivity fica null de propósito: o campo não tem @Valid neste comando,
        // logo não cascata validação -- só o id, alvo deste teste, importa aqui.

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
