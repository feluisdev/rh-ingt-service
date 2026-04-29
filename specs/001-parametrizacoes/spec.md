# Especificação Funcional: Catálogos de Parametrização do Módulo RH

**Feature Branch**: `001-parametrizacoes`
**Created**: 2026-04-29
**Status**: Draft
**Input**: User description: "Catálogos de Parametrização do Módulo RH — gestão centralizada dos catálogos auxiliares (com comportamento e puros de etiqueta) que alimentam todo o sistema, sem necessidade de alterar código ou fazer novo deploy."

---

## Clarifications

### Session 2026-04-29

- Q: Quais perfis podem escrever em catálogos de parametrização? → A: **Diferido** — a definição final dos perfis de autorização está pendente de decisão do projecto e será fechada em momento posterior. A spec deixa a autorização descrita como "perfil administrativo" sem fixar roles concretas.
- Q: Modelo de localização para entradas do catálogo de etiquetas? → A: Uma linha por par (grupo, chave, idioma). O idioma `pt-CV` é obrigatório no momento da criação; outros idiomas são opcionais e podem ser adicionados depois quando o sistema for internacionalizado.
- Q: Pode uma entrada desactivada ser reactivada? → A: Sim, reactivação livre sem restrições adicionais, registada em auditoria. Permite corrigir desactivações por engano e voltar a utilizar entradas previamente desactivadas, sem precisar de criar novas entradas com códigos diferentes.
- Q: Qual o período de retenção do histórico de auditoria? → A: Retenção indefinida. O histórico de alterações em parametrizações é mantido para sempre, sem expurgo automático, para cumprir os requisitos de rastreabilidade do sector público de Cabo Verde (auditoria do Tribunal de Contas).
- Q: Qual a extensão do seed inicial dos catálogos? → A: Seed completo "kit Cabo Verde" — todos os 22 concelhos e 10 ilhas, ~15 nacionalidades (lusófonas + parceiros principais), todos os tipos de ausência da Lei 20/X/2023, todos os tipos de contrato do Decreto-Lei 4/2024, estados núcleo do trabalhador, situações profissionais base, tipos de documento e formação base. O sistema arranca operacional sem necessidade de trabalho manual; administradores podem desactivar ou acrescentar mais depois.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Configurar Catálogos de Etiqueta para Formulários (Priority: P1)

Como administrador de RH do INGT, preciso de gerir os catálogos de etiqueta que alimentam todos os dropdowns do sistema (estados civis, sexos, nacionalidades, ilhas, concelhos, tipos de unidade orgânica, níveis de habilitação literária, tipos de parentesco, tipos de formação, categorias de documento e categorias de ausência) sem necessidade de alterar código ou fazer novo deploy.

**Why this priority**: Sem este catálogo de etiquetas, nenhum formulário do sistema pode ser apresentado correctamente. É o pré-requisito mínimo para qualquer outra feature do RH (criar funcionário, criar habilitação, criar formação, etc.). Todos os outros sub-módulos dependem disto.

**Independent Test**: Pode ser totalmente testado abrindo um formulário de cadastro de funcionário no frontend e confirmando que os dropdowns "Estado Civil", "Sexo", "Nacionalidade", "Ilha" e "Concelho" mostram as opções correctas, traduzidas em pt-CV, ordenadas por relevância. O administrador de RH adiciona uma nova entrada (ex: nova nacionalidade) e a opção aparece imediatamente no dropdown sem qualquer reinício de aplicação.

**Acceptance Scenarios**:

1. **Given** o administrador de RH está autenticado, **When** acede ao catálogo "Estado Civil", **Then** vê a lista das opções existentes (Solteiro(a), Casado(a), União de Facto, Divorciado(a), Viúvo(a)) ordenadas por ordem de exibição.
2. **Given** o administrador de RH visualiza o catálogo "Nacionalidade", **When** adiciona uma nova entrada com chave `GW` e label "Guiné-Bissau", **Then** a nova opção aparece no dropdown da página de criação de funcionário sem qualquer reinício.
3. **Given** existe a entrada "Solteiro" no catálogo "Estado Civil", **When** o administrador a desactiva, **Then** a opção deixa de aparecer em novos dropdowns mas continua visível nos registos antigos que a referenciam.
4. **Given** um utilizador autenticado sem perfil de administração, **When** tenta criar uma nova entrada num catálogo, **Then** o sistema rejeita com mensagem clara de falta de permissão.

---

### User Story 2 — Configurar Catálogos com Comportamento de Negócio (Priority: P1)

