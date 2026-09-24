# Bateria de testes funcionais - RH-Service (so ASCII, para o PowerShell 5.1 nao partir o ficheiro)
# Percurso de cliente: navega (GET), le, e so depois age. Cobre positivo e negativo.

$ErrorActionPreference = 'Continue'
$base = 'http://localhost:8099/api/v1/rh'
$script:resultados = @()

function Chamar {
    param([string]$Nome, [string]$Metodo, [string]$Rota, $Corpo = $null, [int]$Esperado = 200, [string]$Como = '')
    # Accept explicito: sem ele o servidor negoceia e devolve os erros em XML
    # (ProblemDetail), enquanto os sucessos vem em JSON. Um cliente que esqueca o
    # cabecalho fica com dois formatos na mesma API -- e o api_guide avisa disso.
    $params = @{ Method = $Metodo; Uri = "$base$Rota"; UseBasicParsing = $true; TimeoutSec = 90
                 Headers = @{ Accept = 'application/json' } }
    # /me: em desenvolvimento, quem e o utilizador di-lo o cabecalho X-Employee-Id.
    if ($Como) { $params.Headers['X-Employee-Id'] = $Como }
    if ($null -ne $Corpo) {
        $params.Body = ($Corpo | ConvertTo-Json -Depth 8 -Compress)
        $params.ContentType = 'application/json'
    }
    $codigo = 0; $texto = ''
    try {
        $r = Invoke-WebRequest @params
        $codigo = [int]$r.StatusCode; $texto = $r.Content
    } catch {
        if ($_.Exception.Response) {
            $codigo = [int]$_.Exception.Response.StatusCode
            try { $texto = (New-Object IO.StreamReader($_.Exception.Response.GetResponseStream())).ReadToEnd() } catch { $texto = $_.Exception.Message }
        } else { $texto = $_.Exception.Message }
    }
    $ok = ($codigo -eq $Esperado)
    $script:resultados += [pscustomobject]@{ Nome = $Nome; Esperado = $Esperado; Obtido = $codigo; OK = $ok }
    $marca = 'FALHA'; if ($ok) { $marca = 'OK   ' }
    Write-Host ("{0} [{1} -> {2}] {3}" -f $marca, $Esperado, $codigo, $Nome)
    if (-not $ok -and $texto) {
        $curto = ($texto -replace '\s+', ' ')
        if ($curto.Length -gt 200) { $curto = $curto.Substring(0, 200) }
        Write-Host ("      " + $curto)
    }
    $dados = $null
    if ($texto) { try { $dados = $texto | ConvertFrom-Json } catch { } }
    return [pscustomobject]@{ Codigo = $codigo; Dados = $dados; OK = $ok }
}

function Verificar {
    param([string]$Nome, [bool]$Condicao, [string]$Detalhe = '')
    $obtido = 'falso'; if ($Condicao) { $obtido = 'verdade' }
    $script:resultados += [pscustomobject]@{ Nome = $Nome; Esperado = 'verdade'; Obtido = $obtido; OK = $Condicao }
    $marca = 'FALHA'; if ($Condicao) { $marca = 'OK   ' }
    Write-Host ("{0} {1} {2}" -f $marca, $Nome, $Detalhe)
}

function Linhas($resposta) {
    $d = $resposta.Dados
    if ($null -eq $d) { return @() }
    # Uma lista simples (sem pagina) devolve-se como vem: num array, $d.content nao e $null --
    # o PowerShell enumera os membros e devolve um nulo por elemento, e a lista vinha vazia.
    if ($d -is [System.Array]) { return @($d) }
    foreach ($campo in 'content','dados','data','items') {
        if ($null -ne $d.$campo) { return @($d.$campo) }
    }
    return @($d)
}

Write-Host ''
Write-Host '=========== F0 - NAVEGACAO INICIAL (o que o front-end carrega) ==========='

$rFunc = Chamar 'F0.1 listar colaboradores' GET '/funcionarios?pagina=0&tamanho=10'
$colabs = Linhas $rFunc
Write-Host ('      colaboradores: ' + $colabs.Count)
# Os papeis fixam-se pelo numero de funcionario, e nao pela posicao na lista: a
# ordem muda com os dados e os papeis trocavam entre execucoes -- numa execucao o
# "A" era o Francisco, na seguinte era a Joana, e os passos seguintes liam
# resultados de outra pessoa sem que nada falhasse.
function PorNumero($lista, [string]$numero) {
    return ($lista | Where-Object { $_.numeroFuncionario -eq $numero } | Select-Object -First 1).id
}
$colabA = PorNumero $colabs '0000001'   # Francisco Bastos
$colabB = PorNumero $colabs '0000002'   # Maria Santos
# C existe para a substituicao: e preciso alguem impedido que mantenha o Lugar e
# alguem que o substitua. Com dois colaboradores nao ha como montar o cenario.
$colabC = PorNumero $colabs '0000003'   # Joana Tavares
Verificar 'F0.1b os tres colaboradores do seed existem' (($null -ne $colabA) -and ($null -ne $colabB) -and ($null -ne $colabC)) ''
Write-Host ('      A=' + $colabA + '  B=' + $colabB + '  C=' + $colabC)

Chamar 'F0.2 detalhe do colaborador A' GET ('/funcionarios/' + $colabA + '/details') | Out-Null
$rUniA0 = Chamar 'F0.3 unidade atual de A' GET ('/colaboradores/assignments/funcionario/' + $colabA + '/unidade-atual')
# A categoria do Lugar de origem de A: no reingresso (F6.9) volta a um Lugar da mesma (art. 122.o).
$catA0 = (Chamar 'F0.3b Lugar de origem de A' GET ('/estrutura/positions/' + $rUniA0.Dados.positionId)).Dados.categoryId
$rEstados = Chamar 'F0.4 catalogo de estados' GET '/catalogs/worker-states?pagina=0&tamanho=50'
$rTipos = Chamar 'F0.5 catalogo de tipos de ausencia' GET '/catalogs/leave-types?pagina=0&tamanho=50'
$rSubtipos = Chamar 'F0.6 catalogo de subtipos' GET '/catalogs/leave-mobility-subtypes?pagina=0&tamanho=50'

$estados = Linhas $rEstados
$tipos = Linhas $rTipos
$subtipos = Linhas $rSubtipos
Write-Host ('      estados=' + $estados.Count + ' tipos=' + $tipos.Count + ' subtipos=' + $subtipos.Count)

$ws = @{}; foreach ($e in $estados) { $ws[$e.code] = $e }
$lt = @{}; foreach ($t in $tipos) { $lt[$t.code] = $t }
$st = @{}; foreach ($s in $subtipos) { $st[$s.code] = $s }

Write-Host ''
Write-Host '=========== F1 - SITUACOES FUNCIONAIS (catalogo) ==========='

Verificar 'F1.1 ACTIVE = ACTIVIDADE_NO_QUADRO' ($ws['ACTIVE'].situacaoFuncional -eq 'ACTIVIDADE_NO_QUADRO') ('(' + $ws['ACTIVE'].situacaoFuncional + ')')
Verificar 'F1.2 SUSPENDED = INACTIVIDADE_NO_QUADRO' ($ws['SUSPENDED'].situacaoFuncional -eq 'INACTIVIDADE_NO_QUADRO') ('(' + $ws['SUSPENDED'].situacaoFuncional + ')')
Verificar 'F1.3 RETIRED = APOSENTACAO' ($ws['RETIRED'].situacaoFuncional -eq 'APOSENTACAO') ('(' + $ws['RETIRED'].situacaoFuncional + ')')
Verificar 'F1.4 INACTIVE sem situacao (cessacao)' ([string]::IsNullOrEmpty($ws['INACTIVE'].situacaoFuncional))
Verificar 'F1.5 estados novos do seed existem' ($ws.ContainsKey('INACTIVE_OUTSIDE') -and $ws.ContainsKey('AVAILABLE') -and $ws.ContainsKey('ACTIVE_OUTSIDE'))

Chamar 'F1.6 NEG situacao fora da lei' POST '/catalogs/worker-states' @{ code='TST_INV'; description='Teste'; isCore=$false; endsEmployment=$false; situacaoFuncional='FERIAS' } 422 | Out-Null
Chamar 'F1.7 NEG cessacao com situacao que nao cessa' POST '/catalogs/worker-states' @{ code='TST_CESSA'; description='Teste'; isCore=$false; endsEmployment=$true; situacaoFuncional='DISPONIBILIDADE' } 422 | Out-Null
Chamar 'F1.8 NEG APOSENTACAO sem endsEmployment' POST '/catalogs/worker-states' @{ code='TST_APOS'; description='Teste'; isCore=$false; endsEmployment=$false; situacaoFuncional='APOSENTACAO' } 422 | Out-Null

Write-Host ''
Write-Host '=========== F1b - MOVIMENTOS DE CARREIRA (com Lugar) ==========='
# Corre antes de tudo o que abre vaga: progredir exige afectacao corrente (BR-PRG-04).

Chamar 'F1b.1 detalhe de A' GET ('/funcionarios/' + $colabA + '/details') | Out-Null
$rProg = Chamar 'F1b.2 progressao de A' POST ('/funcionarios/' + $colabA + '/progressao') @{ dataEfeito='2026-12-01'; despachoNumero='DESP/PROG/1' } 201
if ($rProg.OK) { Write-Host ('      escalao: ' + $rProg.Dados.escalaoAnterior + ' -> ' + $rProg.Dados.escalaoNovo) }
Chamar 'F1b.3 NEG progressao sem data de efeito' POST ('/funcionarios/' + $colabA + '/progressao') @{ despachoNumero='DESP/PROG/2' } 400 | Out-Null
# Data posterior a da progressao: a afectacao corrente comecou em 2026-12-01 e a
# regra da data e verificada antes de se procurar a categoria (BR-PRM-04).
Chamar 'F1b.4 NEG promocao para categoria inexistente' POST ('/funcionarios/' + $colabA + '/promocao') @{ dataEfeito='2027-01-15'; categoryId='11111111-1111-1111-1111-111111111111' } 404 | Out-Null
Chamar 'F1b.5 NEG transferencia para Lugar inexistente' POST ('/funcionarios/' + $colabA + '/transferencia') @{ dataEfeito='2027-01-15'; positionId='11111111-1111-1111-1111-111111111111' } 404 | Out-Null
Chamar 'F1b.6 NEG promocao com data anterior a afectacao' POST ('/funcionarios/' + $colabA + '/promocao') @{ dataEfeito='2026-11-01'; categoryId='71e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e702' } 422 | Out-Null

Write-Host ''
Write-Host '=========== F2 - ESTADO QUE ABRE VAGA ==========='

$idFora = $ws['INACTIVE_OUTSIDE'].id
$idActive = $ws['ACTIVE'].id

$rContr = Chamar 'F2.1 contratos de A' GET ('/funcionarios/' + $colabA + '/contratos')
$contratoA = (Linhas $rContr)[0]
Write-Host ('      contrato ' + $contratoA.id + ' estado=' + $contratoA.status)

$rAbre = Chamar 'F2.2 A passa a INACTIVIDADE_FORA_QUADRO' PATCH ('/funcionarios/' + $colabA + '/worker-state') @{ workerStateId=$idFora; dataEfectividade='2026-10-01'; motivoCkey='LICENCA_LONGA' } 200
Verificar 'F2.3 abriu vaga' ($null -ne $rAbre.Dados.afectacaoEncerradaId) ('(' + $rAbre.Dados.afectacaoEncerradaId + ')')
Verificar 'F2.4 nao cessou o vinculo' ($rAbre.Dados.cessouVinculo -eq $false)

$rA = Chamar 'F2.5 A continua activo' GET ('/funcionarios/' + $colabA)
Verificar 'F2.6 isActive verdadeiro' ($rA.Dados.isActive -eq $true)
$rContr2 = Chamar 'F2.7 contrato depois' GET ('/funcionarios/' + $colabA + '/contratos')
$c2 = (Linhas $rContr2) | Where-Object { $_.id -eq $contratoA.id }
Verificar 'F2.8 contrato SUSPENSO' ($c2.status -eq 'SUSPENSO') ('(' + $c2.status + ')')

Chamar 'F2.9 NEG repetir o mesmo estado' PATCH ('/funcionarios/' + $colabA + '/worker-state') @{ workerStateId=$idFora; dataEfectividade='2026-10-02' } 409 | Out-Null
Chamar 'F2.10 NEG estado inexistente' PATCH ('/funcionarios/' + $colabA + '/worker-state') @{ workerStateId='11111111-1111-1111-1111-111111111111'; dataEfectividade='2026-10-02' } 404 | Out-Null

$rVolta = Chamar 'F2.11 regresso de A a ACTIVE' PATCH ('/funcionarios/' + $colabA + '/worker-state') @{ workerStateId=$idActive; dataEfectividade='2026-11-01' } 200
$rContr3 = Chamar 'F2.12 contrato no regresso' GET ('/funcionarios/' + $colabA + '/contratos')
$c3 = (Linhas $rContr3) | Where-Object { $_.id -eq $contratoA.id }
Verificar 'F2.13 contrato voltou a ATIVO' ($c3.status -eq 'ATIVO') ('(' + $c3.status + ')')
Verificar 'F2.14 nao recuperou o Lugar sozinho' ($null -eq $rVolta.Dados.afectacaoEncerradaId)

$rHist = Chamar 'F2.15 historico de estados' GET ('/funcionarios/' + $colabA + '/worker-state/historico')
Write-Host ('      entradas no historico: ' + (Linhas $rHist).Count)

Write-Host ''
Write-Host '=========== F3 - LICENCAS QUE ABREM VAGA ==========='

Verificar 'F3.1 subtipos novos existem' ($st.ContainsKey('LIC_LONGA_DURACAO') -and $st.ContainsKey('LIC_FORMACAO') -and $st.ContainsKey('LIC_ACOMP_CONJUGE'))
Verificar 'F3.2 LIC_FORMACAO abre vaga alem de 180 dias' ($st['LIC_FORMACAO'].positionEffect -eq 'ABRE_VAGA' -and $st['LIC_FORMACAO'].vacancyAfterDays -eq 180) ('(' + $st['LIC_FORMACAO'].positionEffect + '/' + $st['LIC_FORMACAO'].vacancyAfterDays + ')')
Verificar 'F3.3 LIC_SEM_VENCIMENTO mantem o Lugar' ($st['LIC_SEM_VENCIMENTO'].positionEffect -eq 'MANTEM')
Verificar 'F3.4 mobilidade mantem o Lugar' ($st['MOB_COMISSAO'].positionEffect -eq 'MANTEM')
Verificar 'F3.5 LIC_PARENTAL nao aparece entre os activos' (-not $st.ContainsKey('LIC_PARENTAL'))

# As SETE modalidades do art. 45.o n.o 1. Faltavam duas no seed -- a sem vencimento ate 90 dias
# (al. a) e a extraordinaria (al. f) --, e a ausencia nao era visivel em lado nenhum: quem
# instalasse a aplicacao ficava sem elas e sem saber que a lei as previa.
$modalidades45 = @('LIC_SEM_VENC_90','LIC_SEM_VENCIMENTO','LIC_LONGA_DURACAO','LIC_ACOMP_CONJUGE','LIC_ORG_INTERNACIONAL','LIC_EXTRAORDINARIA','LIC_FORMACAO')
$emFalta45 = @($modalidades45 | Where-Object { -not $st.ContainsKey($_) })
Verificar 'F3.5b as sete modalidades do art. 45.o estao no catalogo' ($emFalta45.Count -eq 0) ('(em falta: ' + ($emFalta45 -join ', ') + ')')
# A de 90 dias nao e a de tres anos com prazo menor: sao subseccoes diferentes, com requisitos
# de tempo de servico diferentes. O que o catalogo guarda e o tecto de cada uma.
Verificar 'F3.5c a de 90 dias tem o tecto da sua subseccao' (($st['LIC_SEM_VENC_90'].maxDurationDays -eq 90) -and ($st['LIC_SEM_VENCIMENTO'].maxDurationDays -eq 1095)) ('(90d=' + $st['LIC_SEM_VENC_90'].maxDurationDays + ' 3a=' + $st['LIC_SEM_VENCIMENTO'].maxDurationDays + ')')
# Art. 46.o n.o 3: o Lugar e preenchido por contrato a prazo que CADUCA com o regresso -- e
# substituicao, nao vaga. E o art. 47.o n.o 1 manda descontar a antiguidade.
Verificar 'F3.5d a de 90 dias mantem o Lugar e nao conta antiguidade' (($st['LIC_SEM_VENC_90'].positionEffect -eq 'MANTEM') -and ($st['LIC_SEM_VENC_90'].countsForSeniority -eq $false)) ('(' + $st['LIC_SEM_VENC_90'].positionEffect + ' antiguidade=' + $st['LIC_SEM_VENC_90'].countsForSeniority + ')')
# Art. 64.o remete o regime para o diploma da mobilidade, que nao temos: nao se inventou prazo.
Verificar 'F3.5e a extraordinaria nao inventa prazo nenhum' (($null -eq $st['LIC_EXTRAORDINARIA'].maxDurationDays) -and ($null -eq $st['LIC_EXTRAORDINARIA'].maxExtensions)) ('(dias=' + $st['LIC_EXTRAORDINARIA'].maxDurationDays + ')')

Chamar 'F3.6 NEG mobilidade a abrir vaga' POST '/catalogs/leave-mobility-subtypes' @{ code='MOB_TST_VAGA'; description='Teste'; recordType='MOBILIDADE'; affectsPay=$false; countsForSeniority=$true; canSelfSubmit=$false; positionEffect='ABRE_VAGA' } 400 | Out-Null
Chamar 'F3.7 NEG recordType AMBOS' POST '/catalogs/leave-mobility-subtypes' @{ code='TST_AMBOS'; description='Teste'; recordType='AMBOS'; affectsPay=$false; countsForSeniority=$true; canSelfSubmit=$false } 422 | Out-Null

# A coluna name e NOT NULL desde a V6 e a entity nao a mapeava: criar um subtipo
# pela API rebentava. Codigo unico por execucao, para nao colidir com o anterior.
$codigoNovo = 'LIC_TST_' + (Get-Date -Format 'HHmmss')
Chamar 'F3.7b criar subtipo pela API (coluna name)' POST '/catalogs/leave-mobility-subtypes' @{ code=$codigoNovo; description='Licenca de teste'; recordType='LICENCA'; affectsPay=$true; countsForSeniority=$false; canSelfSubmit=$false; positionEffect='ABRE_VAGA'; vacancyAfterDays=180; returnEffect='DISPONIBILIDADE' } 201 | Out-Null

# Datas ancoradas no presente. Desde a V48 o comportamento depende de onde HOJE cai dentro do
# periodo: o despacho e um acto (art. 44.o n.o 2) e a licenca e um periodo (n.o 1). Com datas
# fixas no futuro este bloco provava o defeito em vez da regra -- aprovar uma licenca de 2027
# abria vaga no proprio dia do despacho.
$agora     = Get-Date
$licIni    = $agora.AddDays(-10).ToString('yyyy-MM-dd')
$licFim180 = $agora.AddDays(169).ToString('yyyy-MM-dd')
$licIni2   = $agora.AddDays(-5).ToString('yyyy-MM-dd')
$licFim200 = $agora.AddDays(194).ToString('yyyy-MM-dd')
$futIni    = $agora.AddDays(30).ToString('yyyy-MM-dd')
$futFim    = $agora.AddDays(229).ToString('yyyy-MM-dd')

$idForm = $st['LIC_FORMACAO'].id
$rL1 = Chamar 'F3.8 criar licenca de formacao de 180 dias em curso (B)' POST ('/funcionarios/' + $colabB + '/licencas-mobilidade') @{ subtipoId=$idForm; dataInicio=$licIni; dataFim=$licFim180; despachoNumero='DESP/1'; justification='curta' } 201
$lic1 = $rL1.Dados.id
$rAp1 = Chamar 'F3.9 deferir licenca de 180 dias' PUT ('/funcionarios/' + $colabB + '/licencas-mobilidade/' + $lic1 + '/approve') $null 200
Verificar 'F3.10 nao abriu vaga (no limite do prazo)' ($null -eq $rAp1.Dados.afectacaoEncerradaId)
Chamar 'F3.11 NEG deferir duas vezes' PUT ('/funcionarios/' + $colabB + '/licencas-mobilidade/' + $lic1 + '/approve') $null 409 | Out-Null
Chamar 'F3.12 regresso antecipado da licenca de 180 dias' PUT ('/funcionarios/' + $colabB + '/licencas-mobilidade/' + $lic1 + '/close') $null 200 | Out-Null

# --- os dois eixos: deferir nao e por em vigor (V48) ---
# Uma licenca que abre vaga, mas que so comeca daqui a um mes. O despacho e hoje; o efeito no
# Lugar pertence ao periodo e so chega na data de inicio, pela mao do job diario.
$rFut = Chamar 'F3.12a criar licenca de 200 dias que so comeca daqui a um mes' POST ('/funcionarios/' + $colabB + '/licencas-mobilidade') @{ subtipoId=$idForm; dataInicio=$futIni; dataFim=$futFim; despachoNumero='DESP/FUT'; justification='futura' } 201
$licFut = $rFut.Dados.id
$rApFut = Chamar 'F3.12b deferir a licenca futura' PUT ('/funcionarios/' + $colabB + '/licencas-mobilidade/' + $licFut + '/approve') $null 200
Verificar 'F3.12c deferir NAO abriu vaga (o efeito e do periodo, nao do despacho)' ($null -eq $rApFut.Dados.afectacaoEncerradaId) '(art. 44.o n.os 1 e 2)'
$rLerFut = Chamar 'F3.12d ler a licenca futura' GET ('/funcionarios/' + $colabB + '/licencas-mobilidade/' + $licFut)
Verificar 'F3.12e deferida (APPROVED) e por iniciar' (($rLerFut.Dados.status -eq 'APPROVED') -and ($rLerFut.Dados.estadoPeriodo -eq 'POR_INICIAR')) ('(' + $rLerFut.Dados.status + '/' + $rLerFut.Dados.estadoPeriodo + ')')
Chamar 'F3.12f NEG regressar de uma licenca que ainda nao comecou' PUT ('/funcionarios/' + $colabB + '/licencas-mobilidade/' + $licFut + '/close') $null 409 | Out-Null
$rLerFut2 = Chamar 'F3.12g o periodo ficou intacto' GET ('/funcionarios/' + $colabB + '/licencas-mobilidade/' + $licFut)
Verificar 'F3.12h o fim NAO ficou anterior ao inicio' ([datetime]$rLerFut2.Dados.dataFim -ge [datetime]$rLerFut2.Dados.dataInicio) ('(' + $rLerFut2.Dados.dataInicio + ' a ' + $rLerFut2.Dados.dataFim + ')')
Chamar 'F3.12i desistir da licenca futura (cancelar)' PUT ('/funcionarios/' + $colabB + '/licencas-mobilidade/' + $licFut + '/cancel') $null 200 | Out-Null

$rL2 = Chamar 'F3.13 criar licenca de formacao de 200 dias em curso (B)' POST ('/funcionarios/' + $colabB + '/licencas-mobilidade') @{ subtipoId=$idForm; dataInicio=$licIni2; dataFim=$licFim200; despachoNumero='DESP/2'; justification='longa' } 201
$lic2 = $rL2.Dados.id
$rAp2 = Chamar 'F3.14 deferir licenca de 200 dias ja em curso' PUT ('/funcionarios/' + $colabB + '/licencas-mobilidade/' + $lic2 + '/approve') $null 200
Verificar 'F3.15 abriu vaga (o periodo ja comecou)' ($null -ne $rAp2.Dados.afectacaoEncerradaId) ('(' + $rAp2.Dados.afectacaoEncerradaId + ')')
$rLer2 = Chamar 'F3.15a ler a licenca em curso' GET ('/funcionarios/' + $colabB + '/licencas-mobilidade/' + $lic2)
Verificar 'F3.15b deferida e EM_CURSO' (($rLer2.Dados.status -eq 'APPROVED') -and ($rLer2.Dados.estadoPeriodo -eq 'EM_CURSO')) ('(' + $rLer2.Dados.status + '/' + $rLer2.Dados.estadoPeriodo + ')')
Chamar 'F3.15c NEG cancelar o que ja comecou' PUT ('/funcionarios/' + $colabB + '/licencas-mobilidade/' + $lic2 + '/cancel') $null 409 | Out-Null

$rB = Chamar 'F3.16 estado de B depois da licenca' GET ('/funcionarios/' + $colabB)
Write-Host ('      estado de B: ' + $rB.Dados.workerStateName + ' activo=' + $rB.Dados.isActive)

$rFecha = Chamar 'F3.17 regresso antecipado (art. 46.o n.o 4)' PUT ('/funcionarios/' + $colabB + '/licencas-mobilidade/' + $lic2 + '/close') $null 200
Verificar 'F3.18 regresso pela disponibilidade' ($null -ne $rFecha.Dados.estadoAtribuidoId) ('(' + $rFecha.Dados.estadoAtribuidoId + ')')
$rLer3 = Chamar 'F3.18a ler a licenca depois do regresso' GET ('/funcionarios/' + $colabB + '/licencas-mobilidade/' + $lic2)
Verificar 'F3.18b continua deferida, e a ausencia acabou na vespera do regresso' (($rLer3.Dados.status -eq 'APPROVED') -and ($rLer3.Dados.dataFim -like ((Get-Date).AddDays(-1).ToString('yyyy-MM-dd') + '*'))) ('(' + $rLer3.Dados.status + ' fim=' + $rLer3.Dados.dataFim + ')')

Write-Host ''
Write-Host '=========== F4 - AUSENCIAS E SALDO ==========='

$idFerias = $lt['FERIAS'].id
Chamar 'F4.1 saldos de A antes' GET ('/funcionarios/' + $colabA + '/saldos-ausencia') | Out-Null
Chamar 'F4.2 criar saldo de ferias (22 dias)' POST ('/funcionarios/' + $colabA + '/saldos-ausencia') @{ tipoAusenciaId=$idFerias; ano=2026; diasDireito=22 } 201 | Out-Null

$rPed = Chamar 'F4.3 criar pedido de ferias' POST ('/funcionarios/' + $colabA + '/pedidos-ausencia') @{ tipoAusenciaId=$idFerias; dataInicio='2026-12-07'; dataFim='2026-12-11'; motivo='ferias' } 201
$ped1 = $rPed.Dados.id
Write-Host ('      dias=' + $rPed.Dados.numeroDias + ' estado=' + $rPed.Dados.estado)

$rS1 = Chamar 'F4.4 ler saldo apos submissao' GET ('/funcionarios/' + $colabA + '/saldos-ausencia?ano=2026')
$s1 = (Linhas $rS1)[0]
Verificar 'F4.5 dias RESERVADOS na submissao' ($s1.diasPendentes -gt 0 -and $s1.diasGozados -eq 0) ('(pend=' + $s1.diasPendentes + ' goz=' + $s1.diasGozados + ')')

Chamar 'F4.6 NEG sobreposicao de datas' POST ('/funcionarios/' + $colabA + '/pedidos-ausencia') @{ tipoAusenciaId=$idFerias; dataInicio='2026-12-09'; dataFim='2026-12-14'; motivo='sobreposto' } 409 | Out-Null
Chamar 'F4.7 NEG pedido maior que o saldo' POST ('/funcionarios/' + $colabA + '/pedidos-ausencia') @{ tipoAusenciaId=$idFerias; dataInicio='2026-06-01'; dataFim='2026-08-31'; motivo='sem saldo' } 422 | Out-Null

Chamar 'F4.8 aprovar o pedido' PATCH ('/funcionarios/' + $colabA + '/pedidos-ausencia/' + $ped1 + '/aprovar') @{ aprovadoPorId=$colabB; observacoesDecisao='deferido' } 200 | Out-Null
$rS2 = Chamar 'F4.9 ler saldo apos aprovacao' GET ('/funcionarios/' + $colabA + '/saldos-ausencia?ano=2026')
$s2 = (Linhas $rS2)[0]
Verificar 'F4.10 dias passaram a GOZADOS' ($s2.diasGozados -gt 0 -and $s2.diasPendentes -eq 0) ('(pend=' + $s2.diasPendentes + ' goz=' + $s2.diasGozados + ')')

Chamar 'F4.11 NEG aprovar duas vezes' PATCH ('/funcionarios/' + $colabA + '/pedidos-ausencia/' + $ped1 + '/aprovar') @{ aprovadoPorId=$colabB } 409 | Out-Null
Chamar 'F4.12 NEG cancelar pelo URL de outro colaborador' PATCH ('/funcionarios/' + $colabB + '/pedidos-ausencia/' + $ped1 + '/cancelar') $null 404 | Out-Null

Chamar 'F4.13 cancelar o pedido aprovado' PATCH ('/funcionarios/' + $colabA + '/pedidos-ausencia/' + $ped1 + '/cancelar') $null 200 | Out-Null
$rS3 = Chamar 'F4.14 ler saldo apos cancelamento' GET ('/funcionarios/' + $colabA + '/saldos-ausencia?ano=2026')
$s3 = (Linhas $rS3)[0]
Verificar 'F4.15 dias DEVOLVIDOS' ($s3.diasGozados -eq 0 -and $s3.diasPendentes -eq 0) ('(pend=' + $s3.diasPendentes + ' goz=' + $s3.diasGozados + ')')
Chamar 'F4.16 NEG cancelar duas vezes' PATCH ('/funcionarios/' + $colabA + '/pedidos-ausencia/' + $ped1 + '/cancelar') $null 409 | Out-Null

