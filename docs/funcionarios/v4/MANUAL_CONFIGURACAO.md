# Valores de Configuração do Sistema RH

> SIPPROG — INGT, Cabo Verde · Versão 1.0 · Maio 2026

Este documento lista **todos os valores que têm de ser configurados** no sistema antes de ser possível registar funcionários. A ordem das secções deve ser respeitada.

---

## 1. Listas de Valores Simples (Opções / Lookups)

São as listas que aparecem nos menus de selecção (dropdowns) em todo o sistema.

---

### Género

| Chave | Valor |
|---|---|
| `M` | Masculino |
| `F` | Feminino |

---

### Estado Civil

| Chave | Valor |
|---|---|
| `SOLTEIRO` | Solteiro(a) |
| `CASADO` | Casado(a) |
| `DIVORCIADO` | Divorciado(a) |
| `VIUVO` | Viúvo(a) |
| `UNIAO_FACTO` | União de Facto |

---

### Nacionalidade

| Chave | Valor |
|---|---|
| `CV` | Cabo-verdiana |
| `PT` | Portuguesa |
| `SN` | Senegalesa |
| `GW` | Guineense |
| `BR` | Brasileira |
| `FR` | Francesa |
| `ES` | Espanhola |
| `US` | Americana |
| `OTHER` | Outra |

---

### Ilha

| Chave | Valor |
|---|---|
| `SANTIAGO` | Santiago |
| `SAO_VICENTE` | São Vicente |
| `SANTO_ANTAO` | Santo Antão |
| `SAL` | Sal |
| `BOA_VISTA` | Boa Vista |
| `SAO_NICOLAU` | São Nicolau |
| `FOGO` | Fogo |
| `MAIO` | Maio |
| `BRAVA` | Brava |

---

### Concelho

| Chave | Valor |
|---|---|
| `PRAIA` | Praia |
| `SAO_DOMINGOS` | São Domingos |
| `SANTA_CRUZ` | Santa Cruz |
| `TARRAFAL_STGO` | Tarrafal |
| `SANTA_CATARINA_STGO` | Santa Catarina |
| `SAO_SALVADOR` | São Salvador do Mundo |
| `SAO_LOURENCO` | São Lourenço dos Órgãos |
| `SAO_MIGUEL` | São Miguel |
| `RIBEIRA_GRANDE_STGO` | Ribeira Grande de Santiago |
| `SAO_VICENTE_C` | São Vicente |
| `PORTO_NOVO` | Porto Novo |
| `RIBEIRA_GRANDE_SA` | Ribeira Grande (Santo Antão) |
| `PAUL` | Paul |
| `SAL_C` | Sal |
| `BOA_VISTA_C` | Boa Vista |
| `MAIO_C` | Maio |
| `RIBEIRA_BRAVA_SN` | Ribeira Brava |
| `TARRAFAL_SN` | Tarrafal de São Nicolau |
| `SAO_FILIPE` | São Filipe |
| `SANTA_CATARINA_FOGO` | Santa Catarina do Fogo |
| `MOSTEIROS` | Mosteiros |
| `BRAVA_C` | Brava |

---

### Parentesco (Dependentes)

| Chave | Valor |
|---|---|
| `CONJUGE` | Cônjuge |
| `FILHO` | Filho(a) |
| `PAI` | Pai |
| `MAE` | Mãe |
| `ENTEADO` | Enteado(a) |
| `IRMAO` | Irmão / Irmã |
| `OUTRO` | Outro |

---

### Nível Académico

| Chave | Valor |
|---|---|
| `BASICO` | Ensino Básico |
| `SECUNDARIO` | Ensino Secundário |
| `BACHAREL` | Bacharelato |
| `LICENCIATURA` | Licenciatura |
| `MESTRADO` | Mestrado |
| `DOUTORAMENTO` | Doutoramento |
| `POS_DOUTORAMENTO` | Pós-doutoramento |

---

### Tipo de Formação Profissional