Como administrador de RH, preciso de manter catálogos cujas entradas têm regras de comportamento próprias que afectam o processamento do sistema: estados do trabalhador (com protecção dos estados núcleo), situações profissionais (Efectivo, Contratado, Comissionado, Estagiário), tipos de contrato (Nomeação Definitiva, CTFP, Comissão de Serviço), tipos de documento (com extensões permitidas no upload), tipos de ausência (com regras de saldo e aprovação) e subtipos de licença/mobilidade (com efeito em remuneração e antiguidade).

**Why this priority**: Sem estes catálogos, módulos críticos como o cadastro de contratos, registo de ausências, validação de uploads e cálculo de antiguidade não podem operar. Os comportamentos (flags) controlam fluxos de trabalho do sistema e têm implicações legais (ex: tipos de contrato seguem o Decreto-Lei 4/2024).

**Independent Test**: Pode ser testado criando um novo tipo de ausência "Licença Sem Vencimento" com a flag "desconta saldo anual" desactivada e a flag "exige aprovação" activa, e depois validando no fluxo de pedido de ausência (em outra feature) que essa entrada não desconta dias do saldo anual mas exige aprovação da chefia.

**Acceptance Scenarios**:

1. **Given** o estado núcleo "Activo" está marcado como protegido (núcleo do sistema), **When** o administrador tenta desactivá-lo, **Then** o sistema bloqueia a operação e devolve mensagem explicando que o estado é núcleo do sistema.
2. **Given** existe o tipo de ausência "Férias" com a flag "desconta saldo anual" activa, **When** um colaborador submete um pedido de Férias, **Then** o sistema deve consumir dias do saldo anual (validado nesta feature ao nível de modelo de dados, comportamento completo é validado em feature de Ausências).
3. **Given** existe o tipo de documento "CNI" com extensões permitidas "pdf,jpg", **When** o administrador tenta criar a mesma entrada com extensão "exe", **Then** o sistema rejeita por extensão não suportada.
4. **Given** existe um tipo de contrato "CTFP a Termo Certo" referenciado por contratos activos de funcionários, **When** o administrador tenta desactivar o tipo de contrato, **Then** o sistema bloqueia a desactivação e indica que existem registos a referenciá-lo.
5. **Given** o administrador cria um novo tipo de ausência, **When** preenche o campo "código" com "FERIAS_2026" e regista, **Then** ao tentar editar, o campo "código" não é editável (apenas o nome de exibição).

---

### User Story 3 — Configurar Feriados Nacionais e Municipais (Priority: P2)

Como administrador de RH, preciso de manter o calendário de feriados nacionais e municipais de Cabo Verde para que o sistema calcule correctamente os dias úteis de pedidos de ausência (excluindo feriados além de fins-de-semana).

**Why this priority**: Sem este catálogo, o cálculo de dias úteis de ausências fica comprometido. Pode ser P2 porque as ausências (que dependem deste catálogo) são tratadas em feature posterior; até lá os feriados podem ser introduzidos sem urgência. Mas é necessário antes de a feature de Ausências ser implementada.

**Independent Test**: O administrador insere o feriado "Dia da Independência" a 5 de Julho como nacional, e o feriado "Dia de Santiago" a 25 de Julho como municipal (concelho da Praia). Ao consultar a lista de feriados de 2026, ambas as entradas aparecem com a respectiva categorização.

**Acceptance Scenarios**:

1. **Given** o calendário não contém ainda o feriado "Dia da Liberdade", **When** o administrador insere a entrada para 13 de Janeiro com o tipo "nacional", **Then** o feriado fica disponível para consulta e referência por outros módulos.
2. **Given** existem feriados nacionais e municipais no sistema, **When** o utilizador filtra por "apenas nacionais", **Then** apenas os feriados aplicáveis a todo o território são devolvidos.

---

### User Story 4 — Auditoria Completa de Alterações (Priority: P2)

Como administrador de sistema, preciso de saber quem alterou cada entrada de cada catálogo, quando, e qual foi a alteração, para garantir rastreabilidade e cumprir requisitos de auditoria do sector público.

**Why this priority**: A auditoria é um princípio do projecto (Princípio IV da constituição) e um requisito do sector público. Não bloqueia funcionalidade mas é mandatório antes do go-live em produção.

**Independent Test**: Após uma alteração no catálogo "Estado Civil" (renomear "Solteiro" para "Solteiro(a)"), o registo de auditoria mostra: utilizador autor, timestamp, valor anterior, valor novo, e o tipo de operação (UPDATE).

**Acceptance Scenarios**:

