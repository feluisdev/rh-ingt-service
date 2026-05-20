package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.carreiras.domain.repository.CareerRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.CategoryRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.GradeRepository;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CareerId;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CategoryId;
import cv.igrp.RH_Service.carreiras.domain.valueobject.GradeId;
import cv.igrp.RH_Service.colaboradores.application.dto.EnquadramentoRequestDTO;
import cv.igrp.RH_Service.colaboradores.domain.models.EnquadramentoProfissional;
import cv.igrp.RH_Service.colaboradores.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.EnquadramentoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.estrutura.domain.repository.FunctionRepository;
import cv.igrp.RH_Service.estrutura.domain.repository.JobRepository;
import cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.FunctionId;
import cv.igrp.RH_Service.estrutura.domain.valueobject.JobId;
import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.ContractTypeRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.ContractTypeId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EnquadramentoService {

    private final EnquadramentoRepository enquadramentoRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final ContratoRepository contratoRepository;
    private final ContractTypeRepository contractTypeRepository;
    private final CareerRepository careerRepository;
    private final CategoryRepository categoryRepository;
    private final GradeRepository gradeRepository;
    private final JobRepository jobRepository;
    private final FunctionRepository functionRepository;
    private final OrganizationalUnitRepository organizationalUnitRepository;

    @Transactional
    public EnquadramentoProfissional criarEnquadramento(FuncionarioId funcionarioId, EnquadramentoRequestDTO dto) {
        funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Funcionário não encontrado: " + funcionarioId.getStringValor()));

        var contrato = contratoRepository.findCurrentByFuncionarioId(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                        "O funcionário não tem contrato activo. Crie um contrato antes de enquadrar."));
        if (!"ATIVO".equals(contrato.getStatus()))
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "O contrato actual está " + contrato.getStatus() + ". Só é possível enquadrar com contrato ATIVO.");

        if (dto.getDataInicio().isBefore(contrato.getStartDate()))
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "A data de início do enquadramento (" + dto.getDataInicio()
                            + ") não pode ser anterior à data de início do contrato (" + contrato.getStartDate() + ").");
        if (contrato.getEndDate() != null && dto.getDataInicio().isAfter(contrato.getEndDate()))
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "A data de início do enquadramento (" + dto.getDataInicio()
                            + ") não pode ser posterior à data de fim do contrato (" + contrato.getEndDate() + ").");

        boolean requiresCareer = false;
        if (contrato.getContractTypeId() != null) {
            var contractType = contractTypeRepository.findById(ContractTypeId.from(contrato.getContractTypeId()));
            requiresCareer = contractType.map(ct -> ct.isRequiresCareerStructure()).orElse(false);
        }

        UUID careerId = null;
        UUID categoryId = null;
        UUID gradeId = null;

        if (requiresCareer) {
            if (dto.getCareerId() == null || dto.getCareerId().isBlank())
                throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                        "O campo careerId é obrigatório para este tipo de vínculo.");
            if (dto.getCategoryId() == null || dto.getCategoryId().isBlank())
                throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                        "O campo categoryId é obrigatório para este tipo de vínculo.");
            if (dto.getGradeId() == null || dto.getGradeId().isBlank())
                throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                        "O campo gradeId é obrigatório para este tipo de vínculo.");

            var career = careerRepository.findById(CareerId.from(dto.getCareerId()))
                    .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                            "Carreira não encontrada: " + dto.getCareerId()));
            if (!Boolean.TRUE.equals(career.getIsActive()))
                throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Carreira inactiva: " + dto.getCareerId());

            var category = categoryRepository.findById(CategoryId.from(dto.getCategoryId()))
                    .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                            "Categoria não encontrada: " + dto.getCategoryId()));
            if (!Boolean.TRUE.equals(category.getIsActive()))
                throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Categoria inactiva: " + dto.getCategoryId());

            var grade = gradeRepository.findById(GradeId.from(dto.getGradeId()))
                    .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                            "Escalão não encontrado: " + dto.getGradeId()));
            if (!Boolean.TRUE.equals(grade.getIsActive()))
                throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Escalão inactivo: " + dto.getGradeId());

            careerId = UUID.fromString(dto.getCareerId());
            categoryId = UUID.fromString(dto.getCategoryId());
            gradeId = UUID.fromString(dto.getGradeId());
        } else {
            if (dto.getCareerId() != null && !dto.getCareerId().isBlank()) {
                var career = careerRepository.findById(CareerId.from(dto.getCareerId()))
                        .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                                "Carreira não encontrada: " + dto.getCareerId()));
                if (!Boolean.TRUE.equals(career.getIsActive()))
                    throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                            "Carreira inactiva: " + dto.getCareerId());
                careerId = UUID.fromString(dto.getCareerId());
            }
            if (dto.getCategoryId() != null && !dto.getCategoryId().isBlank()) {
                var category = categoryRepository.findById(CategoryId.from(dto.getCategoryId()))
                        .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                                "Categoria não encontrada: " + dto.getCategoryId()));
                if (!Boolean.TRUE.equals(category.getIsActive()))
                    throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                            "Categoria inactiva: " + dto.getCategoryId());
                categoryId = UUID.fromString(dto.getCategoryId());
            }
            if (dto.getGradeId() != null && !dto.getGradeId().isBlank()) {
                var grade = gradeRepository.findById(GradeId.from(dto.getGradeId()))
                        .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                                "Escalão não encontrado: " + dto.getGradeId()));
                if (!Boolean.TRUE.equals(grade.getIsActive()))
                    throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                            "Escalão inactivo: " + dto.getGradeId());
                gradeId = UUID.fromString(dto.getGradeId());
            }
        }

        var cargo = jobRepository.findById(JobId.from(dto.getCargoId()))
                .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Cargo não encontrado: " + dto.getCargoId()));
        if (!cargo.isActive())
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Cargo inactivo: " + dto.getCargoId());

        var unidade = organizationalUnitRepository.findById(OrganizationalUnitId.from(dto.getUnidadeOrganicaId()))
                .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Unidade orgânica não encontrada: " + dto.getUnidadeOrganicaId()));
        if (!unidade.isActive())
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Unidade orgânica inactiva: " + dto.getUnidadeOrganicaId());

        var currentEnquadramento = enquadramentoRepository.findCurrentByFuncionarioId(funcionarioId);
        if (currentEnquadramento.isPresent()) {
            if (!dto.getDataInicio().isAfter(currentEnquadramento.get().getDataInicio()))
                throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                        "A data de início deve ser posterior à data de início do enquadramento actual ("
                                + currentEnquadramento.get().getDataInicio() + ").");
            var prev = currentEnquadramento.get();
            prev.encerrar(dto.getDataInicio().minusDays(1));
            enquadramentoRepository.save(prev);
        }

        UUID functionId = null;
        if (dto.getFunctionId() != null && !dto.getFunctionId().isBlank()) {
            functionId = UUID.fromString(dto.getFunctionId());
            var funcao = functionRepository.findById(FunctionId.from(functionId))
                    .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                            "Função não encontrada: " + dto.getFunctionId()));
            funcao.validarCompatibilidadeComCargo(UUID.fromString(dto.getCargoId()));
        }

        return enquadramentoRepository.save(
                EnquadramentoProfissional.criar(funcionarioId,
                        careerId, categoryId, gradeId,
                        UUID.fromString(dto.getCargoId()),
                        functionId, UUID.fromString(dto.getUnidadeOrganicaId()),
                        dto.getDataInicio()));
    }
}
