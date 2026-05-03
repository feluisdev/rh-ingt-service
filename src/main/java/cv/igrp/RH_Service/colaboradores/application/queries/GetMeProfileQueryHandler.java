package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.carreiras.infrastructure.persistence.repository.CareerEntityRepository;
import cv.igrp.RH_Service.carreiras.infrastructure.persistence.repository.CategoryEntityRepository;
import cv.igrp.RH_Service.carreiras.infrastructure.persistence.repository.GradeEntityRepository;
import cv.igrp.RH_Service.colaboradores.application.dto.MeProfileResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.EnquadramentoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import org.springframework.http.HttpStatus;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.CargoEntityRepository;
import cv.igrp.RH_Service.estrutura.infrastructure.persistence.repository.OrganizationalUnitEntityRepository;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component("colabsGetMeProfileQueryHandler")
@RequiredArgsConstructor
public class GetMeProfileQueryHandler
        implements QueryHandler<GetMeProfileQuery, ResponseEntity<MeProfileResponseDTO>> {

    private final CurrentEmployeeResolver currentEmployeeResolver;
    private final FuncionarioRepository funcionarioRepository;
    private final EnquadramentoRepository enquadramentoRepository;
    private final OrganizationalUnitEntityRepository unitEntityRepository;
    private final CargoEntityRepository cargoEntityRepository;
    private final CareerEntityRepository careerEntityRepository;
    private final CategoryEntityRepository categoryEntityRepository;
    private final GradeEntityRepository gradeEntityRepository;

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
                unitEntityRepository.findById(enq.getUnidadeOrganicaId()).ifPresent(u ->
                        response.setCurrentUnit(new MeProfileResponseDTO.UnitRef(u.getId().toString(), u.getName())));
            }
            if (enq.getCargoId() != null) {
                cargoEntityRepository.findById(enq.getCargoId()).ifPresent(c ->
                        response.setCurrentJob(new MeProfileResponseDTO.JobRef(c.getId().toString(), c.getNome())));
            }
            if (enq.getCareerId() != null) {
                careerEntityRepository.findById(enq.getCareerId()).ifPresent(c ->
                        response.setCareer(new MeProfileResponseDTO.CareerRef(c.getId().toString(), c.getName())));
            }
            if (enq.getCategoryId() != null) {
                categoryEntityRepository.findById(enq.getCategoryId()).ifPresent(c ->
                        response.setCategory(new MeProfileResponseDTO.CategoryRef(c.getId().toString(), c.getName())));
            }
            if (enq.getGradeId() != null) {
                gradeEntityRepository.findById(enq.getGradeId()).ifPresent(g ->
                        response.setGrade(new MeProfileResponseDTO.GradeRef(g.getId().toString(), g.getGradeNumber())));
            }
        });

        return ResponseEntity.ok(response);
    }
}