$rPed2 = Chamar 'F4.17 novo pedido para rejeitar' POST ('/funcionarios/' + $colabA + '/pedidos-ausencia') @{ tipoAusenciaId=$idFerias; dataInicio='2026-12-21'; dataFim='2026-12-23'; motivo='para rejeitar' } 201
$ped2 = $rPed2.Dados.id
Chamar 'F4.18 rejeitar' PATCH ('/funcionarios/' + $colabA + '/pedidos-ausencia/' + $ped2 + '/rejeitar') @{ aprovadoPorId=$colabB; observacoesDecisao='indeferido' } 200 | Out-Null
$rS4 = Chamar 'F4.19 ler saldo apos rejeicao' GET ('/funcionarios/' + $colabA + '/saldos-ausencia?ano=2026')
$s4 = (Linhas $rS4)[0]
Verificar 'F4.20 rejeicao libertou a reserva' ($s4.diasPendentes -eq 0 -and $s4.diasGozados -eq 0) ('(pend=' + $s4.diasPendentes + ' goz=' + $s4.diasGozados + ')')

$rota = '/funcionarios/' + $colabA + '/pedidos-ausencia?estado=REJEITADO' + [char]38 + 'ano=2026'
$rFil = Chamar 'F4.21 filtrar pedidos por estado (na BD)' GET $rota
$filtrados = @(Linhas $rFil)
$soRejeitados = ($filtrados.Count -ge 1) -and (($filtrados | Where-Object { $_.estado -ne 'REJEITADO' }).Count -eq 0)
Verificar 'F4.22 filtro devolveu so rejeitados' $soRejeitados ('(n=' + $filtrados.Count + ')')

Write-Host ''
Write-Host '=========== F5 - EFEITOS CRUZADOS ==========='

# Quem perdeu o Lugar (A pelo estado, B pela licenca) nao pode progredir: a
# progressao exige afectacao corrente (BR-PRG-04). E a prova de que a vaga abriu
# mesmo, e nao so no papel.
Chamar 'F5.1 NEG progressao de A (sem Lugar, por estado)' POST ('/funcionarios/' + $colabA + '/progressao') @{ dataEfeito='2026-12-05' } 422 | Out-Null
Chamar 'F5.2 NEG progressao de B (sem Lugar, por licenca)' POST ('/funcionarios/' + $colabB + '/progressao') @{ dataEfeito='2026-12-05' } 422 | Out-Null

Write-Host ''
Write-Host '=========== F6 - SUBSTITUICAO DE TITULAR IMPEDIDO ==========='

# Percurso do RH, como no ecra: ve onde o titular esta, ve a categoria do Lugar,
# ve os escaloes dessa categoria, ve as vagas da unidade -- e so entao age.

$rUniC = Chamar 'F6.1 onde esta o C' GET ('/colaboradores/assignments/funcionario/' + $colabC + '/unidade-atual')
$lugarC = $rUniC.Dados.positionId
$unidade = $rUniC.Dados.unidadeOrganicaId
Verificar 'F6.2 C tem Lugar e unidade' (($null -ne $lugarC) -and ($null -ne $unidade)) ('(lugar=' + $rUniC.Dados.numeroLugar + ')')

$rPosC = Chamar 'F6.3 detalhe do Lugar do C' GET ('/estrutura/positions/' + $lugarC)
$catC = $rPosC.Dados.categoryId
$rGradesC = Chamar 'F6.4 escaloes da categoria desse Lugar' GET ('/categories/' + $catC + '/grades')
$escalaoC = (@(Linhas $rGradesC) | Where-Object { $_.isActive -ne $false } | Select-Object -First 1).id
Verificar 'F6.5 ha escalao para usar' ($null -ne $escalaoC) ''

# A chegou aqui sem Lugar (F2 abriu-lhe a vaga). Volta a ter um, porque so assim
# se prova que quem substitui NAO perde o seu (BR-SUB-07).
$rVagas = Chamar 'F6.6 Lugares vagos da unidade' GET ('/colaboradores/assignments/unidade/' + $unidade + '/vagas/lista')
$vagos = @(Linhas $rVagas)
Verificar 'F6.7 ha Lugares vagos para escolher' ($vagos.Count -ge 1) ('(n=' + $vagos.Count + ')')
$lugarLivre = ($vagos | Where-Object { $_.foraDeGrelha -ne $true -and $_.estado -eq 'ATIVO' -and $_.categoryId -eq $catA0 } | Select-Object -First 1)
$rGradesL = Chamar 'F6.8 escaloes da categoria do Lugar vago' GET ('/categories/' + $lugarLivre.categoryId + '/grades')
$escalaoL = (@(Linhas $rGradesL) | Where-Object { $_.isActive -ne $false } | Select-Object -First 1).id
# A ja teve Lugar: e um REINGRESSO, e a origem e do sistema (BR-AF-19).
Chamar 'F6.9a NEG reingresso enviado como ADMISSAO' POST '/colaboradores/assignments' @{ funcionarioId=$colabA; positionId=$lugarLivre.id; gradeId=$escalaoL; origem='ADMISSAO'; dataInicio='2026-11-01' } 422 | Out-Null
Chamar 'F6.9 reingresso de A num Lugar vago da sua categoria' POST '/colaboradores/assignments' @{ funcionarioId=$colabA; positionId=$lugarLivre.id; gradeId=$escalaoL; origem='REINGRESSO'; dataInicio='2026-11-01' } 201 | Out-Null
# Quem ja tem Lugar muda-o por um movimento, nao por aqui (BR-AF-16).
Chamar 'F6.9b NEG colocar quem ja tem Lugar' POST '/colaboradores/assignments' @{ funcionarioId=$colabA; positionId=$lugarLivre.id; gradeId=$escalaoL; dataInicio='2026-11-02' } 422 | Out-Null

# Quem esta em funcoes nao se substitui.
Chamar 'F6.10 NEG substituir titular em actividade' POST ('/funcionarios/' + $colabA + '/substituicao') @{ positionId=$lugarC; gradeId=$escalaoC; dataInicio='2026-11-02' } 422 | Out-Null

# Inactividade NO quadro: suspende o vinculo mas mantem o Lugar (art. 120.o).
Chamar 'F6.11 C passa a SUSPENDED' PATCH ('/funcionarios/' + $colabC + '/worker-state') @{ workerStateId=$ws['SUSPENDED'].id; dataEfectividade='2026-11-02'; motivoCkey='DOENCA'; observacao='incapacidade temporaria' } 200 | Out-Null
$rUniC2 = Chamar 'F6.12 C continua no seu Lugar' GET ('/colaboradores/assignments/funcionario/' + $colabC + '/unidade-atual')
Verificar 'F6.13 a inactividade no quadro NAO abriu vaga' ($rUniC2.Dados.positionId -eq $lugarC) ('(lugar=' + $rUniC2.Dados.numeroLugar + ')')

# Negativos que o ecra tem de tratar.
$rVagas2 = Chamar 'F6.14 relista vagas' GET ('/colaboradores/assignments/unidade/' + $unidade + '/vagas/lista')
$vago2 = (@(Linhas $rVagas2) | Where-Object { $_.estado -eq 'ATIVO' } | Select-Object -First 1)
if ($null -ne $vago2) {
    Chamar 'F6.15 NEG substituir num Lugar VAGO' POST ('/funcionarios/' + $colabB + '/substituicao') @{ positionId=$vago2.id; dataInicio='2026-11-03' } 422 | Out-Null
}
Chamar 'F6.16 NEG C substitui-se a si proprio' POST ('/funcionarios/' + $colabC + '/substituicao') @{ positionId=$lugarC; gradeId=$escalaoC; dataInicio='2026-11-03' } 422 | Out-Null
Chamar 'F6.17 NEG Lugar inexistente' POST ('/funcionarios/' + $colabA + '/substituicao') @{ positionId='00000000-0000-4000-8000-000000000999'; dataInicio='2026-11-03' } 404 | Out-Null
Chamar 'F6.18 NEG sem dataInicio' POST ('/funcionarios/' + $colabA + '/substituicao') @{ positionId=$lugarC } 400 | Out-Null

# O caminho positivo.
$rSub = Chamar 'F6.19 A substitui C' POST ('/funcionarios/' + $colabA + '/substituicao') @{ positionId=$lugarC; gradeId=$escalaoC; dataInicio='2026-11-03'; despachoNumero='DESP-2026/77'; observacoes='substituicao por doenca' } 201
Verificar 'F6.20 a resposta diz quem esta a ser substituido' ($rSub.Dados.titularId -eq $colabC -and $null -ne $rSub.Dados.titularNome) ('(titular=' + $rSub.Dados.titularNome + ')')
Verificar 'F6.21 e traz a afectacao que a faz caducar' ($null -ne $rSub.Dados.titularAssignmentId) ''

# O que a substituicao NAO faz.
$rUniA = Chamar 'F6.22 A mantem o seu proprio Lugar' GET ('/colaboradores/assignments/funcionario/' + $colabA + '/unidade-atual')
Verificar 'F6.23 substituir nao desaloja quem substitui' ($rUniA.Dados.positionId -eq $lugarLivre.id) ('(A esta em ' + $rUniA.Dados.numeroLugar + ')')
$rUniC3 = Chamar 'F6.24 C continua titular' GET ('/colaboradores/assignments/funcionario/' + $colabC + '/unidade-atual')
Verificar 'F6.25 substituir nao desaloja o titular' ($rUniC3.Dados.positionId -eq $lugarC) ''

# O Lugar do titular continua PROVIDO: um substituto nao o torna vago nem o prove.
$rVagas3 = Chamar 'F6.26 vagas da unidade com substituicao em curso' GET ('/colaboradores/assignments/unidade/' + $unidade + '/vagas')
$semTitular = @(Linhas (Chamar 'F6.27 lista de vagas' GET ('/colaboradores/assignments/unidade/' + $unidade + '/vagas/lista')))
Verificar 'F6.28 o Lugar do titular nao aparece como vago' (@($semTitular | Where-Object { $_.id -eq $lugarC }).Count -eq 0) ''

Chamar 'F6.29 NEG segundo substituto para o mesmo titular' POST ('/funcionarios/' + $colabB + '/substituicao') @{ positionId=$lugarC; gradeId=$escalaoC; dataInicio='2026-11-04' } 409 | Out-Null

# --- LER as substituicoes (ate aqui nada as lia) ---
# O POST devolvia o id e mais nada as mostrava; o unidade-atual so responde pela PRINCIPAL.
# Um ecra de RH nao conseguia dizer quem substitui quem.
# Quem substitui e o A (F6.19), nao o B.
$rSubA = Chamar 'F6.29a substituicoes de quem substitui' GET ('/funcionarios/' + $colabA + '/substituicoes?apenasCorrentes=true')
$linhaA = (@($rSubA.Dados.linhas) | Select-Object -First 1)
Verificar 'F6.29b ha uma substituicao em vigor para ele' ($null -ne $linhaA) ('(total=' + $rSubA.Dados.total + ')')
Verificar 'F6.29c o papel dele e SUBSTITUTO' ($linhaA.papel -eq 'SUBSTITUTO') ('(' + $linhaA.papel + ')')
Verificar 'F6.29d a contraparte e o titular impedido' ($linhaA.contraparteId -eq $colabC) ('(' + $linhaA.contraparteNome + ')')
Verificar 'F6.29e diz que Lugar cobre' ($linhaA.positionId -eq $lugarC) ('(' + $linhaA.numeroLugar + ')')
Verificar 'F6.29e2 sem data de fim -- caduca com o regresso (art. 77.o n.o 2)' (($null -ne $linhaA) -and ($null -eq $linhaA.dataFim)) ''

$rSubC = Chamar 'F6.29f a mesma substituicao vista do titular' GET ('/funcionarios/' + $colabC + '/substituicoes?apenasCorrentes=true')
$linhaC = (@($rSubC.Dados.linhas) | Select-Object -First 1)
Verificar 'F6.29g o papel dele e TITULAR' ($linhaC.papel -eq 'TITULAR') ('(' + $linhaC.papel + ')')
Verificar 'F6.29h e a contraparte e quem o substitui' ($linhaC.contraparteId -eq $colabA) ('(' + $linhaC.contraparteNome + ')')

# O regresso do titular fecha a substituicao sozinho (art. 77.o n.o 2). Ate haver endpoint de
# leitura isto provava-se por via indirecta -- se NAO tivesse fechado, a proxima daria 409.
# Agora prova-se DIRECTAMENTE, e a prova indirecta fica como confirmacao.
Chamar 'F6.30 C regressa a actividade' PATCH ('/funcionarios/' + $colabC + '/worker-state') @{ workerStateId=$ws['ACTIVE'].id; dataEfectividade='2026-11-20'; motivoCkey='ALTA'; observacao='fim da incapacidade' } 200 | Out-Null

$rSubFechada = Chamar 'F6.30a ler as substituicoes correntes depois do regresso' GET ('/funcionarios/' + $colabC + '/substituicoes?apenasCorrentes=true')
Verificar 'F6.30b ja nao ha nenhuma em vigor' ((@($rSubFechada.Dados.linhas)).Count -eq 0) ('(' + $rSubFechada.Dados.total + ')')
$rSubHist = Chamar 'F6.30c mas o historico guarda-a' GET ('/funcionarios/' + $colabC + '/substituicoes')
$hist = (@($rSubHist.Dados.linhas) | Select-Object -First 1)
Verificar 'F6.30d encerrada, com data de fim e ja nao corrente' (($null -ne $hist.dataFim) -and ($hist.corrente -ne $true)) ('(fim=' + $hist.dataFim + ')')

Chamar 'F6.31 C volta a ficar impedido' PATCH ('/funcionarios/' + $colabC + '/worker-state') @{ workerStateId=$ws['SUSPENDED'].id; dataEfectividade='2026-11-21'; motivoCkey='DOENCA' } 200 | Out-Null
$rSub2 = Chamar 'F6.32 nova substituicao e aceite' POST ('/funcionarios/' + $colabB + '/substituicao') @{ positionId=$lugarC; gradeId=$escalaoC; dataInicio='2026-11-22' } 201
Verificar 'F6.33 o regresso do titular fechou mesmo a 1a substituicao' $rSub2.OK '(senao teria dado 409)'

Chamar 'F6.33a NEG substituicoes de colaborador inexistente' GET '/funcionarios/00000000-0000-4000-8000-000000000999/substituicoes' $null 404 | Out-Null

Write-Host ''
Write-Host '=========== F7 - CESSACAO PELOS DOIS CAMINHOS ==========='

# A cessacao tem dois caminhos que a aplicacao promete equivalentes: mudar o
# estado para um estado de cessacao, ou encerrar o contrato. Ambos tem de cessar
# o contrato, encerrar a afectacao e mudar o estado. O bloco prova que sim.
#
# Contexto herdado: o C esta impedido (SUSPENDED) e a ser substituido pelo B.
# Cessar o C tem tambem de fechar essa substituicao (art. 77.o n.o 2).

# --- caminho 1: pelo estado do trabalhador ---
$rUniC7 = Chamar 'F7.1 onde esta o C antes de cessar' GET ('/colaboradores/assignments/funcionario/' + $colabC + '/unidade-atual')
$lugarC7 = $rUniC7.Dados.positionId
$unidade7 = $rUniC7.Dados.unidadeOrganicaId

$rVagasAntes = Chamar 'F7.2 vagas da unidade antes' GET ('/colaboradores/assignments/unidade/' + $unidade7 + '/vagas')
$vagasAntes = $rVagasAntes.Dados.vagas

$rCess1 = Chamar 'F7.3 cessar o C pelo estado (RETIRED)' PATCH ('/funcionarios/' + $colabC + '/worker-state') @{ workerStateId=$ws['RETIRED'].id; dataEfectividade='2026-12-01'; motivoCkey='AGE_RETIREMENT'; observacao='limite de idade' } 200
Verificar 'F7.4 a resposta diz que cessou o vinculo' ($rCess1.Dados.cessouVinculo -eq $true) ''
Verificar 'F7.5 encerrou a afectacao' ($null -ne $rCess1.Dados.afectacaoEncerradaId) ''
Verificar 'F7.6 e cessou o contrato' ($null -ne $rCess1.Dados.contratoId) ''

$rVagasDepois = Chamar 'F7.7 vagas da unidade depois' GET ('/colaboradores/assignments/unidade/' + $unidade7 + '/vagas')
Verificar 'F7.8 o Lugar do cessado passou a vago' ($rVagasDepois.Dados.vagas -gt $vagasAntes) ('(' + $vagasAntes + ' -> ' + $rVagasDepois.Dados.vagas + ')')

$rListaV = Chamar 'F7.9 lista de vagas' GET ('/colaboradores/assignments/unidade/' + $unidade7 + '/vagas/lista')
Verificar 'F7.10 o Lugar dele aparece mesmo na lista' (@(@(Linhas $rListaV) | Where-Object { $_.id -eq $lugarC7 }).Count -eq 1) ''

Chamar 'F7.11 NEG progressao de quem cessou' POST ('/funcionarios/' + $colabC + '/progressao') @{ dataEfeito='2026-12-10' } 422 | Out-Null

# A substituicao que o cobria tem de ter caducado: se nao tivesse, substituir
# de novo o mesmo titular daria 409. Aqui da 422, porque o Lugar ficou VAGO --
# e um Lugar vago prove-se com titular, nao com substituto.
Chamar 'F7.12 o Lugar cessado ja nao aceita substituto' POST ('/funcionarios/' + $colabA + '/substituicao') @{ positionId=$lugarC7; dataInicio='2026-12-02' } 422 | Out-Null

# --- caminho 2: pelo encerramento do contrato ---
$rContA = Chamar 'F7.13 contratos do A' GET ('/funcionarios/' + $colabA + '/contratos')
$contA = (@(Linhas $rContA) | Where-Object { $_.isCurrent -eq $true } | Select-Object -First 1)
Verificar 'F7.14 A tem contrato corrente' ($null -ne $contA) ('(' + $contA.contractNumber + ')')

$rUniA7 = Chamar 'F7.15 A tem Lugar antes de cessar' GET ('/colaboradores/assignments/funcionario/' + $colabA + '/unidade-atual')
Verificar 'F7.16 A esta afectado' ($null -ne $rUniA7.Dados.positionId) ('(lugar=' + $rUniA7.Dados.numeroLugar + ')')

$rCess2 = Chamar 'F7.17 cessar o A pelo contrato (close)' PUT ('/funcionarios/' + $colabA + '/contratos/' + $contA.id + '/close') @{ endDate='2026-12-05'; terminationReason='MUTUO_ACORDO' } 200
Verificar 'F7.18 o close cessa o vinculo, como o estado' ($rCess2.Dados.cessouVinculo -eq $true) ''
Verificar 'F7.19 e encerra a afectacao tambem' ($null -ne $rCess2.Dados.afectacaoEncerradaId) ''

Chamar 'F7.20 A ja nao tem afectacao corrente' GET ('/colaboradores/assignments/funcionario/' + $colabA + '/unidade-atual') $null 404 | Out-Null
# Encerrar duas vezes NAO da erro: o CessacaoService salta o contrato que ja esta
# cessado. O que tem de se garantir e que o segundo close nao sobrepoe o primeiro.
Chamar 'F7.21 encerrar o mesmo contrato outra vez e idempotente' PUT ('/funcionarios/' + $colabA + '/contratos/' + $contA.id + '/close') @{ endDate='2026-12-30'; terminationReason='OUTRO_MOTIVO' } 200 | Out-Null
$rContA2 = Chamar 'F7.22 reler os contratos do A' GET ('/funcionarios/' + $colabA + '/contratos')
$contA2 = (@(Linhas $rContA2) | Where-Object { $_.id -eq $contA.id } | Select-Object -First 1)
Verificar 'F7.23 o 2o close nao sobrepos a data nem o motivo' (($contA2.endDate -like '2026-12-05*') -and ($contA2.terminationReason -eq 'MUTUO_ACORDO')) ('(fim=' + $contA2.endDate + ' motivo=' + $contA2.terminationReason + ')')
Verificar 'F7.24 e o contrato continua CESSADO' ($contA2.status -eq 'CESSADO') ('(' + $contA2.status + ')')

Write-Host ''
Write-Host '=========== F8 - MOBILIDADE TRANSITORIA PONTA A PONTA ==========='

# O que este bloco existe para provar: a mobilidade transitoria NAO mexe na
# afectacao (art. 135.o n.o 7). O titular mantem o Lugar; muda so onde exerce
# funcoes. Se o positionId mudar em algum passo, e bug.
#
# Contexto herdado: depois do F7 o A e o C estao cessados e o B esta em
# disponibilidade, sem Lugar. Comeca-se por lhe dar um -- que e exactamente o que
# o art. 122.o preve para quem aguarda vaga.

# As datas ancoram-se no dia corrente, e nao em 2027: "em vigor" quer dizer que a
# licenca cobre HOJE (MobilidadeService.mobilidadeEmVigor). Uma mobilidade marcada
# para o ano que vem existe, mas nao esta em vigor -- e o ecra mostraria a pessoa
# no seu Lugar, correctamente.
$hoje = Get-Date
$dOntem   = $hoje.AddDays(-30).ToString('yyyy-MM-dd')
$dInicio  = $hoje.AddDays(-5).ToString('yyyy-MM-dd')
$dFim     = $hoje.AddMonths(3).ToString('yyyy-MM-dd')
$dProrrog = $hoje.AddMonths(6).ToString('yyyy-MM-dd')
$dHoje    = $hoje.ToString('yyyy-MM-dd')
# B sai do Lugar em $dInicio (licenca do F3): regressa e e recolocado depois disso
$dReing   = $hoje.AddDays(-2).ToString('yyyy-MM-dd')
$dFimExt  = $hoje.AddMonths(2).ToString('yyyy-MM-dd')

Chamar 'F8.1 B regressa a actividade' PATCH ('/funcionarios/' + $colabB + '/worker-state') @{ workerStateId=$ws['ACTIVE'].id; dataEfectividade=$dReing; motivoCkey='VAGA_DISPONIVEL' } 200 | Out-Null

$rUnidades = Chamar 'F8.2 unidades organicas' GET '/estrutura/organizational-units?pagina=0&tamanho=20'
$unidades = @(Linhas $rUnidades)
Verificar 'F8.3 ha pelo menos duas unidades' ($unidades.Count -ge 2) ('(n=' + $unidades.Count + ')')

$rVagas8 = Chamar 'F8.4 Lugares vagos' GET ('/colaboradores/assignments/unidade/' + $unidade7 + '/vagas/lista')
# O B ja teve Lugar (ASS_TEC, no seed): volta por REINGRESSO, a um Lugar da sua categoria (art. 122.o, BR-AF-21).
$catAt8 = (@(Linhas (Chamar 'F8.4b categorias' GET '/categories?pagina=0&tamanho=50')) | Where-Object { $_.code -eq 'ASS_TEC' } | Select-Object -First 1).id
$lugar8 = (@(Linhas $rVagas8) | Where-Object { $_.foraDeGrelha -ne $true -and $_.estado -eq 'ATIVO' -and $_.categoryId -eq $catAt8 } | Select-Object -First 1)
foreach ($u in $unidades) {
    if ($null -ne $lugar8) { break }
    $lugar8 = (@(Linhas (Chamar ('F8.4c Lugares vagos em ' + $u.name) GET ('/colaboradores/assignments/unidade/' + $u.id + '/vagas/lista'))) | Where-Object { $_.foraDeGrelha -ne $true -and $_.estado -eq 'ATIVO' -and $_.categoryId -eq $catAt8 } | Select-Object -First 1)
}
$rGrades8 = Chamar 'F8.5 escaloes da categoria' GET ('/categories/' + $lugar8.categoryId + '/grades')
$escalao8 = (@(Linhas $rGrades8) | Where-Object { $_.isActive -ne $false } | Select-Object -First 1).id
Chamar 'F8.6 reingresso de B num Lugar vago da sua categoria' POST '/colaboradores/assignments' @{ funcionarioId=$colabB; positionId=$lugar8.id; gradeId=$escalao8; origem='REINGRESSO'; dataInicio=$dReing } 201 | Out-Null

$rUniB = Chamar 'F8.7 onde esta o B' GET ('/colaboradores/assignments/funcionario/' + $colabB + '/unidade-atual')
$lugarB = $rUniB.Dados.positionId
$unidadeB = $rUniB.Dados.unidadeOrganicaId
Verificar 'F8.8 B tem Lugar e nao esta em mobilidade' (($null -ne $lugarB) -and ($rUniB.Dados.emMobilidade -eq $false)) ('(lugar=' + $rUniB.Dados.numeroLugar + ')')

# --- mobilidade INTERNA ---
# A mobilidade COMUM, e nao a primeira da lista: a ordem do catalogo nao e garantida, e a
# comissao de servico tem outro regime -- tres anos sucessivamente renovaveis (art. 60.o n.o 1)
# e o regresso do art. 64.o n.o 2. Se calhasse a comissao, o F8.21 (prorrogar alem do maximo)
# deixava de poder falhar, porque ela nao tem limite de prorrogacoes. O F17 e que a exercita.
$subMob = ($subtipos | Where-Object { $_.recordType -eq 'MOBILIDADE' -and $_.returnEffect -eq 'REGRESSA_LUGAR' -and $_.isActive -ne $false } | Select-Object -First 1)
Verificar 'F8.9 ha subtipo de mobilidade comum no catalogo' ($null -ne $subMob) ('(' + $subMob.code + ')')
$destino = ($unidades | Where-Object { $_.id -ne $unidadeB } | Select-Object -First 1)

$rMob = Chamar 'F8.10 criar mobilidade interna' POST ('/funcionarios/' + $colabB + '/licencas-mobilidade') @{ subtipoId=$subMob.id; dataInicio=$dReing; dataFim=$dFim; destinationUnitId=$destino.id; justification='requisicao' } 201
$mobId = $rMob.Dados.id

# Forma de prestacao (art. 134.o n.o 2): quem nao diz nada esta em exclusividade, que e a
# regra do art. 20.o. Nao se inventa acumulacao por omissao.
$rMobLida = Chamar 'F8.10b ler a mobilidade criada' GET ('/funcionarios/' + $colabB + '/licencas-mobilidade/' + $mobId)
Verificar 'F8.10c omissa, a forma e TEMPO_INTEIRO' ($rMobLida.Dados.formaPrestacao -eq 'TEMPO_INTEIRO') ('(' + $rMobLida.Dados.formaPrestacao + ')')

$rApr = Chamar 'F8.11 aprovar a mobilidade' PUT ('/funcionarios/' + $colabB + '/licencas-mobilidade/' + $mobId + '/approve') $null 200
Verificar 'F8.12 aprovar NAO encerrou afectacao nenhuma' ($null -eq $rApr.Dados.afectacaoEncerradaId) '(art. 135.o n.o 7)'

$rUniB2 = Chamar 'F8.13 onde esta o B durante a mobilidade' GET ('/colaboradores/assignments/funcionario/' + $colabB + '/unidade-atual')
Verificar 'F8.14 o Lugar NAO mudou' ($rUniB2.Dados.positionId -eq $lugarB) ('(lugar=' + $rUniB2.Dados.numeroLugar + ')')
Verificar 'F8.15 mas esta em mobilidade' ($rUniB2.Dados.emMobilidade -eq $true) ''
Verificar 'F8.16 e exerce funcoes na unidade de destino' ($rUniB2.Dados.exerceFuncoesUnidadeId -eq $destino.id) ('(' + $rUniB2.Dados.exerceFuncoesUnidadeNome + ')')
Verificar 'F8.17 destino classificado como INTERNO' ($rUniB2.Dados.mobilidadeDestinoTipo -eq 'INTERNO') ''

$rVagasMob = Chamar 'F8.18 vagas da unidade de origem' GET ('/colaboradores/assignments/unidade/' + $unidadeB + '/vagas/lista')
Verificar 'F8.19 o Lugar dele NAO ficou vago' (@(@(Linhas $rVagasMob) | Where-Object { $_.id -eq $lugarB }).Count -eq 0) ''

# Prorrogacao: o limite vem do subtipo, nao do codigo.
Chamar 'F8.20 prorrogar dentro do limite' PUT ('/funcionarios/' + $colabB + '/licencas-mobilidade/' + $mobId + '/prorrogar') @{ novaDataFim=$dProrrog; justificacao='necessidade do servico' } 200 | Out-Null
Chamar 'F8.21 NEG prorrogar alem do maximo de prorrogacoes' PUT ('/funcionarios/' + $colabB + '/licencas-mobilidade/' + $mobId + '/prorrogar') @{ novaDataFim=$hoje.AddMonths(9).ToString('yyyy-MM-dd'); justificacao='outra vez' } 422 | Out-Null

$rClose = Chamar 'F8.22 encerrar a mobilidade' PUT ('/funcionarios/' + $colabB + '/licencas-mobilidade/' + $mobId + '/close') $null 200
$rUniB3 = Chamar 'F8.23 onde esta o B depois' GET ('/colaboradores/assignments/funcionario/' + $colabB + '/unidade-atual')
Verificar 'F8.24 continua no mesmo Lugar (nunca saiu)' ($rUniB3.Dados.positionId -eq $lugarB) ''
Verificar 'F8.25 e ja nao esta em mobilidade' ($rUniB3.Dados.emMobilidade -eq $false) ''
Verificar 'F8.26 volta a exercer na sua unidade' ($rUniB3.Dados.exerceFuncoesUnidadeId -eq $unidadeB) ''
Chamar 'F8.27 NEG encerrar duas vezes' PUT ('/funcionarios/' + $colabB + '/licencas-mobilidade/' + $mobId + '/close') $null 409 | Out-Null

