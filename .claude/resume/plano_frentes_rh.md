# Plano de implementação — frentes RH (fora do sigdi)

> Criado: 2026-09-25 · autorização do utilizador para planear **e** implementar sem aprovação ponto a ponto.
> Dúvidas: (1) lei; (2) interpretação da lei; (3) prática da indústria. Cada decisão tomada em (2) ou (3)
> fica marcada **[interp.]** ou **[ind.]** aqui e na regra BR-* correspondente.

Fontes na raiz do repositório (fora do git): `.lei20.txt` (Lei n.º 20/X/2023, duas colunas — linearizar
antes de ler), `.dl3.txt` (DL n.º 3/2010, uma linha), `.estatuto_disciplinar.txt` (Lei n.º 31/III/87 com a
redacção do Decreto-Legislativo n.º 8/97 — texto consolidado a partir da linha 259). Não encontrado em texto
acessível: DL n.º 57/2019 (concursos, alterado pelo DL 24/2024), diploma de ajudas de custo, regime de
acidentes em serviço — onde fazem falta, **[ind.]** e «por confirmar» no `Open questions` do handoff.

Decisões do cliente (2026-09-25): candidaturas **registadas pelo RH** (sem portal); declarações, cartão
profissional e publicações no BO com **modelo de teste**; processo individual completo = **TODO**;
notificações só na aplicação (SMTP = TODO), **fáceis de chamar de qualquer ponto**.

## Regras transversais

- Um commit por frente (`feat(<módulo>): …`), sem push. Suite verde na cópia isolada antes de cada commit.
- Entidade nova sem migração (ddl-auto). Migração só para alterar tabela existente — **idempotente e
  defensiva** (`IF NOT EXISTS`, `to_regclass` aninhado, seeds `ON CONFLICT DO NOTHING`). Próxima livre: **V61**.
- Manifestos `.igrpstudio/` gerados do Java por `scripts/gerar_manifestos.py` (controller, DTO, entidade).
- Mensagens para o utilizador em pt-PT simples (sem rotas, enums, ids; datas dd/MM/aaaa).
- O que a lei fixa é enum; o que varia com a instituição é catálogo; o derivável não se guarda.
- Docs a par: `regras_negocio.html` (BR-*), `api_guide.md` (cada caminho novo), `modelo_relacional.html`
  (cada tabela nova), `openapi.json` regenerado, `verificar_docs.py` → `FALHAS: 0`.

## Fase 0 — alicerces

### F0.1 Notificações (in-app; SMTP = TODO) — `shared/notificacoes`
- **Chamador:** `Notificador` (serviço Spring) com API fluente — `notificador.para(funcionarioId)`,
  `.paraPerfil(PerfilDestino.RH)`, `.tipo("DISCIPLINAR_ACUSACAO")`, `.titulo(..)`, `.texto(..)`,
  `.recurso("PROCESSO_DISCIPLINAR", id)`, `.enviar()`. Também `notificador.notificar(Notificacao.Pedido)`.
  Corre na mesma transacção do chamador (a notificação nasce com o facto que a causou).
- Tabela `t_notificacao`: destinatário (funcionário **ou** perfil), tipo (texto livre, com constantes num
  enum de conveniência `TipoNotificacao`), título, texto, recurso (tipo+id), criada_em, lida_em.
- Tabela `t_notificacao_envio` (canal EMAIL, estado PENDENTE/ENVIADO/FALHOU) — a fila para o SMTP; o envio é
  `TODO(smtp)`: o job `RH_ENVIO_NOTIFICACOES` existe desactivado por omissão e só marca "sem canal".
- REST: `GET /me/notificacoes?naoLidas=`, `GET /me/notificacoes/contagem`, `PATCH /me/notificacoes/{id}/lida`,
  `PATCH /me/notificacoes/lidas` (todas); `GET /notificacoes?perfil=RH` (caixa do RH).
- Substitui os 4 `TODO(notificacoes)` existentes.
- BR-NOT-01..06.

### F0.2 Diário de factos para o salarial — `colaboradores`
- Tabela `t_facto_rh` (append-only): funcionário, tipo (enum `TipoFactoRh`: ADMISSAO, CESSACAO,
  MUDANCA_ESCALAO, MUDANCA_CATEGORIA, MUDANCA_SITUACAO, PENA_DISCIPLINAR, MISSAO_SERVICO,
  ACIDENTE_SERVICO, EXONERACAO…), data de efeito, referência (tipo+id), dados (JSON pequeno), registado_em,
  mes_competencia (derivado da data de efeito, alterável pelo fecho).