| Chave | Valor |
|---|---|
| `CURSO` | Curso |
| `SEMINARIO` | Seminário |
| `WORKSHOP` | Workshop |
| `CONGRESSO` | Congresso |
| `CONFERENCIA` | Conferência |
| `ELEARNING` | E-Learning |

---

### Categoria de Documento

| Chave | Valor |
|---|---|
| `IDENTIFICACAO` | Identificação |
| `CONTRATO` | Contrato |
| `FORMACAO` | Formação |
| `DISCIPLINAR` | Disciplinar |
| `MEDICO` | Médico |
| `FINANCEIRO` | Financeiro |
| `OUTRO` | Outro |

---

### Categoria de Ausência

| Chave | Valor |
|---|---|
| `GOZAMENTO` | Gozamento |
| `SAUDE` | Saúde |
| `FAMILIAR` | Familiar |
| `PESSOAL` | Pessoal |
| `LEGAL` | Legal |

---

### Tipo de Unidade Orgânica

| Chave | Valor |
|---|---|
| `MINISTRY` | Ministério |
| `DIRECTION` | Direcção Geral |
| `SERVICE` | Serviço |
| `SECTION` | Secção |

---

### Regime de Carreira

| Chave | Valor |
|---|---|
| `GERAL` | Regime Geral |
| `ESPECIAL` | Regime Especial |
| `DIRIGENTE` | Regime Dirigente |

---

### Banco

| Chave | Valor |
|---|---|
| `BCA` | Banco Comercial do Atlântico (BCA) |
| `BCN` | Banco Cabo-verdiano de Negócios (BCN) |
| `CAIXA_CV` | Caixa Económica de Cabo Verde |
| `ECOBANK` | EcoBank Cabo Verde |
| `NOVO_BANCO` | Novo Banco |

---

### Regime de Trabalho

| Chave | Valor |
|---|---|
| `TEMPO_COMPLETO` | Tempo Completo |
| `TEMPO_PARCIAL` | Tempo Parcial |
| `ISENCAO_HORARIO` | Isenção de Horário |
| `DEDICACAO_EXCLUSIVA` | Dedicação Exclusiva |

---

### Motivo de Mudança de Estado (`WORKER_STATE_REASON`)

> Usado no endpoint `PATCH /funcionarios/{id}/worker-state`. O campo `motivoCkey` é opcional mas recomendado para rastreabilidade.

| Chave | Valor |
|---|---|
| `DISCIPLINARY_SUSPENSION` | Suspensão Disciplinar |
| `MEDICAL_SUSPENSION` | Suspensão por Motivo de Saúde |
| `OWN_REQUEST_SUSPENSION` | Suspensão a Pedido Próprio |
| `AGE_RETIREMENT` | Aposentação por Limite de Idade |
| `DISABILITY_RETIREMENT` | Aposentação por Invalidez |
| `VOLUNTARY_RETIREMENT` | Aposentação Voluntária |
| `CONTRACT_TERMINATION` | Cessação de Contrato |
| `MUTUAL_AGREEMENT` | Rescisão por Acordo Mútuo |
| `DISCIPLINARY_DISMISSAL` | Demissão Disciplinar |
| `DEATH` | Falecimento |
| `SUSPENSION_RETURN` | Regresso de Suspensão |
| `REINTEGRATION` | Reintegração |

---

## 2. Estados do Trabalhador

> ⚠️ Os estados `ACTIVE` e `INACTIVE` são obrigatórios e protegidos — o sistema não funciona sem eles.

| Código | Descrição | Protegido |
|---|---|---|
| `ACTIVE` | Ativo | Sim |
| `INACTIVE` | Inativo | Sim |
| `SUSPENDED` | Suspenso | Não |
| `RETIRED` | Aposentado | Não |

---

## 3. Vínculos Laborais

