---
title: Especificação Técnica
---

> **Módulo de Recursos Humanos**
>
> *Sistema Integrado de Planeamento, Procedimentos e Gestão (SIPPROG)*
>
> COUNTRY: Cabo Verde
>
> PROJECT: SIPPROG - Sistema de Informação para Modernização do INGT
>
> **Apresentado pelo Consultor:**

<img src="media/image1.jpeg" style="width:1.925in;height:0.5375in"
alt="Uma imagem com Tipo de letra, logótipo, Gráficos, branco Os conteúdos gerados por IA poderão estar incorretos." />

> *Abril 2026*

## Identificação do Documento

<table style="width:97%;">
<colgroup>
<col style="width: 36%" />
<col style="width: 60%" />
</colgroup>
<thead>
<tr>
<th><strong>Nº de Identificação do Documento</strong></th>
<th>ET-RH-BCK-V3.0</th>
</tr>
</thead>
<tbody>
<tr>
<td><strong>Dono</strong></td>
<td>INGT - Instituto Nacional de Gestão do Território</td>
</tr>
<tr>
<td><strong>Autor</strong></td>
<td><blockquote>
<p>TA Digital</p>
</blockquote></td>
</tr>
<tr>
<td><strong>Contribuintes</strong></td>
<td>Equipa de desenvolvimento TA, Departamento de RH do INGT</td>
</tr>
<tr>
<td><strong>Versão</strong></td>
<td>3.0</td>
</tr>
<tr>
<td><strong>Data de Criação</strong></td>
<td>Abril de 2026</td>
</tr>
<tr>
<td><strong>Status</strong></td>
<td>Draft</td>
</tr>
</tbody>
</table>

> **Histórico de Versões**

<table style="width:97%;">
<colgroup>
<col style="width: 9%" />
<col style="width: 15%" />
<col style="width: 15%" />
<col style="width: 57%" />
</colgroup>
<thead>
<tr>
<th><strong>Versão</strong></th>
<th><blockquote>
<p><strong>Data</strong></p>
</blockquote></th>
<th><strong>Autor</strong></th>
<th><strong>Descrição</strong></th>
</tr>
</thead>
<tbody>
<tr>
<td>1.0</td>
<td><blockquote>
<p>22-04-2026</p>
</blockquote></td>
<td>TA, digital</td>
<td>Reestruturação conforme menu SIPPROG: Cargos e Funções movidos para
Estrutura Organizacional; Mobilidade integrada em Colaboradores;
Avaliação de Desempenho tratada como ponto de integração externo.</td>
</tr>
</tbody>
</table>

## Índice

0.  Introdução

    1.  Visão Geral da API

    2.  Base URL e Versionamento

    3.  Autenticação e Perfis de Acesso

    4.  Convenções Gerais

1.  Módulo Meu Perfil

    1.  Visão Geral e Modelo de Autorização

    2.  Perfil do Colaborador

    3.  Os Meus Recibos

    4.  As Minhas Férias e Ausências

    5.  As Minhas Licenças e Mobilidades

    6.  As Minhas Avaliações (consumo do sistema externo)

    7.  Os Meus Documentos

    8.  Regras Específicas

2.  Módulo Colaboradores

    1.  Visão Geral

    2.  Funcionários (Employees) — CRUD

    3.  Pedidos de Ausência (Leave Requests)

    4.  Saldos de Ausências (Leave Balances)

    5.  Recibos de Vencimento (Payroll Slips)

    6.  Licenças e Mobilidade do Colaborador

    7.  Documentos do Colaborador

    8.  Enquadramento Profissional do Colaborador

    9.  Atribuição a Unidades Organizacionais

    10. Regras e Validações de Negócio

3.  Módulo Estrutura Organizacional

    1.  Visão Geral

    2.  Unidades Orgânicas (Organizational Units)

    3.  Cargos (Jobs)

    4.  Funções (Functions)

    5.  Regras Transversais

4.  Módulo Carreiras e Progressão

    1.  Visão Geral

    2.  Carreiras (Careers)

    3.  Categorias (Categories)

    4.  Escalões (Grades)

    5.  Regras Transversais

5.  Módulo Parametrizações

    1.  Visão Geral

    2.  Estados Civis

    3.  Estados do Trabalhador

    4.  Situações Profissionais

    5.  Tipos de Ausência

    6.  Subtipos de Licença/Mobilidade

    7.  Tipos de Documento

    8.  Endpoints Consolidados de Referência (/reference/\*)

    9.  Regras Transversais

6.  Integração com o Sistema de Avaliação de Desempenho

    1.  Visão Geral e Arquitetura de Integração

    2.  Endpoints de Leitura (Read-Through)

    3.  Sincronização de Catálogos (Webhooks)

    4.  Mapeamento de Identidades

    5.  Tratamento de Falhas e Cache

    6.  Segurança da Integração

7.  API de Anexos / Documentos

    1.  Visão Geral

    2.  Upload

    3.  Download

    4.  Listagem por Funcionário

8.  Modelo de Dados

    1.  Visão Geral

    2.  Diagrama de Entidades e Relacionamentos (ERD)

    3.  Descrição das Tabelas

    4.  Tabelas Externas Referenciadas (Avaliação de Desempenho)

9.  Triggers e Funções de Base de Dados

    1.  fn_audit_generic

    2.  fn_validate_professional_assignment

    3.  fn_apply_mobility

    4.  fn_set_updated_at

10. Considerações de Implementação

    1.  Migrations e Seed

    2.  Validação em Camadas

    3.  Soft Delete e Auditoria

    4.  Gestão de Ficheiros

    5.  Cálculo de Dias Úteis e Calendário

    6.  Estratégia de Testes

    7.  Observabilidade

    8.  Integrações Externas

# Introdução

## Visão Geral da API

> A API do Módulo de Recursos Humanos do SIPPROG (Sistema de Informação
> do Pessoal e Progressões) constitui o backend que disponibiliza as
> funcionalidades de gestão integrada de funcionários do INGT,
> organizada conforme a estrutura de menu do produto: Meu Perfil,
> Colaboradores, Estrutura Organizacional, Carreiras e Progressão e
> Parametrizações. A funcionalidade de Avaliação de Desempenho é externa
> ao módulo e é integrada por consumo, tal como descrito no Capítulo 6.
>
> A API foi concebida segundo o paradigma REST, utilizando JSON como
> formato de troca de dados, e disponibiliza operações para o cadastro
> de colaboradores, organização hierárquica em unidades orgânicas,
> definição de cargos e funções, gestão de carreiras (categorias e
> escalões), processos de ausência (férias, doença, licenças e
> mobilidade), recibos de vencimento e a área reservada do colaborador.
> Todas as operações de escrita são auditadas por triggers de base de
> dados que registam o histórico completo de alterações na tabela
> change_history.

## Base URL e Versionamento

> https://api.sipprog.ingt.gov.cv/v1/rh
>
> O versionamento da API é feito por prefixo no caminho (/v1, /v2, …).
> Versões maiores são introduzidas apenas quando há quebra de
> retrocompatibilidade. Alterações compatíveis (novos campos opcionais,
> novos endpoints) são entregues sem alteração da versão.

## Autenticação e Perfis de Acesso

> A autenticação é baseada em JSON Web Tokens (JWT) emitidos pelo módulo
> de autenticação central do SIPPROG. Todas as requisições devem incluir
> o cabeçalho Authorization com o token Bearer. O token transporta o
> identificador do utilizador (sub), o employee_id, o perfil de acesso e
> a unidade orgânica de origem, sendo essas claims utilizadas pelos
> middlewares de autorização em cada endpoint.
>
> Authorization: Bearer {token}
>
> **Os perfis de acesso considerados pela API são:**

| **Perfil** | **Descrição** |
|----|----|
| ROLE_FUNCIONARIO | Acesso à Área Reservada do Colaborador (/me/\*) e leituras restritas. |
| ROLE_CHEFIA | Aprova pedidos de ausência e licenças/mobilidades dos seus subordinados. |
| ROLE_HR_OPERATOR | Operações correntes sobre colaboradores, ausências e enquadramentos. |
| ROLE_HR_ADMIN | Operações administrativas plenas: parametrizações, estrutura, carreiras. |

| ROLE_SYSTEM_ADMIN | Configurações de sistema, integrações e auditoria. |
|-------------------|----------------------------------------------------|

4.  **Convenções Gerais**

<table style="width:97%;">
<colgroup>
<col style="width: 24%" />
<col style="width: 72%" />
</colgroup>
<thead>
<tr>
<th><strong>Aspecto</strong></th>
<th><strong>Convenção</strong></th>
</tr>
</thead>
<tbody>
<tr>
<td>Formato de data</td>
<td>ISO-8601 (YYYY-MM-DD para datas; YYYY-MM-DDTHH:mm:ss para timestamps
UTC).</td>
</tr>
<tr>
<td>Paginação</td>
<td>Parâmetros page (1-based) e size (default 20, máximo 100); resposta
inclui metadados totalElements e totalPages.</td>
</tr>
<tr>
<td>Ordenação</td>
<td>Parâmetro sort=campo,asc|desc; suporta múltiplos campos.</td>
</tr>
<tr>
<td>Códigos HTTP</td>
<td><p>200 OK, 201 Created, 204 No Content, 400 Bad Request, 401
Unauthorized, 403</p>
<p>Forbidden, 404 Not Found, 409 Conflict, 422 Unprocessable Entity, 500
Internal Error.</p></td>
</tr>
<tr>
<td>Erros</td>
<td>Resposta JSON com timestamp, status, error, message, path e fields[]
com erros de validação por campo.</td>
</tr>
<tr>
<td>Soft delete</td>
<td>Operações DELETE marcam is_active = false; não há remoção física
exposta.</td>
</tr>
<tr>
<td>Auditoria</td>
<td>Todas as escritas registam created_by/updated_by e geram entrada em
change_history via trigger.</td>
</tr>
</tbody>
</table>

# Módulo Meu Perfil

## Visão Geral e Modelo de Autorização

> O módulo Meu Perfil corresponde à área reservada do colaborador,
> acessível a partir do menu lateral do SIPPROG. Todos os endpoints
> estão sob o prefixo /me e exigem o perfil ROLE_FUNCIONARIO. O
> middleware de autenticação injeta automaticamente o employee_id a
> partir da claim sub do JWT, ignorando qualquer tentativa de envio de
> identificador de outro colaborador. Aplica-se sempre o princípio do
> mínimo privilégio: nenhum dado de terceiros é retornado.
>
> Para ações de submissão (pedido de ausência, atualização de objetivo
> via integração externa), aplicam- se as regras de negócio descritas no
> Capítulo 2.10. O acesso à área reservada exige is_active = TRUE;
> colaboradores INACTIVE recebem HTTP 403.

## Perfil do Colaborador

### Obter Perfil