- `DiarioFactos.registar(...)` chamado pelos serviços que já existem (colocação, cessação, progressão,
  promoção, mudança de estado) e por cada frente nova.
- Consulta: `GET /salarial/factos?mes=` (JSON e CSV). O fecho e o contrato de exportação ficam na F5.
- BR-FAC-01..05.

### F0.3 Dias especiais no apuramento — `colaboradores`
- Porta `DiasEspeciaisProvider` (lista injectada) consultada por `ApuramentoFaltasService.estadoPrevio`:
  cada frente que tira um dia do apuramento (missão, formação, suspensão disciplinar, incapacidade por
  acidente) implementa-a. Novos valores em `EstadoDiaApurado`: `MISSAO_SERVICO`, `FORMACAO`,
  `SUSPENSAO_DISCIPLINAR`, `ACIDENTE_SERVICO`. Sem migração (enum não persistido).

## Fase 1 — ganhos rápidos

### F1.1 Aposentação, limite de idade, pré-aposentação — `colaboradores`
Lei 20: **art. 48.º** — o vínculo cessa aos **65 anos**; por interesse público excepcional, até aos **70**,
com vontade do funcionário, proposta e autorização (n.º 2, 3); **art. 96.º n.º 2 c)** — o contrato caduca
aos 70; **art. 175.º** — antecipada a pedido com **34 anos de serviço**; **art. 176.º** — antecipada no
interesse da Administração (carreiras do DL de execução orçamental); **art. 178.º** — o Lugar do aposentado
antecipadamente **extingue-se**; **art. 179.º** — pré-aposentação: **≥ 58 anos e ≥ 30 de serviço**,
suspensão do vínculo, prestação 70–80% da remuneração base, conta tempo de serviço efectivo;
**art. 120.º n.º 1 d)** — desligado do serviço aguardando aposentação = inactividade no quadro.
- Reutilizo: `AntiguidadeService` (tempo de serviço), `CessacaoService` (cessar), estados/situações do
  catálogo (`APOSENTACAO`, `INACTIVIDADE_NO_QUADRO`), `Position.extinguir`.
- **Relatório** `GET /relatorios/aposentacao?ate=&unidadeId=`: quem atinge 65/70 anos, quem já reúne 34 anos
  de serviço, quem reúne as condições de pré-aposentação — com a data em que cada condição se verifica.
- **Prorrogação além dos 65** (`t_prorrogacao_permanencia`): pedido do funcionário → proposta → autorização
  (com despacho), até aos 70. Sem autorização válida, o job avisa.
- **Processo de aposentação** (`t_processo_aposentacao`): modalidade (LIMITE_IDADE, ANTECIPADA_PEDIDO,
  ANTECIPADA_INTERESSE_ADMIN, INVALIDEZ, PRE_APOSENTACAO) · PEDIDO → DESPACHADO → DESLIGADO → CONCLUIDO /
  INDEFERIDO. Desligação = inactividade no quadro; conclusão = `CessacaoService` com estado de aposentação;
  antecipada extingue o Lugar (art. 178.º). Pré-aposentação: suspende o vínculo e termina na aposentação.
- Job `RH_ALERTA_APOSENTACAO` (diário): notifica o RH 180/90/30 dias antes dos 65 e dos 70 **[ind.]**.
- BR-APO-01..12.

### F1.2 Declarações e certidões (modelo de teste) — `colaboradores`
- Tipos (enum, **[ind.]**): VINCULO, TEMPO_SERVICO, ANTIGUIDADE, SITUACAO_FUNCIONAL, EFECTIVIDADE.
- `t_pedido_declaracao`: pedido pelo próprio (`/me`) ou pelo RH; PEDIDO → EMITIDA / RECUSADA; finalidade.
- Emissão: PDF (modelo de teste em HTML → PDF, **openhtmltopdf**), número sequencial por ano
  (`DEC-2026-000123`), **código de verificação** (hash curto) e rota pública de verificação
  `GET /verificacao/declaracoes/{codigo}`; o PDF fica no MinIO como `Documento` do colaborador.
- BR-DEC-01..08.

