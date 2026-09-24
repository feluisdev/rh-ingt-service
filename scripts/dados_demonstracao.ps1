# Dados de demonstracao do RH-Service (so ASCII; os nomes vem de dados_demonstracao.json, em UTF-8).
# Carrega pela API -- passa pelas mesmas regras que o front --, numa base reposta:
#   docker cp scripts/repor_estado.sql postgres-ingt-rh:/tmp/repor.sql
#   docker exec postgres-ingt-rh sh -c "psql -U postgres -d recursoshumanos_db -q -f /tmp/repor.sql"
#   powershell -ExecutionPolicy Bypass -File scripts/dados_demonstracao.ps1
# O repor_estado.sql volta a apagar tudo o que este script cria (colaboradores, Lugares DEMO-*,
# unidades DEMO_*, horarios). Correr uma vez por base reposta.

$ErrorActionPreference = 'Stop'
$base = 'http://localhost:8099/api/v1/rh'
$dados = Get-Content -Raw -Encoding UTF8 (Join-Path $PSScriptRoot 'dados_demonstracao.json') | ConvertFrom-Json
$script:erros = 0

function Api {
    param([string]$Metodo, [string]$Rota, $Corpo = $null, [string]$Como = '', [int[]]$Aceita = @(200, 201))
    $params = @{ Method = $Metodo; Uri = "$base$Rota"; UseBasicParsing = $true; TimeoutSec = 120
                 Headers = @{ Accept = 'application/json' } }
    if ($Como) { $params.Headers['X-Employee-Id'] = $Como }
    if ($null -ne $Corpo) {
        $params.Body = [System.Text.Encoding]::UTF8.GetBytes(($Corpo | ConvertTo-Json -Depth 8 -Compress))
        $params.ContentType = 'application/json; charset=utf-8'
    }
    try {
        $r = Invoke-WebRequest @params
        $codigo = [int]$r.StatusCode
        $texto = [System.Text.Encoding]::UTF8.GetString($r.RawContentStream.ToArray())
    } catch {
        $codigo = 0; $texto = $_.Exception.Message
        if ($_.Exception.Response) {
            $codigo = [int]$_.Exception.Response.StatusCode
            try { $texto = (New-Object IO.StreamReader($_.Exception.Response.GetResponseStream())).ReadToEnd() } catch { }
        }
    }
    if ($Aceita -notcontains $codigo) {
        $script:erros++
        $curto = ($texto -replace '\s+', ' '); if ($curto.Length -gt 220) { $curto = $curto.Substring(0, 220) }
        Write-Host ("  ERRO [" + $codigo + "] " + $Metodo + " " + $Rota + " -> " + $curto)
        return $null
    }
    if ($texto) { try { return ($texto | ConvertFrom-Json) } catch { return $null } }
    return $null
}

function Lista($d) {
    if ($null -eq $d) { return @() }
    if ($d -is [System.Array]) { return @($d) }
    foreach ($c in 'content', 'dados', 'data', 'items') { if ($null -ne $d.$c) { return @($d.$c) } }
    return @($d)
}
function Iso($d) { return $d.ToString('yyyy-MM-dd') }
function DiaUtil($d) { return ($d.DayOfWeek -ne [DayOfWeek]::Saturday) -and ($d.DayOfWeek -ne [DayOfWeek]::Sunday) }

$hoje = (Get-Date).Date
$ano = $hoje.Year
$inicioMesAnterior = (Get-Date -Year $hoje.AddMonths(-1).Year -Month $hoje.AddMonths(-1).Month -Day 1).Date
Write-Host ('Dados de demonstracao -- ' + (Iso $hoje))

# --- 0. Nao carregar duas vezes ---------------------------------------------------------------
$ja = Lista (Api GET '/funcionarios?nif=500000001&pagina=0&tamanho=5')
if (@($ja | Where-Object { $_.nif -eq '500000001' }).Count -gt 0) {
    Write-Host 'Os dados de demonstracao ja estao carregados. Repor a base (repor_estado.sql) antes de voltar a correr.'
    exit 1
}

