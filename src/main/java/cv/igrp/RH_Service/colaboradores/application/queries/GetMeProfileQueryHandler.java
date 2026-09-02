package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.carreiras.domain.repository.CareerRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.CategoryRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.GradeRepository;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CareerId;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CategoryId;
import cv.igrp.RH_Service.carreiras.domain.valueobject.GradeId;
import cv.igrp.RH_Service.colaboradores.application.dto.MeProfileResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.WorkerStateRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.WorkerStateId;
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.JobRepository;
import cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.JobId;
import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;
import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component("colabsGetMeProfileQueryHandler")
@RequiredArgsConstructor
public class GetMeProfileQueryHandler
        implements QueryHandler<GetMeProfileQuery, ResponseEntity<MeProfileResponseDTO>> {

    private final CurrentEmployeeResolver currentEmployeeResolver;
    private final FuncionarioRepository funcionarioRepository;
    private final WorkerStateRepository workerStateRepository;
    private final AssignmentRepository assignmentRepository;
    private final PositionRepository positionRepository;
    private final OrganizationalUnitRepository unitRepository;
    private final JobRepository jobRepository;
    private final CareerRepository careerRepository;
    private final CategoryRepository categoryRepository;
    private final GradeRepository gradeRepository;

    @IgrpQueryHandler
    public ResponseEntity<MeProfileResponseDTO> handle(GetMeProfileQuery query) {
        var funcionarioId = currentEmployeeResolver.resolve();

        var funcionario = funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Funcionário não encontrado: " + funcionarioId.getStringValor()));

        if (!Boolean.TRUE.equals(funcionario.getIsActive()))
            throw IgrpResponseStatusException.of(HttpStatus.FORBIDDEN, "Acesso negado: colaborador inactivo.");

        var response = new MeProfileResponseDTO();
        response.setId(funcionario.getId().getStringValor());
        response.setFullName(funcionario.getNomeCompleto());
        response.setNif(funcionario.getNif());
        response.setEmail(funcionario.getEmail());
        response.setPhone(funcionario.getTelefone());
        if (funcionario.getWorkerStateId() != null) {
            workerStateRepository.findById(WorkerStateId.from(funcionario.getWorkerStateId()))
                    .ifPresent(ws -> response.setWorkerState(ws.getCode()));
        }
        response.setAdmissionDate(funcionario.getDataAdmissao());

        // Enquadramento derivado do NOVO modelo: afectação corrente + Lugar (Position).
        assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId).ifPresent(a -> {
            Position pos = a.getPositionId() != null
                    ? positionRepository.findById(PositionId.from(a.getPositionId())).orElse(null)
                    : null;
            if (pos != null) {
                if (pos.getUnidadeOrganicaId() != null) {
                    unitRepository.findById(OrganizationalUnitId.from(pos.getUnidadeOrganicaId())).ifPresent(u ->
                            response.setCurrentUnit(new MeProfileResponseDTO.UnitRef(u.getId().getStringValor(), u.getName())));
                }
                if (pos.getJobId() != null) {
                    jobRepository.findById(JobId.from(pos.getJobId())).ifPresent(j ->
                            response.setCurrentJob(new MeProfileResponseDTO.JobRef(j.getId().getStringValor(), j.getName())));
                }
                if (pos.getCareerId() != null) {
                    careerRepository.findById(CareerId.from(pos.getCareerId())).ifPresent(c ->
                            response.setCareer(new MeProfileResponseDTO.CareerRef(c.getId().getStringValor(), c.getName())));
                }
                if (pos.getCategoryId() != null) {
                    categoryRepository.findById(CategoryId.from(pos.getCategoryId())).ifPresent(c ->
                            response.setCategory(new MeProfileResponseDTO.CategoryRef(c.getId().getStringValor(), c.getName())));
                }
            }
            if (a.getGradeId() != null) {
                gradeRepository.findById(GradeId.from(a.getGradeId())).ifPresent(g ->
                        response.setGrade(new MeProfileResponseDTO.GradeRef(g.getId().getStringValor(), g.getGradeNumber())));
            }
        });

        return ResponseEntity.ok(response);
    }
}
