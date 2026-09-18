# Bateria de testes funcionais - RH-Service (so ASCII, para o PowerShell 5.1 nao partir o ficheiro)
# Percurso de cliente: navega (GET), le, e so depois age. Cobre positivo e negativo.

$ErrorActionPreference = 'Continue'
$base = 'http://localhost:8099/api/v1/rh'
$script:resultados = @()

function Chamar {
    param([string]$Nome, [string]$Metodo, [string]$Rota, $Corpo = $null, [int]$Esperado = 200)
    $params = @{ Method = $Metodo; Uri = "$base$Rota"; UseBasicParsing = $true; TimeoutSec = 90 }
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
$colabA = $colabs[0].id
$colabB = $colabs[1].id
Write-Host ('      A=' + $colabA + '  B=' + $colabB)

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

$idForm = $st['LIC_FORMACAO'].id
$rL1 = Chamar 'F3.8 criar licenca de formacao de 180 dias (B)' POST ('/funcionarios/' + $colabB + '/licencas-mobilidade') @{ subtipoId=$idForm; dataInicio='2026-10-01'; dataFim='2027-03-29'; despachoNumero='DESP/1'; justification='curta' } 201
$lic1 = $rL1.Dados.id
$rAp1 = Chamar 'F3.9 aprovar licenca de 180 dias' PUT ('/funcionarios/' + $colabB + '/licencas-mobilidade/' + $lic1 + '/approve') $null 200
Verificar 'F3.10 nao abriu vaga (no limite do prazo)' ($null -eq $rAp1.Dados.afectacaoEncerradaId)
Chamar 'F3.11 NEG aprovar duas vezes' PUT ('/funcionarios/' + $colabB + '/licencas-mobilidade/' + $lic1 + '/approve') $null 409 | Out-Null
Chamar 'F3.12 encerrar a licenca de 180 dias' PUT ('/funcionarios/' + $colabB + '/licencas-mobilidade/' + $lic1 + '/close') $null 200 | Out-Null

$rL2 = Chamar 'F3.13 criar licenca de formacao de 200 dias (B)' POST ('/funcionarios/' + $colabB + '/licencas-mobilidade') @{ subtipoId=$idForm; dataInicio='2027-04-01'; dataFim='2027-10-17'; despachoNumero='DESP/2'; justification='longa' } 201
$lic2 = $rL2.Dados.id
$rAp2 = Chamar 'F3.14 aprovar licenca de 200 dias' PUT ('/funcionarios/' + $colabB + '/licencas-mobilidade/' + $lic2 + '/approve') $null 200
Verificar 'F3.15 abriu vaga' ($null -ne $rAp2.Dados.afectacaoEncerradaId) ('(' + $rAp2.Dados.afectacaoEncerradaId + ')')

$rB = Chamar 'F3.16 estado de B depois da licenca' GET ('/funcionarios/' + $colabB)
Write-Host ('      estado de B: ' + $rB.Dados.workerStateName + ' activo=' + $rB.Dados.isActive)

$rFecha = Chamar 'F3.17 encerrar a licenca (regresso)' PUT ('/funcionarios/' + $colabB + '/licencas-mobilidade/' + $lic2 + '/close') $null 200
Verificar 'F3.18 regresso pela disponibilidade' ($null -ne $rFecha.Dados.estadoAtribuidoId) ('(' + $rFecha.Dados.estadoAtribuidoId + ')')

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
Write-Host '=========== RESUMO ==========='
$ok = ($script:resultados | Where-Object { $_.OK }).Count
$total = $script:resultados.Count
Write-Host ('PASSOS: ' + $total + '   OK: ' + $ok + '   FALHAS: ' + ($total - $ok))
$script:resultados | Where-Object { -not $_.OK } | ForEach-Object {
    Write-Host ('  FALHA  ' + $_.Nome + '  (esperado ' + $_.Esperado + ', obtido ' + $_.Obtido + ')')
}