# --- 1. Catalogos ------------------------------------------------------------------------------
$categorias = @{}
foreach ($c in (Lista (Api GET '/categories?pagina=0&tamanho=50'))) { $categorias[$c.name] = $c }
$escaloes = @{}
foreach ($nome in @($dados.colaboradores | ForEach-Object { $_.categoria } | Select-Object -Unique)) {
    $cat = $categorias[$nome]
    if ($null -eq $cat) { Write-Host ('  ERRO categoria nao encontrada: ' + $nome); exit 1 }
    $escaloes[$nome] = @(Lista (Api GET ('/categories/' + $cat.id + '/grades')) | Sort-Object gradeNumber)
}
$jobs = @(Lista (Api GET '/estrutura/jobs?pagina=0&tamanho=20'))
$jobChefe = ($jobs | Select-Object -First 1)
$jobOutro = ($jobs | Select-Object -Last 1)
$tipoContrato = (Lista (Api GET '/catalogs/contract-types?pagina=0&tamanho=20') | Where-Object { $_.isActive -ne $false } | Select-Object -First 1)
$tipos = @{}
foreach ($t in (Lista (Api GET '/catalogs/leave-types?pagina=0&tamanho=80'))) { $tipos[$t.code] = $t }

# --- 2. Unidades: o nome do SERV_RH corrigido e as unidades novas, sob a DGP --------------------
$unidades = @{}
foreach ($u in (Lista (Api GET '/estrutura/organizational-units?pagina=0&tamanho=100'))) { $unidades[$u.code] = $u }
function CorpoUnidade($u) {
    return @{ code=$u.code; name=$u.name; acronym=$u.acronym; unitType=$u.unitType; descricao=$u.descricao;
              parentUnitId=$u.parentUnitId; responsibleEmployeeId=$u.responsibleEmployeeId }
}
$srh = $unidades['SERV_RH']
$corpo = CorpoUnidade $srh; $corpo.name = $dados.servRhNome
Api PUT ('/estrutura/organizational-units/' + $srh.id) $corpo | Out-Null
foreach ($nu in $dados.unidades) {
    $r = Api POST '/estrutura/organizational-units' @{ code=$nu.code; name=$nu.name; acronym=$nu.acronym;
        unitType=$srh.unitType; parentUnitId=$unidades['DGP'].id }
    if ($r) { $unidades[$nu.code] = @{ id=$r.id; code=$nu.code; name=$nu.name; acronym=$nu.acronym; unitType=$srh.unitType; parentUnitId=$unidades['DGP'].id } }
}
Write-Host ('Unidades: SERV_RH corrigido; ' + $dados.unidades.Count + ' novas')

# --- 3. Horario base -----------------------------------------------------------------------------
$blocos = @()
foreach ($d in 1..5) { $blocos += @{ diaSemana=$d; inicio='08:00'; fim='12:30' }; $blocos += @{ diaSemana=$d; inicio='14:00'; fim='17:30' } }
$h = Api POST '/catalogs/horarios' @{ nome=$dados.horarioBase.nome; controlo='FIXO'; blocos=$blocos }
if ($h) { Api PATCH ('/catalogs/horarios/' + $h.id + '/base') | Out-Null }
Write-Host 'Horario base marcado'