1. **Given** o utilizador "ana.silva" altera o nome de exibição de uma entrada do catálogo, **When** se consulta o histórico, **Then** vê-se que ana.silva fez a alteração à hora X, com valores antes/depois.
2. **Given** o utilizador desactiva uma entrada, **When** se consulta o histórico, **Then** o registo de auditoria captura o evento de desactivação com autor e momento.

---

### Edge Cases

- O que acontece quando um administrador tenta criar duas entradas com o mesmo `código` no mesmo grupo? Sistema rejeita por unicidade.
- O que acontece se um catálogo ficar vazio (todas as entradas desactivadas)? Sistema permite desactivar todas excepto as marcadas como núcleo. Os formulários afectados podem ficar sem opções, sendo responsabilidade do administrador manter pelo menos uma entrada activa.
- O que acontece quando duas pessoas editam a mesma entrada simultaneamente? Última escrita ganha; a auditoria deixa rasto de ambas as alterações.
- O que acontece quando o sistema é instalado pela primeira vez (catálogos vazios)? O processo de inicialização (seed) popula automaticamente os 11 grupos do catálogo de etiquetas e os 4 estados núcleo.
- O que acontece se for tentada uma desactivação de uma entrada referenciada por registos com estado lógico inactivo? Bloqueio aplica-se apenas a referências activas; o sistema permite a desactivação se todas as referências estiverem inactivas.
- O que acontece quando se tenta inserir um feriado em data passada? Sistema permite (útil para registar histórico), mas adverte o utilizador.

---

## Requirements *(mandatory)*

### Functional Requirements

**Gestão geral de catálogos**

- **FR-001**: O sistema MUST permitir que o administrador de RH crie, visualize, edite e desactive entradas em qualquer catálogo do módulo de Parametrizações.
- **FR-002**: O sistema MUST suportar a desactivação lógica (sem remoção física) de qualquer entrada, preservando o histórico e a referência por registos antigos.
- **FR-002a**: O sistema MUST permitir a reactivação de entradas previamente desactivadas, sem restrições adicionais, registando o evento em auditoria. A reactivação não cria uma nova entrada — restaura o estado activo da entrada existente preservando o seu código original.
- **FR-003**: O sistema MUST bloquear a desactivação de entradas que estejam a ser referenciadas por registos activos noutros módulos, e devolver mensagem clara que indique a razão do bloqueio.
- **FR-004**: O sistema MUST manter o `código` de cada entrada imutável após a criação; apenas o nome de exibição e os campos descritivos devem ser editáveis.
- **FR-005**: O sistema MUST garantir unicidade do `código` dentro de cada catálogo (e por grupo, no caso do catálogo genérico de etiquetas).

**Catálogo genérico de etiquetas**

- **FR-006**: O sistema MUST disponibilizar um catálogo genérico capaz de armazenar 11 grupos de etiquetas: Estados Civis, Sexo, Nacionalidades, Tipos de Unidade Orgânica, Categorias de Documento, Categorias de Ausência, Níveis de Habilitação Literária, Tipos de Parentesco, Ilhas de Cabo Verde, Concelhos, Tipos de Formação.
- **FR-007**: O sistema MUST suportar localização linguística do catálogo de etiquetas armazenando uma linha por par (grupo, chave, idioma). O idioma `pt-CV` é obrigatório no momento da criação de qualquer entrada; outras línguas são opcionais e podem ser adicionadas posteriormente sem reestruturar os dados existentes.
- **FR-007a**: O sistema MUST permitir consultar o catálogo filtrando por idioma; quando a tradução pedida não existe, devolve o valor em `pt-CV` (fallback).
- **FR-008**: O sistema MUST suportar uma ordem de exibição configurável para cada entrada, permitindo controlar a sequência em dropdowns.

**Catálogos com comportamento — protecção de núcleo**

- **FR-009**: O sistema MUST permitir marcar entradas do catálogo "Estados do Trabalhador" como núcleo do sistema (`is_core`); estas entradas não podem ser desactivadas pelo administrador.
- **FR-010**: O sistema MUST garantir, na inicialização, que os estados núcleo "Activo" e "Inactivo" existem e estão marcados como protegidos.

**Catálogos com comportamento — flags de processamento**

- **FR-011**: O sistema MUST permitir, no catálogo "Tipos de Ausência", configurar para cada entrada se desconta saldo anual de férias, se exige aprovação da chefia e qual o limite máximo legal de dias por ano.
- **FR-012**: O sistema MUST permitir, no catálogo "Subtipos de Licença e Mobilidade", configurar para cada entrada se afecta a remuneração, se conta para antiguidade e se pode ser auto-submetido pelo colaborador.
- **FR-013**: O sistema MUST permitir, no catálogo "Tipos de Documento", configurar quais as extensões de ficheiro permitidas no upload (ex: `pdf,jpg,png`) e a categoria associada (Pessoal, Contratual, Formação, Disciplinar, Avaliação).
- **FR-014**: O sistema MUST permitir, no catálogo "Subtipos de Licença e Mobilidade", classificar cada entrada como aplicável a "Licença", "Mobilidade" ou "Ambos".

