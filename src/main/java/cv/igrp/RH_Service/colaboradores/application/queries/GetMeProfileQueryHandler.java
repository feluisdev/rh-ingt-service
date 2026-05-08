package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.carreiras.domain.repository.CareerRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.CategoryRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.GradeRepository;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CareerId;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CategoryId;
import cv.igrp.RH_Service.carreiras.domain.valueobject.GradeId;
import cv.igrp.RH_Service.colaboradores.application.dto.MeProfileResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.EnquadramentoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.estrutura.domain.repository.JobRepository;
import cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.JobId;
import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;
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
    private final EnquadramentoRepository enquadramentoRepository;
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
        response.setWorkerState(funcionario.getSituacaoProfissional());
        response.setAdmissionDate(funcionario.getDataAdmissao());

        enquadramentoRepository.findCurrentByFuncionarioId(funcionarioId).ifPresent(enq -> {
            if (enq.getUnidadeOrganicaId() != null) {
                unitRepository.findById(OrganizationalUnitId.from(enq.getUnidadeOrganicaId())).ifPresent(u ->
                        response.setCurrentUnit(new MeProfileResponseDTO.UnitRef(u.getId().getStringValor(), u.getName())));
            }
            if (enq.getCargoId() != null) {
                jobRepository.findById(JobId.from(enq.getCargoId())).ifPresent(j ->
                        response.setCurrentJob(new MeProfileResponseDTO.JobRef(j.getId().getStringValor(), j.getName())));
            }
            if (enq.getCareerId() != null) {
                careerRepository.findById(CareerId.from(enq.getCareerId())).ifPresent(c ->
                        response.setCareer(new MeProfileResponseDTO.CareerRef(c.getId().getStringValor(), c.getName())));
            }
            if (enq.getCategoryId() != null) {
                categoryRepository.findById(CategoryId.from(enq.getCategoryId())).ifPresent(c ->
                        response.setCategory(new MeProfileResponseDTO.CategoryRef(c.getId().getStringValor(), c.getName())));
            }
            if (enq.getGradeId() != null) {
                gradeRepository.findById(GradeId.from(enq.getGradeId())).ifPresent(g ->
                        response.setGrade(new MeProfileResponseDTO.GradeRef(g.getId().getStringValor(), g.getGradeNumber())));
            }
        });

        return ResponseEntity.ok(response);
    }
}
