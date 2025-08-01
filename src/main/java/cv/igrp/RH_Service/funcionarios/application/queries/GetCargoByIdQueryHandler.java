package cv.igrp.RH_Service.funcionarios.application.queries;
import cv.igrp.RH_Service.funcionarios.domain.repository.CargoRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.CargoMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import cv.igrp.RH_Service.funcionarios.application.dto.CargoResponseDTO;

@Component
public class GetCargoByIdQueryHandler implements QueryHandler<GetCargoByIdQuery, ResponseEntity<CargoResponseDTO>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(GetCargoByIdQueryHandler.class);

  private final CargoRepository cargoRepository;
  private final CargoMapper cargoMapper;
  public GetCargoByIdQueryHandler(CargoRepository cargoRepository, CargoMapper cargoMapper) {

    this.cargoRepository = cargoRepository;
    this.cargoMapper = cargoMapper;
  }

   @IgrpQueryHandler
  public ResponseEntity<CargoResponseDTO> handle(GetCargoByIdQuery query) {
     var cargoId = ExternalID.from(query.getCargoId());

     var cargo = cargoRepository.getById(cargoId)
         .orElseThrow(() -> IgrpResponseStatusException.notFound("Cargo não encontrado com ID: " + cargoId.getStringValor()));

     var dto = cargoMapper.toDTO(cargo);

     return ResponseEntity.ok(dto);
  }

}