# --- forma de prestacao em ACUMULACAO (art. 134.o n.o 2 al. b)) ---
# A acumulacao e uma forma de PRESTAR a mobilidade, nao um titulo para ocupar um segundo
# Lugar -- foi essa a leitura errada que o TipoAfectacao.ACUMULACAO tinha. Continua a nao
# criar afectacao nenhuma: a mobilidade transitoria e sem ocupacao do lugar do quadro.
$dIniAc = $hoje.AddMonths(4).ToString('yyyy-MM-dd')
$dFimAc = $hoje.AddMonths(7).ToString('yyyy-MM-dd')
$rMobAc = Chamar 'F8.28 criar mobilidade em acumulacao' POST ('/funcionarios/' + $colabB + '/licencas-mobilidade') @{ subtipoId=$subMob.id; dataInicio=$dIniAc; dataFim=$dFimAc; destinationUnitId=$destino.id; justification='acumulacao com o servico de origem'; formaPrestacao='ACUMULACAO' } 201
$mobAcId = $rMobAc.Dados.id
$rMobAcLida = Chamar 'F8.29 ler a mobilidade em acumulacao' GET ('/funcionarios/' + $colabB + '/licencas-mobilidade/' + $mobAcId)
Verificar 'F8.30 ficou registada em ACUMULACAO' ($rMobAcLida.Dados.formaPrestacao -eq 'ACUMULACAO') ('(' + $rMobAcLida.Dados.formaPrestacao + ')')

$rUniAc = Chamar 'F8.31 onde esta o B com a acumulacao marcada' GET ('/colaboradores/assignments/funcionario/' + $colabB + '/unidade-atual')
Verificar 'F8.32 a acumulacao nao criou afectacao nenhuma' ($rUniAc.Dados.positionId -eq $lugarB) '(art. 135.o n.o 7)'

Chamar 'F8.33 NEG forma de prestacao fora da lista' POST ('/funcionarios/' + $colabB + '/licencas-mobilidade') @{ subtipoId=$subMob.id; dataInicio=$dIniAc; dataFim=$dFimAc; destinationUnitId=$destino.id; formaPrestacao='MEIO_TEMPO' } 422 | Out-Null

# Quem esta de licenca nao exerce funcoes em servico nenhum: nao ha nada a acumular.
$subLic = ($subtipos | Where-Object { $_.recordType -eq 'LICENCA' -and $_.isActive -ne $false } | Select-Object -First 1)
if ($null -ne $subLic) {
    Chamar 'F8.34 NEG acumulacao numa licenca' POST ('/funcionarios/' + $colabB + '/licencas-mobilidade') @{ subtipoId=$subLic.id; dataInicio=$dIniAc; dataFim=$dFimAc; justification='nao se aplica'; formaPrestacao='ACUMULACAO' } 422 | Out-Null
}

# Deixar a base como estava: a mobilidade futura nao interessa aos blocos seguintes.
Chamar 'F8.35 cancelar a mobilidade em acumulacao' PUT ('/funcionarios/' + $colabB + '/licencas-mobilidade/' + $mobAcId + '/cancel') $null 200 | Out-Null

# --- mobilidade EXTERNA ---
$rMobE = Chamar 'F8.28 criar mobilidade externa' POST ('/funcionarios/' + $colabB + '/licencas-mobilidade') @{ subtipoId=$subMob.id; dataInicio=$dHoje; dataFim=$dFimExt; entidadeDestino='Camara Municipal da Praia'; justification='cedencia' } 201
$mobE = $rMobE.Dados.id
Chamar 'F8.29 aprovar a externa' PUT ('/funcionarios/' + $colabB + '/licencas-mobilidade/' + $mobE + '/approve') $null 200 | Out-Null
$rUniB4 = Chamar 'F8.30 onde exerce funcoes agora' GET ('/colaboradores/assignments/funcionario/' + $colabB + '/unidade-atual')
Verificar 'F8.31 destino classificado como EXTERNO' ($rUniB4.Dados.mobilidadeDestinoTipo -eq 'EXTERNO') ('(' + $rUniB4.Dados.exerceFuncoesUnidadeNome + ')')
Verificar 'F8.32 e o Lugar continua a ser dele' ($rUniB4.Dados.positionId -eq $lugarB) ''
Chamar 'F8.33 encerrar a externa' PUT ('/funcionarios/' + $colabB + '/licencas-mobilidade/' + $mobE + '/close') $null 200 | Out-Null

# --- negativos ---
$rMobSemDestino = Chamar 'F8.34 criar mobilidade sem destino nenhum' POST ('/funcionarios/' + $colabB + '/licencas-mobilidade') @{ subtipoId=$subMob.id; dataInicio=$dHoje; dataFim=$dFimExt; justification='sem destino' } 201
Chamar 'F8.35 NEG aprovar mobilidade sem destino' PUT ('/funcionarios/' + $colabB + '/licencas-mobilidade/' + $rMobSemDestino.Dados.id + '/approve') $null 422 | Out-Null

$codMob = 'MOB_TST_' + (Get-Date -Format 'HHmmss')
Chamar 'F8.36 NEG subtipo de mobilidade a abrir vaga' POST '/catalogs/leave-mobility-subtypes' @{ code=$codMob; name='Mobilidade que abre vaga'; description='nao permitido'; recordType='MOBILIDADE'; positionEffect='ABRE_VAGA'; returnEffect='REGRESSA_LUGAR' } 400 | Out-Null

Write-Host ''
Write-Host '=========== F9 - PROMOCAO NAS DUAS FORMAS ==========='

# A promocao tem duas modalidades e a aplicacao deduz qual pelo pedido:
#   com positionId  -> a pessoa muda para um Lugar vago da categoria de cima
#   sem positionId  -> e o proprio Lugar que sobe de categoria (reclassificacao)
# A modalidade nao se persiste: deduz-se do historico. Este bloco prova as duas.
#
# O B esta num Lugar TEC_SUP -- a categoria de topo; nao ha para onde o promover, e ninguem
# se desce de categoria pela porta generica (BR-AF-16). Por isso cada forma da promocao usa um
# colaborador novo, ADMITIDO num Lugar da categoria de baixo (a colocacao legitima).

$rCats = Chamar 'F9.1 catalogo de categorias' GET '/categories?pagina=0&tamanho=50'
$cats = @(Linhas $rCats)
$catBaixo = ($cats | Where-Object { $_.ordemProgressao -eq 1 } | Select-Object -First 1)
$catCima  = ($cats | Where-Object { $_.ordemProgressao -eq 2 } | Select-Object -First 1)
Verificar 'F9.2 a grelha tem ordem de progressao definida' (($null -ne $catBaixo) -and ($null -ne $catCima)) ('(' + $catBaixo.code + ' -> ' + $catCima.code + ')')

$rVagas9 = Chamar 'F9.3 Lugares vagos da unidade' GET ('/colaboradores/assignments/unidade/' + $unidadeB + '/vagas/lista')
$vagos9 = @(Linhas $rVagas9)
$lugarBaixo = ($vagos9 | Where-Object { $_.categoryId -eq $catBaixo.id -and $_.estado -eq 'ATIVO' } | Select-Object -First 1)
$lugarCima  = ($vagos9 | Where-Object { $_.categoryId -eq $catCima.id  -and $_.estado -eq 'ATIVO' } | Select-Object -First 1)
Verificar 'F9.4 ha Lugar vago em cada categoria' (($null -ne $lugarBaixo) -and ($null -ne $lugarCima)) ('(' + $lugarBaixo.numeroLugar + ' / ' + $lugarCima.numeroLugar + ')')

$rGr9 = Chamar 'F9.5 escaloes da categoria de baixo' GET ('/categories/' + $catBaixo.id + '/grades')
$escBaixo = (@(Linhas $rGr9) | Where-Object { $_.isActive -ne $false } | Select-Object -First 1).id
$dAfect9 = $hoje.AddDays(-20).ToString('yyyy-MM-dd')
$dPromo  = $hoje.AddDays(-1).ToString('yyyy-MM-dd')
# Um vinculo que permita evoluir: nomeacao definitiva (EFETIVO).
$tipoNomeacao = (@(Linhas (Chamar 'F9.5b tipos de contrato' GET '/catalogs/contract-types?pagina=0&tamanho=50')) | Where-Object { $_.code -eq 'NOMEACAO_DEFINITIVA' } | Select-Object -First 1).id
$nifD = '4' + (Get-Date -Format 'MMddHHmmss')
$colabD = (Chamar 'F9.6a admitir D (promocao com mudanca de Lugar)' POST '/funcionarios' @{ nomeCompleto='Promocao Muda Lugar'; dataNascimento='1991-04-04'; genero='F'; estadoCivil='SOLTEIRO'; nif=$nifD; dataAdmissao=$dAfect9 } 201).Dados.id
Chamar 'F9.6b contrato de D' POST ('/funcionarios/' + $colabD + '/contratos') @{ contractTypeId=$tipoNomeacao; startDate=$dAfect9; regimeTrabalho='TEMPO_COMPLETO' } 201 | Out-Null
Chamar 'F9.6 colocar D na categoria de baixo (ADMISSAO)' POST '/colaboradores/assignments' @{ funcionarioId=$colabD; positionId=$lugarBaixo.id; gradeId=$escBaixo; dataInicio=$dAfect9 } 201 | Out-Null

# --- negativos, antes de gastar o cenario ---
Chamar 'F9.7 NEG promover para a mesma categoria' POST ('/funcionarios/' + $colabD + '/promocao') @{ categoryId=$catBaixo.id; dataEfeito=$dPromo } 422 | Out-Null
Chamar 'F9.8 NEG promover para Lugar de outra categoria' POST ('/funcionarios/' + $colabD + '/promocao') @{ categoryId=$catCima.id; positionId=$lugarBaixo.id; dataEfeito=$dPromo } 422 | Out-Null
Chamar 'F9.9 NEG data de efeito anterior a afectacao' POST ('/funcionarios/' + $colabD + '/promocao') @{ categoryId=$catCima.id; dataEfeito=$hoje.AddDays(-60).ToString('yyyy-MM-dd') } 422 | Out-Null
Chamar 'F9.10 NEG categoria inexistente' POST ('/funcionarios/' + $colabD + '/promocao') @{ categoryId='00000000-0000-4000-8000-000000000999'; dataEfeito=$dPromo } 404 | Out-Null

# --- forma 1: muda de Lugar ---
$rProm1 = Chamar 'F9.11 promover COM positionId (muda de Lugar)' POST ('/funcionarios/' + $colabD + '/promocao') @{ categoryId=$catCima.id; positionId=$lugarCima.id; dataEfeito=$dPromo; despachoNumero='DESP-2026/90'; concursoRef='CI-2026/3' } 201
Verificar 'F9.12 a aplicacao deduziu "mudanca de Lugar"' ($rProm1.Dados.lugarReclassificado -eq $false) ''
Verificar 'F9.13 subiu da categoria de baixo para a de cima' (($rProm1.Dados.categoriaAnteriorId -eq $catBaixo.id) -and ($rProm1.Dados.categoriaNovaId -eq $catCima.id)) ('(' + $rProm1.Dados.categoriaAnterior + ' -> ' + $rProm1.Dados.categoriaNova + ')')

$rUni9 = Chamar 'F9.14 onde esta o D depois da promocao' GET ('/colaboradores/assignments/funcionario/' + $colabD + '/unidade-atual')
Verificar 'F9.15 esta no Lugar de destino' ($rUni9.Dados.positionId -eq $lugarCima.id) ('(' + $rUni9.Dados.numeroLugar + ')')
$rVagas9b = Chamar 'F9.16 vagas depois' GET ('/colaboradores/assignments/unidade/' + $unidadeB + '/vagas/lista')
Verificar 'F9.17 o Lugar que deixou ficou vago' (@(@(Linhas $rVagas9b) | Where-Object { $_.id -eq $lugarBaixo.id }).Count -eq 1) ''

# --- forma 2: o Lugar e que sobe ---
# O Lugar de baixo ficou vago com a promocao do D: entra outro colaborador novo, D2.
$nifD2 = '3' + (Get-Date -Format 'MMddHHmmss')
$colabD2 = (Chamar 'F9.18a admitir D2 (promocao em que o Lugar sobe)' POST '/funcionarios' @{ nomeCompleto='Promocao Lugar Sobe'; dataNascimento='1992-05-05'; genero='M'; estadoCivil='SOLTEIRO'; nif=$nifD2; dataAdmissao=$dPromo } 201).Dados.id
Chamar 'F9.18b contrato de D2' POST ('/funcionarios/' + $colabD2 + '/contratos') @{ contractTypeId=$tipoNomeacao; startDate=$dPromo; regimeTrabalho='TEMPO_COMPLETO' } 201 | Out-Null
Chamar 'F9.18 colocar D2 no Lugar de baixo (ADMISSAO)' POST '/colaboradores/assignments' @{ funcionarioId=$colabD2; positionId=$lugarBaixo.id; gradeId=$escBaixo; dataInicio=$dPromo } 201 | Out-Null
$rProm2 = Chamar 'F9.19 promover SEM positionId (o Lugar sobe)' POST ('/funcionarios/' + $colabD2 + '/promocao') @{ categoryId=$catCima.id; dataEfeito=$hoje.ToString('yyyy-MM-dd'); despachoNumero='DESP-2026/91' } 201
Verificar 'F9.20 a aplicacao deduziu "reclassificacao"' ($rProm2.Dados.lugarReclassificado -eq $true) ''
Verificar 'F9.21 ficou no MESMO Lugar' ($rProm2.Dados.positionId -eq $lugarBaixo.id) ''

$rPos9 = Chamar 'F9.22 detalhe do Lugar reclassificado' GET ('/estrutura/positions/' + $lugarBaixo.id)
Verificar 'F9.23 foi o Lugar que mudou de categoria' ($rPos9.Dados.categoryId -eq $catCima.id) ('(' + $rPos9.Dados.categoryNome + ')')

Write-Host ''
Write-Host '=========== F10 - CONTRATO DAS RESPOSTAS ==========='

# O front-end passou a ler {id, sucesso, alertas} em vez de {message}. Este bloco
# le as respostas como um cliente as leria, e nao so o codigo HTTP.

$codOpt = 'TESTE_F10_' + (Get-Date -Format 'HHmmss')
$rOpt = Chamar 'F10.1 criar etiqueta de catalogo' POST '/reference/options' @{ ccode=$codOpt; ckey='K1'; cvalue='Valor 1'; locale='pt-CV'; sortOrder=1 } 201
Verificar 'F10.2 a resposta traz id e sucesso' (($null -ne $rOpt.Dados.id) -and ($rOpt.Dados.sucesso -eq $true)) ''
Verificar 'F10.3 e alertas vem vazio, nunca nulo' ($null -ne $rOpt.Dados.alertas) ('(n=' + @($rOpt.Dados.alertas).Count + ')')
Verificar 'F10.4 o campo message desapareceu' ($null -eq $rOpt.Dados.message) ''

$optId = $rOpt.Dados.id
$rDes = Chamar 'F10.5 desactivar' DELETE ('/reference/options/' + $optId) $null 200
Verificar 'F10.6 desactivar devolve o id do que foi afectado' ($rDes.Dados.id -eq $optId) ''
Chamar 'F10.7 NEG desactivar duas vezes' DELETE ('/reference/options/' + $optId) $null 409 | Out-Null
$rAct = Chamar 'F10.8 reactivar' PATCH ('/reference/options/' + $optId + '/activate') $null 200
Verificar 'F10.9 reactivar devolve sucesso' ($rAct.Dados.sucesso -eq $true) ''

# O tipo de afectacao passou a ser validado (enum TipoAfectacao).
Chamar 'F10.10 NEG assignmentType fora da lista' POST '/colaboradores/assignments' @{ funcionarioId=$colabB; positionId=$lugarBaixo.id; gradeId=$escBaixo; assignmentType='INTERINO'; origem='ADMISSAO'; dataInicio=$dPromo } 422 | Out-Null
# A porta generica e so da titularidade. A substituicao tem endpoint proprio, e criar uma por
# aqui deixava-a sem ligacao ao titular e sem nenhuma das regras do SubstituicaoService.
Chamar 'F10.10b NEG substituicao pela porta generica' POST '/colaboradores/assignments' @{ funcionarioId=$colabB; positionId=$lugarBaixo.id; gradeId=$escBaixo; assignmentType='SUBSTITUICAO'; origem='ADMISSAO'; dataInicio=$dPromo } 422 | Out-Null
# A ACUMULACAO saiu do enum: o art. 134.o n.o 2 al. b) e forma de prestacao da MOBILIDADE, nao
# um titulo para ocupar um segundo Lugar.
Chamar 'F10.10c NEG acumulacao ja nao e um titulo' POST '/colaboradores/assignments' @{ funcionarioId=$colabB; positionId=$lugarBaixo.id; gradeId=$escBaixo; assignmentType='ACUMULACAO'; origem='ADMISSAO'; dataInicio=$dPromo } 422 | Out-Null

# Erros continuam com o corpo de problema, nao com o DTO de sucesso.
$rErr = Chamar 'F10.11 NEG etiqueta inexistente' DELETE '/reference/options/00000000-0000-4000-8000-000000000999' $null 404
Verificar 'F10.12 o erro traz title, nao sucesso' (($null -ne $rErr.Dados.title) -and ($null -eq $rErr.Dados.sucesso)) ''

Write-Host ''
Write-Host '=========== F11 - VENCIMENTO DE FERIAS (DL 3/2010 cap. II) ==========='

# O saldo de ferias deixou de ser escrito a mao: vence-se sozinho (art. 2.o n.o 4) e e
# proporcional no ano de ingresso (art. 3.o). Quem sabe QUAIS linhas do catalogo sao ferias
# e a coluna `regime`, nunca o codigo.

$anoAgora = (Get-Date).Year
$rTipos = Chamar 'F11.1 catalogo de tipos de ausencia' GET '/catalogs/leave-types'
$tipoFerias = (@(Linhas $rTipos) | Where-Object { $_.regime -eq 'FERIAS' } | Select-Object -First 1)
Verificar 'F11.2 ha um tipo classificado como FERIAS' ($null -ne $tipoFerias) ('(' + $tipoFerias.code + ')')
# Tres regimes desde a V54, e nao dois: o DL separa as faltas justificadas (seccao II) das
# injustificadas (seccao III), que nao contam para antiguidade (art. 43.o n.o 2).
Verificar 'F11.3 os restantes sao FALTA ou FALTA_INJUSTIFICADA' ((@(Linhas $rTipos) | Where-Object { $_.regime -ne 'FERIAS' -and $_.regime -ne 'FALTA' -and $_.regime -ne 'FALTA_INJUSTIFICADA' }).Count -eq 0) ''

# --- ano inteiro: admitido a 1 de Janeiro deste ano ---
$nifA = '9' + (Get-Date -Format 'MMddHHmmss')
$rNovoA = Chamar 'F11.4 admitir colaborador a 1 de Janeiro' POST '/funcionarios' @{ nomeCompleto='Ferias Ano Inteiro'; dataNascimento='1990-05-05'; genero='M'; estadoCivil='SOLTEIRO'; nif=$nifA; dataAdmissao=($anoAgora.ToString() + '-01-01') } 201
$colabFA = $rNovoA.Dados.id
$rSaldoA = Chamar 'F11.5 o saldo nasceu sozinho' GET ('/funcionarios/' + $colabFA + '/saldos-ausencia?ano=' + $anoAgora)
$saldoFA = (@(Linhas $rSaldoA) | Where-Object { $_.tipoAusenciaId -eq $tipoFerias.id } | Select-Object -First 1)
Verificar 'F11.6 ha saldo de ferias sem ninguem o criar' ($null -ne $saldoFA) '(art. 2.o n.o 4)'
Verificar 'F11.7 com os 22 dias do catalogo' ($saldoFA.diasDireito -eq 22) ('(' + $saldoFA.diasDireito + ' dias)')

# --- ano de ingresso: proporcional (art. 3.o) ---
# Admitido a 1 de Julho: dois trimestres completos ate 31 de Dezembro -> 11 dias.
$nifB = '8' + (Get-Date -Format 'MMddHHmmss')
$rNovoB = Chamar 'F11.8 admitir colaborador a 1 de Julho' POST '/funcionarios' @{ nomeCompleto='Ferias Proporcional'; dataNascimento='1992-06-06'; genero='F'; estadoCivil='SOLTEIRO'; nif=$nifB; dataAdmissao=($anoAgora.ToString() + '-07-01') } 201
$colabFB = $rNovoB.Dados.id
$rSaldoB = Chamar 'F11.9 saldo do ano de ingresso' GET ('/funcionarios/' + $colabFB + '/saldos-ausencia?ano=' + $anoAgora)
$saldoFB = (@(Linhas $rSaldoB) | Where-Object { $_.tipoAusenciaId -eq $tipoFerias.id } | Select-Object -First 1)
Verificar 'F11.10 proporcional, nao o ano inteiro' (($null -ne $saldoFB) -and ($saldoFB.diasDireito -eq 11)) ('(' + $saldoFB.diasDireito + ' dias, esperado 11)')

# --- abaixo dos 90 dias de servico nao ha gozo antecipado (art. 3.o) ---
$nifC = '7' + (Get-Date -Format 'MMddHHmmss')
$rNovoC = Chamar 'F11.11 admitir colaborador a 1 de Dezembro' POST '/funcionarios' @{ nomeCompleto='Ferias Sem Direito'; dataNascimento='1995-07-07'; genero='M'; estadoCivil='SOLTEIRO'; nif=$nifC; dataAdmissao=($anoAgora.ToString() + '-12-01') } 201
$colabFC = $rNovoC.Dados.id
$rSaldoC = Chamar 'F11.12 saldo de quem entrou em Dezembro' GET ('/funcionarios/' + $colabFC + '/saldos-ausencia?ano=' + $anoAgora)
$saldoFC = (@(Linhas $rSaldoC) | Where-Object { $_.tipoAusenciaId -eq $tipoFerias.id } | Select-Object -First 1)
Verificar 'F11.13 existe, mas a zero dias' (($null -ne $saldoFC) -and ($saldoFC.diasDireito -eq 0)) ('(' + $saldoFC.diasDireito + ' dias)')

# --- a classificacao e da instituicao, e muda-se pela API, nao por SQL ---
$codigoReg = 'REG_TST_' + (Get-Date -Format 'HHmmss')
$rTipoNovo = Chamar 'F11.14 criar tipo sem indicar regime' POST '/catalogs/leave-types' @{ code=$codigoReg; description='Tipo de teste'; deductsBalance=$false; requiresApproval=$false } 201
$idTipoNovo = $rTipoNovo.Dados.id
# O POST devolve SuccessResponseDTO (id, sucesso, alertas) -- e preciso ler o tipo de volta.
$rTipoLido = Chamar 'F11.15 ler o tipo criado' GET ('/catalogs/leave-types/' + $idTipoNovo)
Verificar 'F11.15a nasce FALTA, que e a omissao segura' ($rTipoLido.Dados.regime -eq 'FALTA') ('(' + $rTipoLido.Dados.regime + ')')
$rReclass = Chamar 'F11.16 reclassificar pela API' PUT ('/catalogs/leave-types/' + $idTipoNovo) @{ code=$codigoReg; description='Tipo de teste'; deductsBalance=$false; requiresApproval=$false; regime='FERIAS' } 200
Verificar 'F11.17 a instituicao reclassifica sem tocar em codigo' ($rReclass.Dados.regime -eq 'FERIAS') ('(' + $rReclass.Dados.regime + ')')
Chamar 'F11.18 NEG regime fora da lista da lei' PUT ('/catalogs/leave-types/' + $idTipoNovo) @{ code=$codigoReg; description='Tipo de teste'; deductsBalance=$false; requiresApproval=$false; regime='INVENTADO' } 422 | Out-Null

# Repor: dois tipos FERIAS activos tornariam o vencimento ambiguo para as proximas execucoes.
Chamar 'F11.19 repor o tipo de teste como FALTA' PUT ('/catalogs/leave-types/' + $idTipoNovo) @{ code=$codigoReg; description='Tipo de teste'; deductsBalance=$false; requiresApproval=$false; regime='FALTA' } 200 | Out-Null

Write-Host ''
Write-Host '=========== F12 - ACUMULACAO DE FERIAS (art. 7.o n.o 1) ==========='

# Depois de o saldo passar a nascer sozinho, os dias nao gozados ficavam na linha do ano
# anterior sem caminho nenhum: um pedido de 2027 so olha para o saldo de 2027. A lei nao os
# deixa cair -- mas tambem nao os arrasta sozinha: so ha acumulacao quando, por motivo de
# servico, nao puderam ser gozados nesse ano.

$anoAc = (Get-Date).Year
$rSaldosAc = Chamar 'F12.1 saldos do colaborador do ano inteiro' GET ('/funcionarios/' + $colabFA + '/saldos-ausencia?ano=' + $anoAc)
$saldoAc = (@(Linhas $rSaldosAc) | Where-Object { $_.tipoAusenciaId -eq $tipoFerias.id } | Select-Object -First 1)
Verificar 'F12.2 tem os 22 dias todos por gozar' (($saldoAc.diasDisponiveis -eq 22) -and ($saldoAc.diasAcumulaveis -eq 22)) ('(disponivel=' + $saldoAc.diasDisponiveis + ' acumulavel=' + $saldoAc.diasAcumulaveis + ')')

Chamar 'F12.3 NEG acumular sem motivo' POST ('/funcionarios/' + $colabFA + '/saldos-ausencia/' + $saldoAc.id + '/acumular') @{ dias=5 } 400 | Out-Null
Chamar 'F12.4 NEG acumular mais do que sobra' POST ('/funcionarios/' + $colabFA + '/saldos-ausencia/' + $saldoAc.id + '/acumular') @{ dias=40; motivo='conveniencia de servico' } 422 | Out-Null

$rAcum = Chamar 'F12.5 acumular 8 dias para o ano seguinte' POST ('/funcionarios/' + $colabFA + '/saldos-ausencia/' + $saldoAc.id + '/acumular') @{ dias=8; motivo='conveniencia de servico - projecto em curso' } 200
Verificar 'F12.6 a resposta mostra os dois anos' (($rAcum.Dados.anoOrigem -eq $anoAc) -and ($rAcum.Dados.anoDestino -eq ($anoAc + 1))) ('(' + $rAcum.Dados.anoOrigem + ' -> ' + $rAcum.Dados.anoDestino + ')')
Verificar 'F12.7 a origem ficou com menos 8' ($rAcum.Dados.disponivelNaOrigem -eq 14) ('(' + $rAcum.Dados.disponivelNaOrigem + ')')
Verificar 'F12.8 o destino tem o seu direito mais os 8' ($rAcum.Dados.disponivelNoDestino -eq 30) ('(' + $rAcum.Dados.disponivelNoDestino + ')')

# O saldo do ano seguinte foi criado pela propria acumulacao -- o job do vencimento ainda nao
# passou por ele, e a autorizacao pode acontecer em Dezembro.
$rSaldoSeg = Chamar 'F12.9 saldo do ano seguinte' GET ('/funcionarios/' + $colabFA + '/saldos-ausencia?ano=' + ($anoAc + 1))
$saldoSeg = (@(Linhas $rSaldoSeg) | Where-Object { $_.tipoAusenciaId -eq $tipoFerias.id } | Select-Object -First 1)
Verificar 'F12.10 nasceu com os dias acumulados a parte do direito' (($saldoSeg.diasDireito -eq 22) -and ($saldoSeg.diasAcumulados -eq 8)) ('(direito=' + $saldoSeg.diasDireito + ' acumulados=' + $saldoSeg.diasAcumulados + ')')
Verificar 'F12.11 e o motivo ficou gravado' (-not [string]::IsNullOrWhiteSpace($saldoSeg.acumulacaoMotivo)) ('(' + $saldoSeg.acumulacaoMotivo + ')')

# Os dias recebidos nao seguem viagem para o ano a seguir: o horizonte da lei e de um ano.
Verificar 'F12.12 os dias recebidos nao sao acumulaveis de novo' ($saldoSeg.diasAcumulaveis -eq 22) ('(acumulavel=' + $saldoSeg.diasAcumulaveis + ', e nao 30)')

# A origem ja so tem 14 por gozar, logo nao pode ceder os 22 outra vez.
Chamar 'F12.13 NEG ceder outra vez os mesmos dias' POST ('/funcionarios/' + $colabFA + '/saldos-ausencia/' + $saldoAc.id + '/acumular') @{ dias=22; motivo='outra vez' } 422 | Out-Null

# Um saldo de outra pessoa nao se acumula pelo URL desta.
Chamar 'F12.14 NEG saldo de outro colaborador pelo URL deste' POST ('/funcionarios/' + $colabFB + '/saldos-ausencia/' + $saldoAc.id + '/acumular') @{ dias=1; motivo='engano' } 404 | Out-Null

