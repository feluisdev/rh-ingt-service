package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.CartaoProfissionalId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.DocumentoEmitidoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.Objects;

/**
 * <b>Cartão de identificação profissional</b> (Lei n.º 20/X/2023, art. 25.º; BR-CID-01..06): identifica o
 * funcionário no exercício da função, com a categoria e o cargo. Disponibilizado pelo serviço, que atesta a
 * entrega (n.º 3); <b>válido enquanto se mantiver no quadro, na mesma função e categoria</b> (n.º 5) — por
 * isso guarda a categoria e a função com que foi emitido.
 *
 * <p>EMITIDO → ENTREGUE → DEVOLVIDO · ANULADO. O PDF (modelo de teste) é um documento emitido, no MinIO.
 */
@Getter
public class CartaoProfissional {

    public enum Estado { EMITIDO, ENTREGUE, DEVOLVIDO, ANULADO }

    private CartaoProfissionalId id;
    private FuncionarioId funcionarioId;
    private DocumentoEmitidoId documentoId;
    private String numero;
    private LocalDate emitidoEm;
    private String categoriaId;
    private String categoria;
    private String funcaoId;
    private String funcao;
    private String cargo;
    private Estado estado;
    private LocalDate dataEntrega;
    private LocalDate dataDevolucao;
    private String motivoAnulacao;

    private CartaoProfissional() {}

    public static CartaoProfissional emitir(FuncionarioId funcionarioId, DocumentoEmitidoId documentoId, String numero,
                                            LocalDate emitidoEm, String categoriaId, String categoria, String funcaoId,
                                            String funcao, String cargo) {
        var c = new CartaoProfissional();
        c.id = CartaoProfissionalId.gerarNovo();
        c.funcionarioId = Objects.requireNonNull(funcionarioId);
        c.documentoId = Objects.requireNonNull(documentoId);
        c.numero = Objects.requireNonNull(numero);
        c.emitidoEm = Objects.requireNonNull(emitidoEm);
        c.categoriaId = categoriaId;
        c.categoria = categoria;
        c.funcaoId = funcaoId;
        c.funcao = funcao;
        c.cargo = cargo;
        c.estado = Estado.EMITIDO;
        return c;
    }

    public static CartaoProfissional reconstruir(CartaoProfissionalId id, FuncionarioId funcionarioId, DocumentoEmitidoId documentoId,
                                                 String numero, LocalDate emitidoEm, String categoriaId, String categoria,
                                                 String funcaoId, String funcao, String cargo, Estado estado, LocalDate dataEntrega,
                                                 LocalDate dataDevolucao, String motivoAnulacao) {
        var c = new CartaoProfissional();
        c.id = id;
        c.funcionarioId = funcionarioId;
        c.documentoId = documentoId;
        c.numero = numero;
        c.emitidoEm = emitidoEm;
        c.categoriaId = categoriaId;
        c.categoria = categoria;
        c.funcaoId = funcaoId;
        c.funcao = funcao;
        c.cargo = cargo;
        c.estado = estado;
        c.dataEntrega = dataEntrega;
        c.dataDevolucao = dataDevolucao;
        c.motivoAnulacao = motivoAnulacao;
        return c;
    }

    /** N.º 3: o funcionário atesta a recepção. */
    public void entregar(LocalDate data) {
        if (estado != Estado.EMITIDO) throw IgrpResponseStatusException.conflict("Este cartão já foi entregue ou já não está em uso.");
        if (data == null) throw invalido("Indique a data da entrega.");
        if (data.isBefore(emitidoEm)) throw invalido("O cartão não se entrega antes de ser emitido.");
        this.dataEntrega = data;
        this.estado = Estado.ENTREGUE;
    }

    /** Devolvido ao serviço (saída, mudança de categoria ou de função, substituição). */
    public void devolver(LocalDate data) {
        exigirEmUso();
        if (data == null) throw invalido("Indique a data da devolução.");
        this.dataDevolucao = data;
        this.estado = Estado.DEVOLVIDO;
    }

    /** Perda, extravio, substituição por outro. */
    public void anular(String motivo) {
        exigirEmUso();
        if (motivo == null || motivo.isBlank()) throw invalido("Anular o cartão exige o motivo.");
        this.motivoAnulacao = motivo.trim();
        this.estado = Estado.ANULADO;
    }

    public boolean emUso() {
        return estado == Estado.EMITIDO || estado == Estado.ENTREGUE;
    }

    /**
     * Porque deixou de valer, ou nulo se vale: n.º 5 — enquanto no quadro, na mesma função e categoria.
     * {@code activo} é o funcionário; a categoria e a função são as de hoje.
     */
    public String motivoInvalidade(boolean activo, String categoriaIdHoje, String funcaoIdHoje) {
        if (!emUso()) return "O cartão foi " + (estado == Estado.DEVOLVIDO ? "devolvido" : "anulado") + ".";
        if (!activo) return "O colaborador já não está ao serviço.";
        if (!Objects.equals(categoriaId, categoriaIdHoje)) return "O colaborador mudou de categoria: emita um cartão novo.";
        if (!Objects.equals(funcaoId, funcaoIdHoje)) return "O colaborador mudou de função: emita um cartão novo.";
        return null;
    }

    private void exigirEmUso() {
        if (!emUso()) throw IgrpResponseStatusException.conflict("Este cartão já não está em uso.");
    }

    private static IgrpResponseStatusException invalido(String m) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, m);
    }
}
