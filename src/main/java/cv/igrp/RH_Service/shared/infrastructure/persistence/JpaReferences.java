package cv.igrp.RH_Service.shared.infrastructure.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.function.Function;

/**
 * Ponte entre a identidade do domínio e as associações JPA.
 *
 * <p>Os agregados do domínio referem-se uns aos outros por identidade tipada
 * (ver "Domain Identity Pattern" no CLAUDE.md), ao passo que as entidades de
 * persistência mapeiam essas referências como {@code @ManyToOne}. Os mappers
 * usam esta classe para traduzir nos dois sentidos sem carregar o agregado
 * referenciado da base de dados.
 */
@Component
public class JpaReferences {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Devolve uma referência lazy (proxy) para a entidade indicada, sem executar
     * SELECT — basta a chave estrangeira para gravar a associação.
     *
     * @param type entidade de persistência alvo da associação
     * @param id   identificador guardado no agregado do domínio
     * @return o proxy, ou {@code null} se {@code id} for nulo (associação opcional)
     */
    public <T> T ref(Class<T> type, UUID id) {
        return id != null ? entityManager.getReference(type, id) : null;
    }

    /**
     * Lê o identificador de uma associação. O Hibernate serve o identificador a
     * partir do próprio proxy, pelo que a leitura não o inicializa.
     *
     * @param association a associação {@code @ManyToOne}, possivelmente nula
     * @param id          acessor do identificador da entidade alvo
     * @return o identificador, ou {@code null} se a associação não estiver preenchida
     */
    public <T> UUID idOf(T association, Function<T, UUID> id) {
        return association != null ? id.apply(association) : null;
    }
}
