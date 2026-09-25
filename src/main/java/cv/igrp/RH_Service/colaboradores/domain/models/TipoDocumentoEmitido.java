package cv.igrp.RH_Service.colaboradores.domain.models;

/** Os documentos que o RH emite e numera, cada tipo com a sua série (BR-DEC-05). */
public enum TipoDocumentoEmitido {
    DECLARACAO("DEC"),
    CARTAO_PROFISSIONAL("CIP"),
    EXTRACTO_PUBLICACAO("EXT");

    private final String serie;

    TipoDocumentoEmitido(String serie) {
        this.serie = serie;
    }

    public String getSerie() {
        return serie;
    }
}
