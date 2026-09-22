# Bateria de testes funcionais - RH-Service (so ASCII, para o PowerShell 5.1 nao partir o ficheiro)
# Percurso de cliente: navega (GET), le, e so depois age. Cobre positivo e negativo.

$ErrorActionPreference = 'Continue'
$base = 'http://localhost:8099/api/v1/rh'
$script:resultados = @()

function Chamar {
    param([string]$Nome, [string]$Metodo, [string]$Rota, $Corpo = $null, [int]$Esperado = 200)
    # Accept explicito: sem ele o servidor negoceia e devolve os erros em XML
    # (ProblemDetail), enquanto os sucessos vem em JSON. Um cliente que esqueca o
    # cabecalho fica com dois formatos na mesma API -- e o api_guide avisa disso.
    $params = @{ Method = $Metodo; Uri = "$base$Rota"; UseBasicParsing = $true; TimeoutSec = 90
                 Headers = @{ Accept = 'application/json' } }
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
Chamar 'F0.3 unidade atual de A' GET ('/colaboradores/assignments/funcionario/' + $colabA + '/unidade-atual') | Out-Null
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
$lugarLivre = ($vagos | Where-Object { $_.foraDeGrelha -ne $true -and $_.estado -eq 'ATIVO' } | Select-Object -First 1)
$rGradesL = Chamar 'F6.8 escaloes da categoria do Lugar vago' GET ('/categories/' + $lugarLivre.categoryId + '/grades')
$escalaoL = (@(Linhas $rGradesL) | Where-Object { $_.isActive -ne $false } | Select-Object -First 1).id
Chamar 'F6.9 reafectar A a um Lugar vago' POST '/colaboradores/assignments' @{ funcionarioId=$colabA; positionId=$lugarLivre.id; gradeId=$escalaoL; origem='ADMISSAO'; dataInicio='2026-11-01' } 201 | Out-Null

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
$dFimExt  = $hoje.AddMonths(2).ToString('yyyy-MM-dd')

Chamar 'F8.1 B regressa a actividade' PATCH ('/funcionarios/' + $colabB + '/worker-state') @{ workerStateId=$ws['ACTIVE'].id; dataEfectividade=$dOntem; motivoCkey='VAGA_DISPONIVEL' } 200 | Out-Null

$rUnidades = Chamar 'F8.2 unidades organicas' GET '/estrutura/organizational-units?pagina=0&tamanho=20'
$unidades = @(Linhas $rUnidades)
Verificar 'F8.3 ha pelo menos duas unidades' ($unidades.Count -ge 2) ('(n=' + $unidades.Count + ')')

$rVagas8 = Chamar 'F8.4 Lugares vagos' GET ('/colaboradores/assignments/unidade/' + $unidade7 + '/vagas/lista')
$lugar8 = (@(Linhas $rVagas8) | Where-Object { $_.foraDeGrelha -ne $true -and $_.estado -eq 'ATIVO' } | Select-Object -First 1)
$rGrades8 = Chamar 'F8.5 escaloes da categoria' GET ('/categories/' + $lugar8.categoryId + '/grades')
$escalao8 = (@(Linhas $rGrades8) | Where-Object { $_.isActive -ne $false } | Select-Object -First 1).id
Chamar 'F8.6 afectar B ao Lugar vago' POST '/colaboradores/assignments' @{ funcionarioId=$colabB; positionId=$lugar8.id; gradeId=$escalao8; origem='ADMISSAO'; dataInicio=$dOntem } 201 | Out-Null

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

$rMob = Chamar 'F8.10 criar mobilidade interna' POST ('/funcionarios/' + $colabB + '/licencas-mobilidade') @{ subtipoId=$subMob.id; dataInicio=$dInicio; dataFim=$dFim; destinationUnitId=$destino.id; justification='requisicao' } 201
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
# Contexto herdado: no fim do F8 so o B esta activo, e esta num Lugar TEC_SUP --
# que e a categoria de topo. Nao ha para onde promover. Comeca-se por o colocar
# na categoria de baixo, que e de onde se promove.

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
Chamar 'F9.6 colocar B na categoria de baixo' POST '/colaboradores/assignments' @{ funcionarioId=$colabB; positionId=$lugarBaixo.id; gradeId=$escBaixo; origem='ADMISSAO'; dataInicio=$dAfect9 } 201 | Out-Null

# --- negativos, antes de gastar o cenario ---
Chamar 'F9.7 NEG promover para a mesma categoria' POST ('/funcionarios/' + $colabB + '/promocao') @{ categoryId=$catBaixo.id; dataEfeito=$dPromo } 422 | Out-Null
Chamar 'F9.8 NEG promover para Lugar de outra categoria' POST ('/funcionarios/' + $colabB + '/promocao') @{ categoryId=$catCima.id; positionId=$lugarBaixo.id; dataEfeito=$dPromo } 422 | Out-Null
Chamar 'F9.9 NEG data de efeito anterior a afectacao' POST ('/funcionarios/' + $colabB + '/promocao') @{ categoryId=$catCima.id; dataEfeito=$hoje.AddDays(-60).ToString('yyyy-MM-dd') } 422 | Out-Null
Chamar 'F9.10 NEG categoria inexistente' POST ('/funcionarios/' + $colabB + '/promocao') @{ categoryId='00000000-0000-4000-8000-000000000999'; dataEfeito=$dPromo } 404 | Out-Null

# --- forma 1: muda de Lugar ---
$rProm1 = Chamar 'F9.11 promover COM positionId (muda de Lugar)' POST ('/funcionarios/' + $colabB + '/promocao') @{ categoryId=$catCima.id; positionId=$lugarCima.id; dataEfeito=$dPromo; despachoNumero='DESP-2026/90'; concursoRef='CI-2026/3' } 201
Verificar 'F9.12 a aplicacao deduziu "mudanca de Lugar"' ($rProm1.Dados.lugarReclassificado -eq $false) ''
Verificar 'F9.13 subiu da categoria de baixo para a de cima' (($rProm1.Dados.categoriaAnteriorId -eq $catBaixo.id) -and ($rProm1.Dados.categoriaNovaId -eq $catCima.id)) ('(' + $rProm1.Dados.categoriaAnterior + ' -> ' + $rProm1.Dados.categoriaNova + ')')

$rUni9 = Chamar 'F9.14 onde esta o B depois da promocao' GET ('/colaboradores/assignments/funcionario/' + $colabB + '/unidade-atual')
Verificar 'F9.15 esta no Lugar de destino' ($rUni9.Dados.positionId -eq $lugarCima.id) ('(' + $rUni9.Dados.numeroLugar + ')')
$rVagas9b = Chamar 'F9.16 vagas depois' GET ('/colaboradores/assignments/unidade/' + $unidadeB + '/vagas/lista')
Verificar 'F9.17 o Lugar que deixou ficou vago' (@(@(Linhas $rVagas9b) | Where-Object { $_.id -eq $lugarBaixo.id }).Count -eq 1) ''

# --- forma 2: o Lugar e que sobe ---
Chamar 'F9.18 voltar a colocar B na categoria de baixo' POST '/colaboradores/assignments' @{ funcionarioId=$colabB; positionId=$lugarBaixo.id; gradeId=$escBaixo; origem='ADMISSAO'; dataInicio=$dAfect9 } 201 | Out-Null
$rProm2 = Chamar 'F9.19 promover SEM positionId (o Lugar sobe)' POST ('/funcionarios/' + $colabB + '/promocao') @{ categoryId=$catCima.id; dataEfeito=$dPromo; despachoNumero='DESP-2026/91' } 201
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
Verificar 'F11.3 os restantes sao FALTA' ((@(Linhas $rTipos) | Where-Object { $_.regime -ne 'FERIAS' -and $_.regime -ne 'FALTA' }).Count -eq 0) ''

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
Chamar 'F16.6 colocar B no Lugar de partida' POST '/colaboradores/assignments' @{ funcionarioId=$colabB; positionId=$lugarOrigem16.id; gradeId=$esc16; origem='ADMISSAO'; dataInicio=$dAfect16 } 201 | Out-Null

$dIni16 = $hoje.AddDays(-20).ToString('yyyy-MM-dd')
$dFim16 = $hoje.AddMonths(6).ToString('yyyy-MM-dd')
$dCons16 = $hoje.ToString('yyyy-MM-dd')
$rMob16 = Chamar 'F16.7 mobilidade para a unidade de destino' POST ('/funcionarios/' + $colabB + '/licencas-mobilidade') @{ subtipoId=$subMob.id; dataInicio=$dIni16; dataFim=$dFim16; destinationUnitId=$unidadeDestino16; justification='mobilidade a consolidar' } 201
$mob16 = $rMob16.Dados.id
Chamar 'F16.8 aprovar a mobilidade' PUT ('/funcionarios/' + $colabB + '/licencas-mobilidade/' + $mob16 + '/approve') $null 200 | Out-Null

# --- negativos, antes de gastar o cenario ---
Chamar 'F16.9 NEG consolidar sem Lugar de destino' POST ('/funcionarios/' + $colabB + '/licencas-mobilidade/' + $mob16 + '/consolidar') @{ dataEfeito=$dCons16 } 400 | Out-Null
Chamar 'F16.10 NEG Lugar de destino inexistente' POST ('/funcionarios/' + $colabB + '/licencas-mobilidade/' + $mob16 + '/consolidar') @{ positionId='00000000-0000-4000-8000-000000000999'; dataEfeito=$dCons16 } 404 | Out-Null
# O Lugar tem de ser do servico onde se esteve em mobilidade: e esse exercicio que se torna definitivo.
$lugarOutraUnidade16 = ($vagasPorUnidade16 | Where-Object { $_.unidadeOrganicaId -ne $unidadeDestino16 -and $_.id -ne $lugarOrigem16.id } | Select-Object -First 1)
if ($null -ne $lugarOutraUnidade16) {
    Chamar 'F16.11 NEG Lugar que nao e da unidade de destino' POST ('/funcionarios/' + $colabB + '/licencas-mobilidade/' + $mob16 + '/consolidar') @{ positionId=$lugarOutraUnidade16.id; dataEfeito=$dCons16 } 422 | Out-Null
}
Chamar 'F16.12 NEG mobilidade de outro colaborador pelo URL deste' POST ('/funcionarios/' + $colabFA + '/licencas-mobilidade/' + $mob16 + '/consolidar') @{ positionId=$lugarDestino16.id; dataEfeito=$dCons16 } 404 | Out-Null

# --- o movimento ---
$rCons16 = Chamar 'F16.13 consolidar a mobilidade' POST ('/funcionarios/' + $colabB + '/licencas-mobilidade/' + $mob16 + '/consolidar') @{ positionId=$lugarDestino16.id; dataEfeito=$dCons16; despachoNumero='DESP-2026/93' } 201
Verificar 'F16.14 saiu do Lugar de origem para o de destino' (($rCons16.Dados.positionAnteriorId -eq $lugarOrigem16.id) -and ($rCons16.Dados.positionId -eq $lugarDestino16.id)) ('(' + $rCons16.Dados.numeroLugarAnterior + ' -> ' + $rCons16.Dados.numeroLugar + ')')
Verificar 'F16.15 e mudou de unidade organica' ($rCons16.Dados.unidadeOrganicaId -eq $unidadeDestino16) ''
Verificar 'F16.16 o ultimo dia em mobilidade e a vespera' ($rCons16.Dados.mobilidadeDataFim -like ($hoje.AddDays(-1).ToString('yyyy-MM-dd') + '*')) ('(' + $rCons16.Dados.mobilidadeDataFim + ')')

$rUni16 = Chamar 'F16.17 onde esta o B depois' GET ('/colaboradores/assignments/funcionario/' + $colabB + '/unidade-atual')
Verificar 'F16.18 e titular do Lugar de destino' ($rUni16.Dados.positionId -eq $lugarDestino16.id) ('(' + $rUni16.Dados.numeroLugar + ')')
$rVagasDest16 = Chamar 'F16.19 vagas da unidade de destino depois' GET ('/colaboradores/assignments/unidade/' + $unidadeDestino16 + '/vagas/lista')
Verificar 'F16.19b o Lugar de destino deixou de estar vago' (@(@(Linhas $rVagasDest16) | Where-Object { $_.id -eq $lugarDestino16.id }).Count -eq 0) ''

$rMobLida16 = Chamar 'F16.20 ler a mobilidade consolidada' GET ('/funcionarios/' + $colabB + '/licencas-mobilidade/' + $mob16)
Verificar 'F16.21 o despacho nao se desfez -- continua APPROVED' ($rMobLida16.Dados.status -eq 'APPROVED') ('(' + $rMobLida16.Dados.status + ')')
Verificar 'F16.22 e o periodo esta TERMINADA' ($rMobLida16.Dados.estadoPeriodo -eq 'TERMINADA') ('(' + $rMobLida16.Dados.estadoPeriodo + ')')

$rVagas16b = Chamar 'F16.23 vagas da unidade de origem depois' GET ('/colaboradores/assignments/unidade/' + $unidadeOrigem16 + '/vagas/lista')
Verificar 'F16.24 o Lugar que deixou ficou vago' (@(@(Linhas $rVagas16b) | Where-Object { $_.id -eq $lugarOrigem16.id }).Count -eq 1) ''

# Consolidada uma vez, o periodo transitorio acabou: nao ha segundo a consolidar.
Chamar 'F16.25 NEG consolidar duas vezes' POST ('/funcionarios/' + $colabB + '/licencas-mobilidade/' + $mob16 + '/consolidar') @{ positionId=$lugarOrigem16.id; dataEfeito=$dCons16 } 409 | Out-Null

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
Write-Host '=========== RESUMO ==========='
$ok = ($script:resultados | Where-Object { $_.OK }).Count
$total = $script:resultados.Count
Write-Host ('PASSOS: ' + $total + '   OK: ' + $ok + '   FALHAS: ' + ($total - $ok))
$script:resultados | Where-Object { -not $_.OK } | ForEach-Object {
    Write-Host ('  FALHA  ' + $_.Nome + '  (esperado ' + $_.Esperado + ', obtido ' + $_.Obtido + ')')
}
