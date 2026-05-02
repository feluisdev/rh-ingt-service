package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.models.Colocacao;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAfectacao;
import cv.igrp.RH_Service.colaboradores.domain.repository.ColocacaoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.LicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.SubtipoLicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.LicencaMobilidadeId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.SubtipoLicencaMobilidadeId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

@Component("colabsAprovarLicencaMobilidadeCommandHandler")
@RequiredArgsConstructor
public class AprovarLicencaMobilidadeCommandHandler
        implements CommandHandler<AprovarLicencaMobilidadeCommand, ResponseEntity<Map<String, ?>>> {

    private final LicencaMobilidadeRepository licencaRepository;
    private final ColocacaoRepository colocacaoRepository;
    private final SubtipoLicencaMobilidadeRepository subtipoRepository;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<Map<String, ?>> handle(AprovarLicencaMobilidadeCommand command) {
        var licenca = licencaRepository.findById(LicencaMobilidadeId.from(command.getLicencaId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Licença/mobilidade não encontrada: " + command.getLicencaId()));

        if (!licenca.isPending())
            throw IgrpResponseStatusException.of(HttpStatus.CONFLICT,
                    "Apenas registos PENDING podem ser aprovados. Estado actual: " + licenca.getStatus());

        licenca.aprovar();
        licencaRepository.save(licenca);

        var subtipo = subtipoRepository.findById(SubtipoLicencaMobilidadeId.from(licenca.getSubtipoId().getValor()))
                .orElse(null);
        boolean isMobilidade = subtipo != null && "MOBILIDADE".equals(subtipo.getRecordType());

        if (isMobilidade) {
            var hoje = LocalDate.now();
            colocacaoRepository.fecharColocacaoAtual(licenca.getFuncionarioId(), hoje);
            var novaColocacao = Colocacao.criar(
                    licenca.getFuncionarioId(),
                    licenca.getDestinationUnitId(),
                    null, hoje,
                    TipoAfectacao.MOBILIDADE, null);
            colocacaoRepository.save(novaColocacao);
        }

        return ResponseEntity.ok(Map.of("message", "Aprovado com sucesso"));
    }
}
