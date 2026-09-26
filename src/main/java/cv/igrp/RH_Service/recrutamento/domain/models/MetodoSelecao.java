package cv.igrp.RH_Service.recrutamento.domain.models;

/** Os métodos de selecção da Lei n.º 20/X/2023, art. 128.º. */
public enum MetodoSelecao {
    /** N.º 1 a). */
    TRIAGEM_CURRICULAR,
    /** N.º 1 a) (segunda alínea a) do texto publicado). */
    PROVA_CONHECIMENTOS,
    /** N.º 1 b): competências, motivações ou aptidões. */
    AVALIACAO_COMPETENCIAS,
    /** N.º 1 c). */
    ENTREVISTA,
    /** N.º 2: quando o diploma da carreira o exija. */
    CURSO_FORMACAO,
    /** N.º 2. */
    PROVAS_FISICAS;

    /** Os obrigatórios do n.º 1. */
    public boolean obrigatorio() {
        return this == TRIAGEM_CURRICULAR || this == PROVA_CONHECIMENTOS || this == AVALIACAO_COMPETENCIAS || this == ENTREVISTA;
    }
}