### F1.3 Ciclo da lista de antiguidade — DL 3/2010, arts. 69.º–74.º — `colaboradores`
- Reutilizo `ListaAntiguidadeService` (gera). Novo agregado `t_lista_antiguidade` (ano, unidade):
  RASCUNHO → APROVADA (dirigente) → AFIXADA (data e local; abre prazo de **30 dias** consecutivos, **60** para
  quem presta serviço no estrangeiro, art. 74.º) → DEFINITIVA → PUBLICADA (BO, **até 30 de Abril**, art. 71.º
  n.º 2). A lista aprovada **congela-se** (as linhas guardam-se: é um documento, não uma consulta).
- Reclamações (`t_reclamacao_antiguidade`): pelo próprio em `/me` ou pelo RH; fundamentos do art. 72.º n.º 2
  (OMISSAO, GRADUACAO, SITUACAO, CONTAGEM); não pode fundar-se no que listas anteriores já consideraram
  (n.º 3) — aviso, não bloqueio **[interp.]**; decisão do dirigente, notificada no prazo de 30 dias (n.º 5);
  recurso em 20 dias (art. 73.º) — regista-se interposição e decisão.
- Não se torna DEFINITIVA com reclamações por decidir.
- BR-LAN-06..15 (continua a série BR-LAN).

### F1.4 Publicações no BO (modelo de teste) — `colaboradores`
- Lei 20, arts. 89.º–90.º; Estatuto Disciplinar art. 15.º n.º 2. Registo transversal `t_publicacao_oficial`:
  tipo de acto (NOMEACAO, CONTRATO, MOBILIDADE, COMISSAO_SERVICO, CESSACAO, PENA_DISCIPLINAR,
  LISTA_ANTIGUIDADE, EXONERACAO, REABILITACAO…), referência (tipo+id), meio (BOLETIM_OFICIAL, PAGINA_ELECTRONICA),
  série/número/data, estado A_PUBLICAR → PUBLICADO. **Extracto** gerado (texto de teste com os dados do n.º 2
  do art. 89.º: carreira, função, categoria, habilitações; remuneração fica de fora — é do salarial).
- As frentes criam o pedido de publicação quando o acto o exige; o RH regista a publicação.
- BR-PUB-01..06.

### F1.5 Cartão de identificação profissional (modelo de teste) — Lei 20, art. 25.º
- `t_cartao_profissional`: número, emitido em, validade (enquanto na mesma função e categoria, n.º 5 —
  **caduca** quando a categoria/cargo muda: derivado), entregue em (atestado de recepção, n.º 3), estado
  EMITIDO → ENTREGUE → DEVOLVIDO / ANULADO. PDF de teste (frente/verso).
- BR-CID-01..06.

## Fase 2 — entrada

### F2.1 Concurso (recrutamento e selecção) — módulo novo `recrutamento`
Lei 20: arts. 123.º (obrigatório para ingresso e acesso; ingresso pelo 1.º nível), 124.º (ingresso
excepcional), 125.º (princípios), 126.º (descentralizado), 127.º (comum/especial; externo, interno, interno
restrito; **quota de deficiência** nos externos), 128.º (métodos obrigatórios: triagem curricular, provas de
conhecimentos, avaliação de competências, entrevista; a termo pode limitar-se a triagem + entrevista, n.º 3;
dispensa por despacho, n.º 5), 129.º (habilitação exigida), 89.º–90.º (publicidade).
Estatuto Disciplinar art. 17.º n.º 2 c): suspenso não é admitido a concurso.
- `t_concurso`: referência, finalidade (INGRESSO, ACESSO), tipo (COMUM, ESPECIAL), modalidade (EXTERNO,
  INTERNO, INTERNO_RESTRITO), vínculo a constituir (NOMEACAO, CONTRATO_INDETERMINADO, CONTRATO_TERMO), carreira
  e categoria, **Lugares a prover** (`t_concurso_lugar` → Position vago), requisitos, habilitação mínima,
  quota de deficiência (n.º de lugares), datas (aviso, candidaturas de/até), estado RASCUNHO → ABERTO →
  CANDIDATURAS_ENCERRADAS → EM_AVALIACAO → LISTA_PROVISORIA → LISTA_HOMOLOGADA → CONCLUIDO / ANULADO.
- `t_concurso_juri` (presidente, vogais, suplentes — **3 efectivos + 2 suplentes** **[ind.]**; um membro não
  pode ser candidato).