# A acumulacao e do capitulo das ferias: uma falta nao se acumula.
$tipoFalta = (@(Linhas $rTipos) | Where-Object { $_.regime -eq 'FALTA' -and $_.deductsBalance -eq $true } | Select-Object -First 1)
if ($null -ne $tipoFalta) {
    $rSaldoFalta = Chamar 'F12.15 criar saldo de um tipo FALTA' POST ('/funcionarios/' + $colabFA + '/saldos-ausencia') @{ tipoAusenciaId=$tipoFalta.id; ano=$anoAc; diasDireito=10 } 201
    $rSaldosTodos = Chamar 'F12.16 ler os saldos' GET ('/funcionarios/' + $colabFA + '/saldos-ausencia?ano=' + $anoAc)
    $saldoFalta = (@(Linhas $rSaldosTodos) | Where-Object { $_.tipoAusenciaId -eq $tipoFalta.id } | Select-Object -First 1)
    Chamar 'F12.17 NEG acumular uma falta' POST ('/funcionarios/' + $colabFA + '/saldos-ausencia/' + $saldoFalta.id + '/acumular') @{ dias=2; motivo='nao se aplica' } 422 | Out-Null
}

Write-Host ''
Write-Host '=========== F13 - SUSPENSAO DE FERIAS (art. 8.o) ==========='

# Ate aqui um pedido de ferias e um de doenca nao se falavam: quem adoecesse a meio das ferias
# perdia-as, porque os dias ja tinham sido contados como gozados na aprovacao.

$hojeF = Get-Date
$iniF = $hojeF.AddDays(-6).ToString('yyyy-MM-dd')
$fimF = $hojeF.AddDays(8).ToString('yyyy-MM-dd')
$dataSusp = $hojeF.ToString('yyyy-MM-dd')

$rSaldoAntes = Chamar 'F13.1 saldo de ferias antes' GET ('/funcionarios/' + $colabFA + '/saldos-ausencia?ano=' + $anoAc)
$sAntes = (@(Linhas $rSaldoAntes) | Where-Object { $_.tipoAusenciaId -eq $tipoFerias.id } | Select-Object -First 1)

$rPedF = Chamar 'F13.2 marcar ferias que ja comecaram' POST ('/funcionarios/' + $colabFA + '/pedidos-ausencia') @{ tipoAusenciaId=$tipoFerias.id; dataInicio=$iniF; dataFim=$fimF; motivo='ferias anuais' } 201
$pedF = $rPedF.Dados.id
$diasPedidos = $rPedF.Dados.numeroDias
Chamar 'F13.3 aprovar as ferias' PATCH ('/funcionarios/' + $colabFA + '/pedidos-ausencia/' + $pedF + '/aprovar') @{ aprovadoPorId=$colabFB; observacoesDecisao='deferido' } 200 | Out-Null

Chamar 'F13.4 NEG suspender sem motivo' PATCH ('/funcionarios/' + $colabFA + '/pedidos-ausencia/' + $pedF + '/suspender') @{ data=$dataSusp } 400 | Out-Null
Chamar 'F13.5 NEG suspender com data futura' PATCH ('/funcionarios/' + $colabFA + '/pedidos-ausencia/' + $pedF + '/suspender') @{ data=$hojeF.AddDays(3).ToString('yyyy-MM-dd'); motivo='doenca' } 400 | Out-Null

$rSusp = Chamar 'F13.6 suspender por doenca a partir de hoje' PATCH ('/funcionarios/' + $colabFA + '/pedidos-ausencia/' + $pedF + '/suspender') @{ data=$dataSusp; motivo='doenca - atestado entregue no servico' } 200
Verificar 'F13.7 o ultimo dia de ferias e a vespera' ($rSusp.Dados.dataFim -like ($hojeF.AddDays(-1).ToString('yyyy-MM-dd') + '*')) ('(' + $rSusp.Dados.dataFim + ')')
Verificar 'F13.8 recuperou dias para o saldo' ($rSusp.Dados.diasRecuperados -gt 0) ('(gozados=' + $rSusp.Dados.diasGozados + ' recuperados=' + $rSusp.Dados.diasRecuperados + ')')
Verificar 'F13.9 gozados mais recuperados dao o pedido inteiro' (($rSusp.Dados.diasGozados + $rSusp.Dados.diasRecuperados) -eq $diasPedidos) ('(' + $diasPedidos + ')')

$rPedLido = Chamar 'F13.10 ler o pedido suspenso' GET ('/funcionarios/' + $colabFA + '/pedidos-ausencia/' + $pedF)
Verificar 'F13.11 continua APROVADO -- a decisao nao se desfaz' ($rPedLido.Dados.estado -eq 'APROVADO') ('(' + $rPedLido.Dados.estado + ')')
Verificar 'F13.12 e o motivo da suspensao ficou gravado' (-not [string]::IsNullOrWhiteSpace($rPedLido.Dados.suspensaoMotivo)) ('(' + $rPedLido.Dados.suspensaoMotivo + ')')

$rSaldoDepois = Chamar 'F13.13 saldo depois da suspensao' GET ('/funcionarios/' + $colabFA + '/saldos-ausencia?ano=' + $anoAc)
$sDepois = (@(Linhas $rSaldoDepois) | Where-Object { $_.tipoAusenciaId -eq $tipoFerias.id } | Select-Object -First 1)
Verificar 'F13.14 os dias voltaram mesmo ao saldo' ($sDepois.diasDisponiveis -eq ($sAntes.diasDisponiveis - $rSusp.Dados.diasGozados)) ('(antes=' + $sAntes.diasDisponiveis + ' depois=' + $sDepois.diasDisponiveis + ' gozados=' + $rSusp.Dados.diasGozados + ')')

Chamar 'F13.15 NEG suspender duas vezes' PATCH ('/funcionarios/' + $colabFA + '/pedidos-ausencia/' + $pedF + '/suspender') @{ data=$dataSusp; motivo='outra vez' } 409 | Out-Null
Chamar 'F13.16 NEG pedido de outro colaborador pelo URL deste' PATCH ('/funcionarios/' + $colabFB + '/pedidos-ausencia/' + $pedF + '/suspender') @{ data=$dataSusp; motivo='engano' } 404 | Out-Null

Write-Host ''
Write-Host '=========== F14 - ANTIGUIDADE (tempo de servico) ==========='

# A antiguidade nao se calculava em lado nenhum, e isso deixava tres colunas mortas:
# contaAntiguidade() da situacao funcional, counts_for_seniority do subtipo e counts_seniority
# do vinculo. Quem as parametrizasse ficava convencido de que tinha feito alguma coisa.
# Nao se guarda: deriva-se do percurso e recalcula-se a cada leitura.

$hojeA = Get-Date
$inicioAno = $hojeA.Year.ToString() + '-01-01'

# O colabFA foi admitido a 1 de Janeiro deste ano (F11.4).
$rAnt = Chamar 'F14.1 antiguidade de quem foi admitido a 1 de Janeiro' GET ('/funcionarios/' + $colabFA + '/antiguidade')
Verificar 'F14.2 conta desde a admissao' ($rAnt.Dados.dataInicio -like ($inicioAno + '*')) ('(' + $rAnt.Dados.dataInicio + ')')
Verificar 'F14.3 sem nada a descontar' ($rAnt.Dados.diasDescontados -eq 0) ('(' + $rAnt.Dados.diasDescontados + ')')
Verificar 'F14.4 o que conta e o total' ($rAnt.Dados.diasContados -eq $rAnt.Dados.diasTotais) ('(' + $rAnt.Dados.diasContados + ' de ' + $rAnt.Dados.diasTotais + ')')

# A data de referencia responde a "quanta antiguidade tinha a data da promocao".
$rAnt31 = Chamar 'F14.5 antiguidade a 31 de Janeiro' GET ('/funcionarios/' + $colabFA + '/antiguidade?ate=' + $hojeA.Year.ToString() + '-01-31')
Verificar 'F14.6 conta os dois extremos: Janeiro tem 31 dias' ($rAnt31.Dados.diasTotais -eq 31) ('(' + $rAnt31.Dados.diasTotais + ')')

# --- a situacao funcional passa a descontar (art. 120.o n.o 2) ---
$de = $hojeA.AddDays(-60).ToString('yyyy-MM-dd')
$ate = $hojeA.AddDays(-30).ToString('yyyy-MM-dd')
Chamar 'F14.7 por o colaborador em inactividade no quadro' PATCH ('/funcionarios/' + $colabFA + '/worker-state') @{ workerStateId=$ws['SUSPENDED'].id; dataEfectividade=$de; motivoCkey='DOENCA' } 200 | Out-Null
Chamar 'F14.8 e traze-lo de volta 30 dias depois' PATCH ('/funcionarios/' + $colabFA + '/worker-state') @{ workerStateId=$ws['ACTIVE'].id; dataEfectividade=$ate; motivoCkey='ALTA' } 200 | Out-Null

$rAnt2 = Chamar 'F14.9 antiguidade depois da inactividade' GET ('/funcionarios/' + $colabFA + '/antiguidade')
Verificar 'F14.10 descontou os 30 dias de inactividade' ($rAnt2.Dados.diasDescontados -eq 30) ('(' + $rAnt2.Dados.diasDescontados + ')')
Verificar 'F14.11 e o contado baixou na mesma medida' ($rAnt2.Dados.diasContados -eq ($rAnt.Dados.diasContados - 30)) ('(antes=' + $rAnt.Dados.diasContados + ' depois=' + $rAnt2.Dados.diasContados + ')')
$periodo = (@($rAnt2.Dados.periodosDescontados) | Select-Object -First 1)
Verificar 'F14.12 e diz porque descontou' ($periodo.motivo -like '*INACTIVIDADE_NO_QUADRO*') ('(' + $periodo.motivo + ')')
Verificar 'F14.13 com o periodo exacto' (($periodo.inicio -like ($de + '*')) -and ($periodo.dias -eq 30)) ('(' + $periodo.inicio + ' a ' + $periodo.fim + ', ' + $periodo.dias + ' dias)')

# A disponibilidade conta expressamente (art. 122.o n.o 1) -- nao acrescenta desconto.
Chamar 'F14.14 por em disponibilidade' PATCH ('/funcionarios/' + $colabFA + '/worker-state') @{ workerStateId=$ws['AVAILABLE'].id; dataEfectividade=$hojeA.AddDays(-10).ToString('yyyy-MM-dd'); motivoCkey='AGUARDA_VAGA' } 200 | Out-Null
$rAnt3 = Chamar 'F14.15 antiguidade com disponibilidade' GET ('/funcionarios/' + $colabFA + '/antiguidade')
Verificar 'F14.16 a disponibilidade nao desconta' ($rAnt3.Dados.diasDescontados -eq 30) ('(' + $rAnt3.Dados.diasDescontados + ')')

Chamar 'F14.17 NEG antiguidade de colaborador inexistente' GET '/funcionarios/00000000-0000-4000-8000-000000000999/antiguidade' $null 404 | Out-Null

Write-Host ''
Write-Host ''
Write-Host '=========== F15 - MUDANCA DE CARREIRA ==========='

# Ate aqui a mudanca de carreira era impossivel: a promocao exige a MESMA carreira e a
# transferencia a MESMA categoria, e nao havia terceira porta. Parece-se com a transferencia
# porque a mecanica e a mesma -- fechar uma afectacao e abrir outra noutro Lugar --, mas o que
# muda e o proprio eixo de que a categoria e o escalao dependem.
#
# Duas coisas que este bloco fixa:
#   a carreira TEM de mudar, senao seria uma promocao sem nenhuma das regras da promocao;
#   o escalao NAO se herda -- pertence a categoria do destino, e quem posiciona e o acto.
#
# Contexto herdado: no fim do F9 o B esta num Lugar do Regime Geral. O LUG-0007 do seed esta
# vago no Regime Especial, e e o unico destino de carreira diferente que existe.

$rCarr15 = Chamar 'F15.1 catalogo de carreiras' GET '/careers?pagina=0&tamanho=50'
Verificar 'F15.2 ha mais do que uma carreira activa' ((@(Linhas $rCarr15) | Where-Object { $_.isActive -ne $false }).Count -ge 2) ''

$rUni15 = Chamar 'F15.3 onde esta o B' GET ('/colaboradores/assignments/funcionario/' + $colabB + '/unidade-atual')
$lugarActual15 = $rUni15.Dados.positionId
$rPosAct15 = Chamar 'F15.4 detalhe do Lugar actual' GET ('/estrutura/positions/' + $lugarActual15)
$carreiraActual15 = $rPosAct15.Dados.careerId

$rVagas15 = Chamar 'F15.5 Lugares vagos da unidade' GET ('/colaboradores/assignments/unidade/' + $unidadeB + '/vagas/lista')
$vagos15 = @(Linhas $rVagas15)
$lugarOutra15 = ($vagos15 | Where-Object { $_.estado -eq 'ATIVO' -and $null -ne $_.careerId -and $_.careerId -ne $carreiraActual15 } | Select-Object -First 1)
$lugarMesma15 = ($vagos15 | Where-Object { $_.estado -eq 'ATIVO' -and $_.careerId -eq $carreiraActual15 } | Select-Object -First 1)
Verificar 'F15.6 ha Lugar vago noutra carreira' ($null -ne $lugarOutra15) ('(' + $lugarOutra15.numeroLugar + ', ' + $lugarOutra15.careerNome + ')')
Verificar 'F15.7 e ha Lugar vago na mesma, para o negativo' ($null -ne $lugarMesma15) ('(' + $lugarMesma15.numeroLugar + ')')

$dMud15 = $hoje.ToString('yyyy-MM-dd')

# --- negativos, antes de gastar o cenario ---
# Este e o que separa o movimento dos outros dois: destino da mesma carreira nao e mudanca.
Chamar 'F15.8 NEG destino da mesma carreira' POST ('/funcionarios/' + $colabB + '/mudanca-carreira') @{ positionId=$lugarMesma15.id; dataEfeito=$dMud15 } 422 | Out-Null
Chamar 'F15.9 NEG destino e o Lugar onde ja esta' POST ('/funcionarios/' + $colabB + '/mudanca-carreira') @{ positionId=$lugarActual15; dataEfeito=$dMud15 } 422 | Out-Null
Chamar 'F15.10 NEG data anterior a afectacao corrente' POST ('/funcionarios/' + $colabB + '/mudanca-carreira') @{ positionId=$lugarOutra15.id; dataEfeito=$hoje.AddDays(-60).ToString('yyyy-MM-dd') } 422 | Out-Null
Chamar 'F15.11 NEG Lugar de destino inexistente' POST ('/funcionarios/' + $colabB + '/mudanca-carreira') @{ positionId='00000000-0000-4000-8000-000000000999'; dataEfeito=$dMud15 } 404 | Out-Null
# O escalao do F9 e da carreira de origem: nao serve a categoria de destino.
Chamar 'F15.12 NEG escalao que nao e da categoria de destino' POST ('/funcionarios/' + $colabB + '/mudanca-carreira') @{ positionId=$lugarOutra15.id; gradeId=$escBaixo; dataEfeito=$dMud15 } 422 | Out-Null
Chamar 'F15.13 NEG sem Lugar de destino' POST ('/funcionarios/' + $colabB + '/mudanca-carreira') @{ dataEfeito=$dMud15 } 400 | Out-Null

# --- o movimento ---
$rGr15 = Chamar 'F15.14 escaloes da categoria de destino' GET ('/categories/' + $lugarOutra15.categoryId + '/grades')
$escDestino15 = (@(Linhas $rGr15) | Where-Object { $_.isActive -ne $false } | Select-Object -First 1)

$rMud15 = Chamar 'F15.15 mudar de carreira' POST ('/funcionarios/' + $colabB + '/mudanca-carreira') @{ positionId=$lugarOutra15.id; dataEfeito=$dMud15; despachoNumero='DESP-2026/92'; concursoRef='CE-2026/1' } 201
Verificar 'F15.16 a carreira mudou mesmo' (($rMud15.Dados.carreiraAnteriorId -eq $carreiraActual15) -and ($rMud15.Dados.carreiraNovaId -eq $lugarOutra15.careerId)) ('(' + $rMud15.Dados.carreiraAnterior + ' -> ' + $rMud15.Dados.carreiraNova + ')')
Verificar 'F15.17 a categoria e a do Lugar de destino' ($rMud15.Dados.categoriaNovaId -eq $lugarOutra15.categoryId) ('(' + $rMud15.Dados.categoriaNova + ')')
Verificar 'F15.18 entrou pelo primeiro escalao da nova categoria' ($rMud15.Dados.escalaoId -eq $escDestino15.id) ('(' + $rMud15.Dados.escalao + ')')
Verificar 'F15.19 e guarda de onde veio' ($rMud15.Dados.numeroLugarAnterior -eq $rPosAct15.Dados.numeroLugar) ('(' + $rMud15.Dados.numeroLugarAnterior + ' -> ' + $rMud15.Dados.numeroLugar + ')')

$rUni15b = Chamar 'F15.20 onde esta o B depois' GET ('/colaboradores/assignments/funcionario/' + $colabB + '/unidade-atual')
Verificar 'F15.21 esta no Lugar da nova carreira' ($rUni15b.Dados.positionId -eq $lugarOutra15.id) ('(' + $rUni15b.Dados.numeroLugar + ')')

$rVagas15b = Chamar 'F15.22 vagas depois' GET ('/colaboradores/assignments/unidade/' + $unidadeB + '/vagas/lista')
$vagos15b = @(Linhas $rVagas15b)
Verificar 'F15.23 o Lugar que deixou ficou vago' (@($vagos15b | Where-Object { $_.id -eq $lugarActual15 }).Count -eq 1) ''
Verificar 'F15.24 e o de destino deixou de estar vago' (@($vagos15b | Where-Object { $_.id -eq $lugarOutra15.id }).Count -eq 0) ''

# Agora que ja esta na outra carreira, o antigo Lugar passou a ser "outra carreira" para ela.
Chamar 'F15.25 NEG colaborador inexistente' POST '/funcionarios/00000000-0000-4000-8000-000000000999/mudanca-carreira' @{ positionId=$lugarActual15; dataEfeito=$dMud15 } 404 | Out-Null

Write-Host ''
Write-Host '=========== F16 - CONSOLIDACAO DA MOBILIDADE ==========='

# Art. 132.o n.o 4: a mobilidade definitiva ocorre por consolidacao da transitoria, "na mesma
# funcao e categoria". E a UNICA via pela qual uma mobilidade toca na afectacao -- e nao
# contradiz o art. 135.o n.o 7, que diz que a TRANSITORIA nao ocupa Lugar: o n.o 8 define a
# definitiva como a que e feita "com ocupacao do lugar do quadro".
#
# Contexto herdado: no fim do F15 o B esta no LUG-0007, de outra carreira. Comeca-se por o pOr
# num Lugar que case com o LUG-0008 -- o unico Lugar vago noutra unidade, e por isso o unico
# destino possivel de uma consolidacao.

$rUnid16 = Chamar 'F16.1 unidades organicas' GET '/estrutura/organizational-units?pagina=0&tamanho=20'
$unidades16 = @(Linhas $rUnid16)

# Junta-se o mapa de vagas de todas as unidades e procura-se o PAR que a consolidacao exige:
# um Lugar vago numa unidade e outro, do MESMO cargo e da MESMA categoria, noutra. Nada de
# numeros de Lugar escritos a mao -- se o seed mudar a numeracao, o bloco continua a servir.
$vagasPorUnidade16 = @()
foreach ($u in $unidades16) {
    $r = Chamar ('F16.2 vagas de ' + $u.code) GET ('/colaboradores/assignments/unidade/' + $u.id + '/vagas/lista')
    foreach ($v in @(Linhas $r)) {
        if ($v.estado -eq 'ATIVO' -and $v.foraDeGrelha -ne $true) { $vagasPorUnidade16 += $v }
    }
}

$lugarOrigem16 = $null
$lugarDestino16 = $null
foreach ($a in $vagasPorUnidade16) {
    $par = ($vagasPorUnidade16 | Where-Object { $_.unidadeOrganicaId -ne $a.unidadeOrganicaId -and $_.jobId -eq $a.jobId -and $_.categoryId -eq $a.categoryId } | Select-Object -First 1)
    if ($null -ne $par) { $lugarOrigem16 = $a; $lugarDestino16 = $par; break }
}
Verificar 'F16.3 ha dois Lugares vagos do mesmo cargo e categoria em unidades diferentes' (($null -ne $lugarOrigem16) -and ($null -ne $lugarDestino16)) ('(' + $lugarOrigem16.numeroLugar + ' -> ' + $lugarDestino16.numeroLugar + ')')
$unidadeDestino16 = $lugarDestino16.unidadeOrganicaId
$unidadeOrigem16 = $lugarOrigem16.unidadeOrganicaId
Verificar 'F16.4 e sao mesmo de unidades diferentes' ($unidadeOrigem16 -ne $unidadeDestino16) ('(' + $lugarOrigem16.unidadeNome + ' -> ' + $lugarDestino16.unidadeNome + ')')

$rGr16 = Chamar 'F16.5 escaloes dessa categoria' GET ('/categories/' + $lugarDestino16.categoryId + '/grades')
$esc16 = (@(Linhas $rGr16) | Where-Object { $_.isActive -ne $false } | Select-Object -First 1).id
$dAfect16 = $hoje.AddDays(-40).ToString('yyyy-MM-dd')
# Quem se consolida e um colaborador novo, ADMITIDO no Lugar de partida: o B ja tem Lugar e
# nao se muda pela porta generica (BR-AF-16).
$tipoNomeacao16 = (@(Linhas (Chamar 'F16.5b tipos de contrato' GET '/catalogs/contract-types?pagina=0&tamanho=50')) | Where-Object { $_.code -eq 'NOMEACAO_DEFINITIVA' } | Select-Object -First 1).id
$nifE = '2' + (Get-Date -Format 'MMddHHmmss')
$colabE = (Chamar 'F16.6a admitir E' POST '/funcionarios' @{ nomeCompleto='Consolida Mobilidade'; dataNascimento='1987-06-06'; genero='F'; estadoCivil='SOLTEIRO'; nif=$nifE; dataAdmissao=$dAfect16 } 201).Dados.id
Chamar 'F16.6b contrato de E' POST ('/funcionarios/' + $colabE + '/contratos') @{ contractTypeId=$tipoNomeacao16; startDate=$dAfect16; regimeTrabalho='TEMPO_COMPLETO' } 201 | Out-Null
Chamar 'F16.6 colocar E no Lugar de partida (ADMISSAO)' POST '/colaboradores/assignments' @{ funcionarioId=$colabE; positionId=$lugarOrigem16.id; gradeId=$esc16; dataInicio=$dAfect16 } 201 | Out-Null

$dIni16 = $hoje.AddDays(-20).ToString('yyyy-MM-dd')
$dFim16 = $hoje.AddMonths(6).ToString('yyyy-MM-dd')
$dCons16 = $hoje.ToString('yyyy-MM-dd')
$rMob16 = Chamar 'F16.7 mobilidade para a unidade de destino' POST ('/funcionarios/' + $colabE + '/licencas-mobilidade') @{ subtipoId=$subMob.id; dataInicio=$dIni16; dataFim=$dFim16; destinationUnitId=$unidadeDestino16; justification='mobilidade a consolidar' } 201
$mob16 = $rMob16.Dados.id
Chamar 'F16.8 aprovar a mobilidade' PUT ('/funcionarios/' + $colabE + '/licencas-mobilidade/' + $mob16 + '/approve') $null 200 | Out-Null

# --- negativos, antes de gastar o cenario ---
Chamar 'F16.9 NEG consolidar sem Lugar de destino' POST ('/funcionarios/' + $colabE + '/licencas-mobilidade/' + $mob16 + '/consolidar') @{ dataEfeito=$dCons16 } 400 | Out-Null
Chamar 'F16.10 NEG Lugar de destino inexistente' POST ('/funcionarios/' + $colabE + '/licencas-mobilidade/' + $mob16 + '/consolidar') @{ positionId='00000000-0000-4000-8000-000000000999'; dataEfeito=$dCons16 } 404 | Out-Null
# O Lugar tem de ser do servico onde se esteve em mobilidade: e esse exercicio que se torna definitivo.
$lugarOutraUnidade16 = ($vagasPorUnidade16 | Where-Object { $_.unidadeOrganicaId -ne $unidadeDestino16 -and $_.id -ne $lugarOrigem16.id } | Select-Object -First 1)
if ($null -ne $lugarOutraUnidade16) {
    Chamar 'F16.11 NEG Lugar que nao e da unidade de destino' POST ('/funcionarios/' + $colabE + '/licencas-mobilidade/' + $mob16 + '/consolidar') @{ positionId=$lugarOutraUnidade16.id; dataEfeito=$dCons16 } 422 | Out-Null
}
Chamar 'F16.12 NEG mobilidade de outro colaborador pelo URL deste' POST ('/funcionarios/' + $colabFA + '/licencas-mobilidade/' + $mob16 + '/consolidar') @{ positionId=$lugarDestino16.id; dataEfeito=$dCons16 } 404 | Out-Null

# --- o movimento ---
$rCons16 = Chamar 'F16.13 consolidar a mobilidade' POST ('/funcionarios/' + $colabE + '/licencas-mobilidade/' + $mob16 + '/consolidar') @{ positionId=$lugarDestino16.id; dataEfeito=$dCons16; despachoNumero='DESP-2026/93' } 201
Verificar 'F16.14 saiu do Lugar de origem para o de destino' (($rCons16.Dados.positionAnteriorId -eq $lugarOrigem16.id) -and ($rCons16.Dados.positionId -eq $lugarDestino16.id)) ('(' + $rCons16.Dados.numeroLugarAnterior + ' -> ' + $rCons16.Dados.numeroLugar + ')')
Verificar 'F16.15 e mudou de unidade organica' ($rCons16.Dados.unidadeOrganicaId -eq $unidadeDestino16) ''
Verificar 'F16.16 o ultimo dia em mobilidade e a vespera' ($rCons16.Dados.mobilidadeDataFim -like ($hoje.AddDays(-1).ToString('yyyy-MM-dd') + '*')) ('(' + $rCons16.Dados.mobilidadeDataFim + ')')

$rUni16 = Chamar 'F16.17 onde esta o E depois' GET ('/colaboradores/assignments/funcionario/' + $colabE + '/unidade-atual')
Verificar 'F16.18 e titular do Lugar de destino' ($rUni16.Dados.positionId -eq $lugarDestino16.id) ('(' + $rUni16.Dados.numeroLugar + ')')
$rVagasDest16 = Chamar 'F16.19 vagas da unidade de destino depois' GET ('/colaboradores/assignments/unidade/' + $unidadeDestino16 + '/vagas/lista')
Verificar 'F16.19b o Lugar de destino deixou de estar vago' (@(@(Linhas $rVagasDest16) | Where-Object { $_.id -eq $lugarDestino16.id }).Count -eq 0) ''

$rMobLida16 = Chamar 'F16.20 ler a mobilidade consolidada' GET ('/funcionarios/' + $colabE + '/licencas-mobilidade/' + $mob16)
Verificar 'F16.21 o despacho nao se desfez -- continua APPROVED' ($rMobLida16.Dados.status -eq 'APPROVED') ('(' + $rMobLida16.Dados.status + ')')
Verificar 'F16.22 e o periodo esta TERMINADA' ($rMobLida16.Dados.estadoPeriodo -eq 'TERMINADA') ('(' + $rMobLida16.Dados.estadoPeriodo + ')')

$rVagas16b = Chamar 'F16.23 vagas da unidade de origem depois' GET ('/colaboradores/assignments/unidade/' + $unidadeOrigem16 + '/vagas/lista')
Verificar 'F16.24 o Lugar que deixou ficou vago' (@(@(Linhas $rVagas16b) | Where-Object { $_.id -eq $lugarOrigem16.id }).Count -eq 1) ''

# Consolidada uma vez, o periodo transitorio acabou: nao ha segundo a consolidar.
Chamar 'F16.25 NEG consolidar duas vezes' POST ('/funcionarios/' + $colabE + '/licencas-mobilidade/' + $mob16 + '/consolidar') @{ positionId=$lugarOrigem16.id; dataEfeito=$dCons16 } 409 | Out-Null

Write-Host ''
Write-Host '=========== F17 - REGRESSO DE COMISSAO DE SERVICO (art. 64.o n.o 2) ==========='

# "Cessada a comissao de servico, o nomeado regressa a situacao juridico-funcional de que era
# titular antes dela, quando constituida e consolidada por tempo indeterminado, ou, NO CASO
# CONTRARIO, CESSA a relacao juridica de emprego publico."
#
# Sao duas saidas, e nao e quem regista que escolhe: o caminho DERIVA-SE DO PERCURSO. A comissao
# mantem o Lugar (position_effect=MANTEM), logo quem tinha situacao anterior continua titular
# dele e regressa sem que nada tenha de acontecer; quem foi recrutado PARA a comissao nunca teve
# situacao para onde voltar, e a relacao termina. Antes disto o catalogo classificava a comissao
# como REGRESSA_LUGAR sem condicao, e o regresso devolvia ao Lugar de origem TODA A GENTE --
# incluindo quem nunca teve Lugar nenhum. Falta silenciosa: nada falhava.
#
# Contexto herdado: no fim do F16 o B e titular do Lugar de destino da consolidacao.

