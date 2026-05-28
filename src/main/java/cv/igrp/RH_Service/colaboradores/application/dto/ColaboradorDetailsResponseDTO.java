package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ColaboradorDetailsResponseDTO {
    private FuncionarioResponseDTO funcionario;
    private ContratoResponseDTO contrato;
    private EnquadramentoResponseDTO enquadramento;
    private DadosBancariosResponseDTO dadosBancarios;
    private List<DocumentoResponseDTO> documentos;
}