- `t_concurso_metodo` (método, ponderação %, eliminatório, nota mínima; escala **0–20** **[ind.]**; soma = 100).
- `t_candidato` (pessoa: nome, documento, NIF, contactos, habilitação, deficiência declarada) — reutiliza um
  `Funcionario` quando o candidato é interno (`funcionarioId`).
- `t_candidatura`: RH regista (decisão do cliente); ADMITIDA / EXCLUIDA (com motivo) → audiência dos
  interessados (**10 dias úteis** **[ind.]**) → notas por método → classificação final (média ponderada,
  eliminatórios) → ordenação (desempate: nota do método de maior peso, depois antiguidade **[ind.]**) →
  homologação → PROVIDO / DESISTIU / RESERVA.
- Regras de admissão automáticas (avisos ao RH, não bloqueios, excepto as da lei): interno exige vínculo;
  interno restrito exige ser da entidade; habilitação; suspenso disciplinarmente não é admitido (bloqueia).
- Lista homologada vale como **reserva de recrutamento por 18 meses** **[ind.]** (o RH pode prover a partir
  dela se um provido desistir).
- **Provimento**: da candidatura homologada nasce a entrada ao serviço (F2.2) — internos por
  promoção/mudança de carreira existentes (o `concursoRef` passa a aceitar a referência do concurso), externos
  por registo de colaborador + provimento.
- Publicação (F1.4) do aviso e da lista homologada.
- BR-CNC-01..20.

### F2.2 Entrada ao serviço: provimento, posse, estágio probatório, período experimental — `colaboradores`
Lei 20: arts. 53.º–58.º (nomeação provisória para estágio probatório; definitiva após avaliação positiva;
exoneração a todo o tempo sem aptidão, art. 57.º n.º 4; tempo conta, n.º 3/5), 61.º–63.º (posse fixa a data
de efeitos; nunca antes do início de funções), 72.º (contrato de estágio: **1 ano**, tutor, relatório final,
cessação antecipada por relatório fundamentado, n.º 6; tempo conta se com sucesso), 79.º–81.º (período
experimental nos contratos a termo: **60 dias** (≥ 6 meses ou incerto > 6 meses) ou **30 dias**; cessação
fundamentada pela entidade ou denúncia pelo agente, sem indemnização), 94.º n.º 3 (exoneração obrigatória
durante o estágio).
- `t_provimento`: funcionário, modalidade (NOMEACAO_PROVISORIA, NOMEACAO_DEFINITIVA, COMISSAO_SERVICO,
  CONTRATO_ESTAGIO, CONTRATO_INDETERMINADO, CONTRATO_TERMO), concurso/candidatura de origem, Lugar, data do
  despacho, **data da posse / início de funções**, publicação.
- `t_periodo_prova` (estágio probatório ou período experimental): tipo, início, fim previsto (1 ano / 60 /
  30 dias, derivado), tutor (funcionário), relatório (data, avaliação POSITIVA/NEGATIVA, fundamentação),
  estado EM_CURSO → CONCLUIDO_COM_SUCESSO / CONCLUIDO_SEM_SUCESSO / CESSADO_ANTECIPADAMENTE.
  Sucesso: nomeação definitiva / contrato por tempo indeterminado (novo provimento) e o tempo conta.
  Insucesso: exoneração obrigatória (via `CessacaoService`) ou regresso à carreira de origem (quem já era
  definitivo noutra carreira faz o estágio em comissão de serviço, art. 57.º n.º 2 / 72.º n.º 8).
- Alertas (job `RH_ALERTA_PERIODOS_PROVA`): relatório do tutor a 30 dias do fim; fim sem relatório.
- Alerta de fim de termo dos contratos a termo (já há `endDate`, `renewalCount`, `maxRenewals`) — no mesmo job.
- Integra com F2.3 (checklist de entrada) e F1.4 (publicação).
- BR-PRV-01..14.

### F2.3 Checklists de entrada e saída — `colaboradores`
- Modelos (`t_checklist_modelo` + itens): evento (ENTRADA, SAIDA, MOBILIDADE), itens com responsável
  (RH, CHEFIA, TI, PROPRIO, OUTRO), obrigatório. Modelos por omissão semeados na primeira utilização
  (cartão profissional art. 25.º n.º 3, processo individual art. 27.º, acolhimento art. 141.º n.º 2,
  devolução do cartão, remessa do processo individual na mobilidade definitiva art. 27.º n.º 5).