# --- 4. Lugares (chefia com Lugar-pai) e colaboradores ------------------------------------------
$lugarChefe = @{}
$pessoas = @{}
$i = 0
foreach ($c in ($dados.colaboradores | Sort-Object { -not $_.chefe })) {
    $i++
    $u = $unidades[$c.unidade]
    $cat = $categorias[$c.categoria]
    $esc = $escaloes[$c.categoria]
    $corpoLugar = @{ numeroLugar=('DEMO-' + $c.unidade.Replace('DEMO_', '') + '-' + $i.ToString('00')); unidadeOrganicaId=$u.id;
                     careerId=$cat.careerId; categoryId=$cat.id }
    if ($c.chefe) { $corpoLugar.jobId = $jobChefe.id; $corpoLugar.managesUnitId = $u.id }
    else { $corpoLugar.jobId = $jobOutro.id; $corpoLugar.parentPositionId = $lugarChefe[$c.unidade] }
    $lugar = Api POST '/estrutura/positions' $corpoLugar
    if ($null -eq $lugar) { continue }
    if ($c.chefe) { $lugarChefe[$c.unidade] = $lugar.id }

    $admissao = $c.admissao.Replace('ANO', $ano.ToString())
    $f = Api POST '/funcionarios' @{ nomeCompleto=$c.nome; dataNascimento=$c.nascimento; genero=$c.genero;
        estadoCivil='SOLTEIRO'; nif=('5000000' + $i.ToString('00')); dataAdmissao=$admissao;
        email=($c.chave + '@demo.gov.cv'); nacionalidade='Cabo-verdiana' }
    if ($null -eq $f) { continue }
    Api POST ('/funcionarios/' + $f.id + '/contratos') @{ contractTypeId=$tipoContrato.id; startDate=$admissao; regimeTrabalho='TEMPO_COMPLETO' } | Out-Null
    $escalao = $esc[0]; if ($c.chefe -and $esc.Count -gt 1) { $escalao = $esc[1] }
    Api POST '/colaboradores/assignments' @{ funcionarioId=$f.id; positionId=$lugar.id; gradeId=$escalao.id;
        origem='ADMISSAO'; assignmentType='PRINCIPAL'; dataInicio=$admissao } | Out-Null
    $pessoas[$c.chave] = @{ id=$f.id; unidade=$c.unidade; admissao=[datetime]$admissao; lugar=$lugar.id }
}
# O responsavel de cada unidade e a chefia.
foreach ($c in ($dados.colaboradores | Where-Object { $_.chefe })) {
    $u = $unidades[$c.unidade]
    $corpo = CorpoUnidade $u; $corpo.responsibleEmployeeId = $pessoas[$c.chave].id
    if ($c.unidade -eq 'SERV_RH') { $corpo.name = $dados.servRhNome }
    Api PUT ('/estrutura/organizational-units/' + $u.id) $corpo | Out-Null
}
Write-Host ('Colaboradores: ' + $pessoas.Count + ' (com contrato, Lugar e chefia directa)')