> **GET** /me/profile
>
> Retorna a informação de cabeçalho apresentada na página de perfil:
> identificação pessoal, unidade orgânica principal, cargo e função
> atuais, dados de contacto e enquadramento profissional corrente.
>
> **Resposta (200 OK)**
>
> {
>
> "id": 42,
>
> "fullName": "Alex Jailson Barbosa Andrade", "initials": "AJ",
>
> "nif": "17361994",
>
> "email": "<Alex.Andrade@ingt.gov.cv>", "phone": "+238 261 2345",
>
> "currentJob": { "id": 7, "name": "Tecnico Superior" },
> "currentFunction": { "id": 12, "name": "Coordenador de Projeto" },
>
> "currentUnit": { "id": 5, "name": "Direccao de Servicos de Gestao
> Territorial" }, "career": { "id": 2, "name": "Nivel Tecnico I" },
>
> "category": { "id": 4, "name": "Tecnico Superior Principal" },
> "grade": { "id": 3, "gradeNumber": 3 },
>
> "admissionDate": "2018-09-01", "workerState": "ACTIVE"
>
> }

## Os Meus Recibos

### Listar Recibos

> **GET** /me/payroll-slips
>
> **Parâmetros de Consulta**

<table style="width:97%;">
<colgroup>
<col style="width: 24%" />
<col style="width: 15%" />
<col style="width: 15%" />
<col style="width: 42%" />
</colgroup>
<thead>
<tr>
<th><strong>Parâmetro</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Obrigatório</strong></p>
</blockquote></th>
<th><strong>Descrição</strong></th>
</tr>
</thead>
<tbody>
<tr>
<td>periodYear</td>
<td>integer</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Filtra por ano de referência.</td>
</tr>
<tr>
<td>periodMonth</td>
<td>integer</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Filtra por mês (1-12).</td>
</tr>
</tbody>
</table>

### Descarregar Recibo

> **GET** /me/payroll-slips/{id}/download
>
> Devolve o ficheiro PDF do recibo. Valida que o registo pertence ao
> colaborador autenticado.

## As Minhas Férias e Ausências

### Listar Pedidos

> **GET** /me/leave-requests
>
> **Parâmetros de Consulta**

<table style="width:97%;">
<colgroup>
<col style="width: 24%" />
<col style="width: 15%" />
<col style="width: 15%" />
<col style="width: 42%" />
</colgroup>
<thead>
<tr>
<th><strong>Parâmetro</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Obrigatório</strong></p>
</blockquote></th>
<th><strong>Descrição</strong></th>
</tr>
</thead>
<tbody>
<tr>
<td>status</td>
<td>string</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>PENDING, APPROVED, REJECTED, CANCELLED.</td>
</tr>
<tr>
<td>leaveTypeId</td>
<td>integer</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Filtra por tipo de ausência.</td>
</tr>
<tr>
<td>year</td>
<td>integer</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Filtra por ano.</td>
</tr>
</tbody>
</table>

### Os Meus Saldos

> **GET** /me/leave-balances
>
> Devolve, por tipo de ausência que desconte saldo, o total atribuído,
> utilizado, em pendentes e disponível para o ano corrente.

### Submeter Pedido de Ausência

> **POST** /me/leave-requests

### Cancelar Pedido Próprio

> **PUT** /me/leave-requests/{id}/cancel

## As Minhas Licenças e Mobilidades

### Listar

### 

> **GET** /me/leaves-mobilities
>
> Devolve a lista das licenças e mobilidades do próprio, ordenadas por
> data de início descendente. Permite consultar o estado de aprovação de
> cada registo.

### Detalhe

> **GET** /me/leaves-mobilities/{id}

### Submeter Pedido (quando aplicável)

> **POST** /me/leaves-mobilities
>
> Disponibilizado quando a parametrização do subtipo permite a
> auto-submissão pelo colaborador (canSelfSubmit = true). Caso
> contrário, o pedido tem de ser registado pelo RH.

## As Minhas Avaliações (consumo do sistema externo)

> Esta secção da Área Reservada é alimentada por integração com o
> Sistema de Avaliação de Desempenho (SAD), descrito no Capítulo 6. Os
> endpoints abaixo expõem uma vista agregada já normalizada, sem
> replicar os dados em base local.

### Histórico das Minhas Avaliações

> **GET** /me/external/evaluations
>
> **Resposta (200 OK)**
>
> \[
>
> {
>
> "externalId": "SAD-2025-0042", "cycle": "2025",
>
> "finalScore": 4.2,
>
> "maxScore": 5.0,
>
> "evaluator": "Carlos Rodrigues", "status": "Concluida", "source":
> "SAD"
>
> }
>
> \]

### Detalhe de Avaliação

> **GET** /me/external/evaluations/{externalId}

### Os Meus Objetivos (sistema externo)

> **GET** /me/external/objectives
>
> Devolve os objetivos do colaborador definidos no SAD para o ciclo
> corrente, com o respetivo estado de progresso. As atualizações de
> progresso são submetidas no próprio SAD; o SIPPROG não expõe escrita
> sobre estes recursos.

## Os Meus Documentos

### Listar

> **GET** /me/documents
>
> **Parâmetros de Consulta**

<table style="width:97%;">
<colgroup>
<col style="width: 24%" />
<col style="width: 15%" />
<col style="width: 15%" />
<col style="width: 42%" />
</colgroup>
<thead>
<tr>
<th><strong>Parâmetro</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Obrigatório</strong></p>
</blockquote></th>
<th><strong>Descrição</strong></th>
</tr>
</thead>
<tbody>
<tr>
<td>documentTypeId</td>
<td>integer</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Filtra por tipo.</td>
</tr>
</tbody>
</table>

### Descarregar

> **GET** /me/documents/{id}/download

8.  **Regras Específicas**

<table style="width:97%;">
<colgroup>
<col style="width: 33%" />
<col style="width: 63%" />
</colgroup>
<thead>
<tr>
<th><strong>Campos</strong></th>
<th><blockquote>
<p><strong>Descrição / Regras</strong></p>
</blockquote></th>
</tr>
</thead>
<tbody>
<tr>
<td>Isolamento de dados</td>
<td><blockquote>
<p>Todos os endpoints /me filtram por employee_id = sub do token, mesmo
que outro identificador seja enviado pelo cliente.</p>
</blockquote></td>
</tr>
<tr>
<td>Estado do colaborador</td>
<td><blockquote>
<p>is_active = TRUE é obrigatório; colaboradores INACTIVE recebem HTTP
403.</p>
</blockquote></td>
</tr>
<tr>
<td>Visibilidade de avaliações</td>
<td><blockquote>
<p>Apenas avaliações concluídas/homologadas no SAD são devolvidas;
rascunhos são omitidos.</p>
</blockquote></td>
</tr>
<tr>
<td>Visibilidade de recibos</td>
<td><blockquote>
<p>Apenas recibos com is_active = TRUE e issue_date ≤ hoje são</p>
<p>devolvidos.</p>
</blockquote></td>
</tr>
<tr>
<td>Submissões</td>
<td><blockquote>
<p>Aplicam-se as regras de negócio do Capítulo 2.10 (sobreposição,
saldo, anexos obrigatórios).</p>
</blockquote></td>
</tr>
<tr>
<td>Auditoria</td>
<td><blockquote>
<p>Todas as ações de escrita via /me são registadas em change_history
com origem 'self-service'.</p>
</blockquote></td>
</tr>
</tbody>
</table>

# Módulo Colaboradores

## Visão Geral

> O módulo Colaboradores agrega o conjunto de operações relativas aos
> funcionários do INGT, abrangendo o cadastro, a vida profissional
> (enquadramento e atribuições orgânicas), os processos de ausência e
> licença, os recibos de vencimento e os documentos pessoais. A
> funcionalidade de Mobilidade
>
> — que afeta diretamente o colaborador — é tratada como parte
> integrante deste módulo, registando-se como um tipo de evento na vida
> profissional do funcionário.
>
> Os endpoints CRUD obedecem aos padrões REST descritos no Capítulo 0.
> Operações de aprovação ou transição de estado são expostas como
> sub-recursos (por exemplo, /leave-requests/{id}/approve).

## Funcionários (Employees) — CRUD

### Listar Funcionários

> **GET** /employees
>
> **Parâmetros de Consulta**

<table style="width:97%;">
<colgroup>
<col style="width: 24%" />
<col style="width: 15%" />
<col style="width: 15%" />
<col style="width: 42%" />
</colgroup>
<thead>
<tr>
<th><strong>Parâmetro</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Obrigatório</strong></p>
</blockquote></th>
<th><strong>Descrição</strong></th>
</tr>
</thead>
<tbody>
<tr>
<td>search</td>
<td>string</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Pesquisa por nome, NIF ou número mecanográfico.</td>
</tr>
<tr>
<td>unitId</td>
<td>integer</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Filtra por unidade orgânica principal.</td>
</tr>
<tr>
<td>workerStateId</td>
<td>integer</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Filtra por estado do trabalhador.</td>
</tr>
<tr>
<td>careerId</td>
<td>integer</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Filtra por carreira atual.</td>
</tr>
<tr>
<td>isActive</td>
<td>boolean</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Filtra por estado lógico.</td>
</tr>
<tr>
<td>page</td>
<td>integer</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Página (default 1).</td>
</tr>
<tr>
<td>size</td>
<td>integer</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Tamanho da página (default 20, máx. 100).</td>
</tr>
<tr>
<td>sort</td>
<td>string</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Campo de ordenação. Ex.: fullName,asc.</td>
</tr>
</tbody>
</table>

> **Resposta (200 OK)**
>
> {
>
> "content": \[
>
> {
>
> "id": 42,
>
> "fullName": "Alex Jailson Barbosa Andrade", "nif": "17361994",
>
> "currentUnit": "Direccao de Servicos de Gestao Territorial",
>
> "currentJob": "Tecnico Superior", "workerState": "ACTIVE"

}

\],

> "page": 1,
>
> "size": 20,
>
> "totalElements": 132,
>
> "totalPages": 7
>
> }

### Obter Funcionário

> **GET** /employees/{id}
>
> Devolve o detalhe completo, incluindo enquadramento corrente, unidade
> principal e contactos.

### Criar Funcionário

> **POST** /employees
>
> **Corpo da Requisição**

<table style="width:97%;">
<colgroup>
<col style="width: 24%" />
<col style="width: 15%" />
<col style="width: 15%" />
<col style="width: 42%" />
</colgroup>
<thead>
<tr>
<th><strong>Parâmetro</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Obrigatório</strong></p>
</blockquote></th>
<th><strong>Descrição</strong></th>
</tr>
</thead>
<tbody>
<tr>
<td>fullName</td>
<td>string</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Nome completo.</td>
</tr>
<tr>
<td>nif</td>
<td>string</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Número de identificação fiscal único.</td>
</tr>
<tr>
<td>birthDate</td>
<td>date</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Data de nascimento.</td>
</tr>
<tr>
<td>sex</td>
<td>string</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>M ou F.</td>
</tr>
<tr>
<td>maritalStatusId</td>
<td>integer</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Estado civil (referência).</td>
</tr>
<tr>
<td>nationality</td>
<td>string</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Nacionalidade.</td>
</tr>
<tr>
<td>admissionDate</td>
<td>date</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Data de admissão.</td>
</tr>
<tr>
<td>workerStateId</td>
<td>integer</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Estado do trabalhador (default ACTIVE).</td>
</tr>
<tr>
<td>professionalSituationId</td>
<td>integer</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Situação profissional.</td>
</tr>
<tr>
<td>email</td>
<td>string</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Email institucional.</td>
</tr>
<tr>
<td>phone</td>
<td>string</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Telefone.</td>
</tr>
<tr>
<td>nib</td>
<td>string</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>NIB para pagamentos.</td>
</tr>
</tbody>
</table>

### Atualizar Funcionário

> **PUT** /employees/{id}
>
> O campo nif é imutável após a criação. As restantes propriedades são
> atualizáveis.

### Inativar Funcionário

> **DELETE** /employees/{id}
>
> Soft delete (is_active = false). Bloqueado se existirem pedidos
> PENDING.

## Pedidos de Ausência (Leave Requests)

> Suporta o ciclo de vida completo dos pedidos de ausência: submissão,
> aprovação ou rejeição pela chefia, e cancelamento. O sistema valida
> saldo, sobreposição e calcula automaticamente o número de dias úteis
> tendo em conta os feriados configurados.

### Listar Pedidos

> **GET** /leave-requests
>
> **Parâmetros de Consulta**

<table style="width:97%;">
<colgroup>
<col style="width: 24%" />
<col style="width: 15%" />
<col style="width: 15%" />
<col style="width: 42%" />
</colgroup>
<thead>
<tr>
<th><strong>Parâmetro</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Obrigatório</strong></p>
</blockquote></th>
<th><strong>Descrição</strong></th>
</tr>
</thead>
<tbody>
<tr>
<td>employeeId</td>
<td>integer</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Filtra por funcionário.</td>
</tr>
<tr>
<td>approverId</td>
<td>integer</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Filtra por aprovador.</td>
</tr>
<tr>
<td>status</td>
<td>string</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>PENDING, APPROVED, REJECTED, CANCELLED.</td>
</tr>
<tr>
<td>leaveTypeId</td>
<td>integer</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Filtra por tipo.</td>
</tr>
<tr>
<td>startDateFrom</td>
<td>date</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Data inicial mínima.</td>
</tr>
<tr>
<td>startDateTo</td>
<td>date</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Data inicial máxima.</td>
</tr>
</tbody>
</table>

### Obter Pedido

> **GET** /leave-requests/{id}

### Criar Pedido

> **POST** /leave-requests
>
> **Corpo da Requisição**

<table style="width:97%;">
<colgroup>
<col style="width: 24%" />
<col style="width: 15%" />
<col style="width: 15%" />
<col style="width: 42%" />
</colgroup>
<thead>
<tr>
<th><strong>Parâmetro</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Obrigatório</strong></p>
</blockquote></th>
<th><strong>Descrição</strong></th>
</tr>
</thead>
<tbody>
<tr>
<td>employeeId</td>
<td>integer</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Funcionário requerente.</td>
</tr>
<tr>
<td>leaveTypeId</td>
<td>integer</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Tipo de ausência.</td>
</tr>
</tbody>
</table>

<table style="width:97%;">
<colgroup>
<col style="width: 24%" />
<col style="width: 15%" />
<col style="width: 15%" />
<col style="width: 42%" />
</colgroup>
<thead>
<tr>
<th>startDate</th>
<th>date</th>
<th><blockquote>
<p>Sim</p>
</blockquote></th>
<th>Data de início.</th>
</tr>
</thead>
<tbody>
<tr>
<td>endDate</td>
<td>date</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Data de fim.</td>
</tr>
<tr>
<td>justification</td>
<td>string</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Justificação textual.</td>
</tr>
<tr>
<td>attachmentDocumentId</td>
<td>integer</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Documento de suporte previamente carregado.</td>
</tr>
</tbody>
</table>

### Aprovar Pedido

> **PUT** /leave-requests/{id}/approve

### Rejeitar Pedido

> **PUT** /leave-requests/{id}/reject
>
> **Corpo da Requisição**

<table style="width:97%;">
<colgroup>
<col style="width: 24%" />
<col style="width: 15%" />
<col style="width: 15%" />
<col style="width: 42%" />
</colgroup>
<thead>
<tr>
<th><strong>Parâmetro</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Obrigatório</strong></p>
</blockquote></th>
<th><strong>Descrição</strong></th>
</tr>
</thead>
<tbody>
<tr>
<td>rejectionReason</td>
<td>string</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Justificação obrigatória da rejeição.</td>
</tr>
</tbody>
</table>

### Cancelar Pedido

> **PUT** /leave-requests/{id}/cancel

## Saldos de Ausências (Leave Balances)

### Listar Saldos do Funcionário

> **GET** /employees/{employeeId}/leave-balances
>
> **Parâmetros de Consulta**

<table style="width:97%;">
<colgroup>
<col style="width: 24%" />
<col style="width: 15%" />
<col style="width: 15%" />
<col style="width: 42%" />
</colgroup>
<thead>
<tr>
<th><strong>Parâmetro</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Obrigatório</strong></p>
</blockquote></th>
<th><strong>Descrição</strong></th>
</tr>
</thead>
<tbody>
<tr>
<td>year</td>
<td>integer</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Ano de referência (default ano corrente).</td>
</tr>
</tbody>
</table>

### Ajuste Manual de Saldo

> **PUT** /employees/{employeeId}/leave-balances/{balanceId}
>
> Permite ao RH ajustar o saldo (assignedDays e usedDays). Operação
> auditada.

## Recibos de Vencimento (Payroll Slips)

## 

### Listar Recibos do Funcionário

> **GET** /employees/{employeeId}/payroll-slips

### Obter Recibo

> **GET** /payroll-slips/{id}

### Registar Recibo

> **POST** /employees/{employeeId}/payroll-slips
>
> **Corpo da Requisição**

<table style="width:97%;">
<colgroup>
<col style="width: 24%" />
<col style="width: 15%" />
<col style="width: 15%" />
<col style="width: 42%" />
</colgroup>
<thead>
<tr>
<th><strong>Parâmetro</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Obrigatório</strong></p>
</blockquote></th>
<th><strong>Descrição</strong></th>
</tr>
</thead>
<tbody>
<tr>
<td>periodMonth</td>
<td>integer</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Mês de referência (1-12).</td>
</tr>
<tr>
<td>periodYear</td>
<td>integer</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Ano de referência.</td>
</tr>
<tr>
<td>issueDate</td>
<td>date</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Data de emissão.</td>
</tr>
<tr>
<td>grossSalary</td>
<td>number</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Salário ilíquido.</td>
</tr>
<tr>
<td>netSalary</td>
<td>number</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Salário líquido.</td>
</tr>
<tr>
<td>documentId</td>
<td>integer</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Identificador do PDF do recibo.</td>
</tr>
</tbody>
</table>

## Licenças e Mobilidade do Colaborador

> Gestão integrada das licenças (sem vencimento, para formação,
> parental, etc.) e mobilidades (comissão de serviço, requisição,
> destacamento, mobilidade interna). Embora consolidadas num único
> recurso, o campo recordType discrimina o caso e o subtipo determina o
> comportamento detalhado.

### Listar

> **GET** /leaves-mobilities
>
> **Parâmetros de Consulta**

<table style="width:97%;">
<colgroup>
<col style="width: 24%" />
<col style="width: 15%" />
<col style="width: 15%" />
<col style="width: 42%" />
</colgroup>
<thead>
<tr>
<th><strong>Parâmetro</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Obrigatório</strong></p>
</blockquote></th>
<th><strong>Descrição</strong></th>
</tr>
</thead>
<tbody>
<tr>
<td>employeeId</td>
<td>integer</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Filtra por colaborador.</td>
</tr>
<tr>
<td>recordType</td>
<td>string</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>LICENCA ou MOBILIDADE.</td>
</tr>
<tr>
<td>status</td>
<td>string</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>PENDING, APPROVED, REJECTED, CLOSED, CANCELLED.</td>
</tr>
</tbody>
</table>

<table style="width:97%;">
<colgroup>
<col style="width: 24%" />
<col style="width: 15%" />
<col style="width: 15%" />
<col style="width: 42%" />
</colgroup>
<thead>
<tr>
<th>subtypeId</th>
<th>integer</th>
<th><blockquote>
<p>Não</p>
</blockquote></th>
<th>Filtra por subtipo.</th>
</tr>
</thead>
<tbody>
</tbody>
</table>

### Obter Registo

> **GET** /leaves-mobilities/{id}

### Registar

> **POST** /leaves-mobilities
>
> **Corpo da Requisição**

<table style="width:97%;">
<colgroup>
<col style="width: 24%" />
<col style="width: 15%" />
<col style="width: 15%" />
<col style="width: 42%" />
</colgroup>
<thead>
<tr>
<th><strong>Parâmetro</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Obrigatório</strong></p>
</blockquote></th>
<th><strong>Descrição</strong></th>
</tr>
</thead>
<tbody>
<tr>
<td>employeeId</td>
<td>integer</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Colaborador alvo.</td>
</tr>
<tr>
<td>recordType</td>
<td>string</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>LICENCA ou MOBILIDADE.</td>
</tr>
<tr>
<td>subtypeId</td>
<td>integer</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Subtipo (ver /reference/leave-mobility- subtypes).</td>
</tr>
<tr>
<td>startDate</td>
<td>date</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Data de início.</td>
</tr>
<tr>
<td>endDate</td>
<td>date</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Data de fim (opcional p/ situações indefinidas; obrigatória se
isTemporary=true).</td>
</tr>
<tr>
<td>isTemporary</td>
<td>boolean</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Indica se há reversão prevista.</td>
</tr>
<tr>
<td>targetUnitId</td>
<td>integer</td>
<td><blockquote>
<p>Condicional</p>
</blockquote></td>
<td>Unidade de destino (obrigatório em mobilidade externa interna).</td>
</tr>
<tr>
<td>targetFunctionId</td>
<td>integer</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Função de destino (em mobilidade).</td>
</tr>
<tr>
<td>legalBase</td>
<td>string</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Despacho ou base legal.</td>
</tr>
<tr>
<td>justification</td>
<td>string</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Justificação.</td>
</tr>
<tr>
<td>attachmentDocumentId</td>
<td>integer</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Documento de suporte.</td>
</tr>
</tbody>
</table>

### Atualizar

> **PUT** /leaves-mobilities/{id}

### Aprovar

> **PUT** /leaves-mobilities/{id}/approve
>
> Ao aprovar uma mobilidade, a função fn_apply_mobility encerra a
> atribuição organizacional anterior e cria a nova, em
> employee_unit_assignments, com base na targetUnitId e
> targetFunctionId.

### Rejeitar

> **PUT** /leaves-mobilities/{id}/reject

### Encerrar

> **PUT** /leaves-mobilities/{id}/close
>
> Encerra o registo (estado CLOSED). Em mobilidades temporárias,
> restaura a atribuição organizacional anterior do colaborador.

### Cancelar

> **PUT** /leaves-mobilities/{id}/cancel

### Histórico do Colaborador

> **GET** /employees/{employeeId}/leaves-mobilities

### Anexar Documento

> **POST** /leaves-mobilities/{id}/attachments

## Documentos do Colaborador

> Gestão dos documentos pessoais associados ao funcionário (CNI,
> contratos, certidões). Ver Capítulo 7 para detalhe da API genérica de
> Anexos.

### Listar

> **GET** /employees/{employeeId}/documents

### Anexar

> **POST** /employees/{employeeId}/documents

## Enquadramento Profissional do Colaborador

> Materializa a relação entre o funcionário e a sua carreira, categoria,
> escalão e função num dado período. O sistema mantém o histórico
> completo, garantindo apenas um enquadramento ativo (is_current = true)
> por funcionário num dado instante. As validações asseguram a
> consistência hierárquica entre carreira, categoria e escalão através
> do trigger fn_validate_professional_assignment.

### Listar Enquadramentos

> **GET** /employees/{employeeId}/professional-assignments

### Criar Enquadramento (Promoção / Reclassificação)

> **POST** /employees/{employeeId}/professional-assignments
>
> **Corpo da Requisição**

<table style="width:97%;">
<colgroup>
<col style="width: 24%" />
<col style="width: 15%" />
<col style="width: 15%" />
<col style="width: 42%" />
</colgroup>
<thead>
<tr>
<th><strong>Parâmetro</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Obrigatório</strong></p>
</blockquote></th>
<th><strong>Descrição</strong></th>
</tr>
</thead>
<tbody>
<tr>
<td>careerId</td>
<td>integer</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Carreira.</td>
</tr>
<tr>
<td>categoryId</td>
<td>integer</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Categoria (deve pertencer à carreira).</td>
</tr>
<tr>
<td>gradeId</td>
<td>integer</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Escalão (deve pertencer à categoria).</td>
</tr>
<tr>
<td>functionId</td>
<td>integer</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Função exercida.</td>
</tr>
<tr>
<td>startDate</td>
<td>date</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Data de início (&gt;= admissão).</td>
</tr>
<tr>
<td>legalBase</td>
<td>string</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Despacho ou base legal da progressão.</td>
</tr>
</tbody>
</table>

> Ao criar um novo enquadramento, o sistema encerra automaticamente o
> anterior (end_date = startDate
>
> − 1 dia).

### Atualizar Enquadramento

> **PUT**
> /employees/{employeeId}/professional-assignments/{assignmentId}

## Atribuição a Unidades Organizacionais

> Um colaborador pode estar atribuído a uma ou mais unidades, sendo uma
> delas obrigatoriamente marcada como principal (isPrimary = true). As
> atribuições têm vigência (startDate, endDate).

### Listar Atribuições

> **GET** /employees/{employeeId}/unit-assignments

### Criar Atribuição

> **POST** /employees/{employeeId}/unit-assignments
>
> **Corpo da Requisição**

<table style="width:97%;">
<colgroup>
<col style="width: 24%" />
<col style="width: 15%" />
<col style="width: 15%" />
<col style="width: 42%" />
</colgroup>
<thead>
<tr>
<th><strong>Parâmetro</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Obrigatório</strong></p>
</blockquote></th>
<th><strong>Descrição</strong></th>
</tr>
</thead>
<tbody>
<tr>
<td>unitId</td>
<td>integer</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Unidade orgânica.</td>
</tr>
<tr>
<td>isPrimary</td>
<td>boolean</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Atribuição principal.</td>
</tr>
<tr>
<td>startDate</td>
<td>date</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Data de início.</td>
</tr>
</tbody>
</table>

<table style="width:97%;">
<colgroup>
<col style="width: 24%" />
<col style="width: 15%" />
<col style="width: 15%" />
<col style="width: 42%" />
</colgroup>
<thead>
<tr>
<th>endDate</th>
<th>date</th>
<th><blockquote>
<p>Não</p>
</blockquote></th>
<th>Data de fim (vazio se em curso).</th>
</tr>
</thead>
<tbody>
</tbody>
</table>

### Encerrar Atribuição

> **PUT** /employees/{employeeId}/unit-assignments/{id}/close

## Regras e Validações de Negócio

1.  **Criação de Funcionário**

<table style="width:97%;">
<colgroup>
<col style="width: 33%" />
<col style="width: 63%" />
</colgroup>
<thead>
<tr>
<th><strong>Campos</strong></th>
<th><blockquote>
<p><strong>Descrição / Regras</strong></p>
</blockquote></th>
</tr>
</thead>
<tbody>
<tr>
<td>nif</td>
<td><blockquote>
<p>Único no sistema. Validação de formato.</p>
</blockquote></td>
</tr>
<tr>
<td>nib</td>
<td><blockquote>
<p>Único. Validação de comprimento (21 dígitos) e formato.</p>
</blockquote></td>
</tr>
<tr>
<td>birthDate</td>
<td><blockquote>
<p>Funcionário maior de 18 anos.</p>
</blockquote></td>
</tr>
<tr>
<td>admissionDate</td>
<td><blockquote>
<p>Não pode ser superior à data corrente.</p>
</blockquote></td>
</tr>
<tr>
<td>sex</td>
<td><blockquote>
<p>Apenas M ou F.</p>
</blockquote></td>
</tr>
</tbody>
</table>

2.  **Enquadramento Profissional**

<table style="width:97%;">
<colgroup>
<col style="width: 33%" />
<col style="width: 63%" />
</colgroup>
<thead>
<tr>
<th><strong>Campos</strong></th>
<th><blockquote>
<p><strong>Descrição / Regras</strong></p>
</blockquote></th>
</tr>
</thead>
<tbody>
<tr>
<td>categoryId</td>
<td><blockquote>
<p>Deve pertencer à carreira indicada (validado por trigger).</p>
</blockquote></td>
</tr>
<tr>
<td>gradeId</td>
<td><blockquote>
<p>Deve pertencer à categoria indicada.</p>
</blockquote></td>
</tr>
<tr>
<td>startDate</td>
<td><blockquote>
<p>Não pode ser anterior à admissão.</p>
</blockquote></td>
</tr>
<tr>
<td>Único corrente</td>
<td><blockquote>
<p>Apenas um is_current = true por funcionário; o anterior é encerrado
automaticamente.</p>
</blockquote></td>
</tr>
</tbody>
</table>

3.  **Pedido de Ausência**

<table style="width:97%;">
<colgroup>
<col style="width: 33%" />
<col style="width: 63%" />
</colgroup>
<thead>
<tr>
<th><strong>Campos</strong></th>
<th><blockquote>
<p><strong>Descrição / Regras</strong></p>
</blockquote></th>
</tr>
</thead>
<tbody>
<tr>
<td>startDate / endDate</td>
<td><blockquote>
<p>endDate ≥ startDate. Cálculo de dias úteis exclui sábados, domingos
e</p>
<p>feriados configurados.</p>
</blockquote></td>
</tr>
<tr>
<td>Sobreposição</td>
<td><blockquote>
<p>Não permitidos pedidos sobrepostos para o mesmo funcionário, exceto
CANCELLED ou REJECTED.</p>
</blockquote></td>
</tr>
<tr>
<td>Saldo</td>
<td><blockquote>
<p>Para tipos com deducts_balance = true, dias solicitados ≤</p>
<p>availableDays do saldo.</p>
</blockquote></td>
</tr>
</tbody>
</table>

<table style="width:97%;">
<colgroup>
<col style="width: 33%" />
<col style="width: 63%" />
</colgroup>
<thead>
<tr>
<th>Aprovação</th>
<th><blockquote>
<p>Apenas tipos com requires_approval = true requerem aprovação;
restantes vão direto para APPROVED.</p>
</blockquote></th>
</tr>
</thead>
<tbody>
<tr>
<td>Anexos</td>
<td><blockquote>
<p>Tipos como DOENCA exigem anexo.</p>
</blockquote></td>
</tr>
</tbody>
</table>

4.  **Inativação de Funcionário**

<table style="width:97%;">
<colgroup>
<col style="width: 33%" />
<col style="width: 63%" />
</colgroup>
<thead>
<tr>
<th><strong>Campos</strong></th>
<th><blockquote>
<p><strong>Descrição / Regras</strong></p>
</blockquote></th>
</tr>
</thead>
<tbody>
<tr>
<td>Pedidos pendentes</td>
<td><blockquote>
<p>Não permitido se existirem pedidos PENDING.</p>
</blockquote></td>
</tr>
<tr>
<td>Soft delete</td>
<td><blockquote>
<p>Inativação lógica (is_active = false) preservando histórico.</p>
</blockquote></td>
</tr>
</tbody>
</table>

5.  **Recibos**

<table style="width:97%;">
<colgroup>
<col style="width: 33%" />
<col style="width: 63%" />
</colgroup>
<thead>
<tr>
<th><strong>Campos</strong></th>
<th><blockquote>
<p><strong>Descrição / Regras</strong></p>
</blockquote></th>
</tr>
</thead>
<tbody>
<tr>
<td>Unicidade</td>
<td><blockquote>
<p>(employee_id, period_month, period_year) único.</p>
</blockquote></td>
</tr>
<tr>
<td>periodMonth</td>
<td><blockquote>
<p>Inteiro entre 1 e 12.</p>
</blockquote></td>
</tr>
<tr>
<td>Salários</td>
<td><blockquote>
<p>grossSalary &gt; 0; netSalary &gt; 0; netSalary ≤ grossSalary.</p>
</blockquote></td>
</tr>
</tbody>
</table>

6.  **Licenças e Mobilidade**

<table style="width:97%;">
<colgroup>
<col style="width: 33%" />
<col style="width: 63%" />
</colgroup>
<thead>
<tr>
<th><strong>Campos</strong></th>
<th><blockquote>
<p><strong>Descrição / Regras</strong></p>
</blockquote></th>
</tr>
</thead>
<tbody>
<tr>
<td>recordType + subtypeId</td>
<td><blockquote>
<p>O subtipo deve pertencer ao tipo selecionado.</p>
</blockquote></td>
</tr>
<tr>
<td>Período</td>
<td><blockquote>
<p>startDate obrigatória; endDate condicional a isTemporary.</p>
</blockquote></td>
</tr>
<tr>
<td>Sobreposição</td>
<td><blockquote>
<p>Não permitidas licenças sobrepostas do mesmo tipo.</p>
</blockquote></td>
</tr>
<tr>
<td>Integração orgânica</td>
<td><blockquote>
<p>Aprovação de mobilidade não temporária encerra a atribuição anterior
em employee_unit_assignments.</p>
</blockquote></td>
</tr>
<tr>
<td>Reativação</td>
<td><blockquote>
<p>Encerramento de mobilidade temporária restaura a atribuição
anterior.</p>
</blockquote></td>
</tr>
</tbody>
</table>

# Módulo Estrutura Organizacional

## Visão Geral

> Este módulo gere a base estrutural sobre a qual o INGT organiza os
> seus recursos humanos: as Unidades Orgânicas (organigrama), os Cargos
> (designações oficiais como Diretor ou Coordenador) e as Funções
> (papéis efetivamente exercidos). Estes três catálogos são consumidos
> pelos módulos de Colaboradores e Carreiras nos enquadramentos e
> atribuições.

## Unidades Orgânicas (Organizational Units)

> Suporta a modelação de Direções, Departamentos, Divisões e Secções de
> forma encadeada através do campo parentUnitId. Funcionários são
> atribuídos a uma ou mais unidades, sendo uma delas a principal.

### Listar

> **GET** /organizational-units
>
> **Parâmetros de Consulta**

<table style="width:97%;">
<colgroup>
<col style="width: 24%" />
<col style="width: 15%" />
<col style="width: 15%" />
<col style="width: 42%" />
</colgroup>
<thead>
<tr>
<th><strong>Parâmetro</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Obrigatório</strong></p>
</blockquote></th>
<th><strong>Descrição</strong></th>
</tr>
</thead>
<tbody>
<tr>
<td>parentUnitId</td>
<td>integer</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Filtra unidades filhas de uma unidade-pai.</td>
</tr>
<tr>
<td>unitType</td>
<td>string</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>DIRECAO, DEPARTAMENTO, DIVISAO, SECCAO.</td>
</tr>
<tr>
<td>isActive</td>
<td>boolean</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Filtra por estado.</td>
</tr>
</tbody>
</table>

### Obter

> **GET** /organizational-units/{id}

### Criar

> **POST** /organizational-units
>
> **Corpo da Requisição**

<table style="width:97%;">
<colgroup>
<col style="width: 24%" />
<col style="width: 15%" />
<col style="width: 15%" />
<col style="width: 42%" />
</colgroup>
<thead>
<tr>
<th><strong>Parâmetro</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Obrigatório</strong></p>
</blockquote></th>
<th><strong>Descrição</strong></th>
</tr>
</thead>
<tbody>
<tr>
<td>code</td>
<td>string</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Código único e estável.</td>
</tr>
<tr>
<td>name</td>
<td>string</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Designação oficial.</td>
</tr>
<tr>
<td>acronym</td>
<td>string</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Sigla.</td>
</tr>
</tbody>
</table>

<table style="width:97%;">
<colgroup>
<col style="width: 24%" />
<col style="width: 15%" />
<col style="width: 15%" />
<col style="width: 42%" />
</colgroup>
<thead>
<tr>
<th>unitType</th>
<th>string</th>
<th><blockquote>
<p>Sim</p>
</blockquote></th>
<th>Tipo (DIRECAO, DEPARTAMENTO, DIVISAO, SECCAO).</th>
</tr>
</thead>
<tbody>
<tr>
<td>parentUnitId</td>
<td>integer</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Unidade-pai (nulo para topo).</td>
</tr>
</tbody>
</table>

### Atualizar

> **PUT** /organizational-units/{id}

### Desativar

> **DELETE** /organizational-units/{id}
>
> Soft delete. Bloqueado se existirem unidades filhas ativas ou
> colaboradores atribuídos.

## Cargos (Jobs)

> Designação oficial atribuída ao funcionário (ex.: Diretor de Serviços,
> Coordenador, Técnico). Cada cargo é identificado por um código único.

### Listar

> **GET** /jobs

### Obter

> **GET** /jobs/{id}

### Criar

> **POST** /jobs
>
> **Corpo da Requisição**

<table style="width:97%;">
<colgroup>
<col style="width: 24%" />
<col style="width: 15%" />
<col style="width: 15%" />
<col style="width: 42%" />
</colgroup>
<thead>
<tr>
<th><strong>Parâmetro</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Obrigatório</strong></p>
</blockquote></th>
<th><strong>Descrição</strong></th>
</tr>
</thead>
<tbody>
<tr>
<td>code</td>
<td>string</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Código único (máx. 50).</td>
</tr>
<tr>
<td>name</td>
<td>string</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Designação (máx. 150).</td>
</tr>
<tr>
<td>description</td>
<td>string</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Descrição.</td>
</tr>
<tr>
<td>isActive</td>
<td>boolean</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Estado inicial (default true).</td>
</tr>
</tbody>
</table>

### Atualizar

> **PUT** /jobs/{id}

### Desativar

> **DELETE** /jobs/{id}
>
> Soft delete. Rejeitado se associado a atribuições profissionais
> ativas.

## Funções (Functions)

> Função efetivamente exercida pelo colaborador (ex.: Técnico Superior,
> Assistente Técnico, Auxiliar). Estrutura idêntica à dos Cargos.

### Listar

> **GET** /functions

### Obter

> **GET** /functions/{id}

### Criar

> **POST** /functions
>
> **Corpo da Requisição**

<table style="width:97%;">
<colgroup>
<col style="width: 24%" />
<col style="width: 15%" />
<col style="width: 15%" />
<col style="width: 42%" />
</colgroup>
<thead>
<tr>
<th><strong>Parâmetro</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Obrigatório</strong></p>
</blockquote></th>
<th><strong>Descrição</strong></th>
</tr>
</thead>
<tbody>
<tr>
<td>code</td>
<td>string</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Código único (máx. 50).</td>
</tr>
<tr>
<td>name</td>
<td>string</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Designação (máx. 150).</td>
</tr>
<tr>
<td>description</td>
<td>string</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Descrição.</td>
</tr>
<tr>
<td>isActive</td>
<td>boolean</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Estado inicial (default true).</td>
</tr>
</tbody>
</table>

### Atualizar

> **PUT** /functions/{id}

### Desativar

> **DELETE** /functions/{id}

5.  **Regras Transversais**

<table style="width:97%;">
<colgroup>
<col style="width: 33%" />
<col style="width: 63%" />
</colgroup>
<thead>
<tr>
<th><strong>Campos</strong></th>
<th><blockquote>
<p><strong>Descrição / Regras</strong></p>
</blockquote></th>
</tr>
</thead>
<tbody>
<tr>
<td>Unicidade</td>
<td><blockquote>
<p>code é globalmente único em jobs, functions e
organizational_units.</p>
</blockquote></td>
</tr>
</tbody>
</table>

<table style="width:97%;">
<colgroup>
<col style="width: 33%" />
<col style="width: 63%" />
</colgroup>
<thead>
<tr>
<th>Imutabilidade</th>
<th><blockquote>
<p>code é imutável após criação.</p>
</blockquote></th>
</tr>
</thead>
<tbody>
<tr>
<td>Soft delete</td>
<td><blockquote>
<p>DELETE = isActive = false; sem remoção física.</p>
</blockquote></td>
</tr>
<tr>
<td>Integridade dependente</td>
<td><blockquote>
<p>Desativação rejeitada (HTTP 409) quando referenciado por registos
ativos.</p>
</blockquote></td>
</tr>
<tr>
<td>Hierarquia (unidades)</td>
<td><blockquote>
<p>Ciclo proibido em parentUnitId; profundidade máxima recomendada de 5
níveis.</p>
</blockquote></td>
</tr>
<tr>
<td>Permissões</td>
<td><blockquote>
<p>Leitura: qualquer autenticado. Escrita: ROLE_HR_ADMIN ou
ROLE_SYSTEM_ADMIN.</p>
</blockquote></td>
</tr>
</tbody>
</table>

# Módulo Carreiras e Progressão

## Visão Geral

> Consolida os catálogos relativos à progressão funcional dos
> colaboradores: Carreiras, Categorias e Escalões. Estes três catálogos
> formam uma hierarquia: cada Carreira agrega múltiplas Categorias, e
> cada Categoria agrega múltiplos Escalões. O índice salarial é definido
> ao nível do Escalão.

## Carreiras (Careers)

> Agrupamento de categorias profissionais (ex.: Carreira de Técnico
> Superior, Carreira de Assistente Técnico).

### Listar

> **GET** /careers

### Obter

> **GET** /careers/{id}

### Criar

> **POST** /careers
>
> **Corpo da Requisição**

<table style="width:97%;">
<colgroup>
<col style="width: 24%" />
<col style="width: 15%" />
<col style="width: 15%" />
<col style="width: 42%" />
</colgroup>
<thead>
<tr>
<th><strong>Parâmetro</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Obrigatório</strong></p>
</blockquote></th>
<th><strong>Descrição</strong></th>
</tr>
</thead>
<tbody>
<tr>
<td>code</td>
<td>string</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Código único (máx. 50).</td>
</tr>
<tr>
<td>name</td>
<td>string</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Designação (máx. 150).</td>
</tr>
<tr>
<td>description</td>
<td>string</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Descrição.</td>
</tr>
<tr>
<td>isActive</td>
<td>boolean</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Estado inicial.</td>
</tr>
</tbody>
</table>

### Atualizar

> **PUT** /careers/{id}

### Desativar

> **DELETE** /careers/{id}

### Listar Categorias da Carreira

### 

> **GET** /careers/{id}/categories

## Categorias (Categories)

> Níveis profissionais dentro de uma carreira (ex.: Técnico Superior
> Principal, Técnico Superior de 1.ª Classe). O par (career_id, code) é
> único.

### Listar

> **GET** /categories
>
> **Parâmetros de Consulta**

<table style="width:97%;">
<colgroup>
<col style="width: 24%" />
<col style="width: 15%" />
<col style="width: 15%" />
<col style="width: 42%" />
</colgroup>
<thead>
<tr>
<th><strong>Parâmetro</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Obrigatório</strong></p>
</blockquote></th>
<th><strong>Descrição</strong></th>
</tr>
</thead>
<tbody>
<tr>
<td>careerId</td>
<td>integer</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Filtra por carreira.</td>
</tr>
<tr>
<td>isActive</td>
<td>boolean</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Filtra por estado.</td>
</tr>
</tbody>
</table>

### Obter

> **GET** /categories/{id}

### Criar

> **POST** /categories
>
> **Corpo da Requisição**

<table style="width:97%;">
<colgroup>
<col style="width: 24%" />
<col style="width: 15%" />
<col style="width: 15%" />
<col style="width: 42%" />
</colgroup>
<thead>
<tr>
<th><strong>Parâmetro</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Obrigatório</strong></p>
</blockquote></th>
<th><strong>Descrição</strong></th>
</tr>
</thead>
<tbody>
<tr>
<td>careerId</td>
<td>integer</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Carreira.</td>
</tr>
<tr>
<td>code</td>
<td>string</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Código único na carreira.</td>
</tr>
<tr>
<td>name</td>
<td>string</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Designação.</td>
</tr>
<tr>
<td>description</td>
<td>string</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Descrição.</td>
</tr>
<tr>
<td>isActive</td>
<td>boolean</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Estado inicial.</td>
</tr>
</tbody>
</table>

### Atualizar

> **PUT** /categories/{id}

### Desativar

> **DELETE** /categories/{id}

### Listar Escalões da Categoria

> **GET** /categories/{id}/grades

## Escalões (Grades)

> Posição remuneratória do colaborador dentro de uma categoria,
> identificada por gradeNumber e salaryIndex. O par (category_id,
> grade_number) é único.

### Listar

> **GET** /grades
>
> **Parâmetros de Consulta**

<table style="width:97%;">
<colgroup>
<col style="width: 24%" />
<col style="width: 15%" />
<col style="width: 15%" />
<col style="width: 42%" />
</colgroup>
<thead>
<tr>
<th><strong>Parâmetro</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Obrigatório</strong></p>
</blockquote></th>
<th><strong>Descrição</strong></th>
</tr>
</thead>
<tbody>
<tr>
<td>categoryId</td>
<td>integer</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Filtra por categoria.</td>
</tr>
<tr>
<td>isActive</td>
<td>boolean</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Filtra por estado.</td>
</tr>
</tbody>
</table>

### Obter

> **GET** /grades/{id}

### Criar

> **POST** /grades
>
> **Corpo da Requisição**

<table style="width:97%;">
<colgroup>
<col style="width: 24%" />
<col style="width: 15%" />
<col style="width: 15%" />
<col style="width: 42%" />
</colgroup>
<thead>
<tr>
<th><strong>Parâmetro</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Obrigatório</strong></p>
</blockquote></th>
<th><strong>Descrição</strong></th>
</tr>
</thead>
<tbody>
<tr>
<td>categoryId</td>
<td>integer</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Categoria.</td>
</tr>
<tr>
<td>gradeNumber</td>
<td>integer</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Número do escalão (&gt;= 1).</td>
</tr>
<tr>
<td>name</td>
<td>string</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Designação.</td>
</tr>
<tr>
<td>salaryIndex</td>
<td>number</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Índice salarial.</td>
</tr>
<tr>
<td>isActive</td>
<td>boolean</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Estado inicial.</td>
</tr>
</tbody>
</table>

### Atualizar

> **PUT** /grades/{id}

### Desativar

### 

> **DELETE** /grades/{id}

5.  **Regras Transversais**

<table style="width:97%;">
<colgroup>
<col style="width: 33%" />
<col style="width: 63%" />
</colgroup>
<thead>
<tr>
<th><strong>Campos</strong></th>
<th><blockquote>
<p><strong>Descrição / Regras</strong></p>
</blockquote></th>
</tr>
</thead>
<tbody>
<tr>
<td>Unicidade</td>
<td><blockquote>
<p>careers.code globalmente único; categories.(career_id, code) único;
grades.(category_id, grade_number) único.</p>
</blockquote></td>
</tr>
<tr>
<td>Imutabilidade</td>
<td><blockquote>
<p>code e estruturais (career_id em categories, category_id em grades)
imutáveis.</p>
</blockquote></td>
</tr>
<tr>
<td>Soft delete</td>
<td><blockquote>
<p>DELETE = isActive = false; HTTP 409 se referenciado por enquadramento
ativo.</p>
</blockquote></td>
</tr>
<tr>
<td>Permissões</td>
<td><blockquote>
<p>Leitura: qualquer autenticado. Escrita: ROLE_HR_ADMIN.</p>
</blockquote></td>
</tr>
</tbody>
</table>

# Módulo Parametrizações

## Visão Geral

> O módulo Parametrizações agrega os catálogos auxiliares (lookup
> tables) que alimentam formulários, validações e regras de negócio.
> Inclui Estados Civis, Estados do Trabalhador, Situações Profissionais,
> Tipos de Ausência, Subtipos de Licença/Mobilidade e Tipos de
> Documento. Cada catálogo é administrável de forma autónoma sem
> necessidade de novo deploy. As operações de leitura estão também
> expostas como endpoints consolidados em 5.8 (/reference/\*).

## Estados Civis (Marital Statuses)

> Classifica o estado civil do funcionário (SOLTEIRO, CASADO,
> UNIAO_FACTO, DIVORCIADO, VIUVO).

### Listar

> **GET** /marital-statuses

### Obter

> **GET** /marital-statuses/{id}

### Criar

> **POST** /marital-statuses
>
> **Corpo da Requisição**

<table style="width:97%;">
<colgroup>
<col style="width: 24%" />
<col style="width: 15%" />
<col style="width: 15%" />
<col style="width: 42%" />
</colgroup>
<thead>
<tr>
<th><strong>Parâmetro</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Obrigatório</strong></p>
</blockquote></th>
<th><strong>Descrição</strong></th>
</tr>
</thead>
<tbody>
<tr>
<td>code</td>
<td>string</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Código único (máx. 30).</td>
</tr>
<tr>
<td>name</td>
<td>string</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Designação (máx. 100).</td>
</tr>
<tr>
<td>isActive</td>
<td>boolean</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Estado inicial (default true).</td>
</tr>
</tbody>
</table>

### Atualizar

> **PUT** /marital-statuses/{id}

### Desativar

> **DELETE** /marital-statuses/{id}

## Estados do Trabalhador (Worker States)

## 

> Define o estado do funcionário (ACTIVE, INACTIVE, SUSPENDED). Os
> códigos núcleo não podem ser desativados.

### Listar

> **GET** /worker-states

### Obter

> **GET** /worker-states/{id}

### Criar

> **POST** /worker-states

### Atualizar

> **PUT** /worker-states/{id}

### Desativar

> **DELETE** /worker-states/{id}

## Situações Profissionais (Professional Situations)

> Define o regime contratual (EFETIVO, CONTRATADO, COMISSIONADO,
> ESTAGIARIO).

### Listar

> **GET** /professional-situations

### Obter

> **GET** /professional-situations/{id}

### Criar

> **POST** /professional-situations

### Atualizar

> **PUT** /professional-situations/{id}

### Desativar

> **DELETE** /professional-situations/{id}

## Tipos de Ausência (Leave Types)

> Catálogo central da gestão de ausências. Cada tipo possui parâmetros
> que determinam o seu comportamento: deductsBalance (desconta saldo),
> requiresApproval (exige aprovação) e category.

### Listar

> **GET** /leave-types
>
> **Parâmetros de Consulta**

<table style="width:97%;">
<colgroup>
<col style="width: 24%" />
<col style="width: 15%" />
<col style="width: 15%" />
<col style="width: 42%" />
</colgroup>
<thead>
<tr>
<th><strong>Parâmetro</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Obrigatório</strong></p>
</blockquote></th>
<th><strong>Descrição</strong></th>
</tr>
</thead>
<tbody>
<tr>
<td>category</td>
<td>string</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Filtra por categoria (FERIAS, DOENCA, FAMILIA, OUTRO).</td>
</tr>
<tr>
<td>deductsBalance</td>
<td>boolean</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Filtra por flag.</td>
</tr>
<tr>
<td>requiresApproval</td>
<td>boolean</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Filtra por flag.</td>
</tr>
</tbody>
</table>

### Obter

> **GET** /leave-types/{id}

### Criar

> **POST** /leave-types
>
> **Corpo da Requisição**

<table style="width:97%;">
<colgroup>
<col style="width: 24%" />
<col style="width: 15%" />
<col style="width: 15%" />
<col style="width: 42%" />
</colgroup>
<thead>
<tr>
<th><strong>Parâmetro</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Obrigatório</strong></p>
</blockquote></th>
<th><strong>Descrição</strong></th>
</tr>
</thead>
<tbody>
<tr>
<td>code</td>
<td>string</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Código único (máx. 30).</td>
</tr>
<tr>
<td>name</td>
<td>string</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Designação (máx. 100).</td>
</tr>
<tr>
<td>category</td>
<td>string</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Categoria.</td>
</tr>
<tr>
<td>deductsBalance</td>
<td>boolean</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Default true.</td>
</tr>
<tr>
<td>requiresApproval</td>
<td>boolean</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Default true.</td>
</tr>
<tr>
<td>isActive</td>
<td>boolean</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Default true.</td>
</tr>
</tbody>
</table>

### Atualizar

> **PUT** /leave-types/{id}

### Desativar

### 

> **DELETE** /leave-types/{id}

## Subtipos de Licença/Mobilidade (Leave-Mobility Subtypes)

> Detalha os tipos da categoria LICENCA ou MOBILIDADE. Cada subtipo
> indica recordType (LICENCA/MOBILIDADE/AMBOS), affectsPay e
> countsForSeniority.

### Listar

> **GET** /leave-mobility-subtypes

### Obter

> **GET** /leave-mobility-subtypes/{id}

### Criar

> **POST** /leave-mobility-subtypes
>
> **Corpo da Requisição**

<table style="width:97%;">
<colgroup>
<col style="width: 24%" />
<col style="width: 15%" />
<col style="width: 15%" />
<col style="width: 42%" />
</colgroup>
<thead>
<tr>
<th><strong>Parâmetro</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Obrigatório</strong></p>
</blockquote></th>
<th><strong>Descrição</strong></th>
</tr>
</thead>
<tbody>
<tr>
<td>code</td>
<td>string</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Código único.</td>
</tr>
<tr>
<td>name</td>
<td>string</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>Designação.</td>
</tr>
<tr>
<td>recordType</td>
<td>string</td>
<td><blockquote>
<p>Sim</p>
</blockquote></td>
<td>LICENCA, MOBILIDADE ou AMBOS.</td>
</tr>
<tr>
<td>affectsPay</td>
<td>boolean</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Default false.</td>
</tr>
<tr>
<td>countsForSeniority</td>
<td>boolean</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Default true.</td>
</tr>
<tr>
<td>canSelfSubmit</td>
<td>boolean</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Permite auto-submissão pelo colaborador (default false).</td>
</tr>
<tr>
<td>isActive</td>
<td>boolean</td>
<td><blockquote>
<p>Não</p>
</blockquote></td>
<td>Default true.</td>
</tr>
</tbody>
</table>

### Atualizar

> **PUT** /leave-mobility-subtypes/{id}

### Desativar

> **DELETE** /leave-mobility-subtypes/{id}

## Tipos de Documento (Document Types)

## 

> Catálogo de tipos de documentos anexáveis (CNI, CONTRATO, CERTIDAO,
> RECIBO, JUSTIFICATIVO, OUTRO).

### Listar

> **GET** /document-types

### Obter

> **GET** /document-types/{id}

### Criar

> **POST** /document-types

### Atualizar

> **PUT** /document-types/{id}

### Desativar

> **DELETE** /document-types/{id}

## Endpoints Consolidados de Referência

> Para optimizar o consumo das parametrizações por formulários e
> selectores do frontend, expõe-se uma família de endpoints de leitura
> sob /reference, com payload reduzido (apenas id, code, name) e suporte
> a cache HTTP via ETag/If-None-Match.

<table style="width:97%;">
<colgroup>
<col style="width: 27%" />
<col style="width: 39%" />
<col style="width: 30%" />
</colgroup>
<thead>
<tr>
<th><strong>Recurso</strong></th>
<th><strong>Endpoint</strong></th>
<th><blockquote>
<p><strong>Descrição</strong></p>
</blockquote></th>
</tr>
</thead>
<tbody>
<tr>
<td>Estados Civis</td>
<td>GET /reference/marital-statuses</td>
<td><blockquote>
<p>Lista resumida de estados civis.</p>
</blockquote></td>
</tr>
<tr>
<td>Estados do Trabalhador</td>
<td>GET /reference/worker-states</td>
<td><blockquote>
<p>Lista resumida.</p>
</blockquote></td>
</tr>
<tr>
<td>Situações Profissionais</td>
<td>GET /reference/professional-situations</td>
<td><blockquote>
<p>Lista resumida.</p>
</blockquote></td>
</tr>
<tr>
<td>Tipos de Ausência</td>
<td>GET /reference/leave-types</td>
<td><blockquote>
<p>Lista resumida.</p>
</blockquote></td>
</tr>
<tr>
<td>Subtipos de Licença/Mobilidade</td>
<td>GET /reference/leave-mobility-subtypes</td>
<td><blockquote>
<p>Filtrável por recordType.</p>
</blockquote></td>
</tr>
<tr>
<td>Tipos de Documento</td>
<td>GET /reference/document-types</td>
<td><blockquote>
<p>Lista resumida.</p>
</blockquote></td>
</tr>
<tr>
<td>Cargos</td>
<td>GET /reference/jobs</td>
<td><blockquote>
<p>Lista resumida.</p>
</blockquote></td>
</tr>
<tr>
<td>Funções</td>
<td>GET /reference/functions</td>
<td><blockquote>
<p>Lista resumida.</p>
</blockquote></td>
</tr>
</tbody>
</table>

<table style="width:97%;">
<colgroup>
<col style="width: 27%" />
<col style="width: 39%" />
<col style="width: 30%" />
</colgroup>
<thead>
<tr>
<th>Carreiras</th>
<th>GET /reference/careers</th>
<th><blockquote>
<p>Lista resumida.</p>
</blockquote></th>
</tr>
</thead>
<tbody>
<tr>
<td>Categorias</td>
<td>GET /reference/categories</td>
<td><blockquote>
<p>Filtrável por carreira.</p>
</blockquote></td>
</tr>
<tr>
<td>Escalões</td>
<td>GET /reference/grades</td>
<td><blockquote>
<p>Filtrável por categoria.</p>
</blockquote></td>
</tr>
<tr>
<td>Unidades Orgânicas</td>
<td>GET /reference/organizational-units</td>
<td><blockquote>
<p>Lista resumida.</p>
</blockquote></td>
</tr>
</tbody>
</table>

9.  **Regras Transversais**

<table style="width:97%;">
<colgroup>
<col style="width: 33%" />
<col style="width: 63%" />
</colgroup>
<thead>
<tr>
<th><strong>Campos</strong></th>
<th><blockquote>
<p><strong>Descrição / Regras</strong></p>
</blockquote></th>
</tr>
</thead>
<tbody>
<tr>
<td>Unicidade</td>
<td><blockquote>
<p>code globalmente único em cada catálogo.</p>
</blockquote></td>
</tr>
<tr>
<td>Imutabilidade</td>
<td><blockquote>
<p>code imutável após criação; nome e flags atualizáveis.</p>
</blockquote></td>
</tr>
<tr>
<td>Soft delete</td>
<td><blockquote>
<p>DELETE = isActive = false.</p>
</blockquote></td>
</tr>
<tr>
<td>Integridade</td>
<td><blockquote>
<p>Desativação rejeitada (HTTP 409) se referenciada por registos
ativos.</p>
</blockquote></td>
</tr>
<tr>
<td>Códigos núcleo</td>
<td><blockquote>
<p>ACTIVE/INACTIVE/SUSPENDED em worker_states e FERIAS em leave_types
não podem ser desativados.</p>
</blockquote></td>
</tr>
<tr>
<td>Permissões</td>
<td><blockquote>
<p>Leitura: qualquer autenticado. Escrita: ROLE_HR_ADMIN ou
ROLE_SYSTEM_ADMIN.</p>
</blockquote></td>
</tr>
<tr>
<td>Cache</td>
<td><blockquote>
<p>Endpoints /reference/* suportam ETag/If-None-Match.</p>
</blockquote></td>
</tr>
</tbody>
</table>

# Integração com o Sistema de Avaliação de Desempenho

## Visão Geral e Arquitetura de Integração

> A funcionalidade de Avaliação de Desempenho (avaliações, ciclos,
> objetivos e respetivas atualizações) é da responsabilidade de um
> sistema externo dedicado (doravante designado por SAD - Sistema de
> Avaliação de Desempenho). O Módulo RH do SIPPROG não persiste
> localmente estes dados como fonte de verdade: limita-se a consumi-los
> para apresentação na Área Reservada do Colaborador (Capítulo 1) e em
> vistas agregadas para o RH.
>
> A integração segue o padrão read-through: o backend do SIPPROG faz
> pedidos HTTP autenticados ao SAD, com cache local de curta duração
> (TTL configurável, recomendado 5 minutos) para reduzir latência e
> proteger o SAD de carga excessiva. A escrita (lançamento de
> avaliações, registo de objetivos, atualizações de progresso) é feita
> diretamente no SAD através do seu próprio frontend; o SIPPROG apenas
> exibe e consulta.

| **Aspecto** | **Decisão** |
|----|----|
| Fonte de verdade | SAD (sistema externo). |
| Persistência local | Apenas cache temporário (Redis ou equivalente); não há tabelas próprias para evaluation_cycles, performance_evaluations, objectives, objective_updates. |
| Padrão de integração | REST sobre HTTPS, autenticação OAuth 2.0 client-credentials. |
| Identidade do colaborador | Mapeamento bidirecional via tabela employee_external_mapping (ver 6.4). |
| Resiliência | Circuit breaker e fallback para 'serviço indisponível' (ver 6.5). |

## Endpoints de Leitura (Read-Through)

> O SIPPROG expõe um conjunto de endpoints sob /external/evaluations e
> /external/objectives que atuam como proxy ao SAD, normalizando o
> payload.

### Histórico de Avaliações de um Colaborador

> **GET** /employees/{employeeId}/external/evaluations

### Detalhe de uma Avaliação

> **GET** /external/evaluations/{externalId}

### Objetivos de um Colaborador

### 

> **GET** /employees/{employeeId}/external/objectives

### Ciclos de Avaliação

> **GET** /external/evaluation-cycles
>
> Lista os ciclos abertos e fechados conhecidos no SAD.

## Sincronização de Catálogos (Webhooks)

> Para informação que beneficia de notificação proativa (encerramento de
> ciclo, publicação de avaliação final), o SAD pode invocar webhooks no
> SIPPROG, que limpa o cache afetado e, opcionalmente, dispara
> notificações ao colaborador via API de notificações.

<table style="width:97%;">
<colgroup>
<col style="width: 30%" />
<col style="width: 42%" />
<col style="width: 24%" />
</colgroup>
<thead>
<tr>
<th><strong>Webhook</strong></th>
<th><blockquote>
<p><strong>Endpoint no SIPPROG</strong></p>
</blockquote></th>
<th><strong>Eventos</strong></th>
</tr>
</thead>
<tbody>
<tr>
<td>Avaliação publicada</td>
<td><blockquote>
<p>POST /webhooks/sad/evaluation-published</p>
</blockquote></td>
<td>evaluation.published</td>
</tr>
<tr>
<td>Ciclo encerrado</td>
<td><blockquote>
<p>POST /webhooks/sad/cycle-closed</p>
</blockquote></td>
<td>cycle.closed</td>
</tr>
<tr>
<td>Objetivo atualizado</td>
<td><blockquote>
<p>POST /webhooks/sad/objective-updated</p>
</blockquote></td>
<td>objective.updated</td>
</tr>
</tbody>
</table>

## Mapeamento de Identidades

> Como o employee_id no SIPPROG e o employeeId no SAD podem divergir,
> mantém-se uma tabela auxiliar employee_external_mapping com
> (employee_id, system_code, external_id, external_username). Esta
> tabela faz parte do esquema do RH (ver 8.3.X) e é gerida pelo
> administrador da integração.

5.  **Tratamento de Falhas e Cache**

<table style="width:97%;">
<colgroup>
<col style="width: 33%" />
<col style="width: 63%" />
</colgroup>
<thead>
<tr>
<th><strong>Campos</strong></th>
<th><blockquote>
<p><strong>Descrição / Regras</strong></p>
</blockquote></th>
</tr>
</thead>
<tbody>
<tr>
<td>Cache HIT</td>
<td><blockquote>
<p>Resposta do cache retornada com cabeçalho X-Cache: HIT e X-Cache-
Age.</p>
</blockquote></td>
</tr>
<tr>
<td>Cache MISS</td>
<td><blockquote>
<p>Pedido ao SAD; resposta armazenada por TTL (default 300s).</p>
</blockquote></td>
</tr>
<tr>
<td>SAD indisponível</td>
<td><blockquote>
<p>Resposta 503 com Retry-After se sem cache; resposta stale (X-Cache:
STALE) se houver cache expirado.</p>
</blockquote></td>
</tr>
<tr>
<td>Circuit breaker</td>
<td><blockquote>
<p>Após 5 falhas consecutivas, circuito abre por 60 segundos; durante
esse período só responde com cache stale.</p>
</blockquote></td>
</tr>
<tr>
<td>Logs</td>
<td><blockquote>
<p>Cada chamada ao SAD é registada em integration_logs com latência,
status e correlação ID.</p>
</blockquote></td>
</tr>
</tbody>
</table>

6.  **Segurança da Integração**

<table style="width:97%;">
<colgroup>
<col style="width: 33%" />
<col style="width: 63%" />
</colgroup>
<thead>
<tr>
<th><strong>Campos</strong></th>
<th><blockquote>
<p><strong>Descrição / Regras</strong></p>
</blockquote></th>
</tr>
</thead>
<tbody>
<tr>
<td>Autenticação outbound</td>
<td><blockquote>
<p>OAuth 2.0 client_credentials; tokens cacheados até expirar.</p>
</blockquote></td>
</tr>
<tr>
<td>Autenticação inbound (webhooks)</td>
<td><blockquote>
<p>HMAC SHA-256 sobre o body, segredo partilhado, tolerância de relógio
de 5 minutos.</p>
</blockquote></td>
</tr>
<tr>
<td>TLS</td>
<td><blockquote>
<p>TLS 1.2+ obrigatório, validação de certificado.</p>
</blockquote></td>
</tr>
<tr>
<td>Auditoria</td>
<td><blockquote>
<p>Cada webhook recebido é registado em integration_logs com payload
(PII redatada).</p>
</blockquote></td>
</tr>
</tbody>
</table>

# API de Anexos / Documentos

## Visão Geral

> API genérica para gestão de ficheiros associados a entidades do Módulo
> RH (funcionários, pedidos, licenças, recibos, etc.). Os ficheiros são
> armazenados em storage de objetos (S3 compatível) e os metadados na
> tabela documents.

| **Limite** | **Valor** |
|----|----|
| Tamanho máximo por ficheiro | 10 MB (configurável). |
| Tipos aceites | PDF, JPG, PNG, DOCX, XLSX (definidos por tipo de documento). |
| Antivirus | Verificação assíncrona após upload; ficheiros suspeitos são bloqueados. |

## Upload

> **POST** /documents
>
> Multipart/form-data com os campos: file, documentTypeId, employeeId,
> description.
>
> **Resposta (201 Created)**
>
> {
>
> "id": 1234,
>
> "fileName": "cni_alex.pdf", "documentType": "CNI", "size": 245680,
>
> "mimeType": "application/pdf", "uploadedAt": "2026-04-22T10:14:00Z",
>
> "employeeId": 42
>
> }

## Download

> **GET** /documents/{id}/download
>
> Devolve o ficheiro com Content-Type e Content-Disposition apropriados.
> Permissões aplicadas conforme o dono.

## Listagem por Funcionário

> **GET** /employees/{employeeId}/documents

# Modelo de Dados

## Visão Geral

> O modelo físico do Módulo RH é implementado em PostgreSQL e
> organiza-se em sete domínios funcionais: parametrizações (lookup
> tables), estrutura organizacional, carreiras e progressão, núcleo de
> funcionários, gestão de ausências, licenças e mobilidade, e financeiro
> (recibos). As tabelas relativas à avaliação de desempenho não fazem
> parte deste esquema, sendo mantidas no sistema externo SAD (ver
> Capítulo 6 e secção 8.4).

## Diagrama de Entidades e Relacionamentos (ERD)

> O diagrama ER do módulo é apresentado no documento
> ERD_Modulo_RH_v3.0.png, anexo a esta especificação. As tabelas estão
> agrupadas por domínio funcional e as relações representadas em notação
> crow's foot, indicando cardinalidade e obrigatoriedade.

## Descrição das Tabelas

1.  **marital_statuses**

<table style="width:97%;">
<colgroup>
<col style="width: 24%" />
<col style="width: 33%" />
<col style="width: 39%" />
</colgroup>
<thead>
<tr>
<th><strong>Coluna</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Descrição</strong></p>
</blockquote></th>
</tr>
</thead>
<tbody>
<tr>
<td>id</td>
<td>BIGSERIAL PK</td>
<td><blockquote>
<p>Identificador único.</p>
</blockquote></td>
</tr>
<tr>
<td>code</td>
<td>VARCHAR(30) UNIQUE NOT NULL</td>
<td><blockquote>
<p>Código único.</p>
</blockquote></td>
</tr>
<tr>
<td>name</td>
<td>VARCHAR(100) NOT NULL</td>
<td><blockquote>
<p>Designação.</p>
</blockquote></td>
</tr>
<tr>
<td>is_active</td>
<td>BOOLEAN NOT NULL DEFAULT TRUE</td>
<td><blockquote>
<p>Estado lógico.</p>
</blockquote></td>
</tr>
</tbody>
</table>

2.  **worker_states**

<table style="width:97%;">
<colgroup>
<col style="width: 24%" />
<col style="width: 33%" />
<col style="width: 39%" />
</colgroup>
<thead>
<tr>
<th><strong>Coluna</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Descrição</strong></p>
</blockquote></th>
</tr>
</thead>
<tbody>
<tr>
<td>id</td>
<td>BIGSERIAL PK</td>
<td><blockquote>
<p>Identificador único.</p>
</blockquote></td>
</tr>
<tr>
<td>code</td>
<td>VARCHAR(30) UNIQUE NOT NULL</td>
<td><blockquote>
<p>Código (ACTIVE, INACTIVE, SUSPENDED).</p>
</blockquote></td>
</tr>
<tr>
<td>name</td>
<td>VARCHAR(100) NOT NULL</td>
<td><blockquote>
<p>Designação.</p>
</blockquote></td>
</tr>
<tr>
<td>is_active</td>
<td>BOOLEAN NOT NULL DEFAULT TRUE</td>
<td><blockquote>
<p>Estado lógico.</p>
</blockquote></td>
</tr>
</tbody>
</table>

3.  **professional_situations**

<table style="width:97%;">
<colgroup>
<col style="width: 24%" />
<col style="width: 33%" />
<col style="width: 39%" />
</colgroup>
<thead>
<tr>
<th><strong>Coluna</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Descrição</strong></p>
</blockquote></th>
</tr>
</thead>
<tbody>
<tr>
<td>id</td>
<td>BIGSERIAL PK</td>
<td><blockquote>
<p>Identificador único.</p>
</blockquote></td>
</tr>
<tr>
<td>code</td>
<td>VARCHAR(30) UNIQUE NOT NULL</td>
<td><blockquote>
<p>Código.</p>
</blockquote></td>
</tr>
<tr>
<td>name</td>
<td>VARCHAR(100) NOT NULL</td>
<td><blockquote>
<p>Designação.</p>
</blockquote></td>
</tr>
<tr>
<td>is_active</td>
<td>BOOLEAN NOT NULL DEFAULT TRUE</td>
<td><blockquote>
<p>Estado lógico.</p>
</blockquote></td>
</tr>
</tbody>
</table>

4.  **leave_types**

<table style="width:97%;">
<colgroup>
<col style="width: 30%" />
<col style="width: 30%" />
<col style="width: 36%" />
</colgroup>
<thead>
<tr>
<th><strong>Coluna</strong></th>
<th><blockquote>
<p><strong>Tipo</strong></p>
</blockquote></th>
<th><strong>Descrição</strong></th>
</tr>
</thead>
<tbody>
<tr>
<td>id</td>
<td><blockquote>
<p>BIGSERIAL PK</p>
</blockquote></td>
<td>Identificador único.</td>
</tr>
<tr>
<td>code</td>
<td><blockquote>
<p>VARCHAR(30) UNIQUE NOT NULL</p>
</blockquote></td>
<td>Código (FERIAS, DOENCA, MATERNIDADE...).</td>
</tr>
<tr>
<td>name</td>
<td><blockquote>
<p>VARCHAR(100) NOT NULL</p>
</blockquote></td>
<td>Designação.</td>
</tr>
<tr>
<td>category</td>
<td><blockquote>
<p>VARCHAR(50)</p>
</blockquote></td>
<td>Categoria agrupadora.</td>
</tr>
<tr>
<td>deducts_balance</td>
<td><blockquote>
<p>BOOLEAN NOT NULL DEFAULT TRUE</p>
</blockquote></td>
<td>Desconta do saldo.</td>
</tr>
<tr>
<td>requires_approval</td>
<td><blockquote>
<p>BOOLEAN NOT NULL DEFAULT TRUE</p>
</blockquote></td>
<td>Exige aprovação.</td>
</tr>
<tr>
<td>is_active</td>
<td><blockquote>
<p>BOOLEAN NOT NULL DEFAULT TRUE</p>
</blockquote></td>
<td>Estado lógico.</td>
</tr>
<tr>
<td>created_at, created_by, updated_at, updated_by</td>
<td><blockquote>
<p>AUDITORIA</p>
</blockquote></td>
<td>Timestamps e autores.</td>
</tr>
</tbody>
</table>

5.  **leave_mobility_subtypes**

<table style="width:97%;">
<colgroup>
<col style="width: 30%" />
<col style="width: 30%" />
<col style="width: 36%" />
</colgroup>
<thead>
<tr>
<th><strong>Coluna</strong></th>
<th><blockquote>
<p><strong>Tipo</strong></p>
</blockquote></th>
<th><strong>Descrição</strong></th>
</tr>
</thead>
<tbody>
<tr>
<td>id</td>
<td><blockquote>
<p>BIGSERIAL PK</p>
</blockquote></td>
<td>Identificador único.</td>
</tr>
<tr>
<td>code</td>
<td><blockquote>
<p>VARCHAR(50) UNIQUE NOT NULL</p>
</blockquote></td>
<td>Código.</td>
</tr>
<tr>
<td>name</td>
<td><blockquote>
<p>VARCHAR(150) NOT NULL</p>
</blockquote></td>
<td>Designação.</td>
</tr>
<tr>
<td>record_type</td>
<td><blockquote>
<p>VARCHAR(20) NOT NULL</p>
</blockquote></td>
<td>LICENCA, MOBILIDADE ou AMBOS.</td>
</tr>
</tbody>
</table>

<table style="width:97%;">
<colgroup>
<col style="width: 30%" />
<col style="width: 30%" />
<col style="width: 36%" />
</colgroup>
<thead>
<tr>
<th>affects_pay</th>
<th><blockquote>
<p>BOOLEAN NOT NULL DEFAULT FALSE</p>
</blockquote></th>
<th>Afeta remuneração.</th>
</tr>
</thead>
<tbody>
<tr>
<td>counts_for_seniority</td>
<td><blockquote>
<p>BOOLEAN NOT NULL DEFAULT TRUE</p>
</blockquote></td>
<td>Conta para antiguidade.</td>
</tr>
<tr>
<td>can_self_submit</td>
<td><blockquote>
<p>BOOLEAN NOT NULL DEFAULT FALSE</p>
</blockquote></td>
<td>Permite auto-submissão.</td>
</tr>
<tr>
<td>is_active</td>
<td><blockquote>
<p>BOOLEAN NOT NULL DEFAULT TRUE</p>
</blockquote></td>
<td>Estado lógico.</td>
</tr>
</tbody>
</table>

6.  **document_types**

<table style="width:97%;">
<colgroup>
<col style="width: 24%" />
<col style="width: 33%" />
<col style="width: 39%" />
</colgroup>
<thead>
<tr>
<th><strong>Coluna</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Descrição</strong></p>
</blockquote></th>
</tr>
</thead>
<tbody>
<tr>
<td>id</td>
<td>BIGSERIAL PK</td>
<td><blockquote>
<p>Identificador único.</p>
</blockquote></td>
</tr>
<tr>
<td>code</td>
<td>VARCHAR(30) UNIQUE NOT NULL</td>
<td><blockquote>
<p>Código.</p>
</blockquote></td>
</tr>
<tr>
<td>name</td>
<td>VARCHAR(100) NOT NULL</td>
<td><blockquote>
<p>Designação.</p>
</blockquote></td>
</tr>
<tr>
<td>is_active</td>
<td>BOOLEAN NOT NULL DEFAULT TRUE</td>
<td><blockquote>
<p>Estado lógico.</p>
</blockquote></td>
</tr>
</tbody>
</table>

7.  **organizational_units**

<table style="width:97%;">
<colgroup>
<col style="width: 27%" />
<col style="width: 30%" />
<col style="width: 39%" />
</colgroup>
<thead>
<tr>
<th><strong>Coluna</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Descrição</strong></p>
</blockquote></th>
</tr>
</thead>
<tbody>
<tr>
<td>id</td>
<td>BIGSERIAL PK</td>
<td><blockquote>
<p>Identificador único.</p>
</blockquote></td>
</tr>
<tr>
<td>code</td>
<td>VARCHAR(50) UNIQUE NOT NULL</td>
<td><blockquote>
<p>Código único.</p>
</blockquote></td>
</tr>
<tr>
<td>name</td>
<td>VARCHAR(150) NOT NULL</td>
<td><blockquote>
<p>Designação oficial.</p>
</blockquote></td>
</tr>
<tr>
<td>acronym</td>
<td>VARCHAR(20)</td>
<td><blockquote>
<p>Sigla.</p>
</blockquote></td>
</tr>
<tr>
<td>unit_type</td>
<td>VARCHAR(30) NOT NULL</td>
<td><blockquote>
<p>DIRECAO, DEPARTAMENTO, DIVISAO, SECCAO.</p>
</blockquote></td>
</tr>
<tr>
<td>parent_unit_id</td>
<td>BIGINT FK→self</td>
<td><blockquote>
<p>Unidade-pai.</p>
</blockquote></td>
</tr>
<tr>
<td>is_active</td>
<td>BOOLEAN NOT NULL DEFAULT TRUE</td>
<td><blockquote>
<p>Estado lógico.</p>
</blockquote></td>
</tr>
<tr>
<td>auditoria</td>
<td>created_at/by, updated_at/by</td>
<td><blockquote>
<p>Auditoria.</p>
</blockquote></td>
</tr>
</tbody>
</table>

8.  **jobs**

<table style="width:97%;">
<colgroup>
<col style="width: 27%" />
<col style="width: 30%" />
<col style="width: 39%" />
</colgroup>
<thead>
<tr>
<th><strong>Coluna</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Descrição</strong></p>
</blockquote></th>
</tr>
</thead>
<tbody>
<tr>
<td>id</td>
<td>BIGSERIAL PK</td>
<td><blockquote>
<p>Identificador único.</p>
</blockquote></td>
</tr>
<tr>
<td>code</td>
<td>VARCHAR(50) UNIQUE NOT NULL</td>
<td><blockquote>
<p>Código único.</p>
</blockquote></td>
</tr>
<tr>
<td>name</td>
<td>VARCHAR(150) NOT NULL</td>
<td><blockquote>
<p>Designação.</p>
</blockquote></td>
</tr>
<tr>
<td>description</td>
<td>TEXT</td>
<td><blockquote>
<p>Descrição.</p>
</blockquote></td>
</tr>
<tr>
<td>is_active</td>
<td>BOOLEAN NOT NULL DEFAULT TRUE</td>
<td><blockquote>
<p>Estado lógico.</p>
</blockquote></td>
</tr>
<tr>
<td>auditoria</td>
<td>created_at/by, updated_at/by</td>
<td><blockquote>
<p>Auditoria.</p>
</blockquote></td>
</tr>
</tbody>
</table>

9.  **functions**

<table style="width:97%;">
<colgroup>
<col style="width: 27%" />
<col style="width: 30%" />
<col style="width: 39%" />
</colgroup>
<thead>
<tr>
<th><strong>Coluna</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Descrição</strong></p>
</blockquote></th>
</tr>
</thead>
<tbody>
<tr>
<td>id</td>
<td>BIGSERIAL PK</td>
<td><blockquote>
<p>Identificador único.</p>
</blockquote></td>
</tr>
<tr>
<td>code</td>
<td>VARCHAR(50) UNIQUE NOT NULL</td>
<td><blockquote>
<p>Código único.</p>
</blockquote></td>
</tr>
<tr>
<td>name</td>
<td>VARCHAR(150) NOT NULL</td>
<td><blockquote>
<p>Designação.</p>
</blockquote></td>
</tr>
<tr>
<td>description</td>
<td>TEXT</td>
<td><blockquote>
<p>Descrição.</p>
</blockquote></td>
</tr>
<tr>
<td>is_active</td>
<td>BOOLEAN NOT NULL DEFAULT TRUE</td>
<td><blockquote>
<p>Estado lógico.</p>
</blockquote></td>
</tr>
<tr>
<td>auditoria</td>
<td>created_at/by, updated_at/by</td>
<td><blockquote>
<p>Auditoria.</p>
</blockquote></td>
</tr>
</tbody>
</table>

10. **careers**

<table style="width:97%;">
<colgroup>
<col style="width: 27%" />
<col style="width: 30%" />
<col style="width: 39%" />
</colgroup>
<thead>
<tr>
<th><strong>Coluna</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Descrição</strong></p>
</blockquote></th>
</tr>
</thead>
<tbody>
<tr>
<td>id</td>
<td>BIGSERIAL PK</td>
<td><blockquote>
<p>Identificador único.</p>
</blockquote></td>
</tr>
<tr>
<td>code</td>
<td>VARCHAR(50) UNIQUE NOT NULL</td>
<td><blockquote>
<p>Código único.</p>
</blockquote></td>
</tr>
<tr>
<td>name</td>
<td>VARCHAR(150) NOT NULL</td>
<td><blockquote>
<p>Designação.</p>
</blockquote></td>
</tr>
<tr>
<td>description</td>
<td>TEXT</td>
<td><blockquote>
<p>Descrição.</p>
</blockquote></td>
</tr>
</tbody>
</table>

<table style="width:97%;">
<colgroup>
<col style="width: 27%" />
<col style="width: 30%" />
<col style="width: 39%" />
</colgroup>
<thead>
<tr>
<th>is_active</th>
<th>BOOLEAN NOT NULL DEFAULT TRUE</th>
<th><blockquote>
<p>Estado lógico.</p>
</blockquote></th>
</tr>
</thead>
<tbody>
<tr>
<td>auditoria</td>
<td>created_at/by, updated_at/by</td>
<td><blockquote>
<p>Auditoria.</p>
</blockquote></td>
</tr>
</tbody>
</table>

11. **categories**

<table style="width:97%;">
<colgroup>
<col style="width: 27%" />
<col style="width: 30%" />
<col style="width: 39%" />
</colgroup>
<thead>
<tr>
<th><strong>Coluna</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Descrição</strong></p>
</blockquote></th>
</tr>
</thead>
<tbody>
<tr>
<td>id</td>
<td>BIGSERIAL PK</td>
<td><blockquote>
<p>Identificador único.</p>
</blockquote></td>
</tr>
<tr>
<td>career_id</td>
<td>BIGINT NOT NULL FK→careers</td>
<td><blockquote>
<p>Carreira a que pertence.</p>
</blockquote></td>
</tr>
<tr>
<td>code</td>
<td>VARCHAR(50) NOT NULL</td>
<td><blockquote>
<p>Código (único por carreira).</p>
</blockquote></td>
</tr>
<tr>
<td>name</td>
<td>VARCHAR(150) NOT NULL</td>
<td><blockquote>
<p>Designação.</p>
</blockquote></td>
</tr>
<tr>
<td>description</td>
<td>TEXT</td>
<td><blockquote>
<p>Descrição.</p>
</blockquote></td>
</tr>
<tr>
<td>is_active</td>
<td>BOOLEAN NOT NULL DEFAULT TRUE</td>
<td><blockquote>
<p>Estado lógico.</p>
</blockquote></td>
</tr>
<tr>
<td>UQ</td>
<td>(career_id, code)</td>
<td><blockquote>
<p>Restrição de unicidade composta.</p>
</blockquote></td>
</tr>
</tbody>
</table>

12. **grades**

<table style="width:97%;">
<colgroup>
<col style="width: 27%" />
<col style="width: 30%" />
<col style="width: 39%" />
</colgroup>
<thead>
<tr>
<th><strong>Coluna</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Descrição</strong></p>
</blockquote></th>
</tr>
</thead>
<tbody>
<tr>
<td>id</td>
<td>BIGSERIAL PK</td>
<td><blockquote>
<p>Identificador único.</p>
</blockquote></td>
</tr>
<tr>
<td>category_id</td>
<td><p>BIGINT NOT NULL</p>
<p>FK→categories</p></td>
<td><blockquote>
<p>Categoria.</p>
</blockquote></td>
</tr>
<tr>
<td>grade_number</td>
<td>INT NOT NULL</td>
<td><blockquote>
<p>Número do escalão.</p>
</blockquote></td>
</tr>
<tr>
<td>name</td>
<td>VARCHAR(150) NOT NULL</td>
<td><blockquote>
<p>Designação.</p>
</blockquote></td>
</tr>
<tr>
<td>salary_index</td>
<td>NUMERIC(12,2)</td>
<td><blockquote>
<p>Índice salarial.</p>
</blockquote></td>
</tr>
<tr>
<td>is_active</td>
<td>BOOLEAN NOT NULL DEFAULT TRUE</td>
<td><blockquote>
<p>Estado lógico.</p>
</blockquote></td>
</tr>
<tr>
<td>UQ</td>
<td>(category_id, grade_number)</td>
<td><blockquote>
<p>Restrição de unicidade.</p>
</blockquote></td>
</tr>
</tbody>
</table>

13. **employees**

<table style="width:97%;">
<colgroup>
<col style="width: 27%" />
<col style="width: 33%" />
<col style="width: 36%" />
</colgroup>
<thead>
<tr>
<th><strong>Coluna</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Descrição</strong></p>
</blockquote></th>
</tr>
</thead>
<tbody>
</tbody>
</table>

<table style="width:97%;">
<colgroup>
<col style="width: 27%" />
<col style="width: 33%" />
<col style="width: 36%" />
</colgroup>
<thead>
<tr>
<th>id</th>
<th>BIGSERIAL PK</th>
<th><blockquote>
<p>Identificador único.</p>
</blockquote></th>
</tr>
</thead>
<tbody>
<tr>
<td>full_name</td>
<td>VARCHAR(200) NOT NULL</td>
<td><blockquote>
<p>Nome completo.</p>
</blockquote></td>
</tr>
<tr>
<td>nif</td>
<td>VARCHAR(20) UNIQUE NOT NULL</td>
<td><blockquote>
<p>NIF.</p>
</blockquote></td>
</tr>
<tr>
<td>birth_date</td>
<td>DATE NOT NULL</td>
<td><blockquote>
<p>Data de nascimento.</p>
</blockquote></td>
</tr>
<tr>
<td>sex</td>
<td>CHAR(1) NOT NULL</td>
<td><blockquote>
<p>M ou F.</p>
</blockquote></td>
</tr>
<tr>
<td>marital_status_id</td>
<td><p>BIGINT NOT NULL</p>
<p>FK→marital_statuses</p></td>
<td><blockquote>
<p>Estado civil.</p>
</blockquote></td>
</tr>
<tr>
<td>nationality</td>
<td>VARCHAR(50) NOT NULL</td>
<td><blockquote>
<p>Nacionalidade.</p>
</blockquote></td>
</tr>
<tr>
<td>admission_date</td>
<td>DATE NOT NULL</td>
<td><blockquote>
<p>Data de admissão.</p>
</blockquote></td>
</tr>
<tr>
<td>worker_state_id</td>
<td><p>BIGINT NOT NULL</p>
<p>FK→worker_states</p></td>
<td><blockquote>
<p>Estado do trabalhador.</p>
</blockquote></td>
</tr>
<tr>
<td>professional_situation_id</td>
<td><p>BIGINT NOT NULL</p>
<p>FK→professional_situations</p></td>
<td><blockquote>
<p>Situação profissional.</p>
</blockquote></td>
</tr>
<tr>
<td>email</td>
<td>VARCHAR(150)</td>
<td><blockquote>
<p>Email.</p>
</blockquote></td>
</tr>
<tr>
<td>phone</td>
<td>VARCHAR(30)</td>
<td><blockquote>
<p>Telefone.</p>
</blockquote></td>
</tr>
<tr>
<td>nib</td>
<td>VARCHAR(30)</td>
<td><blockquote>
<p>NIB para pagamentos.</p>
</blockquote></td>
</tr>
<tr>
<td>is_active</td>
<td>BOOLEAN NOT NULL DEFAULT TRUE</td>
<td><blockquote>
<p>Estado lógico.</p>
</blockquote></td>
</tr>
<tr>
<td>auditoria</td>
<td>created_at/by, updated_at/by</td>
<td><blockquote>
<p>Auditoria.</p>
</blockquote></td>
</tr>
</tbody>
</table>

14. **employee_unit_assignments**

<table style="width:97%;">
<colgroup>
<col style="width: 27%" />
<col style="width: 33%" />
<col style="width: 36%" />
</colgroup>
<thead>
<tr>
<th><strong>Coluna</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Descrição</strong></p>
</blockquote></th>
</tr>
</thead>
<tbody>
<tr>
<td>id</td>
<td>BIGSERIAL PK</td>
<td><blockquote>
<p>Identificador único.</p>
</blockquote></td>
</tr>
<tr>
<td>employee_id</td>
<td>BIGINT NOT NULL FK→employees</td>
<td><blockquote>
<p>Funcionário.</p>
</blockquote></td>
</tr>
<tr>
<td>unit_id</td>
<td><p>BIGINT NOT NULL</p>
<p>FK→organizational_units</p></td>
<td><blockquote>
<p>Unidade orgânica.</p>
</blockquote></td>
</tr>
<tr>
<td>is_primary</td>
<td>BOOLEAN NOT NULL DEFAULT FALSE</td>
<td><blockquote>
<p>Atribuição principal.</p>
</blockquote></td>
</tr>
<tr>
<td>start_date</td>
<td>DATE NOT NULL</td>
<td><blockquote>
<p>Início.</p>
</blockquote></td>
</tr>
</tbody>
</table>

<table style="width:97%;">
<colgroup>
<col style="width: 27%" />
<col style="width: 33%" />
<col style="width: 36%" />
</colgroup>
<thead>
<tr>
<th>end_date</th>
<th>DATE</th>
<th><blockquote>
<p>Fim (vazio se em curso).</p>
</blockquote></th>
</tr>
</thead>
<tbody>
</tbody>
</table>

15. **employee_professional_assignments**

<table style="width:97%;">
<colgroup>
<col style="width: 27%" />
<col style="width: 33%" />
<col style="width: 36%" />
</colgroup>
<thead>
<tr>
<th><strong>Coluna</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Descrição</strong></p>
</blockquote></th>
</tr>
</thead>
<tbody>
<tr>
<td>id</td>
<td>BIGSERIAL PK</td>
<td><blockquote>
<p>Identificador único.</p>
</blockquote></td>
</tr>
<tr>
<td>employee_id</td>
<td>BIGINT NOT NULL FK→employees</td>
<td><blockquote>
<p>Funcionário.</p>
</blockquote></td>
</tr>
<tr>
<td>career_id</td>
<td>BIGINT NOT NULL FK→careers</td>
<td><blockquote>
<p>Carreira.</p>
</blockquote></td>
</tr>
<tr>
<td>category_id</td>
<td>BIGINT NOT NULL FK→categories</td>
<td><blockquote>
<p>Categoria (validada vs. carreira).</p>
</blockquote></td>
</tr>
<tr>
<td>grade_id</td>
<td>BIGINT NOT NULL FK→grades</td>
<td><blockquote>
<p>Escalão (validado vs. categoria).</p>
</blockquote></td>
</tr>
<tr>
<td>function_id</td>
<td>BIGINT FK→functions</td>
<td><blockquote>
<p>Função.</p>
</blockquote></td>
</tr>
<tr>
<td>start_date</td>
<td>DATE NOT NULL</td>
<td><blockquote>
<p>Início (≥ admissão).</p>
</blockquote></td>
</tr>
<tr>
<td>end_date</td>
<td>DATE</td>
<td><blockquote>
<p>Fim.</p>
</blockquote></td>
</tr>
<tr>
<td>is_current</td>
<td>BOOLEAN NOT NULL DEFAULT FALSE</td>
<td><blockquote>
<p>Enquadramento corrente (único por funcionário).</p>
</blockquote></td>
</tr>
<tr>
<td>legal_base</td>
<td>VARCHAR(200)</td>
<td><blockquote>
<p>Despacho ou base legal.</p>
</blockquote></td>
</tr>
</tbody>
</table>

16. **documents**

<table style="width:97%;">
<colgroup>
<col style="width: 27%" />
<col style="width: 33%" />
<col style="width: 36%" />
</colgroup>
<thead>
<tr>
<th><strong>Coluna</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Descrição</strong></p>
</blockquote></th>
</tr>
</thead>
<tbody>
<tr>
<td>id</td>
<td>BIGSERIAL PK</td>
<td><blockquote>
<p>Identificador único.</p>
</blockquote></td>
</tr>
<tr>
<td>employee_id</td>
<td>BIGINT FK→employees</td>
<td><blockquote>
<p>Funcionário associado (opcional).</p>
</blockquote></td>
</tr>
<tr>
<td>document_type_id</td>
<td><p>BIGINT NOT NULL</p>
<p>FK→document_types</p></td>
<td><blockquote>
<p>Tipo de documento.</p>
</blockquote></td>
</tr>
<tr>
<td>file_name</td>
<td>VARCHAR(255) NOT NULL</td>
<td><blockquote>
<p>Nome original.</p>
</blockquote></td>
</tr>
<tr>
<td>storage_key</td>
<td>VARCHAR(500) NOT NULL</td>
<td><blockquote>
<p>Chave no storage.</p>
</blockquote></td>
</tr>
<tr>
<td>mime_type</td>
<td>VARCHAR(100) NOT NULL</td>
<td><blockquote>
<p>MIME type.</p>
</blockquote></td>
</tr>
<tr>
<td>size_bytes</td>
<td>BIGINT NOT NULL</td>
<td><blockquote>
<p>Tamanho.</p>
</blockquote></td>
</tr>
<tr>
<td>description</td>
<td>TEXT</td>
<td><blockquote>
<p>Descrição.</p>
</blockquote></td>
</tr>
<tr>
<td>uploaded_at</td>
<td>TIMESTAMP NOT NULL</td>
<td><blockquote>
<p>Data de upload.</p>
</blockquote></td>
</tr>
</tbody>
</table>

<table style="width:97%;">
<colgroup>
<col style="width: 27%" />
<col style="width: 33%" />
<col style="width: 36%" />
</colgroup>
<thead>
<tr>
<th>uploaded_by</th>
<th>BIGINT NOT NULL</th>
<th><blockquote>
<p>Utilizador que carregou.</p>
</blockquote></th>
</tr>
</thead>
<tbody>
</tbody>
</table>

17. **leave_balances**

<table style="width:97%;">
<colgroup>
<col style="width: 27%" />
<col style="width: 33%" />
<col style="width: 36%" />
</colgroup>
<thead>
<tr>
<th><strong>Coluna</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Descrição</strong></p>
</blockquote></th>
</tr>
</thead>
<tbody>
<tr>
<td>id</td>
<td>BIGSERIAL PK</td>
<td><blockquote>
<p>Identificador único.</p>
</blockquote></td>
</tr>
<tr>
<td>employee_id</td>
<td>BIGINT NOT NULL FK→employees</td>
<td><blockquote>
<p>Funcionário.</p>
</blockquote></td>
</tr>
<tr>
<td>leave_type_id</td>
<td>BIGINT NOT NULL FK→leave_types</td>
<td><blockquote>
<p>Tipo de ausência.</p>
</blockquote></td>
</tr>
<tr>
<td>year</td>
<td>INT NOT NULL</td>
<td><blockquote>
<p>Ano de referência.</p>
</blockquote></td>
</tr>
<tr>
<td>assigned_days</td>
<td>NUMERIC(5,2) NOT NULL</td>
<td><blockquote>
<p>Dias atribuídos.</p>
</blockquote></td>
</tr>
<tr>
<td>used_days</td>
<td>NUMERIC(5,2) NOT NULL DEFAULT 0</td>
<td><blockquote>
<p>Dias utilizados.</p>
</blockquote></td>
</tr>
<tr>
<td>UQ</td>
<td>(employee_id, leave_type_id, year)</td>
<td><blockquote>
<p>Único por colaborador/tipo/ano.</p>
</blockquote></td>
</tr>
</tbody>
</table>

18. **leave_requests**

<table style="width:97%;">
<colgroup>
<col style="width: 27%" />
<col style="width: 33%" />
<col style="width: 36%" />
</colgroup>
<thead>
<tr>
<th><strong>Coluna</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Descrição</strong></p>
</blockquote></th>
</tr>
</thead>
<tbody>
<tr>
<td>id</td>
<td>BIGSERIAL PK</td>
<td><blockquote>
<p>Identificador único.</p>
</blockquote></td>
</tr>
<tr>
<td>employee_id</td>
<td>BIGINT NOT NULL FK→employees</td>
<td><blockquote>
<p>Requerente.</p>
</blockquote></td>
</tr>
<tr>
<td>leave_type_id</td>
<td>BIGINT NOT NULL FK→leave_types</td>
<td><blockquote>
<p>Tipo.</p>
</blockquote></td>
</tr>
<tr>
<td>start_date</td>
<td>DATE NOT NULL</td>
<td><blockquote>
<p>Início.</p>
</blockquote></td>
</tr>
<tr>
<td>end_date</td>
<td>DATE NOT NULL</td>
<td><blockquote>
<p>Fim.</p>
</blockquote></td>
</tr>
<tr>
<td>working_days</td>
<td>NUMERIC(5,2) NOT NULL</td>
<td><blockquote>
<p>Dias úteis calculados.</p>
</blockquote></td>
</tr>
<tr>
<td>justification</td>
<td>TEXT</td>
<td><blockquote>
<p>Justificação.</p>
</blockquote></td>
</tr>
<tr>
<td>status</td>
<td>VARCHAR(20) NOT NULL</td>
<td><blockquote>
<p>PENDING, APPROVED, REJECTED, CANCELLED.</p>
</blockquote></td>
</tr>
<tr>
<td>approver_id</td>
<td>BIGINT FK→employees</td>
<td><blockquote>
<p>Aprovador.</p>
</blockquote></td>
</tr>
<tr>
<td>decision_date</td>
<td>TIMESTAMP</td>
<td><blockquote>
<p>Data da decisão.</p>
</blockquote></td>
</tr>
<tr>
<td>rejection_reason</td>
<td>TEXT</td>
<td><blockquote>
<p>Motivo (em rejeição).</p>
</blockquote></td>
</tr>
<tr>
<td>attachment_document_id</td>
<td>BIGINT FK→documents</td>
<td><blockquote>
<p>Anexo de suporte.</p>
</blockquote></td>
</tr>
</tbody>
</table>

19. **payroll_slips**

<table style="width:97%;">
<colgroup>
<col style="width: 27%" />
<col style="width: 33%" />
<col style="width: 36%" />
</colgroup>
<thead>
<tr>
<th><strong>Coluna</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Descrição</strong></p>
</blockquote></th>
</tr>
</thead>
<tbody>
<tr>
<td>id</td>
<td>BIGSERIAL PK</td>
<td><blockquote>
<p>Identificador único.</p>
</blockquote></td>
</tr>
<tr>
<td>employee_id</td>
<td>BIGINT NOT NULL FK→employees</td>
<td><blockquote>
<p>Funcionário.</p>
</blockquote></td>
</tr>
<tr>
<td>period_month</td>
<td>INT NOT NULL</td>
<td><blockquote>
<p>Mês (1-12).</p>
</blockquote></td>
</tr>
<tr>
<td>period_year</td>
<td>INT NOT NULL</td>
<td><blockquote>
<p>Ano.</p>
</blockquote></td>
</tr>
<tr>
<td>issue_date</td>
<td>DATE NOT NULL</td>
<td><blockquote>
<p>Data de emissão.</p>
</blockquote></td>
</tr>
<tr>
<td>gross_salary</td>
<td>NUMERIC(12,2) NOT NULL</td>
<td><blockquote>
<p>Salário ilíquido.</p>
</blockquote></td>
</tr>
<tr>
<td>net_salary</td>
<td>NUMERIC(12,2) NOT NULL</td>
<td><blockquote>
<p>Salário líquido.</p>
</blockquote></td>
</tr>
<tr>
<td>document_id</td>
<td>BIGINT FK→documents</td>
<td><blockquote>
<p>PDF do recibo.</p>
</blockquote></td>
</tr>
<tr>
<td>UQ</td>
<td>(employee_id, period_month, period_year)</td>
<td><blockquote>
<p>Único.</p>
</blockquote></td>
</tr>
</tbody>
</table>

20. **leave_mobility_records**

<table style="width:97%;">
<colgroup>
<col style="width: 27%" />
<col style="width: 33%" />
<col style="width: 36%" />
</colgroup>
<thead>
<tr>
<th><strong>Coluna</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Descrição</strong></p>
</blockquote></th>
</tr>
</thead>
<tbody>
<tr>
<td>id</td>
<td>BIGSERIAL PK</td>
<td><blockquote>
<p>Identificador único.</p>
</blockquote></td>
</tr>
<tr>
<td>employee_id</td>
<td>BIGINT NOT NULL FK→employees</td>
<td><blockquote>
<p>Colaborador alvo.</p>
</blockquote></td>
</tr>
<tr>
<td>subtype_id</td>
<td><p>BIGINT NOT NULL</p>
<p>FK→leave_mobility_subtypes</p></td>
<td><blockquote>
<p>Subtipo.</p>
</blockquote></td>
</tr>
<tr>
<td>record_type</td>
<td>VARCHAR(20) NOT NULL</td>
<td><blockquote>
<p>LICENCA ou MOBILIDADE.</p>
</blockquote></td>
</tr>
<tr>
<td>start_date</td>
<td>DATE NOT NULL</td>
<td><blockquote>
<p>Início.</p>
</blockquote></td>
</tr>
<tr>
<td>end_date</td>
<td>DATE</td>
<td><blockquote>
<p>Fim.</p>
</blockquote></td>
</tr>
<tr>
<td>is_temporary</td>
<td>BOOLEAN NOT NULL DEFAULT TRUE</td>
<td><blockquote>
<p>Reversão prevista.</p>
</blockquote></td>
</tr>
<tr>
<td>target_unit_id</td>
<td>BIGINT FK→organizational_units</td>
<td><blockquote>
<p>Unidade de destino (mobilidade).</p>
</blockquote></td>
</tr>
<tr>
<td>target_function_id</td>
<td>BIGINT FK→functions</td>
<td><blockquote>
<p>Função de destino (mobilidade).</p>
</blockquote></td>
</tr>
<tr>
<td>legal_base</td>
<td>VARCHAR(200)</td>
<td><blockquote>
<p>Despacho/base legal.</p>
</blockquote></td>
</tr>
<tr>
<td>justification</td>
<td>TEXT</td>
<td><blockquote>
<p>Justificação.</p>
</blockquote></td>
</tr>
</tbody>
</table>

<table style="width:97%;">
<colgroup>
<col style="width: 27%" />
<col style="width: 33%" />
<col style="width: 36%" />
</colgroup>
<thead>
<tr>
<th>status</th>
<th>VARCHAR(20) NOT NULL</th>
<th><blockquote>
<p>PENDING, APPROVED, REJECTED, CLOSED, CANCELLED.</p>
</blockquote></th>
</tr>
</thead>
<tbody>
<tr>
<td>approver_id</td>
<td>BIGINT FK→employees</td>
<td><blockquote>
<p>Aprovador.</p>
</blockquote></td>
</tr>
<tr>
<td>decision_date</td>
<td>TIMESTAMP</td>
<td><blockquote>
<p>Data da decisão.</p>
</blockquote></td>
</tr>
<tr>
<td>rejection_reason</td>
<td>TEXT</td>
<td><blockquote>
<p>Motivo (em rejeição).</p>
</blockquote></td>
</tr>
</tbody>
</table>

21. **leave_mobility_attachments**

<table style="width:97%;">
<colgroup>
<col style="width: 27%" />
<col style="width: 33%" />
<col style="width: 36%" />
</colgroup>
<thead>
<tr>
<th><strong>Coluna</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Descrição</strong></p>
</blockquote></th>
</tr>
</thead>
<tbody>
<tr>
<td>id</td>
<td>BIGSERIAL PK</td>
<td><blockquote>
<p>Identificador único.</p>
</blockquote></td>
</tr>
<tr>
<td>record_id</td>
<td><p>BIGINT NOT NULL</p>
<p>FK→leave_mobility_records</p></td>
<td><blockquote>
<p>Registo associado.</p>
</blockquote></td>
</tr>
<tr>
<td>document_id</td>
<td>BIGINT NOT NULL FK→documents</td>
<td><blockquote>
<p>Documento anexado.</p>
</blockquote></td>
</tr>
<tr>
<td>uploaded_at</td>
<td>TIMESTAMP NOT NULL</td>
<td><blockquote>
<p>Data.</p>
</blockquote></td>
</tr>
<tr>
<td>uploaded_by</td>
<td>BIGINT NOT NULL</td>
<td><blockquote>
<p>Utilizador.</p>
</blockquote></td>
</tr>
</tbody>
</table>

22. **employee_external_mapping**

<table style="width:97%;">
<colgroup>
<col style="width: 27%" />
<col style="width: 33%" />
<col style="width: 36%" />
</colgroup>
<thead>
<tr>
<th><strong>Coluna</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Descrição</strong></p>
</blockquote></th>
</tr>
</thead>
<tbody>
<tr>
<td>id</td>
<td>BIGSERIAL PK</td>
<td><blockquote>
<p>Identificador único.</p>
</blockquote></td>
</tr>
<tr>
<td>employee_id</td>
<td>BIGINT NOT NULL FK→employees</td>
<td><blockquote>
<p>Funcionário SIPPROG.</p>
</blockquote></td>
</tr>
<tr>
<td>system_code</td>
<td>VARCHAR(30) NOT NULL</td>
<td><blockquote>
<p>Sistema externo (ex.: 'SAD').</p>
</blockquote></td>
</tr>
<tr>
<td>external_id</td>
<td>VARCHAR(100) NOT NULL</td>
<td><blockquote>
<p>Identificador no sistema externo.</p>
</blockquote></td>
</tr>
<tr>
<td>external_username</td>
<td>VARCHAR(100)</td>
<td><blockquote>
<p>Username opcional.</p>
</blockquote></td>
</tr>
<tr>
<td>UQ</td>
<td>(employee_id, system_code)</td>
<td><blockquote>
<p>Único.</p>
</blockquote></td>
</tr>
</tbody>
</table>

23. **change_history**

<table style="width:97%;">
<colgroup>
<col style="width: 27%" />
<col style="width: 33%" />
<col style="width: 36%" />
</colgroup>
<thead>
<tr>
<th><strong>Coluna</strong></th>
<th><strong>Tipo</strong></th>
<th><blockquote>
<p><strong>Descrição</strong></p>
</blockquote></th>
</tr>
</thead>
<tbody>
<tr>
<td>id</td>
<td>BIGSERIAL PK</td>
<td><blockquote>
<p>Identificador único.</p>
</blockquote></td>
</tr>
<tr>
<td>table_name</td>
<td>VARCHAR(100) NOT NULL</td>
<td><blockquote>
<p>Tabela alterada.</p>
</blockquote></td>
</tr>
<tr>
<td>record_id</td>
<td>BIGINT NOT NULL</td>
<td><blockquote>
<p>PK do registo.</p>
</blockquote></td>
</tr>
</tbody>
</table>

<table style="width:97%;">
<colgroup>
<col style="width: 27%" />
<col style="width: 33%" />
<col style="width: 36%" />
</colgroup>
<thead>
<tr>
<th>operation</th>
<th>VARCHAR(10) NOT NULL</th>
<th><blockquote>
<p>INSERT, UPDATE, DELETE.</p>
</blockquote></th>
</tr>
</thead>
<tbody>
<tr>
<td>changed_at</td>
<td>TIMESTAMP NOT NULL</td>
<td><blockquote>
<p>Quando.</p>
</blockquote></td>
</tr>
<tr>
<td>changed_by</td>
<td>BIGINT</td>
<td><blockquote>
<p>Utilizador.</p>
</blockquote></td>
</tr>
<tr>
<td>old_data</td>
<td>JSONB</td>
<td><blockquote>
<p>Snapshot anterior.</p>
</blockquote></td>
</tr>
<tr>
<td>new_data</td>
<td>JSONB</td>
<td><blockquote>
<p>Snapshot posterior.</p>
</blockquote></td>
</tr>
<tr>
<td>origin</td>
<td>VARCHAR(50)</td>
<td><blockquote>
<p>Origem (ex.: 'self-service', 'admin').</p>
</blockquote></td>
</tr>
</tbody>
</table>

## Tabelas Externas Referenciadas (Avaliação de Desempenho)

> As entidades evaluation_cycles, performance_evaluations, objectives e
> objective_updates são mantidas no esquema do SAD (Sistema externo de
> Avaliação de Desempenho) e não fazem parte do modelo físico do
> SIPPROG. O acesso é exclusivo via API REST do SAD, mediado pelos
> endpoints /external/\* documentados no Capítulo 6.

# Triggers e Funções de Base de Dados

## fn_audit_generic

> Função genérica de auditoria, ligada a triggers AFTER INSERT, UPDATE,
> DELETE em todas as tabelas de domínio (employees, leave_requests,
> leave_mobility_records, etc.). Regista em change_history uma linha com
> (table_name, record_id, operation, changed_by, old_data JSONB,
> new_data JSONB, origin).

## fn_validate_professional_assignment

> Trigger BEFORE INSERT, UPDATE em employee_professional_assignments.
> Garante que a category pertence à career indicada e que o grade
> pertence à category. Adicionalmente, ao ativar um novo is_current =
> true, encerra o anterior (set is_current = false; end_date =
> new.start_date − 1).

## fn_apply_mobility

> Função invocada quando uma mobilidade transita para o estado APPROVED.
> Encerra a employee_unit_assignments principal corrente (end_date) e
> cria uma nova com a target_unit_id indicada na mobilidade. Em CLOSED
> de mobilidades temporárias, restaura a atribuição anterior.
>
> CREATE OR REPLACE FUNCTION fn_apply_mobility() RETURNS TRIGGER AS \$\$
> BEGIN
>
> IF NEW.status = 'APPROVED' AND OLD.status \<\> 'APPROVED' THEN
>
> -- encerra atribuição principal corrente UPDATE
> employee_unit_assignments
>
> SET end_date = NEW.start_date - INTERVAL '1 day' WHERE employee_id =
> NEW.employee_id
>
> AND is_primary = TRUE AND end_date IS NULL;
>
> -- cria a nova atribuição principal INSERT INTO
> employee_unit_assignments
>
> (employee_id, unit_id, is_primary, start_date)
>
> VALUES (NEW.employee_id, NEW.target_unit_id, TRUE, NEW.start_date);
> END IF;
>
> RETURN NEW;
>
> END; \$\$ LANGUAGE plpgsql;

## fn_set_updated_at

> Trigger BEFORE UPDATE em todas as tabelas com coluna updated_at.
> Define automaticamente updated_at = CURRENT_TIMESTAMP.

# Considerações de Implementação

## Migrations e Seed

> A gestão das migrations da base de dados deve seguir uma ferramenta
> versionada (Flyway ou Liquibase). O esquema RH é instalado em scripts
> ordenados por número (V01 create_lookups.sql,
>
> V02 create_org.sql, ...). Os seeds populam os códigos núcleo de cada
> parametrização (ACTIVE, INACTIVE, FERIAS, etc.) e devem ser
> idempotentes.

## Validação em Camadas

> As validações estão distribuídas em três camadas. A camada de DTO
> valida formato, comprimento e obrigatoriedade. A camada de serviço
> valida regras de negócio (sobreposição, saldo, hierarquia). A camada
> de base de dados valida integridade (foreign keys, unique, triggers).
> Erros são encapsulados em respostas JSON estruturadas conforme 0.4.

## Soft Delete e Auditoria

> Todas as operações DELETE são soft delete (is_active = false),
> preservando o histórico. As alterações são registadas em
> change_history pelo trigger fn_audit_generic. O campo
> created_by/updated_by é populado pela aplicação a partir da claim sub
> do JWT.

## Gestão de Ficheiros

> Os ficheiros são armazenados num storage de objetos (S3 compatível ou
> MinIO). A tabela documents guarda apenas a chave (storage_key) e os
> metadados. URLs pré-assinados (presigned URLs) são usadas para
> download direto sem comprometer a segurança.

## Cálculo de Dias Úteis e Calendário

> O cálculo de dias úteis em pedidos de ausência exclui sábados,
> domingos e os feriados configurados numa tabela calendar_holidays
> (ano, data, designação). Esta tabela é gerida pelo administrador do
> sistema antes do início de cada ano.

## Estratégia de Testes

> Testes unitários em serviços e validadores. Testes de integração com
> testcontainers (PostgreSQL real) para validar queries, triggers e
> regras. Testes contractuais (Pact) na integração com o SAD. Cobertura
> mínima recomendada: 80% nas camadas de serviço.

## Observabilidade

## 

> Logs estruturados em JSON enviados para o stack ELK; métricas
> (Prometheus) com latência e taxa de erro por endpoint; tracing
> distribuído (OpenTelemetry) com correlação ID propagada na integração
> com o SAD.

## Integrações Externas

> Para além do SAD (Capítulo 6), o Módulo RH pode integrar-se com o
> módulo de Autenticação Central (JWT), o módulo de Notificações (avisos
> de aprovação/rejeição), o módulo Financeiro (recibos), e o sistema de
> assiduidade (importação de marcações). Cada integração documenta-se em
> ADRs próprios.