$subCom = ($subtipos | Where-Object { $_.recordType -eq 'MOBILIDADE' -and $_.returnEffect -eq 'REGRESSA_OU_CESSA' -and $_.isActive -ne $false } | Select-Object -First 1)
Verificar 'F17.1 ha subtipo classificado REGRESSA_OU_CESSA' ($null -ne $subCom) ('(' + $subCom.code + ')')
Verificar 'F17.2 e mantem o Lugar, como a comissao manda' ($subCom.positionEffect -eq 'MANTEM') ('(' + $subCom.positionEffect + ')')
# Tres anos, sucessivamente renovavel (art. 60.o n.o 1) -- e nao um ano com uma prorrogacao,
# que e a regra da mobilidade comum (art. 132.o n.o 5).
Verificar 'F17.3 com a duracao da comissao, nao a da mobilidade comum' (($subCom.maxDurationDays -eq 1095) -and ($null -eq $subCom.maxExtensions)) ('(' + $subCom.maxDurationDays + ' dias, prorrogacoes=' + $subCom.maxExtensions + ')')

$dIniCom = $hoje.AddDays(-15).ToString('yyyy-MM-dd')
$dFimCom = $hoje.AddMonths(6).ToString('yyyy-MM-dd')

# --- 1a parte do n.o 2: quem tinha situacao anterior REGRESSA, e regressar e nao acontecer nada ---
$rUniAntes17 = Chamar 'F17.4 onde esta o B antes da comissao' GET ('/colaboradores/assignments/funcionario/' + $colabB + '/unidade-atual')
$lugar17 = $rUniAntes17.Dados.positionId
Verificar 'F17.5 o B e titular de um Lugar' ($null -ne $lugar17) ('(' + $rUniAntes17.Dados.numeroLugar + ')')

$rCom17 = Chamar 'F17.6 nomear o B em comissao de servico' POST ('/funcionarios/' + $colabB + '/licencas-mobilidade') @{ subtipoId=$subCom.id; dataInicio=$dIniCom; dataFim=$dFimCom; destinationUnitId=$unidadeOrigem16; justification='comissao de servico' } 201
$com17 = $rCom17.Dados.id
$rApr17 = Chamar 'F17.7 aprovar a comissao' PUT ('/funcionarios/' + $colabB + '/licencas-mobilidade/' + $com17 + '/approve') $null 200
Verificar 'F17.8 a comissao nao tirou o Lugar a ninguem' ($null -eq $rApr17.Dados.afectacaoEncerradaId) '(art. 135.o n.o 7)'

$rFim17 = Chamar 'F17.9 cessar a comissao' PUT ('/funcionarios/' + $colabB + '/licencas-mobilidade/' + $com17 + '/close') $null 200
Verificar 'F17.10 quem tinha Lugar regressa, e nao ha estado novo nenhum' ($null -eq $rFim17.Dados.estadoAtribuidoId) '(art. 64.o n.o 2, 1a parte)'

$rUniDep17 = Chamar 'F17.11 onde esta o B depois' GET ('/colaboradores/assignments/funcionario/' + $colabB + '/unidade-atual')
Verificar 'F17.12 continua titular do mesmo Lugar' ($rUniDep17.Dados.positionId -eq $lugar17) ('(' + $rUniDep17.Dados.numeroLugar + ')')
# Prova-se o proprio registo, e nao o campo emMobilidade do colaborador: esse responde por
# TODAS as mobilidades em vigor, e o B herda do F8 uma externa que abriu e fechou no mesmo dia
# -- que por desenho fica com um dia, e cobre hoje. Que o regresso acaba a mobilidade ja esta
# provado no F8.25; aqui interessa o que esta comissao ficou a dizer.
$rComLida17 = Chamar 'F17.13 ler a comissao depois do regresso' GET ('/funcionarios/' + $colabB + '/licencas-mobilidade/' + $com17)
Verificar 'F17.14 o despacho nao se desfaz -- continua APPROVED' ($rComLida17.Dados.status -eq 'APPROVED') ('(' + $rComLida17.Dados.status + ')')
Verificar 'F17.15 e o periodo esta TERMINADA' ($rComLida17.Dados.estadoPeriodo -eq 'TERMINADA') ('(' + $rComLida17.Dados.estadoPeriodo + ')')
# O ultimo dia em comissao e a vespera do regresso: quem regressa hoje nao esta em comissao hoje.
Verificar 'F17.16 o ultimo dia em comissao e a vespera do regresso' ($rComLida17.Dados.dataFim -like ($hoje.AddDays(-1).ToString('yyyy-MM-dd') + '*')) ('(' + $rComLida17.Dados.dataFim + ')')

# --- 2a parte do n.o 2: quem foi recrutado PARA a comissao nao tem para onde voltar ---
# Admite-se alguem sem afectacao nenhuma. E o caso que a lei preve e que o codigo nao via: sem
# Lugar do quadro, a comissao E a relacao de emprego, e acabada ela nao sobra vinculo.
$nifCom = '6' + (Get-Date -Format 'MMddHHmmss')
$rNovoCom = Chamar 'F17.17 admitir alguem para a comissao, sem Lugar do quadro' POST '/funcionarios' @{ nomeCompleto='Comissao Sem Lugar'; dataNascimento='1988-02-02'; genero='F'; estadoCivil='SOLTEIRO'; nif=$nifCom; dataAdmissao=($anoAgora.ToString() + '-01-15') } 201
$colabCom = $rNovoCom.Dados.id
Chamar 'F17.18 nao tem afectacao nenhuma' GET ('/colaboradores/assignments/funcionario/' + $colabCom + '/unidade-atual') $null 404 | Out-Null

$rCom17b = Chamar 'F17.19 nomea-lo em comissao' POST ('/funcionarios/' + $colabCom + '/licencas-mobilidade') @{ subtipoId=$subCom.id; dataInicio=$dIniCom; dataFim=$dFimCom; destinationUnitId=$unidadeOrigem16; justification='recrutado para a comissao' } 201
$com17b = $rCom17b.Dados.id
Chamar 'F17.20 aprovar a comissao' PUT ('/funcionarios/' + $colabCom + '/licencas-mobilidade/' + $com17b + '/approve') $null 200 | Out-Null

$rFim17b = Chamar 'F17.21 cessar a comissao' PUT ('/funcionarios/' + $colabCom + '/licencas-mobilidade/' + $com17b + '/close') $null 200
Verificar 'F17.22 sem situacao anterior, a relacao CESSA' ($null -ne $rFim17b.Dados.estadoAtribuidoId) '(art. 64.o n.o 2, 2a parte)'
Verificar 'F17.23 e o estado atribuido e o de cessacao' ($rFim17b.Dados.estadoAtribuidoId -eq $ws['INACTIVE'].id) ('(' + $rFim17b.Dados.estadoAtribuidoId + ')')

# A cessacao nao e um desaparecimento silencioso: fica no historico a dizer porque aconteceu.
$rHist17 = Chamar 'F17.24 historico de estados de quem cessou' GET ('/funcionarios/' + $colabCom + '/worker-state/historico')
$cess17 = (@(Linhas $rHist17) | Where-Object { $_.estadoNovoId -eq $ws['INACTIVE'].id } | Select-Object -First 1)
Verificar 'F17.25 a cessacao ficou registada' ($null -ne $cess17) ''
Verificar 'F17.26 com o motivo do subtipo e o artigo por extenso' (($cess17.motivoCkey -eq $subCom.code) -and ($cess17.observacao -like '*64*')) ('(' + $cess17.motivoCkey + ' / ' + $cess17.observacao + ')')

# --- a bifurcacao e do CATALOGO, nao de "nao ter Lugar" ---
# Mesmo cenario -- ninguem com Lugar --, subtipo classificado REGRESSA_LUGAR: nao cessa nada.
# Sem este passo, o bloco provaria apenas que quem nao tem Lugar cessa, que e outra regra.
$subReg = ($subtipos | Where-Object { $_.recordType -eq 'MOBILIDADE' -and $_.returnEffect -eq 'REGRESSA_LUGAR' -and $_.isActive -ne $false } | Select-Object -First 1)
Verificar 'F17.27 ha subtipo de mobilidade comum no catalogo' ($null -ne $subReg) ('(' + $subReg.code + ')')

$nifReg = '5' + (Get-Date -Format 'MMddHHmmss')
$rNovoReg = Chamar 'F17.28 admitir outro sem Lugar' POST '/funcionarios' @{ nomeCompleto='Mobilidade Sem Lugar'; dataNascimento='1989-03-03'; genero='M'; estadoCivil='SOLTEIRO'; nif=$nifReg; dataAdmissao=($anoAgora.ToString() + '-01-15') } 201
$colabReg = $rNovoReg.Dados.id
$rMobReg = Chamar 'F17.29 po-lo em mobilidade comum' POST ('/funcionarios/' + $colabReg + '/licencas-mobilidade') @{ subtipoId=$subReg.id; dataInicio=$dIniCom; dataFim=$dFimCom; destinationUnitId=$unidadeOrigem16; justification='mobilidade comum' } 201
Chamar 'F17.30 aprovar' PUT ('/funcionarios/' + $colabReg + '/licencas-mobilidade/' + $rMobReg.Dados.id + '/approve') $null 200 | Out-Null
$rFimReg = Chamar 'F17.31 encerrar' PUT ('/funcionarios/' + $colabReg + '/licencas-mobilidade/' + $rMobReg.Dados.id + '/close') $null 200
Verificar 'F17.32 sem Lugar, mas a mobilidade comum nao cessa vinculo nenhum' ($null -eq $rFimReg.Dados.estadoAtribuidoId) ''
$rHistReg = Chamar 'F17.33 historico de estados desse' GET ('/funcionarios/' + $colabReg + '/worker-state/historico')
Verificar 'F17.34 e nao ha cessacao nenhuma registada' ((@(@(Linhas $rHistReg) | Where-Object { $_.estadoNovoId -eq $ws['INACTIVE'].id })).Count -eq 0) ''

# Encerrada a comissao pela cessacao, nao ha segundo regresso a registar.
Chamar 'F17.35 NEG cessar a comissao duas vezes' PUT ('/funcionarios/' + $colabCom + '/licencas-mobilidade/' + $com17b + '/close') $null 409 | Out-Null

Write-Host ''
Write-Host ''
Write-Host '=========== F18 - LIMITES DE DIAS POR NATUREZA (art. 15.o n.o 1) ==========='

# O catalogo so sabia dizer "X dias por ano", e o pedido somava sempre o ano civil. Mas o
# art. 15.o n.o 1 do DL n.o 3/2010 quase nunca fala em anos: "ate 6, POR OCASIAO do casamento",
# "ate 8, por motivo de FALECIMENTO do conjuge", "duas por CADA prova". Escrever isso no limite
# anual errava nos dois sentidos ao mesmo tempo -- recusava o segundo funeral do ano e deixava
# passar oito dias seguidos de uma so vez.
#
# A V53 poe os tres tectos lado a lado, porque a al. q) tem limite anual E mensal ao mesmo
# tempo: "nao podendo ultrapassar 6 dias em cada ano civil e um dia por mes".
#
# As datas ancoram-se no dia corrente e sao TODAS disjuntas: a sobreposicao e verificada por
# funcionario e nao por tipo, logo dois pedidos que se cruzem dao 409 antes de chegarem ao
# limite -- e o bloco provaria outra coisa.

# Segunda-feira da semana que vem, para as contagens cairem em dias uteis inteiros.
$segunda18 = (Get-Date).Date.AddDays(7)
while ($segunda18.DayOfWeek -ne [DayOfWeek]::Monday) { $segunda18 = $segunda18.AddDays(1) }
function Dia18([int]$offsetDias) { return $segunda18.AddDays($offsetDias).ToString('yyyy-MM-dd') }

$rTipos18 = Chamar 'F18.1 catalogo de tipos de ausencia' GET '/catalogs/leave-types?pagina=0&tamanho=50'
$tipos18 = @(Linhas $rTipos18)
$tProva = ($tipos18 | Where-Object { $_.code -eq 'PROVA_EXAME' } | Select-Object -First 1)
$tAssist = ($tipos18 | Where-Object { $_.code -eq 'ASSISTENCIA_FAMILIA' } | Select-Object -First 1)
$tAutoriz = ($tipos18 | Where-Object { $_.code -eq 'AUTORIZADA_DIRIGENTE' } | Select-Object -First 1)
$tLuto = ($tipos18 | Where-Object { $_.code -eq 'LUTO' } | Select-Object -First 1)

Verificar 'F18.2 o catalogo traz os tres tectos, e nao so o anual' (($null -ne $tProva) -and ($null -ne $tAssist) -and ($null -ne $tAutoriz)) ''
Verificar 'F18.3 a prova de exame sao 2 dias por CADA prova (al. f)' (($tProva.maxDaysPerOccurrence -eq 2) -and ($null -eq $tProva.maxDaysPerYear)) ('(ocorrencia=' + $tProva.maxDaysPerOccurrence + ' ano=' + $tProva.maxDaysPerYear + ')')
Verificar 'F18.4 a assistencia a familiar sao 15 POR ANO (al. j)' (($tAssist.maxDaysPerYear -eq 15) -and ($null -eq $tAssist.maxDaysPerOccurrence)) ('(ano=' + $tAssist.maxDaysPerYear + ')')
# A alinea que obriga a ter as duas colunas: os dois tectos valem ao mesmo tempo.
Verificar 'F18.5 a falta autorizada tem tecto anual E mensal (al. q)' (($tAutoriz.maxDaysPerYear -eq 6) -and ($tAutoriz.maxDaysPerMonth -eq 1)) ('(ano=' + $tAutoriz.maxDaysPerYear + ' mes=' + $tAutoriz.maxDaysPerMonth + ')')
# O luto conta por falecimento, nao por ano -- e o valor e o do grau de parentesco (al. b).
Verificar 'F18.6 o luto conta por falecimento, nao por ano (al. b)' (($tLuto.maxDaysPerOccurrence -eq 8) -and ($null -eq $tLuto.maxDaysPerYear)) ('(ocorrencia=' + $tLuto.maxDaysPerOccurrence + ' ano=' + $tLuto.maxDaysPerYear + ')')

# --- o tecto por ocorrencia olha so para o pedido que tem a frente ---
Chamar 'F18.7 NEG uma semana inteira para uma prova de 2 dias' POST ('/funcionarios/' + $colabB + '/pedidos-ausencia') @{ tipoAusenciaId=$tProva.id; dataInicio=(Dia18 0); dataFim=(Dia18 4); motivo='prova' } 422 | Out-Null

# Tres provas em tres semanas. Cada uma cabe no tecto; juntas ultrapassam-no de longe -- e e
# esse o ponto: um tecto por ocorrencia NAO se acumula. Antes da V53 o segundo pedido morria.
$rP1 = Chamar 'F18.8 primeira prova, dois dias' POST ('/funcionarios/' + $colabB + '/pedidos-ausencia') @{ tipoAusenciaId=$tProva.id; dataInicio=(Dia18 0); dataFim=(Dia18 1); motivo='prova de Janeiro' } 201
$rP2 = Chamar 'F18.9 segunda prova, na semana seguinte' POST ('/funcionarios/' + $colabB + '/pedidos-ausencia') @{ tipoAusenciaId=$tProva.id; dataInicio=(Dia18 7); dataFim=(Dia18 8); motivo='segunda prova' } 201
$rP3 = Chamar 'F18.10 terceira prova' POST ('/funcionarios/' + $colabB + '/pedidos-ausencia') @{ tipoAusenciaId=$tProva.id; dataInicio=(Dia18 14); dataFim=(Dia18 15); motivo='terceira prova' } 201
$somaProvas = $rP1.Dados.numeroDias + $rP2.Dados.numeroDias + $rP3.Dados.numeroDias
Verificar 'F18.11 no ano ja vao mais dias do que o tecto de cada prova' ($somaProvas -gt $tProva.maxDaysPerOccurrence) ('(' + $somaProvas + ' dias no ano, tecto por prova=' + $tProva.maxDaysPerOccurrence + ')')

# --- o tecto anual, esse, soma ---
$rA1 = Chamar 'F18.12 assistencia a familiar, duas semanas' POST ('/funcionarios/' + $colabB + '/pedidos-ausencia') @{ tipoAusenciaId=$tAssist.id; dataInicio=(Dia18 21); dataFim=(Dia18 32); motivo='assistencia' } 201
Write-Host ('      dias do primeiro pedido de assistencia: ' + $rA1.Dados.numeroDias)
Chamar 'F18.13 NEG outras duas semanas passam dos 15 do ano' POST ('/funcionarios/' + $colabB + '/pedidos-ausencia') @{ tipoAusenciaId=$tAssist.id; dataInicio=(Dia18 35); dataFim=(Dia18 46); motivo='assistencia outra vez' } 422 | Out-Null

# --- e o tecto mensal conta o mes civil, nao o ano ---
# Um dia de cada vez, para o tecto que se exercita ser o mensal e nao o da ocorrencia.
$diaMes1 = $segunda18.AddDays(49)
$diaMes2 = $diaMes1.AddDays(1)
# O mes seguinte a contar do primeiro dia usado: e a fronteira que se quer provar.
$diaOutroMes = (Get-Date -Year $diaMes1.Year -Month $diaMes1.Month -Day 1).AddMonths(1)
while ($diaOutroMes.DayOfWeek -ne [DayOfWeek]::Monday) { $diaOutroMes = $diaOutroMes.AddDays(1) }

$rM1 = Chamar 'F18.14 uma falta autorizada pelo dirigente' POST ('/funcionarios/' + $colabB + '/pedidos-ausencia') @{ tipoAusenciaId=$tAutoriz.id; dataInicio=$diaMes1.ToString('yyyy-MM-dd'); dataFim=$diaMes1.ToString('yyyy-MM-dd'); motivo='assunto pessoal' } 201
Verificar 'F18.15 contou um dia util' ($rM1.Dados.numeroDias -eq 1) ('(' + $rM1.Dados.numeroDias + ')')
Chamar 'F18.16 NEG a segunda no mesmo mes passa do dia por mes' POST ('/funcionarios/' + $colabB + '/pedidos-ausencia') @{ tipoAusenciaId=$tAutoriz.id; dataInicio=$diaMes2.ToString('yyyy-MM-dd'); dataFim=$diaMes2.ToString('yyyy-MM-dd'); motivo='outra vez' } 422 | Out-Null
# O mes seguinte tem conta propria: o tecto anual de 6 ainda tem folga.
Chamar 'F18.17 mas no mes seguinte ja cabe' POST ('/funcionarios/' + $colabB + '/pedidos-ausencia') @{ tipoAusenciaId=$tAutoriz.id; dataInicio=$diaOutroMes.ToString('yyyy-MM-dd'); dataFim=$diaOutroMes.ToString('yyyy-MM-dd'); motivo='mes seguinte' } 201 | Out-Null

# --- a instituicao parametriza os tectos pela API, sem tocar em codigo ---
$codLim = 'REG_TST_' + (Get-Date -Format 'HHmmss')
Chamar 'F18.18 NEG tecto de zero dias' POST '/catalogs/leave-types' @{ code=$codLim; description='Tipo de teste'; deductsBalance=$false; requiresApproval=$false; maxDaysPerOccurrence=0 } 400 | Out-Null
$rNovoLim = Chamar 'F18.19 criar tipo com tecto por ocorrencia' POST '/catalogs/leave-types' @{ code=$codLim; description='Tipo de teste'; deductsBalance=$false; requiresApproval=$false; maxDaysPerOccurrence=3; maxDaysPerMonth=4 } 201
$rLidoLim = Chamar 'F18.20 ler o tipo criado' GET ('/catalogs/leave-types/' + $rNovoLim.Dados.id)
Verificar 'F18.21 os tectos ficaram guardados' (($rLidoLim.Dados.maxDaysPerOccurrence -eq 3) -and ($rLidoLim.Dados.maxDaysPerMonth -eq 4)) ('(ocorrencia=' + $rLidoLim.Dados.maxDaysPerOccurrence + ' mes=' + $rLidoLim.Dados.maxDaysPerMonth + ')')
$rAltLim = Chamar 'F18.22 e alteram-se pela API' PUT ('/catalogs/leave-types/' + $rNovoLim.Dados.id) @{ code=$codLim; description='Tipo de teste'; deductsBalance=$false; requiresApproval=$false; maxDaysPerOccurrence=5 } 200
Verificar 'F18.23 o tecto mudou e o mensal foi limpo' (($rAltLim.Dados.maxDaysPerOccurrence -eq 5) -and ($null -eq $rAltLim.Dados.maxDaysPerMonth)) ('(ocorrencia=' + $rAltLim.Dados.maxDaysPerOccurrence + ' mes=' + $rAltLim.Dados.maxDaysPerMonth + ')')

Write-Host ''
Write-Host ''
Write-Host '=========== F19 - FALTA INJUSTIFICADA E EFEITO NA REMUNERACAO ==========='

# Art. 43.o n.o 2: "As faltas injustificadas, para alem das consequencias disciplinares a que
# possam dar lugar, NAO CONTAM PARA EFEITOS DE ANTIGUIDADE e implicam a opcao entre a perda das
# remuneracoes correspondentes aos dias de ausencia, ou o seu desconto nas ferias."
#
# Duas coisas numa frase, e so uma delas e escolha. O desconto na antiguidade e imperativo -- por
# isso nao ha booleano a configura-lo, ha um REGIME, e a instituicao so diz QUAIS das suas linhas
# sao injustificadas. Ja entre perder a remuneracao e descontar nas ferias, a lei deixa escolher,
# e a escolha e de CADA CASO: vive no pedido.
#
# E o art. 16.o classifica o efeito na remuneracao das justificadas. A aplicacao NAO calcula
# remuneracao nenhuma -- guarda a classificacao para o sistema que a processa a poder ler.

$rTipos19 = Chamar 'F19.1 catalogo de tipos de ausencia' GET '/catalogs/leave-types?pagina=0&tamanho=50'
$tipos19 = @(Linhas $rTipos19)
$tInj = ($tipos19 | Where-Object { $_.regime -eq 'FALTA_INJUSTIFICADA' } | Select-Object -First 1)
$tGreve = ($tipos19 | Where-Object { $_.code -eq 'GREVE' } | Select-Object -First 1)
$tDoenca = ($tipos19 | Where-Object { $_.code -eq 'DOENCA' } | Select-Object -First 1)

Verificar 'F19.2 ha um tipo classificado no regime das injustificadas' ($null -ne $tInj) ('(' + $tInj.code + ')')
# A classificacao e do REGIME, nao do codigo: e por aqui que a antiguidade sabe o que descontar.
Verificar 'F19.3 e a lei diz o que isso implica na remuneracao' ($tInj.efeitoRemuneracao -eq 'DEPENDE_DA_OPCAO') ('(' + $tInj.efeitoRemuneracao + ')')
# Art. 16.o n.o 4: a greve perde remuneracao mas NAO desconta antiguidade. Dois eixos separados.
Verificar 'F19.4 a greve perde remuneracao e continua FALTA' (($tGreve.efeitoRemuneracao -eq 'PERDA_TOTAL') -and ($tGreve.regime -eq 'FALTA')) ('(' + $tGreve.efeitoRemuneracao + '/' + $tGreve.regime + ')')
# Art. 16.o n.o 2: doenca perde PARCIALMENTE, com direito a subsidio da previdencia.
Verificar 'F19.5 a doenca perde parcialmente (al. d)' ($tDoenca.efeitoRemuneracao -eq 'PERDA_PARCIAL') ('(' + $tDoenca.efeitoRemuneracao + ')')

# --- a opcao do n.o 2 e obrigatoria, e so existe aqui ---
# A antiguidade de partida mede-se ANTES: o B ja traz dias descontados dos blocos anteriores
# (esteve em inactividade fora do quadro), e sem esta linha o bloco provaria esse desconto em
# vez do seu. O que interessa e a DIFERENCA.
$rAntAntes19 = Chamar 'F19.5b antiguidade antes das faltas' GET ('/funcionarios/' + $colabB + '/antiguidade')
$descontadosAntes19 = $rAntAntes19.Dados.diasDescontados
Write-Host ('      descontados antes: ' + $descontadosAntes19)

# No PASSADO, e nao no futuro como o resto do F18: uma falta que ainda nao aconteceu nao
# desconta antiguidade nenhuma -- o calculador recorta os periodos ao tempo ja servido, e bem.
# Duas segundas-feiras ja passadas, que nao colidem com os pedidos futuros do bloco anterior.
$dInj1 = $segunda18.AddDays(-28)
$dInj2 = $segunda18.AddDays(-21)
Chamar 'F19.6 NEG falta injustificada sem dizer o que se faz aos dias' POST ('/funcionarios/' + $colabB + '/pedidos-ausencia') @{ tipoAusenciaId=$tInj.id; dataInicio=$dInj1.ToString('yyyy-MM-dd'); dataFim=$dInj1.ToString('yyyy-MM-dd'); motivo='faltou' } 422 | Out-Null
Chamar 'F19.7 NEG opcao fora da lista' POST ('/funcionarios/' + $colabB + '/pedidos-ausencia') @{ tipoAusenciaId=$tInj.id; dataInicio=$dInj1.ToString('yyyy-MM-dd'); dataFim=$dInj1.ToString('yyyy-MM-dd'); motivo='faltou'; opcaoFaltaInjustificada='PERDOAR' } 422 | Out-Null
# A opcao so existe no art. 43.o n.o 2: num tipo que nao e injustificado nao ha nada a optar.
Chamar 'F19.8 NEG opcao num tipo que nao e injustificado' POST ('/funcionarios/' + $colabB + '/pedidos-ausencia') @{ tipoAusenciaId=$tDoenca.id; dataInicio=$dInj1.ToString('yyyy-MM-dd'); dataFim=$dInj1.ToString('yyyy-MM-dd'); motivo='doente'; opcaoFaltaInjustificada='DESCONTO_FERIAS' } 422 | Out-Null

$rInj1 = Chamar 'F19.9 registar falta injustificada com perda de remuneracao' POST ('/funcionarios/' + $colabB + '/pedidos-ausencia') @{ tipoAusenciaId=$tInj.id; dataInicio=$dInj1.ToString('yyyy-MM-dd'); dataFim=$dInj1.ToString('yyyy-MM-dd'); motivo='faltou sem avisar'; opcaoFaltaInjustificada='PERDA_REMUNERACAO' } 201
# As duas opcoes existem mesmo: a segunda falta escolhe a outra. Nao e uma configuracao do tipo.
$rInj2 = Chamar 'F19.10 e outra com desconto nas ferias' POST ('/funcionarios/' + $colabB + '/pedidos-ausencia') @{ tipoAusenciaId=$tInj.id; dataInicio=$dInj2.ToString('yyyy-MM-dd'); dataFim=$dInj2.ToString('yyyy-MM-dd'); motivo='outra vez'; opcaoFaltaInjustificada='DESCONTO_FERIAS' } 201

$rLido19 = Chamar 'F19.11 ler os pedidos do colaborador' GET ('/funcionarios/' + $colabB + '/pedidos-ausencia')
$ped19a = (@(Linhas $rLido19) | Where-Object { $_.id -eq $rInj1.Dados.id } | Select-Object -First 1)
$ped19b = (@(Linhas $rLido19) | Where-Object { $_.id -eq $rInj2.Dados.id } | Select-Object -First 1)
Verificar 'F19.12 cada pedido guarda a SUA opcao' (($ped19a.opcaoFaltaInjustificada -eq 'PERDA_REMUNERACAO') -and ($ped19b.opcaoFaltaInjustificada -eq 'DESCONTO_FERIAS')) ('(' + $ped19a.opcaoFaltaInjustificada + ' / ' + $ped19b.opcaoFaltaInjustificada + ')')

# --- e a antiguidade desconta-as, que e a parte que a lei nao deixa configurar ---
$rAnt19 = Chamar 'F19.13 antiguidade depois das faltas' GET ('/funcionarios/' + $colabB + '/antiguidade')
Write-Host ('      dias descontados: ' + $rAnt19.Dados.diasDescontados)
# Dois dias uteis, um por cada falta. O desconto e pela DIFERENCA, nao pelo total.
Verificar 'F19.14 as duas faltas descontaram dois dias de antiguidade' (($rAnt19.Dados.diasDescontados - $descontadosAntes19) -eq 2) ('(' + $descontadosAntes19 + ' -> ' + $rAnt19.Dados.diasDescontados + ', art. 43.o n.o 2)')
$motivos19 = ($rAnt19.Dados.periodosDescontados | ForEach-Object { $_.motivo }) -join ' | '
Verificar 'F19.15 e o motivo diz porque foi descontado' ($motivos19 -like '*43*') ('(' + $motivos19 + ')')

# Cancelar uma delas: um pedido sem efeito nao produziu ausencia, logo devolve a antiguidade.
Chamar 'F19.16 cancelar uma das faltas' PATCH ('/funcionarios/' + $colabB + '/pedidos-ausencia/' + $rInj2.Dados.id + '/cancelar') $null 200 | Out-Null
$rAnt19b = Chamar 'F19.17 antiguidade depois do cancelamento' GET ('/funcionarios/' + $colabB + '/antiguidade')
Verificar 'F19.18 o pedido cancelado deixou de descontar' (($rAnt19b.Dados.diasDescontados - $descontadosAntes19) -eq 1) ('(' + $rAnt19.Dados.diasDescontados + ' -> ' + $rAnt19b.Dados.diasDescontados + ')')