# --- 5. Marcacoes: do inicio do mes anterior ate ontem -----------------------------------------
# Excepcoes de proposito: um atraso (dulce), um dia sem registo (edson), uma entrada sem saida
# (helena, ontem), ferias (gilberto) e luto (leonel), e trabalho suplementar (carlos).
$diasUteis = @()
for ($d = $inicioMesAnterior; $d -lt $hoje; $d = $d.AddDays(1)) { if (DiaUtil $d) { $diasUteis += $d } }
$semanaFerias = @($diasUteis | Where-Object { $_.Month -eq $inicioMesAnterior.Month } | Select-Object -Skip 5 -First 5)
$diasLuto = @($diasUteis | Where-Object { $_.Month -eq $inicioMesAnterior.Month } | Select-Object -Skip 12 -First 2)
$diaAtraso = $diasUteis[3]
$diaFalta = $diasUteis[8]
$diaSuplementar = $diasUteis[6]
$ontem = $diasUteis[-1]
$picagens = @()
# Os tres colaboradores do seed (Francisco, Maria, Joana Tavares) tambem picam, senao a relacao
# mensal mostra-os com o mes inteiro sem registo.
$marcam = @{}
foreach ($chave in $pessoas.Keys) { $marcam[$chave] = $pessoas[$chave] }
foreach ($s in @('e901', 'e902', 'e903')) {
    $marcam['seed' + $s] = @{ id=('91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1' + $s); admissao=[datetime]'2000-01-01' }
}
foreach ($chave in $marcam.Keys) {
    $p = $marcam[$chave]
    $n = 0
    foreach ($d in $diasUteis) {
        $n++
        if ($d -lt $p.admissao) { continue }
        if ($chave -eq 'gilberto' -and $semanaFerias -contains $d) { continue }
        if ($chave -eq 'leonel' -and $diasLuto -contains $d) { continue }
        if ($chave -eq 'edson' -and $d -eq $diaFalta) { continue }
        $varia = ($chave.Length * 7 + $n * 3) % 11
        # Entrada entre as 07:45 e as 07:52: um minuto de atraso ja pesa no debito do mes.
        $entrada = $d.AddHours(7).AddMinutes(45 + ($varia % 8))
        if ($chave -eq 'dulce' -and $d -eq $diaAtraso) { $entrada = $d.AddHours(8).AddMinutes(47) }
        $marcas = @(@($entrada, 'ENTRADA'), @($d.AddHours(12).AddMinutes(30), 'SAIDA'), @($d.AddHours(14), 'ENTRADA'),
                    @($d.AddHours(17).AddMinutes(30 + $varia), 'SAIDA'))
        if ($chave -eq 'helena' -and $d -eq $ontem) { $marcas = @(,$marcas[0]) }
        if ($chave -eq 'carlos' -and $d -eq $diaSuplementar) { $marcas += ,@($d.AddHours(18), 'ENTRADA'); $marcas += ,@($d.AddHours(19).AddMinutes(30), 'SAIDA') }
        $k = 0
        foreach ($m in $marcas) {
            $k++
            $picagens += @{ funcionarioId=$p.id; momento=$m[0].ToString('yyyy-MM-ddTHH:mm'); sentido=$m[1];
                            referenciaExterna=('DEMO-' + $chave + '-' + $m[0].ToString('yyyyMMddHHmm') + '-' + $k) }
        }
    }
}
$rImp = Api POST '/assiduidade/importacao' @{ picagens=$picagens }
if ($rImp) { Write-Host ('Marcacoes: ' + $rImp.importadas + ' importadas, ' + @($rImp.rejeitadas).Count + ' rejeitadas') }

# --- 6. Ausencias, ferias, trabalho suplementar, promocao, pedidos pendentes --------------------
# O saldo de ferias do ano vence pelo job das 00:05 (art. 2.o n.o 4); numa base acabada de carregar
# ainda nao correu, por isso cria-se aqui o de quem nao o tem (o dos admitidos este ano ja nasceu).
foreach ($chave in $pessoas.Keys) {
    $rotaS = '/funcionarios/' + $pessoas[$chave].id + '/saldos-ausencia'
    $tem = @(Lista (Api GET ($rotaS + '?ano=' + $ano)) | Where-Object { $_.tipoAusenciaId -eq $tipos['FERIAS'].id })
    if ($tem.Count -eq 0) { Api POST $rotaS @{ tipoAusenciaId=$tipos['FERIAS'].id; ano=$ano; diasDireito=22 } | Out-Null }
}
$rotaP = { param($chave) '/funcionarios/' + $pessoas[$chave].id + '/pedidos-ausencia' }
$rFer = Api POST (& $rotaP 'gilberto') @{ tipoAusenciaId=$tipos['FERIAS'].id; dataInicio=(Iso $semanaFerias[0]); dataFim=(Iso $semanaFerias[-1]); motivo='Ferias' }
if ($rFer -and $rFer.estado -ne 'APROVADO') {
    Api PATCH ((& $rotaP 'gilberto') + '/' + $rFer.id + '/aprovar') @{ aprovadoPorId=$pessoas['fatima'].id; observacoesDecisao='Aprovado pela chefia' } | Out-Null
}
Api POST (& $rotaP 'leonel') @{ tipoAusenciaId=$tipos['LUTO'].id; dataInicio=(Iso $diasLuto[0]); dataFim=(Iso $diasLuto[-1]); motivo='Falecimento de familiar' } | Out-Null
Api POST ('/funcionarios/' + $pessoas['carlos'].id + '/trabalho-suplementar') @{ data=(Iso $diaSuplementar); horaInicio='18:00'; horaFim='20:00'; motivo='Fecho do relatorio mensal' } | Out-Null