| Código | Descrição | Conta antiguidade | Elegível para progressão |
|---|---|---|---|
| `EFETIVO` | Efetivo | Sim | Sim |
| `CONTRATADO` | Contratado | Sim | Sim |
| `COMISSIONADO` | Em Comissão de Serviço | Não | Não |
| `ESTAGIARIO` | Estagiário | Não | Não |
| `PRESTADOR` | Prestador de Serviços | Não | Não |

---

## 4. Tipos de Contrato

> Requer que os **Vínculos Laborais** (secção 3) já existam.

| Código | Descrição | Vínculo | Renovável | Máx. renovações | Duração máx. (meses) | Requer carreira PCFR |
|---|---|---|---|---|---|---|
| `NOMEACAO_DEFINITIVA` | Nomeação Definitiva | `EFETIVO` | Não | — | — | Sim |
| `NOMEACAO_PROVISORIA` | Nomeação Provisória | `EFETIVO` | Não | — | — | Sim |
| `CTFP_TERMO_CERTO` | CTFP a Termo Certo | `CONTRATADO` | Sim | 3 | 24 | Sim |
| `CTFP_TERMO_INCERTO` | CTFP a Termo Incerto | `CONTRATADO` | Não | — | — | Sim |
| `COMISSAO_SERVICO` | Comissão de Serviço | `COMISSIONADO` | Sim | — | 36 | Não |
| `ESTAGIO` | Estágio | `ESTAGIARIO` | Não | — | 12 | Não |
| `PRESTACAO_SERVICOS` | Prestação de Serviços | `PRESTADOR` | Sim | — | — | Não |

---

## 5. Tipos de Documento de Identificação

| Código | Descrição | Extensões aceites | Categoria |
|---|---|---|---|
| `BI` | Bilhete de Identidade | `pdf,jpg,png` | `IDENTIFICACAO` |
| `CNI` | Cartão Nacional de Identificação | `pdf,jpg,png` | `IDENTIFICACAO` |
| `PASS` | Passaporte | `pdf,jpg,png` | `IDENTIFICACAO` |
| `CRES` | Cartão de Residência | `pdf,jpg,png` | `IDENTIFICACAO` |

---

## 6. Tipos de Ausência (Curta Duração)

| Código | Descrição | Categoria | Desconta saldo | Requer aprovação | Máx. dias/ano |
|---|---|---|---|---|---|
| `FERIAS` | Férias | `GOZAMENTO` | Sim | Sim | 22 |
| `DOENCA` | Doença | `SAUDE` | Não | Não | — |
| `MATERNIDADE` | Licença de Maternidade | `FAMILIAR` | Não | Não | — |
| `PATERNIDADE` | Licença de Paternidade | `FAMILIAR` | Não | Não | — |
| `LUTO` | Licença de Luto | `FAMILIAR` | Não | Não | 5 |
| `FALTA_JUSTIFICADA` | Falta Justificada | `PESSOAL` | Sim | Não | — |
| `FALTA_INJUSTIFICADA` | Falta Injustificada | `PESSOAL` | Sim | Não | — |

---

## 7. Subtipos de Licença e Mobilidade (Longa Duração)

| Código | Descrição | Tipo | Afecta vencimento | Conta antiguidade | Próprio submete |
|---|---|---|---|---|---|
| `LIC_SEM_VENCIMENTO` | Licença sem Vencimento | Licença | Sim | Não | Não |
| `LIC_PARENTAL` | Licença Parental | Licença | Não | Sim | Não |
| `LIC_FORMACAO` | Licença para Formação | Licença | Não | Sim | Sim |
| `MOB_COMISSAO` | Comissão de Serviço | Mobilidade | Não | Sim | Não |
| `MOB_REQUISICAO` | Requisição | Mobilidade | Não | Sim | Não |
| `MOB_DESTACAMENTO` | Destacamento | Mobilidade | Não | Sim | Não |

---

## 8. Feriados Nacionais — 2026

> Feriados móveis: a Sexta-feira Santa e o Corpus Christi têm datas diferentes a cada ano.