# --- a classificacao e da instituicao, e muda-se pela API ---
$codEf = 'REG_TST_' + (Get-Date -Format 'HHmmss') + 'E'
$rNovoEf = Chamar 'F19.19 criar tipo sem dizer o efeito na remuneracao' POST '/catalogs/leave-types' @{ code=$codEf; description='Tipo de teste'; deductsBalance=$false; requiresApproval=$false } 201
$rLidoEf = Chamar 'F19.20 ler o tipo criado' GET ('/catalogs/leave-types/' + $rNovoEf.Dados.id)
# SEM_PERDA e a omissao segura: afirma que nao ha perda em vez de a provocar.
Verificar 'F19.21 nasce SEM_PERDA, que e a omissao segura' ($rLidoEf.Dados.efeitoRemuneracao -eq 'SEM_PERDA') ('(' + $rLidoEf.Dados.efeitoRemuneracao + ')')
$rAltEf = Chamar 'F19.22 classificar pela API' PUT ('/catalogs/leave-types/' + $rNovoEf.Dados.id) @{ code=$codEf; description='Tipo de teste'; deductsBalance=$false; requiresApproval=$false; regime='FALTA_INJUSTIFICADA'; efeitoRemuneracao='PERDA_VENCIMENTO_EXERCICIO' } 200
Verificar 'F19.23 a instituicao classifica sem tocar em codigo' (($rAltEf.Dados.regime -eq 'FALTA_INJUSTIFICADA') -and ($rAltEf.Dados.efeitoRemuneracao -eq 'PERDA_VENCIMENTO_EXERCICIO')) ('(' + $rAltEf.Dados.regime + '/' + $rAltEf.Dados.efeitoRemuneracao + ')')
Chamar 'F19.24 NEG efeito fora da lista da lei' PUT ('/catalogs/leave-types/' + $rNovoEf.Dados.id) @{ code=$codEf; description='Tipo de teste'; deductsBalance=$false; requiresApproval=$false; efeitoRemuneracao='PERDOA_TUDO' } 422 | Out-Null
Chamar 'F19.25 NEG regime fora da lista da lei' PUT ('/catalogs/leave-types/' + $rNovoEf.Dados.id) @{ code=$codEf; description='Tipo de teste'; deductsBalance=$false; requiresApproval=$false; regime='FALTA_QUALQUER' } 422 | Out-Null

Write-Host ''
Write-Host ''
Write-Host '=========== F20 - FERIADOS: RECORRENTES, COM AREA, E TODOS CONTAM (V55) ==========='

# Anos futuros, longe dos pedidos dos blocos anteriores (que andam nos proximos meses): assim os
# blocos novos nao colidem com nada, e os feriados recorrentes do seed (desde 2026) valem la.
$anoMapa = (Get-Date).Year + 2
$anoFer = (Get-Date).Year + 3
$anoPar = (Get-Date).Year + 4
function PrimeiraSegunda([int]$ano, [int]$mes) {
    $d = (Get-Date -Year $ano -Month $mes -Day 1).Date
    while ($d.DayOfWeek -ne [DayOfWeek]::Monday) { $d = $d.AddDays(1) }
    return $d
}
function Iso($d) { return $d.ToString('yyyy-MM-dd') }
function DiasUteisEntre($ini, $fim, $feriados) {
    $n = 0; $d = $ini
    while ($d -le $fim) {
        if (($d.DayOfWeek -ne [DayOfWeek]::Saturday) -and ($d.DayOfWeek -ne [DayOfWeek]::Sunday) -and -not ($feriados -contains (Iso $d))) { $n++ }
        $d = $d.AddDays(1)
    }
    return $n
}
function CorpoUnidade($u) {
    return @{ code=$u.code; name=$u.name; acronym=$u.acronym; unitType=$u.unitType; descricao=$u.descricao;
              parentUnitId=$u.parentUnitId; responsibleEmployeeId=$u.responsibleEmployeeId }
}

# Um tipo sem saldo e sem tectos, em dias uteis: o que se mede e so o calendario.
$codCnt = 'CNT_TST_' + (Get-Date -Format 'HHmmss')
$rTipoCnt = Chamar 'F20.1 criar tipo de teste em dias uteis' POST '/catalogs/leave-types' @{ code=$codCnt; description='Tipo de teste (calendario)'; deductsBalance=$false; requiresApproval=$false; contagem='DIAS_UTEIS' } 201
$tCnt = $rTipoCnt.Dados.id

# Natal e Ano Novo sao recorrentes no seed (desde 2026): tem de contar num ano que o seed nao traz.
$iniNat = Get-Date -Year $anoFer -Month 12 -Day 24
$fimNat = Get-Date -Year ($anoFer + 1) -Month 1 -Day 4
$esperadoNat = DiasUteisEntre $iniNat.Date $fimNat.Date @((Iso (Get-Date -Year $anoFer -Month 12 -Day 25)), (Iso (Get-Date -Year ($anoFer + 1) -Month 1 -Day 1)))
$rNat = Chamar 'F20.2 pedido de 24/12 a 04/01 num ano futuro' POST ('/funcionarios/' + $colabB + '/pedidos-ausencia') @{ tipoAusenciaId=$tCnt; dataInicio=(Iso $iniNat); dataFim=(Iso $fimNat); motivo='fim de ano' } 201
Verificar 'F20.3 o Natal e o Ano Novo recorrentes contam, no periodo inteiro' ($rNat.Dados.numeroDias -eq $esperadoNat) ('(' + $rNat.Dados.numeroDias + ', esperado ' + $esperadoNat + ')')

# Area: um feriado municipal so conta para quem trabalha numa unidade dessa area.
$ckeyArea = 'TST_' + (Get-Date -Format 'HHmmss')
Chamar 'F20.4 criar area geografica de teste' POST '/reference/options' @{ ccode='AREA_GEOGRAFICA'; ckey=$ckeyArea; cvalue='Area de teste'; locale='pt'; sortOrder=99; description='bateria' } 201 | Out-Null
$segFer1 = PrimeiraSegunda $anoFer 2
$segFer2 = $segFer1.AddDays(7)
Chamar 'F20.5 feriado municipal com area, numa quarta' POST '/catalogs/public-holidays' @{ name='Municipal TST 1'; holidayDate=(Iso $segFer1.AddDays(2)); isNational=$false; isRecurring=$false; areaCkey=$ckeyArea } 201 | Out-Null
Chamar 'F20.6 outro na quarta da semana seguinte' POST '/catalogs/public-holidays' @{ name='Municipal TST 2'; holidayDate=(Iso $segFer2.AddDays(2)); isNational=$false; isRecurring=$false; areaCkey=$ckeyArea } 201 | Out-Null
$rSem1 = Chamar 'F20.7 semana do primeiro, com a unidade sem area' POST ('/funcionarios/' + $colabB + '/pedidos-ausencia') @{ tipoAusenciaId=$tCnt; dataInicio=(Iso $segFer1); dataFim=(Iso $segFer1.AddDays(4)); motivo='semana 1' } 201
Verificar 'F20.8 sem area na unidade, o municipal nao conta' ($rSem1.Dados.numeroDias -eq 5) ('(' + $rSem1.Dados.numeroDias + ')')

$rUni20 = Chamar 'F20.9 unidade onde a Maria exerce' GET ('/colaboradores/assignments/funcionario/' + $colabB + '/unidade-atual')
$uni20 = $rUni20.Dados.unidadeOrganicaId
$u20 = (Chamar 'F20.10 ler a unidade' GET ('/estrutura/organizational-units/' + $uni20)).Dados
$corpo20 = CorpoUnidade $u20
$corpo20.areaCkey = $ckeyArea
$rPut20 = Chamar 'F20.11 dar a area a unidade' PUT ('/estrutura/organizational-units/' + $uni20) $corpo20 200
Verificar 'F20.12 a unidade ficou com a area' ($rPut20.Dados.areaCkey -eq $ckeyArea) ('(' + $rPut20.Dados.areaCkey + ')')
$rSem2 = Chamar 'F20.13 semana do segundo, com a unidade na area' POST ('/funcionarios/' + $colabB + '/pedidos-ausencia') @{ tipoAusenciaId=$tCnt; dataInicio=(Iso $segFer2); dataFim=(Iso $segFer2.AddDays(4)); motivo='semana 2' } 201
Verificar 'F20.14 com a area, o municipal conta' ($rSem2.Dados.numeroDias -eq 4) ('(' + $rSem2.Dados.numeroDias + ')')

# Nao partir o front: a area nao se valida contra o catalogo (decisao de 2026-09-23).
Chamar 'F20.15 feriado com area que nao existe no catalogo e aceite' POST '/catalogs/public-holidays' @{ name='Area desconhecida'; holidayDate=(Iso $segFer2.AddDays(14)); isNational=$false; isRecurring=$false; areaCkey='NAO_EXISTE' } 201 | Out-Null
Chamar 'F20.16 NEG feriado nacional com area' POST '/catalogs/public-holidays' @{ name='Nacional com area'; holidayDate=(Iso $segFer2.AddDays(15)); isNational=$true; isRecurring=$false; areaCkey=$ckeyArea } 422 | Out-Null
Chamar 'F20.17 NEG nacional no dia de um recorrente (Natal)' POST '/catalogs/public-holidays' @{ name='Natal repetido'; holidayDate=(Iso (Get-Date -Year $anoFer -Month 12 -Day 25)); isNational=$true; isRecurring=$false } 409 | Out-Null

# A area sai da unidade: os blocos seguintes contam sem ela.
$corpo20.areaCkey = ''
$rLimpa20 = Chamar 'F20.18 limpar a area da unidade' PUT ('/estrutura/organizational-units/' + $uni20) $corpo20 200
Verificar 'F20.19 a area limpa-se em branco' ($null -eq $rLimpa20.Dados.areaCkey) ''

Write-Host ''
Write-Host '=========== F21 - DIAS SEGUIDOS OU UTEIS (V56, art. 76.o) ==========='

# Regra: dias seguidos, contando entre o primeiro e o ultimo dia util. Uteis so onde a lei o diz.
$tipos21 = @(Linhas (Chamar 'F21.1 catalogo de tipos de ausencia' GET '/catalogs/leave-types?pagina=0&tamanho=50'))
$tLuto21 = ($tipos21 | Where-Object { $_.code -eq 'LUTO' } | Select-Object -First 1)
$tSem21 = ($tipos21 | Where-Object { $_.code -eq 'SEMINARIO' } | Select-Object -First 1)
$tTe21 = ($tipos21 | Where-Object { $_.code -eq 'TE_PESQUISA' } | Select-Object -First 1)
Verificar 'F21.2 o seed classifica: luto e seminario seguidos, pesquisa em uteis' (($tLuto21.contagem -eq 'DIAS_SEGUIDOS') -and ($tSem21.contagem -eq 'DIAS_SEGUIDOS') -and ($tTe21.contagem -eq 'DIAS_UTEIS')) ('(' + $tLuto21.contagem + '/' + $tSem21.contagem + '/' + $tTe21.contagem + ')')

$s21 = PrimeiraSegunda $anoFer 10
$rLuto21 = Chamar 'F21.3 luto de sexta a segunda' POST ('/funcionarios/' + $colabB + '/pedidos-ausencia') @{ tipoAusenciaId=$tLuto21.id; dataInicio=(Iso $s21.AddDays(4)); dataFim=(Iso $s21.AddDays(7)); motivo='luto' } 201
Verificar 'F21.4 o fim-de-semana intercalado conta' ($rLuto21.Dados.numeroDias -eq 4) ('(' + $rLuto21.Dados.numeroDias + ')')
Chamar 'F21.5 NEG seminario de quinta a quarta passa dos 5 seguidos' POST ('/funcionarios/' + $colabB + '/pedidos-ausencia') @{ tipoAusenciaId=$tSem21.id; dataInicio=(Iso $s21.AddDays(10)); dataFim=(Iso $s21.AddDays(16)); motivo='seminario' } 422 | Out-Null
$rSem21 = Chamar 'F21.6 seminario de segunda a sexta' POST ('/funcionarios/' + $colabB + '/pedidos-ausencia') @{ tipoAusenciaId=$tSem21.id; dataInicio=(Iso $s21.AddDays(14)); dataFim=(Iso $s21.AddDays(18)); motivo='seminario' } 201
Verificar 'F21.7 cinco dias seguidos' ($rSem21.Dados.numeroDias -eq 5) ('(' + $rSem21.Dados.numeroDias + ')')
$rTe21 = Chamar 'F21.8 pesquisa de trabalhador-estudante de sexta a segunda' POST ('/funcionarios/' + $colabB + '/pedidos-ausencia') @{ tipoAusenciaId=$tTe21.id; dataInicio=(Iso $s21.AddDays(25)); dataFim=(Iso $s21.AddDays(28)); motivo='pesquisa' } 201
Verificar 'F21.9 em dias uteis o fim-de-semana nao conta' ($rTe21.Dados.numeroDias -eq 2) ('(' + $rTe21.Dados.numeroDias + ')')

# Nao partir o front: o PUT que omite a contagem nao a apaga.
$rPut21 = Chamar 'F21.10 alterar o tipo de teste sem enviar a contagem' PUT ('/catalogs/leave-types/' + $tCnt) @{ code=$codCnt; description='Tipo de teste (calendario)'; deductsBalance=$false; requiresApproval=$false } 200
Verificar 'F21.11 a contagem ficou como estava' ($rPut21.Dados.contagem -eq 'DIAS_UTEIS') ('(' + $rPut21.Dados.contagem + ')')
Chamar 'F21.12 NEG contagem fora da lei' PUT ('/catalogs/leave-types/' + $tCnt) @{ code=$codCnt; description='Tipo de teste (calendario)'; deductsBalance=$false; requiresApproval=$false; contagem='CORRIDOS' } 422 | Out-Null

Write-Host ''
Write-Host '=========== F22 - MAPA DE FERIAS (arts. 5.o e 6.o) ==========='

$setMapa = PrimeiraSegunda $anoMapa 9
$rotaF22 = '/funcionarios/' + $colabB + '/ferias/' + $anoMapa
$rPref22 = Chamar 'F22.1 indicar a preferencia, dentro do prazo' PUT ($rotaF22 + '/preferencia') @{ periodos=@(@{ dataInicio=(Iso $setMapa); dataFim=(Iso $setMapa.AddDays(18)) }); observacoes='Setembro' } 200
Verificar 'F22.2 dentro do prazo, sem alertas' (@($rPref22.Dados.alertas).Count -eq 0) ('(' + (@($rPref22.Dados.alertas) -join ' | ') + ')')

# Sem acordo, o dirigente fixa entre 1 de Maio e 31 de Outubro (art. 5.o n.o 5).
$dez22 = PrimeiraSegunda $anoMapa 12
Chamar 'F22.3 NEG fixada em Dezembro, fora da janela' PUT ($rotaF22 + '/marcacao') @{ origem='FIXADA'; periodos=@(@{ dataInicio=(Iso $dez22); dataFim=(Iso $dez22.AddDays(11)) }) } 422 | Out-Null
$rMarc22 = Chamar 'F22.4 marcar por acordo tres semanas de Setembro' PUT ($rotaF22 + '/marcacao') @{ origem='ACORDO'; periodos=@(@{ dataInicio=(Iso $setMapa); dataFim=(Iso $setMapa.AddDays(18)) }) } 200
Verificar 'F22.5 abaixo do direito, aceite com alerta' (@($rMarc22.Dados.alertas).Count -ge 1) ('(' + (@($rMarc22.Dados.alertas) -join ' | ') + ')')
$rLer22 = Chamar 'F22.6 ler as ferias do ano' GET $rotaF22
Verificar 'F22.7 quinze dias uteis marcados, com o direito do ano' (($rLer22.Dados.totalMarcado -eq 15) -and ($null -ne $rLer22.Dados.direito)) ('(marcado=' + $rLer22.Dados.totalMarcado + ' direito=' + $rLer22.Dados.direito + ')')

$rMapa22 = Chamar 'F22.8 mapa do ano' GET ('/ferias/mapa/' + $anoMapa)
$linhaB22 = (@($rMapa22.Dados.linhas) | Where-Object { $_.funcionarioId -eq $colabB } | Select-Object -First 1)
Verificar 'F22.9 a Maria esta no mapa, marcada por acordo' (($null -ne $linhaB22) -and ($linhaB22.origem -eq 'ACORDO')) ''
$rPub22 = Chamar 'F22.10 dar conhecimento do mapa' POST ('/ferias/mapa/' + $anoMapa + '/publicar') $null 201
Chamar 'F22.11 NEG dar conhecimento outra vez' POST ('/ferias/mapa/' + $anoMapa + '/publicar') $null 409 | Out-Null

# Depois de publicado, alterar pede o motivo do art. 6.o n.o 2.
$setMapa2 = $setMapa.AddDays(7)
Chamar 'F22.12 NEG alterar o mapa publicado sem motivo' PUT ($rotaF22 + '/marcacao') @{ origem='ACORDO'; periodos=@(@{ dataInicio=(Iso $setMapa2); dataFim=(Iso $setMapa2.AddDays(18)) }) } 422 | Out-Null
Chamar 'F22.13 alterar por acordo' PUT ($rotaF22 + '/marcacao') @{ origem='ACORDO'; motivoAlteracao='ACORDO'; periodos=@(@{ dataInicio=(Iso $setMapa2); dataFim=(Iso $setMapa2.AddDays(18)) }) } 200 | Out-Null
$rLer22b = Chamar 'F22.14 ler as ferias depois da alteracao' GET $rotaF22
Verificar 'F22.15 a alteracao ficou registada, com o motivo' ((@($rLer22b.Dados.alteracoes).Count -eq 1) -and (@($rLer22b.Dados.alteracoes)[0].motivo -eq 'ACORDO')) ''

Write-Host ''
Write-Host '=========== F23 - PARAMETROS DO MAPA DE FERIAS, POR VIGENCIA ==========='

$rVig23 = Chamar 'F23.1 parametros em vigor num ano sem linha propria' GET ('/catalogs/parametros-ferias/vigente?ano=' + $anoPar)
Verificar 'F23.2 vale a linha da lei, desde 2010' (($rVig23.Dados.vigenteDesde -eq 2010) -and ($rVig23.Dados.prazoMapa -eq '03-31') -and ($rVig23.Dados.periodoMinimoInterpolado -eq 11)) ('(' + $rVig23.Dados.vigenteDesde + ' ' + $rVig23.Dados.prazoMapa + ' ' + $rVig23.Dados.origem + ')')
$corpo23 = @{ vigenteDesde=$anoPar; prazoPreferencia='02-15'; prazoMapa='04-15'; fixacaoInicio='05-01'; fixacaoFim='10-31'; periodoMinimoInterpolado=10; fundamento='Diploma de teste' }
Chamar 'F23.3 vigencia nova, como faria um diploma novo' POST '/catalogs/parametros-ferias' $corpo23 201 | Out-Null
Chamar 'F23.4 NEG outra vigencia no mesmo ano' POST '/catalogs/parametros-ferias' $corpo23 409 | Out-Null
Chamar 'F23.5 NEG prazo a 29 de Fevereiro' POST '/catalogs/parametros-ferias' @{ vigenteDesde=($anoPar + 1); prazoPreferencia='02-29'; prazoMapa='04-15'; fixacaoInicio='05-01'; fixacaoFim='10-31'; periodoMinimoInterpolado=10 } 422 | Out-Null
Chamar 'F23.6 NEG preferencia depois do mapa' POST '/catalogs/parametros-ferias' @{ vigenteDesde=($anoPar + 1); prazoPreferencia='05-15'; prazoMapa='04-15'; fixacaoInicio='05-01'; fixacaoFim='10-31'; periodoMinimoInterpolado=10 } 422 | Out-Null
$rVigN = Chamar 'F23.7 em vigor no ano do diploma' GET ('/catalogs/parametros-ferias/vigente?ano=' + $anoPar)
$rVigA = Chamar 'F23.8 e no ano anterior' GET ('/catalogs/parametros-ferias/vigente?ano=' + ($anoPar - 1))
Verificar 'F23.9 cada ano le as regras do seu tempo' (($rVigN.Dados.prazoMapa -eq '04-15') -and ($rVigA.Dados.prazoMapa -eq '03-31')) ('(' + $rVigN.Dados.prazoMapa + ' / ' + $rVigA.Dados.prazoMapa + ')')
$rFer23 = Chamar 'F23.10 ferias da Maria no ano do diploma' GET ('/funcionarios/' + $colabB + '/ferias/' + $anoPar)
Verificar 'F23.11 o mapa de ferias ja le o prazo novo' ($rFer23.Dados.prazoPreferencia -eq ("$anoPar" + '-02-15')) ('(' + $rFer23.Dados.prazoPreferencia + ')')

# Gozo interpolado 10 + 6: o minimo novo (10) aceita, o da lei (11) recusa.
function Interpolado([int]$ano) {
    $p1 = PrimeiraSegunda $ano 9
    $p2 = PrimeiraSegunda $ano 10
    return @{ origem='ACORDO'; periodos=@(@{ dataInicio=(Iso $p1); dataFim=(Iso $p1.AddDays(11)) }, @{ dataInicio=(Iso $p2); dataFim=(Iso $p2.AddDays(7)) }) }
}
Chamar 'F23.12 interpolado 10+6 no ano do diploma (minimo 10)' PUT ('/funcionarios/' + $colabB + '/ferias/' + $anoPar + '/marcacao') (Interpolado $anoPar) 200 | Out-Null
Chamar 'F23.13 NEG o mesmo no ano anterior (minimo 11 da lei)' PUT ('/funcionarios/' + $colabB + '/ferias/' + ($anoPar - 1) + '/marcacao') (Interpolado ($anoPar - 1)) 422 | Out-Null

Write-Host ''
Write-Host '=========== F24 - HORARIOS (assiduidade, primeiro passo) ==========='

# O horario que vale numa data: o do colaborador; senao o da unidade (ou da mae); senao o base;
# senao nenhum. O repor_estado apaga os horarios: comeca-se sem nenhum.
function Semana($ini1, $fim1, $ini2, $fim2, [bool]$obrigatorio) {
    $blocos = @()
    foreach ($d in 1..5) {
        $blocos += @{ diaSemana=$d; inicio=$ini1; fim=$fim1; obrigatorio=$obrigatorio }
        if ($ini2) { $blocos += @{ diaSemana=$d; inicio=$ini2; fim=$fim2; obrigatorio=$obrigatorio } }
    }
    return $blocos
}
$rotaH = '/funcionarios/' + $colabB + '/horarios'
$hoje24 = Iso (Get-Date)
# Hoje a Maria esta numa mobilidade externa de um dia (F8): sem unidade, vale o base, e bem.
# A heranca da unidade prova-se daqui a cinco dias -- depois dela, antes da atribuicao (dez).
$antes24 = Iso (Get-Date).Date.AddDays(5)
function Vigente($data) { return (Chamar ('F24 vigente em ' + $data) GET ($rotaH + '/vigente?data=' + $data)).Dados }

$v0 = Vigente $hoje24
Verificar 'F24.1 sem horario nenhum, a origem e NENHUM' ($v0.origem -eq 'NENHUM') ('(' + $v0.origem + ')')
$rNormal = Chamar 'F24.2 criar horario fixo' POST '/catalogs/horarios' @{ nome='TST Normal'; controlo='FIXO'; blocos=(Semana '08:00' '12:30' '14:00' '17:30' $true) } 201
$hNormal = $rNormal.Dados.id
$rBase = Chamar 'F24.3 marcar como horario base' PATCH ('/catalogs/horarios/' + $hNormal + '/base') $null 200
Verificar 'F24.4 base, com as horas calculadas' (($rBase.Dados.isBase -eq $true) -and ($rBase.Dados.horasSemanais -eq '40:00')) ('(' + $rBase.Dados.horasSemanais + ')')
$v1 = Vigente $hoje24
Verificar 'F24.5 sem horario na pessoa nem na unidade, vale o base' (($v1.origem -eq 'BASE') -and ($v1.regimePrestacao -eq 'PRESENCIAL')) ('(' + $v1.origem + ')')

$rAtend = Chamar 'F24.6 criar horario de atendimento' POST '/catalogs/horarios' @{ nome='TST Atendimento'; controlo='FIXO'; blocos=(Semana '07:30' '15:30' $null $null $true) } 201
$hAtend = $rAtend.Dados.id
# O horario vai para a MAE da unidade da Maria (ou para a propria, se nao tiver mae): prova a heranca.
$uH = (Chamar 'F24.7 ler a unidade da Maria' GET ('/estrutura/organizational-units/' + $uni20)).Dados
$uniH = $uni20
if ($uH.parentUnitId) { $uniH = $uH.parentUnitId }
$uAlvo = (Chamar 'F24.8 ler a unidade que recebe o horario' GET ('/estrutura/organizational-units/' + $uniH)).Dados
$corpoH = CorpoUnidade $uAlvo
$corpoH.horarioId = $hAtend
$rPutH = Chamar 'F24.9 dar horario a unidade' PUT ('/estrutura/organizational-units/' + $uniH) $corpoH 200
Verificar 'F24.10 a unidade ficou com o horario' ($rPutH.Dados.horarioId -eq $hAtend) ''
$v2 = Vigente $antes24
Verificar 'F24.11 vale o da unidade, herdado' (($v2.origem -eq 'UNIDADE') -and ($v2.horario.nome -eq 'TST Atendimento')) ('(' + $v2.origem + ' ' + $v2.horario.nome + ')')
$corpoH.Remove('horarioId')
$rPutH2 = Chamar 'F24.12 PUT da unidade sem horarioId' PUT ('/estrutura/organizational-units/' + $uniH) $corpoH 200
Verificar 'F24.13 nao apaga o horario' ($rPutH2.Dados.horarioId -eq $hAtend) ''
$corpoH.horarioId = '00000000-0000-0000-0000-000000000000'
Chamar 'F24.14 NEG unidade com horario que nao existe' PUT ('/estrutura/organizational-units/' + $uniH) $corpoH 422 | Out-Null

Chamar 'F24.15 NEG flexivel sem periodo de afericao' POST '/catalogs/horarios' @{ nome='TST Flex'; controlo='FLEXIVEL'; duracaoDiaria='07:00'; blocos=(Semana '07:00' '19:00' $null $null $false) } 422 | Out-Null
$rFlex = Chamar 'F24.16 flexivel com periodo e duracao' POST '/catalogs/horarios' @{ nome='TST Flex'; controlo='FLEXIVEL'; periodoAfericao='MES'; duracaoDiaria='07:00'; blocos=(Semana '07:00' '19:00' $null $null $false) } 201
$hFlex = $rFlex.Dados.id
Chamar 'F24.17 NEG blocos sobrepostos' POST '/catalogs/horarios' @{ nome='X'; controlo='FIXO'; blocos=@(@{ diaSemana=1; inicio='08:00'; fim='12:00' }, @{ diaSemana=1; inicio='11:00'; fim='13:00' }) } 422 | Out-Null

$dA = (Get-Date).Date.AddDays(10)
Chamar 'F24.18 atribuir o flexivel em teletrabalho' POST $rotaH @{ horarioId=$hFlex; regimePrestacao='TELETRABALHO'; dataInicio=(Iso $dA) } 201 | Out-Null
$v3 = Vigente (Iso $dA.AddDays(4))
Verificar 'F24.19 depois da data, vale o da pessoa' (($v3.origem -eq 'COLABORADOR') -and ($v3.regimePrestacao -eq 'TELETRABALHO') -and ($v3.horario.horasSemanais -eq '35:00')) ('(' + $v3.origem + ' ' + $v3.regimePrestacao + ' ' + $v3.horario.horasSemanais + ')')
$v4 = Vigente $antes24
Verificar 'F24.20 antes da data, ainda o da unidade' ($v4.origem -eq 'UNIDADE') ('(' + $v4.origem + ')')
Chamar 'F24.21 NEG outra atribuicao na mesma data' POST $rotaH @{ horarioId=$hNormal; dataInicio=(Iso $dA) } 422 | Out-Null
Chamar 'F24.22 atribuir a seguinte' POST $rotaH @{ horarioId=$hNormal; dataInicio=(Iso $dA.AddDays(31)) } 201 | Out-Null
$hist24 = @(Linhas (Chamar 'F24.23 historico' GET $rotaH))
Verificar 'F24.24 a anterior fechou na vespera' (($hist24.Count -eq 2) -and ($hist24[0].dataFim -eq (Iso $dA.AddDays(30))) -and ($null -eq $hist24[1].dataFim)) ('(' + $hist24.Count + ' atribuicoes, fim da 1a=' + $hist24[0].dataFim + ')')
Chamar 'F24.25 NEG regime de prestacao fora da lei' POST $rotaH @{ horarioId=$hNormal; regimePrestacao='REMOTO'; dataInicio=(Iso $dA.AddDays(60)) } 422 | Out-Null

Chamar 'F24.26 NEG desactivar o horario base' DELETE ('/catalogs/horarios/' + $hNormal) $null 409 | Out-Null
# O base tem data de efeito: o de atendimento passa a base daqui a 60 dias; ate la, e para tras, o normal.
$rBase2 = Chamar 'F24.27 agendar outro base daqui a 60 dias' PATCH ('/catalogs/horarios/' + $hAtend + '/base?desde=' + (Iso (Get-Date).Date.AddDays(60))) $null 200
$rNormal2 = Chamar 'F24.28 ler o base de hoje' GET ('/catalogs/horarios/' + $hNormal)
Verificar 'F24.29 hoje o base continua o normal; o agendado ainda nao e' (($rNormal2.Dados.isBase -eq $true) -and ($rBase2.Dados.isBase -eq $false)) ''
$corpoH.horarioId = ''
$rPutH3 = Chamar 'F24.30 horarioId em branco limpa a unidade' PUT ('/estrutura/organizational-units/' + $uniH) $corpoH 200
Verificar 'F24.31 a unidade ficou sem horario' ($null -eq $rPutH3.Dados.horarioId) ''