# Pendentes nas caixas das chefias (pelo /me de quem pede).
$daqui = $hoje.AddDays(21); while (-not (DiaUtil $daqui)) { $daqui = $daqui.AddDays(1) }
Api POST '/me/leave-requests' @{ leaveTypeId=$tipos['SEMINARIO'].id; startDate=(Iso $daqui); endDate=(Iso $daqui.AddDays(1)); notes='Seminario de gestao publica' } -Como $pessoas['dulce'].id | Out-Null
Api POST '/me/leave-requests' @{ leaveTypeId=$tipos['FERIAS'].id; startDate=(Iso $daqui.AddDays(7)); endDate=(Iso $daqui.AddDays(11)); notes='Ferias' } -Como $pessoas['nuno'].id | Out-Null
$sabado = $hoje.AddDays(7); while ($sabado.DayOfWeek -ne [DayOfWeek]::Saturday) { $sabado = $sabado.AddDays(1) }
Api POST '/me/trabalho-suplementar' @{ data=(Iso $sabado); horaInicio='09:00'; horaFim='13:00'; motivo='Inventario do patrimonio' } -Como $pessoas['ivo'].id | Out-Null
Api POST '/me/marcacoes/correcoes' @{ momento=((Iso $ontem) + 'T17:30'); sentido='SAIDA'; motivo='Esqueci-me de picar a saida' } -Como $pessoas['helena'].id | Out-Null

# Ferias marcadas no ano (por acordo) e preferencias do ano seguinte, pelo proprio.
$agosto = (Get-Date -Year $ano -Month 8 -Day 1).Date; while ($agosto.DayOfWeek -ne [DayOfWeek]::Monday) { $agosto = $agosto.AddDays(1) }
foreach ($chave in @('ana', 'carlos', 'fatima', 'joana', 'ivo')) {
    Api PUT ('/funcionarios/' + $pessoas[$chave].id + '/ferias/' + $ano + '/marcacao') @{ origem='ACORDO'; periodos=@(@{ dataInicio=(Iso $agosto); dataFim=(Iso $agosto.AddDays(20)) }) } | Out-Null
}
$julho = (Get-Date -Year ($ano + 1) -Month 7 -Day 1).Date; while ($julho.DayOfWeek -ne [DayOfWeek]::Monday) { $julho = $julho.AddDays(1) }
foreach ($chave in @('dulce', 'edson', 'helena')) {
    Api PUT ('/me/ferias/' + ($ano + 1) + '/preferencia') @{ periodos=@(@{ dataInicio=(Iso $julho); dataFim=(Iso $julho.AddDays(18)) }); observacoes='Julho' } -Como $pessoas[$chave].id | Out-Null
}

# Uma promocao recente: a Dulce passa a Tecnico Superior (o proprio Lugar e reclassificado).
$inicioMes = (Get-Date -Year $ano -Month $hoje.Month -Day 1).Date
$catSuperior = ($dados.colaboradores | Where-Object { $_.chefe } | Select-Object -First 1).categoria
Api POST ('/funcionarios/' + $pessoas['dulce'].id + '/promocao') @{ categoryId=$categorias[$catSuperior].id; dataEfeito=(Iso $inicioMes); despachoNumero='DEMO/' + $ano; observacoes='Promocao por concurso' } | Out-Null

Write-Host ''
Write-Host ('Concluido com ' + $script:erros + ' erro(s).')
Write-Host ('Chefias: SERV_RH=' + $pessoas['ana'].id + '  DEMO_SFIN=' + $pessoas['fatima'].id + '  DEMO_GJUR=' + $pessoas['joana'].id)
Write-Host '  (em desenvolvimento, o /me de cada um usa o cabecalho X-Employee-Id com estes ids)'
Write-Host ('Ministerio (para os relatorios): ' + $unidades['MIN_FIN'].id)
if ($script:erros -gt 0) { exit 1 }