- Instância (`t_checklist` + itens) gerada automaticamente no provimento, na cessação e na mobilidade
  definitiva; marcar item feito/dispensado com nota. Não bloqueia a cessação **[ind.]**; avisa.
- BR-CHK-01..07.

### F2.4 Comissão de serviço dos dirigentes — Lei 20, arts. 59.º, 60.º, 64.º; Estatuto Disciplinar art. 17.º n.º 8
- `t_comissao_servico`: funcionário, cargo/função dirigente, lugar de origem, início, **3 anos** renováveis
  (art. 60.º n.º 1), renovações, cessação (iniciativa, **aviso prévio de 60 dias**, art. 64.º n.º 1),
  regresso à situação de origem (n.º 2); tempo conta na carreira de origem (art. 60.º n.º 2).
- Reutilizo a licença "regresso de comissão" existente (`LicencaService.aplicarRegressoDeComissao`) para o
  regresso.
- Alerta de fim de comissão (90 dias antes).
- BR-CMS-01..08.

## Fase 3 — processos

### F3.1 Processo disciplinar — Estatuto Disciplinar (Lei 31/III/87 + DL 8/97)
Reutilizo a entidade existente `ProcessoDisciplinar` (registo) — passa a ser o agregado do processo,
com os campos actuais preservados (não partir o front).
- Espécies (enum): DISCIPLINAR_COMUM, INFRACCAO_CONSTATADA (art. 78.º), FALTA_ASSIDUIDADE (art. 80.º),
  ABANDONO_LUGAR (art. 81.º), INQUERITO, SINDICANCIA, AVERIGUACOES (art. 96.º).
- Fases: PARTICIPADO → INSTAURADO (despacho, entidade) → EM_INSTRUCAO (instrutor nomeado; início em
  **3 dias úteis**, instrução **30 dias** + 1 prorrogação ≤ 30 — omissão = 15, art. 48.º) → ACUSADO (acusação
  em **5 dias úteis**; notificação em 48h; defesa **10–20 dias**, até **45** se complexo, art. 62.º) →
  DEFESA_APRESENTADA / SEM_RESPOSTA (vale audiência, art. 69.º) → RELATORIO (**10 dias**, +20, art. 71.º) →
  DECIDIDO (**15 dias úteis**, art. 72.º) → NOTIFICADO → RECURSO (hierárquico **15 dias**, art. 84.º; suspende a
  execução) → TRANSITADO; ARQUIVADO a qualquer passo antes da decisão (art. 60.º n.º 1).
- **Prazos calculados e visíveis** (a vencer / vencidos) — alertas pelo job `RH_PRAZOS_DISCIPLINARES`.
- **Prescrição** (art. 6.º): 6 meses (censura), 2 anos (multa/suspensão/inactividade), 3 anos
  (aposentação compulsiva/demissão), contados da infracção; aviso ao instaurar.
- **Suspensão preventiva** (art. 56.º): até **90 dias**, com ou sem perda do vencimento de exercício
  (facto para o salarial); dia suspenso sai do apuramento de faltas.
- **Penas** (art. 14.º, enum): CENSURA_ESCRITA, MULTA (≤ 20 dias de remuneração — valor é do salarial; aqui
  só os dias), SUSPENSAO (21–90 ou 91–121 dias... **art. 16.º n.º 4**: 21–90 / 91–121), INACTIVIDADE
  (6–18 meses), APOSENTACAO_COMPULSIVA, DEMISSAO, CESSACAO_COMISSAO (dirigentes). Competência (art. 21.º)
  validada por nível do decisor (texto) — aviso **[interp.]**.
- **Efeitos** (art. 17.º), aplicados na data de efeito (dia seguinte à notificação, art. 77.º):
  suspensão/inactividade → estado com situação INACTIVIDADE_NO_QUADRO / INACTIVIDADE_FORA_QUADRO (abre vaga,
  Lei 20 art. 121.º), dias descontados na antiguidade, férias bloqueadas 1 ano (10 dias se ≤ 90), impedimento
  de concurso/promoção (lido pela F2.1 e pelos movimentos); aposentação compulsiva e demissão → `CessacaoService`
  + publicação no BO (art. 15.º n.º 2); cessação de comissão → F2.4, sem nova nomeação dirigente por 2 anos.
- **Suspensão da pena** (art. 34.º, 1–3 anos) e **extinção/prescrição da pena** (art. 35.º); **reabilitação**
  (art. 95.º, após 5 anos; publicação no BO) e **revisão** (arts. 90.º–94.º).
