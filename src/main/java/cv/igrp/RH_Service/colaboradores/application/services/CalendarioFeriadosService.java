package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.models.Feriado;
import cv.igrp.RH_Service.colaboradores.domain.repository.FeriadoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.estrutura.domain.models.OrganizationalUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * <b>O calendário de feriados que se aplica a um colaborador</b> (V55).
 *
 * <p>O calendário é da <b>instituição</b>, não da pessoa: ela cataloga os feriados nacionais e os
 * do sítio onde tem serviços, e conta-se tudo o que estiver activo. A única coisa que depende
 * do colaborador é <b>onde trabalha</b>, e só para as instituições que limitam feriados a uma
 * área:
 *
 * <ul>
 *   <li>a unidade é a de onde <b>exerce funções</b> no início do período — numa mobilidade
 *       interna, a de destino; sem mobilidade, a do seu Lugar;</li>
 *   <li>a área é a da unidade ou, se ela não a tiver, a da unidade-mãe mais próxima que tenha;</li>
 *   <li>numa mobilidade <b>externa</b>, ou sem Lugar, ou sem área em lado nenhum, a área é
 *       desconhecida e só contam os feriados sem área.</li>
 * </ul>
 *
 * <p><b>A direcção segura quando falta configuração:</b> conta-se só o que se sabe. Nunca se
 * desconta um dia que ninguém declarou feriado para aquele sítio.
 */
@Service
@RequiredArgsConstructor
public class CalendarioFeriadosService {

    private final FeriadoRepository feriadoRepository;
    private final UnidadeDeExercicioService unidadeDeExercicio;

    @Transactional(readOnly = true)
    public Set<LocalDate> feriadosDoColaborador(FuncionarioId funcionarioId, LocalDate inicio, LocalDate fim) {
        String area = unidadeDeExercicio.herdado(
                unidadeDeExercicio.unidadeOndeExerceFuncoes(funcionarioId, inicio), OrganizationalUnit::getAreaCkey);
        Set<LocalDate> datas = new HashSet<>();
        for (Feriado feriado : feriadoRepository.findAplicaveis(inicio, fim, area))
            feriado.ocorrenciasEntre(inicio, fim).forEach(datas::add);
        return datas;
    }
}
