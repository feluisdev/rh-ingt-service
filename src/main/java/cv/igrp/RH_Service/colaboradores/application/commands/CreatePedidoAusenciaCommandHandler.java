package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.services.SaldoAusenciaService;
import cv.igrp.RH_Service.colaboradores.domain.models.PedidoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.repository.FeriadoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.PedidoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.TipoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.service.DiasUteisCalculator;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;

@Component("colabsCreatePedidoAusenciaCommandHandler")
@RequiredArgsConstructor
public class CreatePedidoAusenciaCommandHandler
        implements CommandHandler<CreatePedidoAusenciaCommand, ResponseEntity<Map<String, ?>>> {

    private final PedidoAusenciaRepository pedidoRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final TipoAusenciaRepository tipoAusenciaRepository;
    private final FeriadoRepository feriadoRepository;
    private final DiasUteisCalculator diasUteisCalculator;
    private final SaldoAusenciaService saldoAusenciaService;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<Map<String, ?>> handle(CreatePedidoAusenciaCommand command) {
        var funcionarioId = FuncionarioId.from(command.getFuncionarioId());
        funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Funcionário não encontrado: " + command.getFuncionarioId()));

        var dto = command.getRequest();
        var tipoId = TipoAusenciaId.from(dto.getTipoAusenciaId());
        var tipo = tipoAusenciaRepository.findById(tipoId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Tipo de ausência não encontrado: " + dto.getTipoAusenciaId()));
        if (!Boolean.TRUE.equals(tipo.getIsActive()))
            throw IgrpResponseStatusException.badRequest("Tipo de ausência inactivo: " + dto.getTipoAusenciaId());

        var feriados = new HashSet<>(feriadoRepository.findAllNacionaisActivosByAno(dto.getDataInicio().getYear()));
        int numeroDias = diasUteisCalculator.calcular(dto.getDataInicio(), dto.getDataFim(), feriados);

        if (pedidoRepository.existsOverlapForFuncionario(funcionarioId, dto.getDataInicio(), dto.getDataFim()))
            throw IgrpResponseStatusException.conflict("Existe sobreposição de datas com um pedido APROVADO ou PENDENTE do mesmo funcionário.");

        if (tipo.getMaxDaysPerYear() != null) {
            int ano = dto.getDataInicio().getYear();
            int diasUsados = pedidoRepository.somarDiasNoAno(funcionarioId, tipoId, ano);
            if (diasUsados + numeroDias > tipo.getMaxDaysPerYear())
                throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Limite anual de dias excedido. Disponíveis: " + (tipo.getMaxDaysPerYear() - diasUsados) + ", solicitados: " + numeroDias);
        }

        var pedido = PedidoAusencia.criar(
                funcionarioId, tipoId, dto.getDataInicio(), dto.getDataFim(), numeroDias, dto.getMotivo());

        // Os dias ficam reservados desde a submissão: dois pedidos em simultâneo já não
        // podem esgotar duas vezes o mesmo saldo. Sem saldo suficiente, é 422 já aqui.
        saldoAusenciaService.reservar(pedido);

        var saved = pedidoRepository.save(pedido);

        return ResponseEntity.status(201).body(Map.of(
                "id", saved.getId().getStringValor(),
                "numeroDias", saved.getNumeroDias(),
                "estado", saved.getEstadoTexto(),
                "message", "Criado com sucesso"));
    }
}
