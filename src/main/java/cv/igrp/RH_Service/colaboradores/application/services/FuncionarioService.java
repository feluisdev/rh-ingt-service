package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.application.dto.FuncionarioRequestDTO;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.WorkerStateRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;
    private final WorkerStateRepository workerStateRepository;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public Funcionario criarFuncionario(FuncionarioRequestDTO dto) {
        if (funcionarioRepository.existsByNif(dto.getNif()))
            throw IgrpResponseStatusException.conflict(
                    "Já existe um funcionário com NIF '" + dto.getNif() + "'.");

        if (dto.getNumeroDocumento() != null && !dto.getNumeroDocumento().isBlank()
                && funcionarioRepository.existsByNumeroDocumento(dto.getNumeroDocumento()))
            throw IgrpResponseStatusException.conflict(
                    "Já existe um funcionário com número de documento '" + dto.getNumeroDocumento() + "'.");

        UUID workerStateId = workerStateRepository.findByCode("ATIVO")
                .orElseThrow(() -> IgrpResponseStatusException.badRequest(
                        "Estado 'ATIVO' não configurado no sistema."))
                .getId().getValor();

        Long seq = jdbcTemplate.queryForObject("SELECT nextval('seq_numero_funcionario')", Long.class);
        String numeroFuncionario = String.format("F%06d", seq);

        return funcionarioRepository.save(
                Funcionario.criar(numeroFuncionario, dto.getNomeCompleto(), dto.getDataNascimento(),
                        dto.getGenero(), dto.getEstadoCivil(), dto.getNif(),
                        dto.getDocumentTypeId(), dto.getNumeroDocumento(),
                        dto.getDataEmissaoDoc(), dto.getDataValidadeDoc(),
                        dto.getNacionalidade(), dto.getEmail(), dto.getTelefone(),
                        dto.getMorada(), dto.getIlha(), dto.getConcelho(), dto.getLocalidade(),
                        workerStateId, dto.getDataAdmissao()));
    }
}