**Tipos de Contrato**

- **FR-015**: O sistema MUST disponibilizar um catálogo "Tipos de Contrato" com a capacidade de configurar Nomeação Definitiva, Contrato de Função Pública (CFP), CTFP a Termo Certo, CTFP a Termo Incerto e Comissão de Serviço, com descrição da base legal aplicável.

**Calendário de Feriados**

- **FR-016**: O sistema MUST permitir registar feriados com data, designação, e classificação como nacional (aplicável a todo o território) ou municipal (aplicável a um concelho).
- **FR-017**: O sistema MUST garantir unicidade de cada data de feriado nacional.

**Autorização**

- **FR-018**: O sistema MUST restringir as operações de criação, alteração e desactivação a um perfil administrativo (perfis concretos a definir em decisão posterior do projecto); a leitura é permitida a qualquer utilizador autenticado.

**Auditoria**

- **FR-019**: O sistema MUST registar para cada criação, alteração, desactivação ou reactivação: utilizador autor, momento, valor anterior e valor novo.
- **FR-020**: O sistema MUST disponibilizar a consulta do histórico de alterações de cada entrada de catálogo.
- **FR-020a**: O sistema MUST manter o histórico de auditoria de forma indefinida, sem expurgo automático, para cumprir requisitos de rastreabilidade do sector público (auditoria do Tribunal de Contas de Cabo Verde).

**Inicialização e seed**

- **FR-021**: O sistema MUST, no primeiro deploy, popular automaticamente os 11 grupos do catálogo de etiquetas com o "kit Cabo Verde" — todos os 5 estados civis, ambos os sexos (M/F), os 22 concelhos e 10 ilhas de Cabo Verde, ~15 nacionalidades (lusófonas + parceiros principais), 5 níveis de habilitação literária (Básico, Secundário, Licenciatura, Mestrado, Doutoramento), tipos de parentesco principais (Cônjuge, Filho, Pai, Mãe, Irmão), tipos de unidade orgânica (Direcção, Departamento, Divisão, Secção), categorias de documento (Pessoal, Contratual, Formação, Disciplinar, Avaliação), categorias de ausência (Férias, Doença, Família, Outro) e tipos de formação (Presencial, e-Learning, Seminário, Congresso). Todas as entradas com tradução em `pt-CV`.
- **FR-022**: O sistema MUST, no primeiro deploy, popular os catálogos com comportamento com os valores base previstos pela legislação cabo-verdiana: estados do trabalhador (Activo, Inactivo) marcados como núcleo; situações profissionais (Efectivo, Contratado, Comissionado, Estagiário); todos os tipos de contrato do Decreto-Lei 4/2024 (Nomeação Definitiva, CFP, CTFP a Termo Certo, CTFP a Termo Incerto, Comissão de Serviço); todos os tipos de ausência da Lei 20/X/2023 (Férias, Doença, Maternidade, Paternidade, Luto, Casamento) com flags pré-configuradas; tipos de documento principais (CNI, Passaporte, Contrato, Certidão, Habilitação, Formação, Disciplinar, Recibo, Justificativo) com extensões permitidas; subtipos de licença e mobilidade base.
- **FR-022a**: O sistema MUST permitir que administradores acrescentem, desactivem ou actualizem qualquer entrada do seed após o primeiro deploy — o seed é o ponto de partida, não um inventário fechado.
- **FR-022b**: O processo de seed MUST ser defensivo e idempotente: (1) é executado verificando primeiro se cada entrada já existe (por chave única); (2) entradas pré-existentes são preservadas tal como estão, sem sobrescrita de campos editáveis (nomes, ordem, descrições); (3) entradas que tenham sido alteradas por administradores em ambientes existentes não são revertidas; (4) o seed pode ser re-executado em qualquer momento sem corromper dados; (5) o sistema regista quais entradas foram inseridas e quais foram saltadas em cada execução.

---

### Key Entities