- **Autos automáticos sugeridos**: o apuramento de faltas injustificadas propõe auto de falta de assiduidade
  (5 seguidos / 8 interpolados no ano) e de abandono de lugar (12 seguidos / 15 interpolados / 25 em 24 meses)
  — notificação ao RH e à chefia, não abre processo sozinho **[interp.]**.
- Exoneração voluntária condicionada enquanto arguido (Lei 20 art. 95.º a)) — lido pela F3.4.
- Registo no processo individual (art. 15.º n.º 1): as penas ficam no histórico do colaborador.
- BR-DIS-03..30 (continua a série BR-DIS).

### F3.2 Formação como processo — Lei 20, art. 141.º; art. 95.º b) (prazo de garantia); DL 3/2010 licença para formação
Reutilizo `Formacao` (registo do histórico) — o fim de uma acção com aproveitamento cria-o.
- `t_plano_formacao` (ano, unidade, estado RASCUNHO → APROVADO) com `t_necessidade_formacao`
  (identificada pela chefia ou pelo próprio: tema, colaboradores, prioridade).
- `t_accao_formacao`: tema, entidade formadora, modalidade (PRESENCIAL, DISTANCIA, MISTA), interna/externa,
  datas, horário, horas, vagas, local, custo previsto (informativo), **prazo de garantia** (meses de
  permanência após a formação, quando a Administração a custeia — art. 95.º b)), estado PLANEADA → INSCRICOES
  → EM_CURSO → CONCLUIDA / CANCELADA.
- `t_inscricao_formacao`: pedido pelo próprio em `/me` ou inscrição pelo RH/chefia; decisão da chefia directa
  ou do RH; presenças (dias); avaliação (APROVEITAMENTO / SEM_APROVEITAMENTO / FALTOU); certificado (documento).
- Dias de formação saem do apuramento (F0.3) — `FORMACAO`.
- Relatório de horas de formação por colaborador/unidade/ano (indicador do balanço social).
- BR-FRM-05..16.

### F3.3 Missão de serviço — Lei 20, art. 159.º (ajudas de custo e transporte)
- `t_missao_servico`: colaborador(es) (uma missão, vários participantes), destino (NACIONAL com ilha/concelho,
  ESTRANGEIRO com país/cidade), objectivo, datas de partida/regresso, meio de transporte, alojamento a cargo
  (sim/não), adiantamento pedido (sim/não), estado PEDIDA → AUTORIZADA / RECUSADA → REALIZADA (relatório de
  regresso) / CANCELADA.
- Pedida pelo próprio ou pelo RH/chefia; autorizada pela chefia directa ou pelo RH.
- **Dias de ajudas de custo** (inteiros / meios dias pela hora de partida e regresso **[ind.]**) calculados e
  entregues como facto (F0.2) — **sem valores** (as tabelas são do diploma de desenvolvimento e do salarial).
- Dias em missão saem do apuramento (F0.3) — `MISSAO_SERVICO`; sobreposição com férias/ausência avisa.
- BR-MSS-01..10.

### F3.4 Exoneração voluntária — Lei 20, arts. 94.º, 95.º
- `t_pedido_exoneracao`: pedido do próprio (`/me`) ou registado pelo RH; data do pré-aviso; **efeito = pré-aviso
  + 60 dias**; **condicionantes** (art. 95.º): processo disciplinar em que é arguido, inquérito/sindicância ao
  serviço, prazo de garantia de formação por cumprir → CONDICIONADO; concede-se quando cessa a causa ou
  **90 dias** após o pré-aviso (n.º 4). Estados PEDIDO → CONDICIONADO → DEFERIDO → EFECTIVADO (cessação) /
  DESISTIDO. Publicação no BO (n.º 5).
- BR-EXO-01..08.

### F3.5 Acumulação de funções — Lei 20, arts. 20.º–24.º
- `t_acumulacao_funcoes`: tipo (PUBLICA, PRIVADA), remunerada, entidade, funções, horário (não sobreposto —
  art. 22.º n.º 3 b)), período; casos do art. 21.º n.º 2 para as públicas; autorização (art. 23.º: não
  remunerada — dirigente máximo; remunerada — membros do Governo) — só se regista quem autorizou e o despacho.
  Estados PEDIDO → AUTORIZADA / INDEFERIDA → CESSADA / CADUCADA (fim do período). Declaração de
  inexistência de conflito pelo próprio.