Write-Host ''
Write-Host '=========== F25 - REGISTO DIARIO DE ASSIDUIDADE (art. 164.o n.o 3) ==========='

# Guardam-se as marcacoes (a prova) e o dia calcula-se delas. Seis semanas atras: longe das
# mobilidades e dos pedidos dos blocos anteriores. O horario que vale e o base que o F24 deixou
# (TST Atendimento, 8h de segunda a sexta).
$seg25 = (Get-Date).Date.AddDays(-42)
while ($seg25.DayOfWeek -ne [DayOfWeek]::Monday) { $seg25 = $seg25.AddDays(-1) }
$ter25 = $seg25.AddDays(1)
$tag25 = 'TST-' + (Get-Date -Format 'HHmmss')
function Pic($dia, $hora, $sentido, $n) {
    return @{ numeroFuncionario='0000002'; momento=((Iso $dia) + 'T' + $hora); sentido=$sentido; referenciaExterna=($tag25 + '-' + $n) }
}
$lote25 = @{ picagens=@(
    (Pic $seg25 '08:00' 'ENTRADA' 1), (Pic $seg25 '12:30' 'SAIDA' 2), (Pic $seg25 '14:00' 'ENTRADA' 3), (Pic $seg25 '17:30' 'SAIDA' 4),
    (Pic $ter25 '08:00' 'ENTRADA' 5),
    @{ numeroFuncionario='9999999'; momento=((Iso $ter25) + 'T08:00'); sentido='ENTRADA'; referenciaExterna=($tag25 + '-6') },
    @{ numeroFuncionario='0000002'; momento=((Iso $ter25) + 'T09:00'); sentido='ENTRADA' }) }
$rImp = Chamar 'F25.1 importar picagens do relogio' POST '/assiduidade/importacao' $lote25 200
Verificar 'F25.2 cinco importadas, duas rejeitadas com motivo' (($rImp.Dados.importadas -eq 5) -and (@($rImp.Dados.rejeitadas).Count -eq 2)) ('(importadas=' + $rImp.Dados.importadas + ' rejeitadas=' + @($rImp.Dados.rejeitadas).Count + ')')
$rImp2 = Chamar 'F25.3 importar o mesmo lote outra vez' POST '/assiduidade/importacao' $lote25 200
Verificar 'F25.4 a importacao e repetivel: nada duplica' (($rImp2.Dados.importadas -eq 0) -and ($rImp2.Dados.duplicadas -eq 5)) ('(duplicadas=' + $rImp2.Dados.duplicadas + ')')

$rotaA = '/funcionarios/' + $colabB + '/assiduidade?de=' + (Iso $seg25) + '&ate=' + (Iso $seg25.AddDays(6))
$a25 = (Chamar 'F25.5 assiduidade da semana' GET $rotaA).Dados
$d0 = @($a25.dias)[0]; $d1 = @($a25.dias)[1]
Verificar 'F25.6 segunda: dois periodos, 90 min de intervalo, 8h' (($d0.minutosTrabalhados -eq 480) -and (@($d0.periodos).Count -eq 2) -and (@($d0.intervalosMinutos)[0] -eq 90) -and (@($d0.anomalias).Count -eq 0)) ('(' + $d0.minutosTrabalhados + ' min)')
Verificar 'F25.7 terca: entrada sem saida e anomalia, e nao conta' (($d1.minutosTrabalhados -eq 0) -and (@($d1.anomalias) -contains 'ENTRADA_SEM_SAIDA')) ('(' + (@($d1.anomalias) -join ',') + ')')
Verificar 'F25.8 esperado pelo horario do dia, total da semana' (($d0.minutosEsperados -eq 480) -and (@($a25.semanas)[0].minutosEsperados -eq 2400) -and (@($a25.semanas)[0].minutosTrabalhados -eq 480)) ('(esperado dia=' + $d0.minutosEsperados + ' semana=' + @($a25.semanas)[0].minutosEsperados + ')')

# Corrigir: num dia com marcacoes, lancar outra e uma correccao, e exige motivo.
$rotaM = '/funcionarios/' + $colabB + '/marcacoes'
Chamar 'F25.9 NEG corrigir a terca sem motivo' POST $rotaM @{ momento=((Iso $ter25) + 'T17:00'); sentido='SAIDA' } 422 | Out-Null
Chamar 'F25.10 corrigir a terca com motivo' POST $rotaM @{ momento=((Iso $ter25) + 'T17:00'); sentido='SAIDA'; motivo='esqueceu-se de picar a saida' } 201 | Out-Null
$d1b = @((Chamar 'F25.11 ler a semana depois da correccao' GET $rotaA).Dados.dias)[1]
Verificar 'F25.12 a terca ficou completa' (($d1b.minutosTrabalhados -eq 540) -and (@($d1b.anomalias).Count -eq 0)) ('(' + $d1b.minutosTrabalhados + ' min)')
Chamar 'F25.13 NEG marcacao no futuro' POST $rotaM @{ momento=((Iso (Get-Date).AddDays(2)) + 'T08:00'); sentido='ENTRADA' } 422 | Out-Null
Chamar 'F25.14 NEG sentido fora da lista' POST $rotaM @{ momento=((Iso $seg25.AddDays(2)) + 'T08:00'); sentido='PAUSA' } 422 | Out-Null

# Anular: a marcacao fica, anulada, com o motivo. E prova do que foi picado.
$saida1230 = (@($d0.marcacoes) | Where-Object { $_.sentido -eq 'SAIDA' } | Sort-Object momento | Select-Object -First 1).id
$rotaAn = $rotaM + '/' + $saida1230 + '/anular'
Chamar 'F25.15 NEG anular sem motivo' PATCH $rotaAn @{ motivo='' } 422 | Out-Null
Chamar 'F25.16 anular com motivo' PATCH $rotaAn @{ motivo='picagem duplicada do relogio' } 200 | Out-Null
Chamar 'F25.17 NEG anular outra vez' PATCH $rotaAn @{ motivo='de novo' } 409 | Out-Null
$d0b = @((Chamar 'F25.18 ler a semana depois de anular' GET $rotaA).Dados.dias)[0]
$anuladas = @($d0b.marcacoes | Where-Object { $_.anulada }).Count
Verificar 'F25.19 a anulada fica visivel e deixa de contar' (($anuladas -eq 1) -and (@($d0b.marcacoes).Count -eq 4) -and (@($d0b.anomalias) -contains 'ENTRADAS_SEGUIDAS') -and ($d0b.minutosTrabalhados -eq 210)) ('(' + $d0b.minutosTrabalhados + ' min, ' + (@($d0b.anomalias) -join ',') + ')')

$rSab = Chamar 'F25.20 marcacao num sabado' POST $rotaM @{ momento=((Iso $seg25.AddDays(5)) + 'T09:00'); sentido='ENTRADA' } 201
Verificar 'F25.21 aceite, com alerta de fim-de-semana' ((@($rSab.Dados.alertas) -join ' ') -like '*fim-de-semana*') ('(' + (@($rSab.Dados.alertas) -join ' | ') + ')')
Chamar 'F25.22 NEG consulta de mais de dois meses' GET ('/funcionarios/' + $colabB + '/assiduidade?de=' + (Iso $seg25) + '&ate=' + (Iso $seg25.AddDays(70))) $null 422 | Out-Null

Write-Host ''
Write-Host '=========== F26 - FALTAS POR DEBITO (DL 3/2010, art. 13.o) ==========='

# Sobre a semana do F25: a segunda ficou com anomalia (anulou-se uma saida), a terca foi corrigida
# (08:00-17:00 contra o base 07:30-15:30: 30 min de atraso), a quarta nao tem marcacoes, e o sabado
# e descanso. Calcula-se a cada leitura: aprovar um pedido que cubra a quarta tira-a do apuramento.
$qua26 = $seg25.AddDays(2)
$mes26 = $seg25.ToString('yyyy-MM')
$rotaF = '/funcionarios/' + $colabB + '/faltas-apuradas?mes=' + $mes26
function DiaF($apur, $data) { return (@($apur.dias) | Where-Object { $_.data -eq (Iso $data) } | Select-Object -First 1) }

$f1 = (Chamar 'F26.1 apuramento do mes da semana do F25' GET $rotaF).Dados
Verificar 'F26.2 a segunda tem anomalia: fica por corrigir, nao conta' ((DiaF $f1 $seg25).estado -eq 'POR_CORRIGIR') ('(' + (DiaF $f1 $seg25).estado + ')')
$t26 = DiaF $f1 $ter25
Verificar 'F26.3 a terca tem 30 min de atraso contra o horario fixo' (($t26.estado -eq 'COM_FALTA') -and ($t26.motivo -eq 'INCOMPLETO') -and ($t26.minutosEmFalta -eq 30)) ('(' + $t26.estado + ' ' + $t26.motivo + ' ' + $t26.minutosEmFalta + ')')
$q26 = DiaF $f1 $qua26
Verificar 'F26.4 a quarta, sem nenhuma marcacao, conta inteira' (($q26.motivo -eq 'SEM_REGISTO') -and ($q26.minutosEmFalta -eq 480)) ('(' + $q26.motivo + ' ' + $q26.minutosEmFalta + ')')
# O domingo: o sabado desta semana pode ser feriado (15 de Agosto), e ai o estado e FERIADO.
$dom26 = (DiaF $f1 $seg25.AddDays(6)).estado
Verificar 'F26.5 o domingo nao se apura' (($dom26 -eq 'DESCANSO') -or ($dom26 -eq 'FERIADO')) ('(' + $dom26 + ')')
Verificar 'F26.6 o parcial converte-se em meios-dias' (($f1.periodoNormalMinutos -gt 0) -and ($f1.totalFaltas -ge $f1.diasSemRegisto)) ('(sem registo=' + $f1.diasSemRegisto + ' parciais=' + $f1.minutosParciais + ' total=' + $f1.totalFaltas + ')')

# Justificar a quarta: um pedido de ausencia aprovado que a cubra tira-a do apuramento.
$rPed26 = Chamar 'F26.7 pedido de ausencia para a quarta' POST ('/funcionarios/' + $colabB + '/pedidos-ausencia') @{ tipoAusenciaId=$tCnt; dataInicio=(Iso $qua26); dataFim=(Iso $qua26); motivo='justificacao da quarta' } 201
if ($rPed26.Dados.estado -ne 'APROVADO') {
    Chamar 'F26.8 aprovar o pedido' PATCH ('/funcionarios/' + $colabB + '/pedidos-ausencia/' + $rPed26.Dados.id + '/aprovar') @{ aprovadoPorId=$colabA; observacoesDecisao='justificada' } 200 | Out-Null
}
$f2 = (Chamar 'F26.9 apuramento depois de justificar' GET $rotaF).Dados
Verificar 'F26.10 a quarta passou a ausencia justificada' ((DiaF $f2 $qua26).estado -eq 'AUSENCIA_JUSTIFICADA') ('(' + (DiaF $f2 $qua26).estado + ')')
Verificar 'F26.11 e deixou de contar' ($f2.diasSemRegisto -eq ($f1.diasSemRegisto - 1)) ('(' + $f1.diasSemRegisto + ' -> ' + $f2.diasSemRegisto + ')')
Chamar 'F26.12 NEG mes mal escrito' GET ('/funcionarios/' + $colabB + '/faltas-apuradas?mes=09-2026') $null 422 | Out-Null

Write-Host ''
Write-Host '=========== F27 - PEDIDO EM HORAS E DISPENSA DE AMAMENTACAO (V58) ==========='

# O pedido de ausencia aceita horaInicio/horaFim, que valem em cada dia do intervalo. A amamentacao
# sao duas horas por dia (Lei 20/X/2023, art. 172.o n.o 3): dois pedidos de 1 hora, durante meses.
$tipos27 = @(Linhas (Chamar 'F27.1 catalogo de tipos de ausencia' GET '/catalogs/leave-types?pagina=0&tamanho=60'))
$tAmam = ($tipos27 | Where-Object { $_.code -eq 'DISPENSA_AMAMENTACAO' } | Select-Object -First 1)
$tTrat = ($tipos27 | Where-Object { $_.code -eq 'TRATAMENTO_AMBULATORIO' } | Select-Object -First 1)
$tFer27 = ($tipos27 | Where-Object { $_.code -eq 'FERIAS' } | Select-Object -First 1)
Verificar 'F27.2 a amamentacao: 120 min por dia, 183 dias por ocorrencia' (($tAmam.maxMinutosPorDia -eq 120) -and ($tAmam.maxDaysPerOccurrence -eq 183)) ('(' + $tAmam.maxMinutosPorDia + ' min, ' + $tAmam.maxDaysPerOccurrence + ' dias)')

$ini27 = (Get-Date).Date.AddDays(200)
$fim27 = $ini27.AddDays(99)
$rotaP27 = '/funcionarios/' + $colabB + '/pedidos-ausencia'
function Horas27($tipo, $de, $ate, $hi, $hf) { return @{ tipoAusenciaId=$tipo.id; dataInicio=(Iso $de); dataFim=(Iso $ate); horaInicio=$hi; horaFim=$hf; motivo='bateria' } }
$rA1 = Chamar 'F27.3 amamentacao de manha, uma hora por dia durante 100 dias' POST $rotaP27 (Horas27 $tAmam $ini27 $fim27 '08:00' '09:00') 201
Verificar 'F27.4 nao conta dias, conta minutos por dia' (($rA1.Dados.numeroDias -eq 0) -and ($rA1.Dados.minutosPorDia -eq 60)) ('(' + $rA1.Dados.numeroDias + ' dias, ' + $rA1.Dados.minutosPorDia + ' min)')
Chamar 'F27.5 a segunda hora, a tarde' POST $rotaP27 (Horas27 $tAmam $ini27 $fim27 '15:00' '16:00') 201 | Out-Null
Chamar 'F27.6 NEG uma terceira passa das 2 horas por dia' POST $rotaP27 (Horas27 $tAmam $ini27 $fim27 '12:00' '12:30') 422 | Out-Null
Chamar 'F27.7 NEG horas que se cruzam com a amamentacao' POST $rotaP27 (Horas27 $tTrat $ini27.AddDays(3) $ini27.AddDays(3) '08:30' '09:30') 409 | Out-Null
Chamar 'F27.8 mas noutra hora do mesmo dia cabe' POST $rotaP27 (Horas27 $tTrat $ini27.AddDays(3) $ini27.AddDays(3) '10:00' '11:00') 201 | Out-Null
Chamar 'F27.9 NEG intervalo de 184 dias passa do tecto por ocorrencia' POST $rotaP27 (Horas27 $tAmam $fim27.AddDays(10) $fim27.AddDays(193) '08:00' '09:00') 422 | Out-Null
Chamar 'F27.10 NEG ferias em horas (desconta saldo)' POST $rotaP27 (Horas27 $tFer27 $fim27.AddDays(10) $fim27.AddDays(10) '08:00' '09:00') 422 | Out-Null
Chamar 'F27.11 NEG so a hora de inicio' POST $rotaP27 @{ tipoAusenciaId=$tTrat.id; dataInicio=(Iso $fim27.AddDays(12)); dataFim=(Iso $fim27.AddDays(12)); horaInicio='08:00'; motivo='x' } 422 | Out-Null

# Terminar antes do fim: a decisao fica, o periodo acaba na vespera.
Chamar 'F27.12 aprovar a amamentacao da manha' PATCH ($rotaP27 + '/' + $rA1.Dados.id + '/aprovar') @{ aprovadoPorId=$colabA; observacoesDecisao='deferido' } 200 | Out-Null
$rTerm = Chamar 'F27.13 terminar antes do fim' PATCH ($rotaP27 + '/' + $rA1.Dados.id + '/terminar') @{ data=(Iso $ini27.AddDays(10)); motivo='deixou de amamentar' } 200
Verificar 'F27.14 continua aprovado e acaba na vespera' (($rTerm.Dados.estado -eq 'APROVADO') -and ($rTerm.Dados.suspensoEm -eq (Iso $ini27.AddDays(10)))) ('(' + $rTerm.Dados.estado + ' ' + $rTerm.Dados.suspensoEm + ')')
Chamar 'F27.15 NEG terminar outra vez' PATCH ($rotaP27 + '/' + $rA1.Dados.id + '/terminar') @{ data=(Iso $ini27.AddDays(20)); motivo='de novo' } 409 | Out-Null

# No apuramento: a terca do F25 foi das 08:00 as 17:00 contra o base normal (ate as 17:30) -- 30 min
# em falta no fim do dia. Um tratamento ambulatorio aprovado das 17:00 as 17:30 cobre-os.
$rTr = Chamar 'F27.16 tratamento ambulatorio de meia hora na terca do F25' POST $rotaP27 (Horas27 $tTrat $ter25 $ter25 '17:00' '17:30') 201
Chamar 'F27.17 aprovar' PATCH ($rotaP27 + '/' + $rTr.Dados.id + '/aprovar') @{ aprovadoPorId=$colabA; observacoesDecisao='comprovado' } 200 | Out-Null
$f27 = (Chamar 'F27.18 apuramento depois de justificar a meia hora' GET $rotaF).Dados
$t27 = DiaF $f27 $ter25
Verificar 'F27.19 a terca ficou sem falta, com 30 min justificados' (($t27.estado -eq 'SEM_FALTA') -and ($t27.minutosJustificados -eq 30)) ('(' + $t27.estado + ' ' + $t27.minutosJustificados + ')')

Write-Host ''
Write-Host '=========== F28 - PEDIDO DE AUSENCIA PELO PROPRIO (/me) COM AS REGRAS DO RH ==========='

# Antes, o /me contava dias de calendario e nao via contagem, feriados nem tectos. Agora delega no
# caminho do RH. O luto conta dias seguidos entre o primeiro e o ultimo dia util (V56).
$s28 = PrimeiraSegunda ($anoFer + 1) 3
$rMe1 = Chamar 'F28.1 luto pedido pelo proprio, de sexta a segunda' POST '/me/leave-requests' @{ leaveTypeId=$tLuto21.id; startDate=(Iso $s28.AddDays(4)); endDate=(Iso $s28.AddDays(7)); notes='luto' } 201 $colabB
$pedMe = (@(Linhas (Chamar 'F28.2 ler os pedidos da Maria' GET ('/funcionarios/' + $colabB + '/pedidos-ausencia'))) | Where-Object { $_.id -eq $rMe1.Dados.id } | Select-Object -First 1)
Verificar 'F28.3 contou como o RH: 4 dias seguidos' ($pedMe.numeroDias -eq 4) ('(' + $pedMe.numeroDias + ')')
Chamar 'F28.4 NEG seminario de 7 dias pelo proprio passa do tecto' POST '/me/leave-requests' @{ leaveTypeId=$tSem21.id; startDate=(Iso $s28.AddDays(10)); endDate=(Iso $s28.AddDays(16)); notes='x' } 422 $colabB | Out-Null
Chamar 'F28.5 NEG sobreposicao com o proprio pedido' POST '/me/leave-requests' @{ leaveTypeId=$tLuto21.id; startDate=(Iso $s28.AddDays(7)); endDate=(Iso $s28.AddDays(7)); notes='x' } 409 $colabB | Out-Null
$rMe2 = Chamar 'F28.6 amamentacao em horas pelo proprio' POST '/me/leave-requests' @{ leaveTypeId=$tAmam.id; startDate=(Iso $s28.AddDays(21)); endDate=(Iso $s28.AddDays(60)); startTime='12:00'; endTime='13:00'; notes='amamentacao' } 201 $colabB
$pedMe2 = (@(Linhas (Chamar 'F28.7 ler outra vez' GET ('/funcionarios/' + $colabB + '/pedidos-ausencia'))) | Where-Object { $_.id -eq $rMe2.Dados.id } | Select-Object -First 1)
Verificar 'F28.8 ficou em horas, 60 min por dia' (($pedMe2.minutosPorDia -eq 60) -and ($pedMe2.horaInicio -like '12:00*')) ('(' + $pedMe2.minutosPorDia + ' ' + $pedMe2.horaInicio + ')')

Write-Host ''
Write-Host '=========== F29 - REGISTO PELO PROPRIO E VALIDACAO (Lei 20/X/2023, art. 170.o) ==========='

# Picagem em tempo real pelo proprio: so em teletrabalho ou misto. Hoje a Maria e presencial (o base).
Chamar 'F29.1 NEG a Maria pica pelo /me num dia presencial' POST '/me/marcacoes' @{ sentido='ENTRADA' } 422 $colabB | Out-Null
# Um dos admitidos do F11 (activo) passa a teletrabalho a partir de hoje, com o horario flexivel do F24.
# O Francisco nao serve: no fim do F19 esta inactivo, e o /me recusa-o (403) -- tambem se prova.
Chamar 'F29.2 NEG colaborador inactivo nao pica' POST '/me/marcacoes' @{ sentido='ENTRADA' } 403 $colabA | Out-Null
Chamar 'F29.3 atribuir teletrabalho a um admitido do F11 a partir de hoje' POST ('/funcionarios/' + $colabFA + '/horarios') @{ horarioId=$hFlex; regimePrestacao='TELETRABALHO'; dataInicio=$hoje24 } 201 | Out-Null
$rPic = Chamar 'F29.3b ele pica pelo /me em teletrabalho' POST '/me/marcacoes' @{ sentido='ENTRADA' } 201 $colabFA

# Pedido de correcao: fica PENDENTE e nao conta ate ser validado.
$ontem29 = (Get-Date).Date.AddDays(-1)
$rotaA29 = '/funcionarios/' + $colabB + '/assiduidade?de=' + (Iso $ontem29) + '&ate=' + (Iso $ontem29)
$rCor = Chamar 'F29.4 a Maria pede correcao de uma entrada de ontem' POST '/me/marcacoes/correcoes' @{ momento=((Iso $ontem29) + 'T08:00'); sentido='ENTRADA'; motivo='esqueci-me de picar' } 201 $colabB
Chamar 'F29.5 NEG correcao sem motivo' POST '/me/marcacoes/correcoes' @{ momento=((Iso $ontem29) + 'T08:05'); sentido='ENTRADA' } 422 $colabB | Out-Null
$d29 = @((Chamar 'F29.6 ler o dia de ontem' GET $rotaA29).Dados.dias)[0]
$m29 = (@($d29.marcacoes) | Where-Object { $_.id -eq $rCor.Dados.id } | Select-Object -First 1)
Verificar 'F29.7 a correcao esta PENDENTE e nao conta' (($m29.estado -eq 'PENDENTE') -and ($d29.minutosTrabalhados -eq 0) -and (@($d29.anomalias).Count -eq 0)) ('(' + $m29.estado + ')')

# Quem decide: a chefia directa (pelo /me/equipa) ou o RH. O Francisco nao e chefia da Maria.
Chamar 'F29.8 NEG quem nao e chefia directa nao valida' PATCH ('/me/equipa/marcacoes/' + $rCor.Dados.id + '/validar') $null 403 $colabFA | Out-Null
Chamar 'F29.9 NEG ninguem valida as proprias' PATCH ('/me/equipa/marcacoes/' + $rCor.Dados.id + '/validar') $null 422 $colabB | Out-Null
$rotaDec = '/funcionarios/' + $colabB + '/marcacoes/' + $rCor.Dados.id
Chamar 'F29.10 NEG o RH rejeita sem motivo' PATCH ($rotaDec + '/rejeitar') @{ motivo='' } 422 | Out-Null
Chamar 'F29.11 o RH valida' PATCH ($rotaDec + '/validar') $null 200 | Out-Null
Chamar 'F29.12 NEG decidir outra vez' PATCH ($rotaDec + '/validar') $null 409 | Out-Null
$d29b = @((Chamar 'F29.13 ler o dia depois de validar' GET $rotaA29).Dados.dias)[0]
Verificar 'F29.14 validada, conta: a entrada sem saida e anomalia' (((@($d29b.marcacoes) | Where-Object { $_.id -eq $rCor.Dados.id }).estado -eq 'VALIDA') -and (@($d29b.anomalias) -contains 'ENTRADA_SEM_SAIDA')) ''

$rCor2 = Chamar 'F29.15 outra correcao: a saida de ontem' POST '/me/marcacoes/correcoes' @{ momento=((Iso $ontem29) + 'T17:00'); sentido='SAIDA'; motivo='esqueci-me outra vez' } 201 $colabB
Chamar 'F29.16 o RH rejeita, com motivo' PATCH ('/funcionarios/' + $colabB + '/marcacoes/' + $rCor2.Dados.id + '/rejeitar') @{ motivo='sem prova' } 200 | Out-Null
$d29c = @((Chamar 'F29.17 ler o dia depois de rejeitar' GET $rotaA29).Dados.dias)[0]
$m29c = (@($d29c.marcacoes) | Where-Object { $_.id -eq $rCor2.Dados.id } | Select-Object -First 1)
Verificar 'F29.18 a rejeitada fica visivel e nao conta' (($m29c.estado -eq 'REJEITADA') -and ($m29c.motivoRejeicao -eq 'sem prova') -and ($d29c.minutosTrabalhados -eq 0)) ('(' + $m29c.estado + ')')
$rPend = Chamar 'F29.19 a caixa de pendentes da Maria (nao chefia ninguem)' GET '/me/equipa/marcacoes-pendentes' $null 200 $colabB
Verificar 'F29.20 vazia' (@(Linhas $rPend).Count -eq 0) ''
Chamar 'F29.21 a Maria le a sua assiduidade pelo /me' GET ('/me/assiduidade?de=' + (Iso $ontem29) + '&ate=' + (Iso $ontem29)) $null 200 $colabB | Out-Null

Write-Host ''
Write-Host '=========== F30 - TRABALHO SUPLEMENTAR (Lei 20/X/2023, art. 155.o n.o 2 a)) ==========='

# A terca do F25: o base normal vai ate as 17:30. A Maria voltou das 18:00 as 19:30 (o RH lanca as
# marcacoes, com motivo) e o RH autoriza depois (caso urgente) das 18:00 as 20:00; realizado = 90 min.
$rotaS = '/funcionarios/' + $colabB + '/trabalho-suplementar'
Chamar 'F30.0a marcacao de entrada as 18:00' POST ('/funcionarios/' + $colabB + '/marcacoes') @{ momento=((Iso $ter25) + 'T18:00'); sentido='ENTRADA'; motivo='voltou para o fecho' } 201 | Out-Null
Chamar 'F30.0b marcacao de saida as 19:30' POST ('/funcionarios/' + $colabB + '/marcacoes') @{ momento=((Iso $ter25) + 'T19:30'); sentido='SAIDA'; motivo='voltou para o fecho' } 201 | Out-Null
Chamar 'F30.1 NEG num dia util o intervalo toca no horario' POST $rotaS @{ data=(Iso $ter25); horaInicio='17:00'; horaFim='19:00'; motivo='fecho' } 422 | Out-Null
Chamar 'F30.2 NEG sem motivo' POST $rotaS @{ data=(Iso $ter25); horaInicio='18:00'; horaFim='20:00' } 422 | Out-Null
Chamar 'F30.3 NEG hora mal escrita' POST $rotaS @{ data=(Iso $ter25); horaInicio='18h00'; horaFim='20:00'; motivo='x' } 422 | Out-Null
$rS1 = Chamar 'F30.4 o RH autoriza depois: terca do F25, 18:00-20:00' POST $rotaS @{ data=(Iso $ter25); horaInicio='18:00'; horaFim='20:00'; motivo='fecho de contas urgente' } 201
Chamar 'F30.5 NEG sobreposto ao ja autorizado' POST $rotaS @{ data=(Iso $ter25); horaInicio='19:00'; horaFim='21:00'; motivo='x' } 409 | Out-Null
Chamar 'F30.6 NEG colaborador inactivo' POST ('/funcionarios/' + $colabA + '/trabalho-suplementar') @{ data=(Iso $ter25); horaInicio='18:00'; horaFim='19:00'; motivo='x' } 403 | Out-Null
$m30 = (Chamar 'F30.7 trabalho suplementar do mes do F25' GET ($rotaS + '?mes=' + $mes26)).Dados
$s30 = (@($m30.trabalhos) | Where-Object { $_.id -eq $rS1.Dados.id } | Select-Object -First 1)
Verificar 'F30.8 autorizado depois, dia util, 90 min realizados pelas marcacoes' (($s30.estado -eq 'AUTORIZADO') -and $s30.autorizacaoPosterior -and ($s30.tipoDia -eq 'DIA_UTIL') -and ($s30.minutosAutorizados -eq 120) -and ($s30.minutosRealizados -eq 90)) ('(' + $s30.estado + ' ' + $s30.tipoDia + ' ' + $s30.minutosRealizados + '/' + $s30.minutosAutorizados + ')')
Verificar 'F30.9 o total do mes conta-o no dia util' (($m30.minutosRealizadosDiaUtil -ge 90) -and ($m30.minutosRealizados -ge 90)) ('(' + $m30.minutosRealizadosDiaUtil + ')')
# No apuramento de faltas a presenca das 15:30 as 17:00 passa a suplementar: sai do tempo normal.
$f30 = (Chamar 'F30.10 apuramento do mes com o trabalho suplementar' GET $rotaF).Dados
$t30 = DiaF $f30 $ter25
Verificar 'F30.11 a terca separa tempo normal e suplementar' (($t30.minutosSuplementares -eq 90) -and ($t30.estado -eq 'SEM_FALTA')) ('(' + $t30.minutosTrabalhados + ' normal + ' + $t30.minutosSuplementares + ' suplementar, ' + $t30.estado + ')')