| Data | Nome |
|---|---|
| 2026-01-01 | Ano Novo |
| 2026-01-13 | Dia da Liberdade e Democracia |
| 2026-01-20 | Dia dos Heróis Nacionais |
| 2026-04-03 | Sexta-feira Santa |
| 2026-05-01 | Dia do Trabalhador |
| 2026-06-01 | Dia da Criança |
| 2026-06-04 | Corpus Christi |
| 2026-07-05 | Dia da Independência |
| 2026-08-15 | Assunção de Nossa Senhora |
| 2026-11-01 | Dia de Todos os Santos |
| 2026-12-25 | Natal |

---

## 9. Estrutura Organizacional

> A hierarquia é criada de cima para baixo — a unidade-mãe tem de existir antes das suas filhas.

Exemplo de estrutura mínima:

| Código | Nome | Tipo | Unidade-mãe |
|---|---|---|---|
| `INGT` | Instituto Nacional de Gestão do Território | `MINISTRY` | *(nenhuma)* |
| `DRH` | Direcção de Recursos Humanos | `DIRECTION` | `INGT` |
| `DFORM` | Departamento de Formação | `SERVICE` | `DRH` |

Adapte esta estrutura à hierarquia real da sua organização.

---

## 10. Cargos

Adapte à sua organização. Exemplo:

| Código | Nome | Nível |
|---|---|---|
| `DIRECTOR_SERVICOS` | Director de Serviços | 1 |
| `CHEFE_DIVISAO` | Chefe de Divisão | 2 |
| `TEC_SUP` | Técnico Superior | 3 |
| `TEC_ADMIN` | Técnico Administrativo | 4 |
| `AUX_ADMIN` | Auxiliar Administrativo | 5 |

---

## 11. Funções

O campo **Cargo associado** é opcional — se preenchido, a função só aparece disponível para funcionários com esse cargo.

| Código | Nome | Cargo associado |
|---|---|---|
| `ANALISTA_SISTEMAS` | Analista de Sistemas | Técnico Superior |
| `COORD_PROJETO` | Coordenador de Projecto | Técnico Superior |
| `GESTOR_RH` | Gestor de Recursos Humanos | Técnico Superior |
| `SECRETARIADO` | Secretariado | *(qualquer)* |

---

## 12. Carreiras PCFR

> Apenas necessário para funcionários com grelha salarial (contratos com *Requer carreira PCFR = Sim*).

| Código | Nome | Regime |
|---|---|---|
| `TEC_SUP_I` | Técnico Superior I | `GERAL` |
| `ASSISTENTE_TEC` | Assistente Técnico | `GERAL` |
| `DIRIGENTE` | Dirigente | `DIRIGENTE` |

---

## 13. Categorias

> Requer que as **Carreiras** (secção 12) já existam. A ordem de progressão define a sequência de avanço na carreira (1 = entrada).

Exemplo para a carreira **Técnico Superior I**:

| Código | Nome | Carreira | Ordem de progressão |
|---|---|---|---|
| `TSA` | Técnico Superior Assistente | Técnico Superior I | 1 |
| `TSP` | Técnico Superior Principal | Técnico Superior I | 2 |
| `TSE` | Técnico Superior Especialista | Técnico Superior I | 3 |

---

## 14. Escalões

> Requer que as **Categorias** (secção 13) já existam.

Exemplo para a categoria **Técnico Superior Assistente (TSA)**:

| N.º | Código | Nome | Índice salarial | Salário base (CVE) |
|---|---|---|---|---|
| 1 | `TSA-1` | Escalão 1 | 100 | 45 000,00 |
| 2 | `TSA-2` | Escalão 2 | 110 | 49 500,00 |
| 3 | `TSA-3` | Escalão 3 | 120 | 54 000,00 |

Repita para todas as categorias de todas as carreiras, usando os índices e salários da grelha oficial do PCFR.

---

*Valores de Configuração — RH-Service v4.6 — SIPPROG/INGT · Maio 2026*