- BR-ACU-01..07.

## Fase 4 — SST

### F4.1 Acidentes em serviço e doenças profissionais — Lei 20, arts. 187.º–191.º
- `t_acidente_servico`: tipo (ACIDENTE_SERVICO, ACIDENTE_TRAJECTO, DOENCA_PROFISSIONAL), data/hora, local,
  descrição, testemunhas, participação (data; **2 dias úteis** para a participação **[ind.]**, prazo por
  confirmar no diploma de desenvolvimento), qualificação (PARTICIPADO → QUALIFICADO_COMO_EM_SERVICO /
  NAO_QUALIFICADO), seguradora e apólice (art. 191.º), incapacidade temporária (períodos; faltas justificadas
  **sem perda de direitos**, art. 188.º → `ACIDENTE_SERVICO` no apuramento), alta, incapacidade permanente
  (percentagem) → aposentação por invalidez (art. 189.º n.º 3–4, liga a F1.1).
- BR-SST-01..10.

### F4.2 Medicina do trabalho e aptidão
- Lei 20 art. 45.º n.º 1 d) (aptidão física e psíquica como requisito); **[ind.]** exames de admissão,
  periódicos e ocasionais. `t_exame_saude`: tipo (ADMISSAO, PERIODICO, OCASIONAL, REGRESSO), data, resultado
  (APTO, APTO_CONDICIONADO, INAPTO_TEMPORARIO, INAPTO_DEFINITIVO), restrições, **validade** → alerta antes de
  caducar (job). Sem dados clínicos (só o resultado de aptidão).
- **Comissão de verificação de incapacidade** (Lei 20 art. 121.º n.º 1 c)): doença > 30 dias → pedido de
  junta; resultado passa a situação (inactividade fora do quadro abre vaga).
- BR-SST-11..18.

## Fase 5 — fronteira salarial

### F5.1 Fecho mensal e exportação — DL 3/2010, art. 75.º
- `t_fecho_mensal` (mês, estado ABERTO → FECHADO → REABERTO → FECHADO…, quem, quando, motivo da reabertura).
- Fechar: congela a relação mensal (guarda as linhas — faltas e licenças por natureza) e os factos do mês;
  depois de fechado, **factos com data de efeito no mês fechado vão para o mês seguinte** (`mes_competencia`)
  — não se reescreve o que o salarial já recebeu **[ind.]**; lançamentos que mexam em dias de um mês fechado
  (marcações, pedidos) ficam permitidos mas assinalados como "após fecho" (ajuste no mês seguinte).
- **Divisão confirmada com o utilizador (2026-09-25):** o RH parametriza o **bruto base no escalão** (`t_grade.salary_base`)
  e é a fonte de quem está em que escalão, situação, dias e horas; a integração calcula remunerações, suplementos,
  **descontos** (IUR, segurança social, quotas) e líquido. A fazer aqui: os factos de movimento passam a levar o
  **bruto do escalão à data** (`remuneracaoBase`) além do `escalaoId`.
- Exportação: `GET /salarial/exportacao?mes=` — JSON e CSV com os factos + relação mensal congelada, por
  identificador do colaborador (número e NIF). Contrato versionado (`versao: 1`). Canal (API/ficheiro/Kafka)
  = **por decidir com a equipa do salarial** — a exportação por API fica pronta; o resto fica `TODO`.
- BR-FEC-01..08.

## TODO conscientes
- Processo individual completo (Lei 20 art. 27.º) — vista única do dossier. **TODO** (decisão do cliente).
- SMTP das notificações — `TODO(smtp)`.
- Portal público de candidaturas — fora (decisão do cliente).
- Modelos oficiais (declarações, cartão, extractos BO) — hoje de teste.
- Por confirmar com o texto: DL 57/2019 (júri, prazos, reserva), diploma de ajudas de custo, regime de acidentes
  em serviço (prazo de participação).

## Ordem de execução (um commit cada)
F0.1 → F0.2 → F0.3 → F1.1 → F1.2 → F1.3 → F1.4 → F1.5 → F2.2 → F2.1 → F2.3 → F2.4 → F3.1 → F3.2 → F3.3 →
F3.4 → F3.5 → F4.1 → F4.2 → F5.1. (F2.2 antes de F2.1: o concurso termina num provimento que tem de existir.)
