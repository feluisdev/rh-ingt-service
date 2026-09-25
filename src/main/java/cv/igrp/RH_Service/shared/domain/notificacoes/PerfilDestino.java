package cv.igrp.RH_Service.shared.domain.notificacoes;

/**
 * Destinatário colectivo de uma notificação: uma caixa partilhada, que qualquer pessoa do perfil lê.
 * A chefia não é um perfil — é uma pessoa (o titular do Lugar-pai), e notifica-se pelo seu id.
 *
 * <p>Enquanto as permissões por perfil não existem (adiadas), a caixa do RH lê-se sem restrição; a
 * leitura por uma pessoa marca-a lida para todas.
 */
public enum PerfilDestino {
    RH
}