# O proprio pede, para a frente; a chefia directa ou o RH decidem. Num sabado qualquer hora serve.
$sab30 = (Get-Date).Date.AddDays(7)
while ($sab30.DayOfWeek -ne [DayOfWeek]::Saturday) { $sab30 = $sab30.AddDays(1) }
Chamar 'F30.12 NEG o proprio nao pede para um dia passado' POST '/me/trabalho-suplementar' @{ data=(Iso (Get-Date).Date.AddDays(-1)); horaInicio='18:00'; horaFim='19:00'; motivo='x' } 422 $colabB | Out-Null
$rS2 = Chamar 'F30.13 a Maria pede para um sabado' POST '/me/trabalho-suplementar' @{ data=(Iso $sab30); horaInicio='09:00'; horaFim='13:00'; motivo='inventario anual' } 201 $colabB
Chamar 'F30.14 NEG ninguem autoriza o seu proprio' PATCH ('/me/equipa/trabalho-suplementar/' + $rS2.Dados.id + '/autorizar') $null 422 $colabB | Out-Null
Chamar 'F30.15 NEG quem nao e chefia directa nao autoriza' PATCH ('/me/equipa/trabalho-suplementar/' + $rS2.Dados.id + '/autorizar') $null 403 $colabFA | Out-Null
Chamar 'F30.16 NEG quem nao e chefia directa nao lanca' POST '/me/equipa/trabalho-suplementar' @{ funcionarioId=$colabB; data=(Iso $sab30); horaInicio='14:00'; horaFim='15:00'; motivo='x' } 403 $colabFA | Out-Null
$rotaS2 = $rotaS + '/' + $rS2.Dados.id
Chamar 'F30.17 NEG o RH recusa sem motivo' PATCH ($rotaS2 + '/recusar') @{ motivo='' } 422 | Out-Null
Chamar 'F30.18 o RH autoriza' PATCH ($rotaS2 + '/autorizar') $null 200 | Out-Null
Chamar 'F30.19 NEG decidir outra vez' PATCH ($rotaS2 + '/autorizar') $null 409 | Out-Null
$mMe = (Chamar 'F30.20 a Maria le o seu trabalho suplementar pelo /me' GET ('/me/trabalho-suplementar?mes=' + $sab30.ToString('yyyy-MM')) $null 200 $colabB).Dados
$s30b = (@($mMe.trabalhos) | Where-Object { $_.id -eq $rS2.Dados.id } | Select-Object -First 1)
Verificar 'F30.21 autorizado, em dia de descanso, pedido pelo proprio' (($s30b.estado -eq 'AUTORIZADO') -and $s30b.pedidoPeloProprio -and (($s30b.tipoDia -eq 'DESCANSO') -or ($s30b.tipoDia -eq 'FERIADO'))) ('(' + $s30b.estado + ' ' + $s30b.tipoDia + ')')
Chamar 'F30.22 NEG cancelar sem motivo' PATCH ($rotaS2 + '/cancelar') @{ motivo='' } 422 | Out-Null
Chamar 'F30.23 o RH cancela, com motivo' PATCH ($rotaS2 + '/cancelar') @{ motivo='inventario adiado' } 200 | Out-Null
Chamar 'F30.24 NEG cancelar outra vez' PATCH ($rotaS2 + '/cancelar') @{ motivo='de novo' } 409 | Out-Null
$rPendS = Chamar 'F30.25 a caixa de pendentes da Maria (nao chefia ninguem)' GET '/me/equipa/trabalho-suplementar-pendente' $null 200 $colabB
Verificar 'F30.26 vazia' (@(Linhas $rPendS).Count -eq 0) ''
Chamar 'F30.27 NEG mes mal escrito' GET ($rotaS + '?mes=2026/09') $null 422 | Out-Null

Write-Host ''
Write-Host '=========== F31 - RELACAO MENSAL (DL 3/2010, art. 75.o) ==========='

# O mes da semana do F25, pedido ao ministerio com as subunidades: a Maria esta no SERV_RH, dois niveis
# abaixo. Esse mes tem o que os blocos anteriores deixaram: a segunda com anomalia (por corrigir), a
# meia hora de tratamento ambulatorio (F27) e os 90 min de trabalho suplementar (F30).
$uMin = '31e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e301'
$rotaR = '/assiduidade/relacao-mensal?mes=' + $mes26 + '&unidadeId=' + $uMin
$r31 = (Chamar 'F31.1 relacao mensal do ministerio, com subunidades' GET $rotaR).Dados
$linhas31 = @($r31.unidades | ForEach-Object { $_.linhas } | Where-Object { $_.funcionarioId -eq $colabB })
Verificar 'F31.2 a Maria aparece uma vez' ($linhas31.Count -eq 1) ('(' + $linhas31.Count + ')')
$l31 = $linhas31[0]
$u31 = (@($r31.unidades) | Where-Object { @($_.linhas | Where-Object { $_.funcionarioId -eq $colabB }).Count -gt 0 } | Select-Object -First 1)
Verificar 'F31.3 na unidade dela (SERV_RH)' ($u31.codigo -eq 'SERV_RH') ('(' + $u31.codigo + ')')
Verificar 'F31.4 o trabalho suplementar do F30 esta la' ($l31.minutosSuplementarDiaUtil -ge 90) ('(' + $l31.minutosSuplementarDiaUtil + ' min)')
$trat31 = (@($l31.faltasJustificadas) | Where-Object { $_.codigo -eq 'TRATAMENTO_AMBULATORIO' } | Select-Object -First 1)
Verificar 'F31.5 a meia hora de tratamento ambulatorio, em minutos' ($trat31.minutos -eq 30) ('(' + $trat31.minutos + ')')
Verificar 'F31.6 a segunda por corrigir deixa a linha com pendencias' (($l31.estado -eq 'COM_PENDENCIAS') -and ($l31.diasPorCorrigir -ge 1)) ('(' + $l31.estado + ', por corrigir=' + $l31.diasPorCorrigir + ')')
Verificar 'F31.7 um mes passado nao e provisorio' (-not $r31.provisoria) ''

$r31b = (Chamar 'F31.8 so o ministerio, sem subunidades' GET ($rotaR + '&incluirSubunidades=false')).Dados
Verificar 'F31.9 sem subunidades a Maria nao entra' ((@($r31b.unidades).Count -eq 1) -and (@($r31b.unidades | ForEach-Object { $_.linhas } | Where-Object { $_.funcionarioId -eq $colabB }).Count -eq 0)) ''
$r31c = (Chamar 'F31.10 o mes corrente' GET ('/assiduidade/relacao-mensal?mes=' + (Get-Date).ToString('yyyy-MM') + '&unidadeId=' + $uMin)).Dados
Verificar 'F31.11 o mes corrente e provisorio' ($r31c.provisoria) ''

# CSV: o mesmo, para a folha de calculo.
try {
    $csv = Invoke-WebRequest -UseBasicParsing -TimeoutSec 90 -Uri ($base + '/assiduidade/relacao-mensal.csv?mes=' + $mes26 + '&unidadeId=' + $uMin)
    # O PowerShell 5.1 entrega text/csv ja como texto; outras versoes, como bytes.
    if ($csv.Content -is [byte[]]) { $csvTexto = [System.Text.Encoding]::UTF8.GetString($csv.Content) } else { $csvTexto = [string]$csv.Content }
    $csvTipo = $csv.Headers['Content-Type']
    $csvNome = $csv.Headers['Content-Disposition']
} catch { $csvTexto = ''; $csvTipo = ''; $csvNome = '' }
Verificar 'F31.12 CSV: text/csv, como anexo' (($csvTipo -like 'text/csv*') -and ($csvNome -like '*relacao-mensal-*')) ('(' + $csvTipo + ')')
$csvLinhas = @($csvTexto -split "`r`n" | Where-Object { $_ })
Verificar 'F31.13 CSV: cabecalho e a linha da Maria' (($csvLinhas[0] -like '*mes;provisoria;unidade_codigo*') -and (@($csvLinhas | Where-Object { $_ -like '*;0000002;*' }).Count -eq 1)) ('(' + $csvLinhas.Count + ' linhas)')

Chamar 'F31.14 NEG mes futuro' GET ('/assiduidade/relacao-mensal?mes=' + (Get-Date).AddMonths(2).ToString('yyyy-MM') + '&unidadeId=' + $uMin) $null 422 | Out-Null
Chamar 'F31.15 NEG mes mal escrito' GET ('/assiduidade/relacao-mensal?mes=08-2026&unidadeId=' + $uMin) $null 422 | Out-Null
Chamar 'F31.16 NEG unidade vazia' GET ('/assiduidade/relacao-mensal?mes=' + $mes26 + '&unidadeId=') $null 422 | Out-Null
Chamar 'F31.17 NEG unidade mal escrita' GET ('/assiduidade/relacao-mensal?mes=' + $mes26 + '&unidadeId=abc') $null 422 | Out-Null
Chamar 'F31.18 NEG unidade que nao existe' GET ('/assiduidade/relacao-mensal?mes=' + $mes26 + '&unidadeId=' + [guid]::NewGuid()) $null 404 | Out-Null

Write-Host ''
Write-Host '=========== F32 - PEDIDOS DE AUSENCIA: APROVACAO AUTOMATICA E DECISAO DA CHEFIA ==========='

# Os tipos que o catalogo diz que nao requerem aprovacao (o direito do art. 15.o: luto, casamento,
# doenca...) nascem APROVADOS, sem decisor. Os outros nascem PENDENTES e decide a chefia directa (pela
# caixa /me/equipa) ou o RH. A decisao pela chefia fica nos testes unitarios (o seed nao tem Lugar-pai).
$s32 = PrimeiraSegunda ($anoFer + 2) 5
$rotaP32 = '/funcionarios/' + $colabB + '/pedidos-ausencia'
$rLuto32 = Chamar 'F32.1 luto de segunda a terca (nao requer aprovacao)' POST $rotaP32 @{ tipoAusenciaId=$tLuto21.id; dataInicio=(Iso $s32); dataFim=(Iso $s32.AddDays(1)); motivo='falecimento' } 201
Verificar 'F32.2 nasceu APROVADO' ($rLuto32.Dados.estado -eq 'APROVADO') ('(' + $rLuto32.Dados.estado + ')')
$pLuto32 = (@(Linhas (Chamar 'F32.3 ler os pedidos da Maria' GET $rotaP32)) | Where-Object { $_.id -eq $rLuto32.Dados.id } | Select-Object -First 1)
Verificar 'F32.4 aprovacao automatica, sem decisor' ($pLuto32.aprovacaoAutomatica -and (-not $pLuto32.aprovadoPor)) ('(' + $pLuto32.aprovacaoAutomatica + ')')
Chamar 'F32.5 NEG aprovar o que ja nasceu aprovado' PATCH ($rotaP32 + '/' + $rLuto32.Dados.id + '/aprovar') @{ aprovadoPorId=$colabA; observacoesDecisao='x' } 409 | Out-Null

$rSem32 = Chamar 'F32.6 seminario de dois dias (requer aprovacao)' POST $rotaP32 @{ tipoAusenciaId=$tSem21.id; dataInicio=(Iso $s32.AddDays(7)); dataFim=(Iso $s32.AddDays(8)); motivo='conferencia' } 201
Verificar 'F32.7 nasceu PENDENTE' ($rSem32.Dados.estado -eq 'PENDENTE') ('(' + $rSem32.Dados.estado + ')')
$rotaEq = '/me/equipa/pedidos-ausencia/' + $rSem32.Dados.id
Chamar 'F32.8 NEG ninguem decide os seus proprios pedidos' PATCH ($rotaEq + '/aprovar') $null 422 $colabB | Out-Null
Chamar 'F32.9 NEG quem nao e chefia directa nao aprova' PATCH ($rotaEq + '/aprovar') $null 403 $colabFA | Out-Null
Chamar 'F32.10 NEG quem nao e chefia directa nao rejeita' PATCH ($rotaEq + '/rejeitar') @{ motivo='nao' } 403 $colabFA | Out-Null
Chamar 'F32.11 NEG o RH pelo caminho de outro colaborador' PATCH ('/funcionarios/' + $colabFA + '/pedidos-ausencia/' + $rSem32.Dados.id + '/aprovar') @{ aprovadoPorId=$colabA; observacoesDecisao='x' } 404 | Out-Null
Chamar 'F32.12 o RH aprova' PATCH ($rotaP32 + '/' + $rSem32.Dados.id + '/aprovar') @{ aprovadoPorId=$colabA; observacoesDecisao='deferido' } 200 | Out-Null
Chamar 'F32.13 NEG decidir outra vez' PATCH ($rotaP32 + '/' + $rSem32.Dados.id + '/rejeitar') @{ aprovadoPorId=$colabA; observacoesDecisao='tarde' } 409 | Out-Null
$rPend32 = Chamar 'F32.14 a caixa de pedidos da Maria (nao chefia ninguem)' GET '/me/equipa/pedidos-ausencia-pendentes' $null 200 $colabB
Verificar 'F32.15 vazia' (@(Linhas $rPend32).Count -eq 0) ''

Write-Host ''
Write-Host '=========== F33 - HORARIOS COM DATA DE EFEITO ==========='

# Um horario que ja vigorou nao muda de conteudo (so o nome): duplica-se. O horario da unidade e o base
# mudam de hoje ou de uma data futura; os dias passados ficam com o que vigorava.
$rotaHN = '/catalogs/horarios/' + $hNormal
$corpoHN = @{ blocos=(Semana '09:00' '13:00' '14:00' '18:00' $true) }
Chamar 'F33.1 NEG mudar os blocos do base (vigorou)' PUT $rotaHN $corpoHN 409 | Out-Null
$rRen = Chamar 'F33.2 mudar so o nome do base' PUT $rotaHN @{ nome='TST Normal (base)' } 200
Verificar 'F33.3 o nome mudou, os blocos nao' (($rRen.Dados.nome -eq 'TST Normal (base)') -and ($rRen.Dados.horasSemanais -eq '40:00')) ('(' + $rRen.Dados.nome + ')')
$rDup = Chamar 'F33.4 duplicar o base' POST ($rotaHN + '/duplicar') $null 201
Verificar 'F33.5 a copia nao e base e tem os mesmos blocos' ((-not $rDup.Dados.isBase) -and ($rDup.Dados.horasSemanais -eq '40:00') -and ($rDup.Dados.nome -like '*(c*pia)')) ('(' + $rDup.Dados.nome + ')')
$hCopia = $rDup.Dados.id
$rCopia = Chamar 'F33.6 a copia, que nunca vigorou, edita-se' PUT ('/catalogs/horarios/' + $hCopia) $corpoHN 200
Verificar 'F33.7 blocos novos na copia' ($rCopia.Dados.horasSemanais -eq '40:00') ''
Chamar 'F33.8 NEG marcar base numa data passada' PATCH ('/catalogs/horarios/' + $hCopia + '/base?desde=' + (Iso (Get-Date).Date.AddDays(-1))) $null 422 | Out-Null
Chamar 'F33.9 NEG desactivar o base agendado' DELETE ('/catalogs/horarios/' + $hAtend) $null 409 | Out-Null

# Unidade: a copia passa a horario da unidade daqui a 3 dias (a Maria tem horario proprio so daqui a 10).
$corpoU33 = CorpoUnidade (Chamar 'F33.10 ler a unidade' GET ('/estrutura/organizational-units/' + $uniH)).Dados
$corpoU33.horarioId = $hCopia
$corpoU33.horarioDesde = Iso (Get-Date).Date.AddDays(3)
$rU33 = Chamar 'F33.11 horario da unidade daqui a 3 dias' PUT ('/estrutura/organizational-units/' + $uniH) $corpoU33 200
Verificar 'F33.12 hoje a unidade ainda nao tem horario' ($null -eq $rU33.Dados.horarioId) ('(' + $rU33.Dados.horarioId + ')')
$v33a = Vigente (Iso (Get-Date).Date.AddDays(2))
Verificar 'F33.13 antes da data vale o base' ($v33a.origem -eq 'BASE') ('(' + $v33a.origem + ')')
$v33b = Vigente (Iso (Get-Date).Date.AddDays(5))
Verificar 'F33.14 depois da data vale o da unidade' (($v33b.origem -eq 'UNIDADE') -and ($v33b.horario.id -eq $hCopia)) ('(' + $v33b.origem + ')')
$corpoU33.horarioDesde = Iso (Get-Date).Date.AddDays(-1)
Chamar 'F33.15 NEG horario da unidade numa data passada' PUT ('/estrutura/organizational-units/' + $uniH) $corpoU33 422 | Out-Null
$corpoU33.horarioDesde = $null
Chamar 'F33.16 a copia agendada para a unidade ainda nao vigorou: continua editavel' PUT ('/catalogs/horarios/' + $hCopia) @{ blocos=(Semana '08:00' '12:00' '13:00' '17:00' $true) } 200 | Out-Null

Write-Host ''
Write-Host '=========== F34 - LISTA DE ANTIGUIDADE (DL 3/2010, arts. 69.o e 70.o) ==========='

# Com referencia a 31 de Dezembro do ano anterior, por cargo e, em cada cargo, por antiguidade no cargo.
$ano34 = (Get-Date).Year
$rotaL = '/relatorios/lista-antiguidade?ano=' + $ano34 + '&unidadeId=' + $uMin
$l34 = (Chamar 'F34.1 lista de antiguidade do ministerio, com subunidades' GET $rotaL).Dados
Verificar 'F34.2 referencia a 31 de Dezembro do ano anterior' ($l34.referencia -eq (($ano34 - 1).ToString() + '-12-31')) ('(' + $l34.referencia + ')')
$linhas34 = @($l34.grupos | ForEach-Object { $_.linhas } | Where-Object { $_.funcionarioId -eq $colabB })
Verificar 'F34.3 a Maria aparece uma vez' ($linhas34.Count -eq 1) ('(' + $linhas34.Count + ')')
$m34 = $linhas34[0]
Verificar 'F34.4 com data de inicio no cargo, posicao e tempo contado' (($null -ne $m34.dataInicioNoCargo) -and ($m34.posicao -ge 1) -and ($m34.diasContados -gt 0)) ('(' + $m34.dataInicioNoCargo + ', ' + $m34.anos + 'a ' + $m34.meses + 'm ' + $m34.dias + 'd)')
$ok34 = $true
foreach ($g in @($l34.grupos)) { $p = 0; foreach ($x in @($g.linhas)) { if ($x.posicao -ne ($p + 1)) { $ok34 = $false }; $p = $x.posicao } }
Verificar 'F34.5 em cada cargo, posicoes seguidas a partir de 1' $ok34 ''
try {
    $csv34 = Invoke-WebRequest -UseBasicParsing -TimeoutSec 90 -Uri ($base + '/relatorios/lista-antiguidade.csv?ano=' + $ano34 + '&unidadeId=' + $uMin)
    if ($csv34.Content -is [byte[]]) { $t34 = [System.Text.Encoding]::UTF8.GetString($csv34.Content) } else { $t34 = [string]$csv34.Content }
    $tipo34 = $csv34.Headers['Content-Type']
} catch { $t34 = ''; $tipo34 = '' }
$csvL34 = @($t34 -split "`r`n" | Where-Object { $_ })
Verificar 'F34.6 CSV com cabecalho e a linha da Maria' (($tipo34 -like 'text/csv*') -and ($csvL34[0] -like '*ano;referencia;carreira;categoria;posicao*') -and (@($csvL34 | Where-Object { $_ -like '*;0000002;*' }).Count -eq 1)) ('(' + $csvL34.Count + ' linhas)')
Chamar 'F34.7 NEG ano futuro' GET ('/relatorios/lista-antiguidade?ano=' + ($ano34 + 1) + '&unidadeId=' + $uMin) $null 422 | Out-Null
Chamar 'F34.8 NEG unidade mal escrita' GET ('/relatorios/lista-antiguidade?ano=' + $ano34 + '&unidadeId=abc') $null 422 | Out-Null
Chamar 'F34.9 NEG unidade que nao existe' GET ('/relatorios/lista-antiguidade?ano=' + $ano34 + '&unidadeId=' + [guid]::NewGuid()) $null 404 | Out-Null

Write-Host ''
Write-Host '=========== F35 - MAPA DE FERIAS: PREFERENCIA PELO PROPRIO E PEDIDO FORA DA MARCACAO ==========='

# A preferencia (art. 5.o n.o 4) pode ser indicada pelo proprio no /me; a chefia directa ve a da equipa.
$ano35 = $anoMapa + 1
$set35 = PrimeiraSegunda $ano35 9
$rPref35 = Chamar 'F35.1 a Maria indica a preferencia pelo /me' PUT ('/me/ferias/' + $ano35 + '/preferencia') @{ periodos=@(@{ dataInicio=(Iso $set35); dataFim=(Iso $set35.AddDays(18)) }); observacoes='Setembro' } 200 $colabB
$f35 = (Chamar 'F35.2 a Maria le as suas ferias' GET ('/me/ferias/' + $ano35) $null 200 $colabB).Dados
Verificar 'F35.3 a preferencia ficou, indicada pelo proprio' ((@($f35.preferencia).Count -eq 1) -and ($f35.preferenciaIndicadaPor -eq 'PROPRIO')) ('(' + $f35.preferenciaIndicadaPor + ')')
Chamar 'F35.4 NEG colaborador inactivo nao indica' PUT ('/me/ferias/' + $ano35 + '/preferencia') @{ periodos=@(@{ dataInicio=(Iso $set35); dataFim=(Iso $set35.AddDays(18)) }) } 403 $colabA | Out-Null
$eq35 = Chamar 'F35.5 as ferias da equipa da Maria (nao chefia ninguem)' GET ('/me/equipa/ferias/' + $ano35) $null 200 $colabB
Verificar 'F35.6 vazia' (@(Linhas $eq35).Count -eq 0) ''

# Um pedido de ferias fora da marcacao do mapa (ja dado a conhecer no F22) da aviso, sem recusar.
$rotaP35 = '/funcionarios/' + $colabB + '/pedidos-ausencia'
$rDentro = Chamar 'F35.7 pedido de ferias dentro da marcacao' POST $rotaP35 @{ tipoAusenciaId=$tFer27.id; dataInicio=(Iso $setMapa2.AddDays(1)); dataFim=(Iso $setMapa2.AddDays(3)); motivo='ferias' } 201
Verificar 'F35.8 sem aviso' (@($rDentro.Dados.alertas).Count -eq 0) ('(' + (@($rDentro.Dados.alertas) -join ' | ') + ')')
$nov35 = PrimeiraSegunda $anoMapa 11
$rFora = Chamar 'F35.9 pedido de ferias fora da marcacao' POST $rotaP35 @{ tipoAusenciaId=$tFer27.id; dataInicio=(Iso $nov35.AddDays(1)); dataFim=(Iso $nov35.AddDays(3)); motivo='ferias' } 201
Verificar 'F35.10 aceite, com o aviso do art. 6.o n.o 2' ((@($rFora.Dados.alertas) -join ' ') -like '*art. 6*') ('(' + @($rFora.Dados.alertas).Count + ' alerta)')
$rMe35 = Chamar 'F35.11 o mesmo pelo /me' POST '/me/leave-requests' @{ leaveTypeId=$tFer27.id; startDate=(Iso $nov35.AddDays(8)); endDate=(Iso $nov35.AddDays(9)); notes='ferias' } 201 $colabB
Verificar 'F35.12 o /me tambem avisa' ((@($rMe35.Dados.alertas) -join ' ') -like '*art. 6*') ''

Write-Host ''
Write-Host '=========== F36 - MAPA DE EFECTIVOS (Lei 20/X/2023, art. 4.o al. aa)) ==========='

# Por unidade e cargo, os Lugares activos, providos, vagos e congelados (hoje).
$m36 = (Chamar 'F36.1 mapa de efectivos do ministerio, com subunidades' GET ('/relatorios/mapa-efectivos?unidadeId=' + $uMin)).Dados
# A Maria acaba a bateria num Lugar do DGP (os blocos anteriores mudam-na de unidade): e o provido.
$u36 = (@($m36.unidades) | Where-Object { $_.providos -ge 1 } | Select-Object -First 1)
Verificar 'F36.2 ha Lugares, e o da Maria esta provido numa das unidades' (($m36.lugares -ge 1) -and ($null -ne $u36)) ('(' + $u36.codigo + ': lugares=' + $u36.lugares + ' providos=' + $u36.providos + ')')
$soma36 = 0; foreach ($u in @($m36.unidades)) { $soma36 += $u.lugares }
Verificar 'F36.3 os totais somam as unidades; providos + vagos = lugares' (($m36.lugares -eq $soma36) -and (($m36.providos + $m36.vagos) -eq $m36.lugares)) ('(' + $m36.lugares + ' = ' + $m36.providos + ' + ' + $m36.vagos + ')')
try {
    $csv36 = Invoke-WebRequest -UseBasicParsing -TimeoutSec 90 -Uri ($base + '/relatorios/mapa-efectivos.csv?unidadeId=' + $uMin)
    if ($csv36.Content -is [byte[]]) { $t36 = [System.Text.Encoding]::UTF8.GetString($csv36.Content) } else { $t36 = [string]$csv36.Content }
} catch { $t36 = '' }
Verificar 'F36.4 CSV com cabecalho' ((@($t36 -split "`r`n" | Where-Object { $_ })[0]) -like '*data;unidade_codigo;unidade;carreira;categoria*') ''
Chamar 'F36.5 NEG unidade mal escrita' GET '/relatorios/mapa-efectivos?unidadeId=abc' $null 422 | Out-Null
Chamar 'F36.6 NEG unidade que nao existe' GET ('/relatorios/mapa-efectivos?unidadeId=' + [guid]::NewGuid()) $null 404 | Out-Null

Write-Host ''
Write-Host '=========== F37 - INDICADORES DO PESSOAL (Lei 20/X/2023, art. 38.o n.o 3) ==========='

# Os numeros do balanco social de um servico num ano; o grafico e do front.
$i37 = (Chamar 'F37.1 indicadores do ministerio no ano corrente' GET ('/relatorios/indicadores?unidadeId=' + $uMin)).Dados
Verificar 'F37.2 ha efectivos, e as distribuicoes somam o total' (($i37.efectivos -ge 1) -and ((@($i37.porGenero) | Measure-Object -Property valor -Sum).Sum -eq $i37.efectivos) -and ((@($i37.porUnidade) | Measure-Object -Property valor -Sum).Sum -eq $i37.efectivos)) ('(' + $i37.efectivos + ' efectivos)')
Verificar 'F37.3 escaloes etarios, carreira e contrato presentes' ((@($i37.porEscalaoEtario).Count -ge 5) -and (@($i37.porCarreira).Count -ge 1) -and (@($i37.porTipoContrato).Count -ge 1)) ''
Verificar 'F37.4 absentismo e horas extras calculados' (($null -ne $i37.taxaAbsentismo) -and ($i37.diasUteisPotenciais -gt 0) -and ($null -ne $i37.horasSuplementares)) ('(absentismo=' + $i37.taxaAbsentismo + '% horas extras=' + $i37.horasSuplementares + ')')
# Os admitidos do F11 nao tem Lugar (nao estao em nenhum servico): as entradas contam so quem passou pelo servico.
Verificar 'F37.5 entradas e saidas contadas' (($null -ne $i37.entradas) -and ($null -ne $i37.saidas) -and ($i37.entradas -ge 0) -and ($i37.saidas -ge 0)) ('(' + $i37.entradas + ' entradas, ' + $i37.saidas + ' saidas)')
$i37b = (Chamar 'F37.6 um ano passado' GET ('/relatorios/indicadores?unidadeId=' + $uMin + '&ano=' + ((Get-Date).Year - 1))).Dados
Verificar 'F37.7 com referencia a 31 de Dezembro' ($i37b.referencia -eq (((Get-Date).Year - 1).ToString() + '-12-31')) ('(' + $i37b.referencia + ')')
Chamar 'F37.8 NEG ano futuro' GET ('/relatorios/indicadores?unidadeId=' + $uMin + '&ano=' + ((Get-Date).Year + 1)) $null 422 | Out-Null
Chamar 'F37.9 NEG unidade que nao existe' GET ('/relatorios/indicadores?unidadeId=' + [guid]::NewGuid()) $null 404 | Out-Null

Write-Host ''
Write-Host '=========== RESUMO ==========='
$ok = ($script:resultados | Where-Object { $_.OK }).Count
$total = $script:resultados.Count
Write-Host ('PASSOS: ' + $total + '   OK: ' + $ok + '   FALHAS: ' + ($total - $ok))
$script:resultados | Where-Object { -not $_.OK } | ForEach-Object {
    Write-Host ('  FALHA  ' + $_.Nome + '  (esperado ' + $_.Esperado + ', obtido ' + $_.Obtido + ')')
}