- **Catálogo de Etiquetas**: Catálogo único genérico que serve 11 grupos diferentes de etiquetas configuráveis. Cada entrada pertence a um grupo (Estado Civil, Sexo, Nacionalidade, etc.), tem uma chave única dentro do grupo, um valor de exibição traduzível, ordem de exibição, estado activo/inactivo, e descrição opcional.
- **Estado do Trabalhador**: Catálogo com flag `núcleo` que protege estados base (Activo, Inactivo) de desactivação acidental.
- **Situação Profissional**: Catálogo com regras distintas no PCFR (Plano de Carreiras, Funções e Remunerações) — Efectivo, Contratado, Comissionado, Estagiário — cada uma com direitos e obrigações próprias.
- **Tipo de Contrato**: Catálogo com implicações legais distintas (renovabilidade, prazo, direitos) — Nomeação Definitiva, CFP, CTFP a Termo Certo, CTFP a Termo Incerto, Comissão de Serviço.
- **Tipo de Documento**: Catálogo com regras de validação de upload (extensões permitidas) e categorização (Pessoal, Contratual, Formação, Disciplinar, Avaliação).
- **Tipo de Ausência**: Catálogo com flags que controlam o processamento de pedidos: desconta saldo anual, exige aprovação, limite legal de dias.
- **Subtipo de Licença e Mobilidade**: Catálogo com flags que controlam efeitos: afecta remuneração, conta para antiguidade, permite auto-submissão pelo colaborador. Classificado como Licença, Mobilidade ou Ambos.
- **Feriado**: Entrada com data, designação e classificação Nacional ou Municipal — alimenta o cálculo de dias úteis em pedidos de ausência.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Os 11 grupos do catálogo de etiquetas têm dados base completos disponíveis após o primeiro deploy, sem necessidade de configuração manual.
- **SC-002**: Os 4 estados núcleo do trabalhador (Activo, Inactivo) e as 4 situações profissionais base estão protegidos contra desactivação acidental — 100% das tentativas de desactivação destes estados são bloqueadas com mensagem explicativa.
- **SC-003**: O administrador de RH consegue criar uma nova entrada em qualquer catálogo em menos de 1 minuto a partir do momento em que abre a página de gestão.
- **SC-004**: 100% das tentativas de desactivação de entradas em uso por registos activos são bloqueadas com mensagem clara.
- **SC-005**: Os utilizadores vêem as opções dos catálogos imediatamente disponíveis em formulários após qualquer alteração administrativa, sem necessidade de recarregar manualmente o sistema.
- **SC-006**: 100% das alterações em catálogos têm registo de auditoria completo (autor, momento, valor anterior, valor novo) — verificável por inspecção do histórico.
- **SC-007**: O catálogo de feriados de Cabo Verde está completo para o ano corrente — todos os feriados nacionais oficiais estão registados antes da activação da feature de Ausências.
- **SC-008**: O administrador de RH não precisa de mais de 5 minutos de formação para conseguir gerir autonomamente qualquer catálogo do sistema.

---

## Assumptions

- A localização inicial é `pt-CV` (português de Cabo Verde); o sistema é projectado para suportar expansão multilingue mas o seed inicial não inclui outras línguas.
- O sistema atende uma única instituição (INGT) — não há multi-tenancy nem catálogos por organização cliente.
- A população inicial dos catálogos é feita por scripts de inicialização defensivos e idempotentes (ver FR-022b), executados em qualquer ambiente sem risco de corromper dados ou reverter alterações administrativas. O seed é re-executável sem efeitos colaterais.
- Os feriados municipais de Cabo Verde são suficientemente cobertos por uma flag `nacional/municipal` simples; o sistema não modela o concelho específico de cada feriado municipal nesta feature inicial — fica como possibilidade de expansão.
- O processo de "alteração de uma entrada com referências activas" não é coberto por esta feature: se um administrador precisar de reformular uma entrada referenciada, deve criar uma nova e desactivar a antiga após migração manual de referências (ferramentas de migração assistida ficam fora de âmbito).
- A internacionalização e formato de datas seguem as convenções de Cabo Verde (calendário gregoriano, semana segunda a sexta como dias úteis).
- O comportamento de aprovação de pedidos de ausência (que consome o catálogo "Tipos de Ausência") é implementado em feature posterior — esta feature apenas garante que os flags estão disponíveis para serem consumidos.
- A migração dos `enums` actualmente em uso pelo módulo legacy (`Sexo`, `EstadoCivil`, `GrauParentesco`, `TipoContrato`) NÃO faz parte desta feature. Esta feature popula os novos catálogos; a substituição do código que usa enums acontecerá em features posteriores (`feat/colaboradores-core`, `feat/vida-profissional`, `feat/dossier`).
- Os 11 grupos do catálogo de etiquetas estão pré-definidos e fixados; novos grupos requerem alteração de código, não apenas de configuração.
