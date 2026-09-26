package cv.igrp.RH_Service.colaboradores.infrastructure.seed;

import cv.igrp.RH_Service.colaboradores.application.services.ChecklistService;
import cv.igrp.RH_Service.colaboradores.domain.models.ResponsavelChecklist;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoChecklist;
import cv.igrp.RH_Service.colaboradores.domain.repository.ChecklistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static cv.igrp.RH_Service.colaboradores.domain.models.ResponsavelChecklist.*;
import static cv.igrp.RH_Service.colaboradores.domain.models.TipoChecklist.ENTRADA;
import static cv.igrp.RH_Service.colaboradores.domain.models.TipoChecklist.SAIDA;

/**
 * <b>O modelo por omissão das checklists</b> (BR-CHK-02): só se o modelo está vazio, ao arrancar. A tabela nasce pelo
 * ddl-auto, depois do Flyway, por isso a semente é código e não migração. Idempotente: com o modelo preenchido não faz
 * nada, e o índice único (tipo, código) trava duas réplicas a semear ao mesmo tempo.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ModeloChecklistSeeder {

    private record Item(TipoChecklist tipo, String codigo, String descricao, ResponsavelChecklist responsavel, boolean obrigatorio,
                        Integer prazoDias) {}

    private static final Item[] ITENS = {
            new Item(ENTRADA, "DOCUMENTOS_PESSOAIS", "Recolher os documentos pessoais (identificação, NIF, habilitações, registo criminal)", RH, true, 5),
            new Item(ENTRADA, ChecklistService.PROVIMENTO, "Registar o provimento e a posse", RH, true, 0),
            new Item(ENTRADA, "DADOS_BANCARIOS", "Registar os dados bancários para o processamento", RH, true, 10),
            new Item(ENTRADA, "POSTO_TRABALHO", "Preparar o posto de trabalho e o equipamento", PATRIMONIO, true, 0),
            new Item(ENTRADA, "CONTAS_ACESSO", "Criar as contas de acesso (correio electrónico e sistemas)", INFORMATICA, true, 0),
            new Item(ENTRADA, "HORARIO", "Comunicar o horário e as regras de assiduidade", CHEFIA, true, 5),
            new Item(ENTRADA, "ACOLHIMENTO", "Acolhimento e integração na unidade (Lei n.º 20/X/2023, art. 141.º n.º 2)", CHEFIA, false, 15),
            new Item(ENTRADA, "DEVERES", "Tomar conhecimento dos deveres e das regras de ética", PROPRIO, true, 15),
            new Item(ENTRADA, ChecklistService.CARTAO_PROFISSIONAL, "Entregar o cartão de identificação profissional", RH, true, 30),
            new Item(SAIDA, "PASSAGEM_SERVICO", "Passagem de serviço e dos processos em curso", CHEFIA, true, 0),
            new Item(SAIDA, "DEVOLUCAO_BENS", "Devolver os bens e o equipamento confiados", PATRIMONIO, true, 0),
            new Item(SAIDA, ChecklistService.DEVOLUCAO_CARTAO, "Devolver o cartão de identificação profissional", RH, true, 0),
            new Item(SAIDA, "CONTAS_ACESSO", "Desactivar as contas de acesso", INFORMATICA, true, 1),
            new Item(SAIDA, "ACERTOS", "Apurar as férias não gozadas e os acertos para o processamento", RH, true, 5),
            new Item(SAIDA, "DECLARACAO_SERVICO", "Emitir a declaração de tempo de serviço, se pedida", RH, false, 15),
    };

    private final ChecklistRepository repository;
    private final ChecklistService service;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void semear() {
        if (repository.contarModelos() > 0) return;
        try {
            int ordem = 0;
            for (var i : ITENS)
                service.criarModelo(i.tipo(), i.codigo(), i.descricao(), i.responsavel(), i.obrigatorio(), i.prazoDias(), ++ordem);
            log.info("Modelo das checklists de entrada e saída semeado ({} itens).", ITENS.length);
        } catch (DataIntegrityViolationException e) {
            log.info("Modelo das checklists já semeado por outra instância.");
        }
    }
}
