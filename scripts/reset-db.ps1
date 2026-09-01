<#
.SYNOPSIS
    Repoe a base de dados local recursoshumanos_db a partir do zero.

.DESCRIPTION
    Deixa a base com os dados de RH (funcionarios, unidades organicas, carreiras,
    parametrizacoes) e as tabelas do SIGDI -- SIADAP, PAA, OKR, objetivos
    estrategicos -- VAZIAS, para os testes funcionais as preencherem pelos fluxos
    reais. A unica excecao e t_bsc_perspective_config: as 4 perspetivas BSC sao
    configuracao fixa semeada pela V26, nao dados de teste.

    Sem argumentos corre tudo (-Full): wipe -> migrate -> seed.

    -Migrate arranca o servico TRES vezes, e a ordem nao e arbitraria. O esquema
    deste repositorio nao se reconstroi do zero numa passagem so:

      1. Flyway ligado  -- aplica V1..V23 e FALHA na V24, que faz ALTER TABLE a
         t_key_results. Essa tabela nao e criada por migration nenhuma, so pelo
         ddl-auto=update do Hibernate, que corre depois do Flyway. A falha e
         esperada; o que interessa e que V1..V23 ficaram aplicadas.
      2. Flyway desligado -- o Hibernate cria as tabelas em falta a partir das
         entidades, t_key_results incluida.
      3. Flyway ligado  -- V24..V36 correm agora com as tabelas todas de pe.

    Enquanto a V24 depender de uma tabela que nenhuma migration cria, esta danca
    e obrigatoria. A alternativa (Hibernate primeiro, Flyway depois) e pior: os
    CREATE TABLE IF NOT EXISTS das migrations tornam-se no-ops sobre tabelas com
    a forma do Hibernate, e colunas como t_leave_mobility_subtype.name nunca
    chegam a existir.

.PARAMETER Full
    Wipe + Migrate + Seed. E o comportamento por omissao.

.PARAMETER Wipe
    Larga e recria os schemas 'public' e 'audit_schema'. Nao larga a base de
    dados em si -- o utilizador, as permissoes e o nome mantem-se.

.PARAMETER Migrate
    Corre a sequencia de tres arranques descrita acima. Exige o jar construido.

.PARAMETER Seed
    Corre src/main/resources/db/seed/master_seed.sql. Exige as tabelas criadas.

.PARAMETER Force
    Salta a confirmacao interativa do wipe.

.EXAMPLE
    .\scripts\reset-db.ps1
    Reset completo, com confirmacao.

.EXAMPLE
    .\scripts\reset-db.ps1 -Seed
    So re-semeia, sem largar nada. Corre com o servico de pe.
#>
[CmdletBinding()]
param(
    [switch]$Full,
    [switch]$Wipe,
    [switch]$Migrate,
    [switch]$Seed,
    [switch]$Force
)

$ErrorActionPreference = 'Stop'

# ---- configuracao lida do .env do repositorio -------------------------------
$repoRoot = Split-Path -Parent $PSScriptRoot
$envFile  = Join-Path $repoRoot '.env'
if (-not (Test-Path $envFile)) { throw "Nao encontrei $envFile" }

$cfg = @{}
foreach ($line in Get-Content $envFile) {
    if ($line -match '^\s*([A-Z0-9_]+)\s*=\s*(.*?)\s*$') { $cfg[$Matches[1]] = $Matches[2] }
}

$pgHost = $cfg['POSTGRES_HOST']
$pgPort = $cfg['POSTGRES_PORT']
$pgDb   = $cfg['POSTGRES_DATABASE']
$pgUser = $cfg['POSTGRES_USER']
$pgPass = $cfg['POSTGRES_PASSWORD']

# ---- localizar o psql -------------------------------------------------------
$psql = (Get-Command psql -ErrorAction SilentlyContinue).Source
if (-not $psql) {
    $psql = Get-ChildItem 'C:\Program Files\PostgreSQL\*\bin\psql.exe' -ErrorAction SilentlyContinue |
            Sort-Object FullName -Descending | Select-Object -First 1 -ExpandProperty FullName
}
if (-not $psql) { throw 'psql.exe nao encontrado. Instala o cliente PostgreSQL ou poe-no no PATH.' }

# ---- localizar o java e o jar (so precisos para -Migrate) -------------------
# JDK 26 e obrigatorio: os jars do IGRP estao em bytecode major 70 e nenhum JDK
# anterior os le. O nosso proprio codigo sai em bytecode 23 (-Djava.version=23
# no build), senao o repackage do spring-boot-maven-plugin rebenta.
$java = if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\java.exe")) { "$env:JAVA_HOME\bin\java.exe" }
        else {
            Get-ChildItem 'C:\Program Files\Java\jdk-*\bin\java.exe' -ErrorAction SilentlyContinue |
                Sort-Object FullName -Descending | Select-Object -First 1 -ExpandProperty FullName
        }

$jar = Get-ChildItem (Join-Path $repoRoot 'target\RH-Service-*.jar') -ErrorAction SilentlyContinue |
       Where-Object { $_.Name -notlike '*.original' } |
       Select-Object -First 1 -ExpandProperty FullName

$env:PGPASSWORD = $pgPass

# Cala os NOTICE do psql. Nao e cosmetica: os NOTICE saem em stderr, e no Windows
# PowerShell 5.1 basta encaminhar a saida (um `| Select-String`, por exemplo) para
# que cada linha de stderr vire um NativeCommandError. Com $ErrorActionPreference
# a 'Stop' isso mata o psql a meio, e como os DROP SCHEMA correm numa unica
# transacao o wipe reverte inteiro -- parecendo ter corrido, sem ter corrido.
$env:PGOPTIONS = '--client-min-messages=warning'

function Invoke-Psql {
    param([string]$Database, [string]$Sql, [string]$File)
    $psqlArgs = @('-h', $pgHost, '-p', $pgPort, '-U', $pgUser, '-d', $Database, '-v', 'ON_ERROR_STOP=1', '-q')
    if ($File) { $psqlArgs += @("-f", $File) } else { $psqlArgs += @("-c", $Sql) }
    & $psql @psqlArgs
    if ($LASTEXITCODE -ne 0) { throw "psql falhou com codigo $LASTEXITCODE" }
}

if (-not $Wipe -and -not $Seed -and -not $Migrate) { $Full = $true }
if ($Full) { $Wipe = $true; $Migrate = $true; $Seed = $true }

$svcPort = if ($cfg['SERVICE_PORT']) { [int]$cfg['SERVICE_PORT'] } else { 8084 }

# Arranca o jar, espera pelo veredito no log e mata o processo. Devolve
# STARTED / FAILED / TIMEOUT -- na sequencia de tres passagens, o FAILED da
# primeira e o resultado esperado, nao um erro.
function Start-Boot {
    param([string]$Tag, [string]$FlywayEnabled, [int]$TimeoutSeconds = 180)

    $log = Join-Path $repoRoot ".logs\reset-boot-$Tag.log"
    $env:SPRING_FLYWAY_ENABLED = $FlywayEnabled

    $proc = Start-Process -FilePath $java `
        -ArgumentList '-jar', $jar `
        -WorkingDirectory $repoRoot `
        -RedirectStandardOutput $log -RedirectStandardError "$log.err" `
        -PassThru -NoNewWindow

    $elapsed = 0; $verdict = 'TIMEOUT'
    while ($elapsed -lt $TimeoutSeconds) {
        Start-Sleep -Seconds 3; $elapsed += 3
        if (-not (Test-Path $log)) { continue }
        $content = Get-Content $log -Raw -ErrorAction SilentlyContinue
        if ($content -match 'Started RecursosHumanosApplication') { $verdict = 'STARTED'; break }
        if ($content -match 'APPLICATION FAILED TO START|Application run failed') { $verdict = 'FAILED'; break }
    }

    if (-not $proc.HasExited) { Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue }
    Start-Sleep -Seconds 2
    $env:SPRING_FLYWAY_ENABLED = $null
    return $verdict
}

# ---- guarda-fogo: o wipe e o migrate exigem o servico parado ----------------
# O -Seed corre com o servico de pe sem problema: escreve em tabelas que ja existem
# e a cache do Spring (caffeine, 60s) apanha os dados novos na expiracao seguinte.
if ($Wipe -or $Migrate) {
    if (Get-NetTCPConnection -LocalPort $svcPort -State Listen -ErrorAction SilentlyContinue) {
        throw "Ha algo a escutar na porta $svcPort (o recursoshumanos-service?). Para-o primeiro."
    }
}

if ($Migrate) {
    if (-not $java) { throw 'Nao encontrei o java. Define JAVA_HOME para um JDK 26.' }
    if (-not $jar)  {
        throw @"
Nao encontrei o jar em target\. Constroi-o primeiro:
    `$env:JAVA_HOME = 'C:\Program Files\Java\jdk-26.0.2'
    mvn -B -DskipTests -Djava.version=23 clean package
"@
    }
}

# ---- Fase 1: wipe -----------------------------------------------------------
if ($Wipe) {
    Write-Host "Alvo: $pgUser@${pgHost}:$pgPort/$pgDb" -ForegroundColor Yellow
    Write-Host 'Vao ser largados os schemas public e audit_schema. Perde-se TUDO.' -ForegroundColor Yellow
    if (-not $Force) {
        $answer = Read-Host "Escreve o nome da base de dados para confirmar"
        if ($answer -ne $pgDb) { Write-Host 'Cancelado.'; exit 1 }
    }

    Write-Host '-> a terminar ligacoes abertas...'
    Invoke-Psql -Database 'postgres' -Sql @"
SELECT pg_terminate_backend(pid) FROM pg_stat_activity
 WHERE datname = '$pgDb' AND pid <> pg_backend_pid();
"@

    Write-Host '-> a largar e recriar os schemas...'
    Invoke-Psql -Database $pgDb -Sql @"
DROP SCHEMA IF EXISTS audit_schema CASCADE;
DROP SCHEMA IF EXISTS public CASCADE;
CREATE SCHEMA public;
CREATE SCHEMA audit_schema;
GRANT ALL ON SCHEMA public TO $pgUser;
GRANT ALL ON SCHEMA public TO public;
"@

    # Objetos grandes pertencem a BASE DE DADOS e nao ao esquema, pelo que o
    # DROP SCHEMA CASCADE acima nao lhes toca. Sem isto, cada reset deixa orfaos os
    # objetos grandes do anterior -- medido a 2026-09-01, com 183 acumulados. Sao as
    # colunas @Lob das entidades do SIGDI (mission, vision, values_json, description
    # e companhia), que o Hibernate guarda em pg_largeobject com o OID na coluna.
    Write-Host '-> a limpar objetos grandes orfaos...'
    Invoke-Psql -Database $pgDb -Sql "SELECT lo_unlink(oid) FROM pg_largeobject_metadata;"

    Write-Host 'Base de dados vazia.' -ForegroundColor Green
    if (-not $Migrate) {
        Write-Host 'Passo seguinte:  .\scripts\reset-db.ps1 -Migrate -Seed'
    }
}

# ---- Fase 2: migrate (tres arranques; ver .DESCRIPTION) ---------------------
if ($Migrate) {
    New-Item -ItemType Directory -Force -Path (Join-Path $repoRoot '.logs') | Out-Null

    $steps = @(
        @{ Tag = 'a'; Flyway = 'true';  Expect = 'FAILED';  Note = 'V1..V23; a V24 falha, e esperado' },
        @{ Tag = 'b'; Flyway = 'false'; Expect = 'STARTED'; Note = 'Hibernate cria as tabelas em falta' },
        @{ Tag = 'c'; Flyway = 'true';  Expect = 'STARTED'; Note = 'V24..V36' }
    )

    $n = 0
    foreach ($step in $steps) {
        $n++
        Write-Host "-> arranque $n/3 (flyway=$($step.Flyway)) -- $($step.Note)"
        $verdict = Start-Boot -Tag $step.Tag -FlywayEnabled $step.Flyway
        if ($verdict -ne $step.Expect) {
            throw ("Arranque $n/3 devolveu $verdict, esperava $($step.Expect). " +
                   "Ve .logs\reset-boot-$($step.Tag).log")
        }
    }

    $applied = & $psql -h $pgHost -p $pgPort -U $pgUser -d $pgDb -Atc `
        "SELECT count(*) FROM flyway_schema_history WHERE success;"
    $failed = & $psql -h $pgHost -p $pgPort -U $pgUser -d $pgDb -Atc `
        "SELECT count(*) FROM flyway_schema_history WHERE NOT success;"
    if ([int]$failed -ne 0) { throw "Ha $failed migrations em estado failed no flyway_schema_history." }

    Write-Host "Esquema construido ($applied migrations aplicadas)." -ForegroundColor Green
}

# ---- Fase 2: seed -----------------------------------------------------------
if ($Seed) {
    $seedDir = Join-Path $repoRoot 'src\main\resources\db\seed'
    if (-not (Test-Path (Join-Path $seedDir 'master_seed.sql'))) { throw "Nao encontrei os seeds em $seedDir" }

    $tables = & $psql -h $pgHost -p $pgPort -U $pgUser -d $pgDb -Atc `
        "SELECT count(*) FROM information_schema.tables WHERE table_schema='public';"
    if ([int]$tables -eq 0) {
        throw 'O schema public esta vazio. Arranca o servico primeiro para o Flyway criar as tabelas.'
    }

    Write-Host "-> a semear ($tables tabelas no schema public)..."
    Push-Location $seedDir
    try { Invoke-Psql -Database $pgDb -File 'master_seed.sql' } finally { Pop-Location }
    Write-Host 'Seeds aplicados.' -ForegroundColor Green

    # ---- identidade institucional, pela API e nao por SQL --------------------
    # Ver o cabecalho de seed_identidade.sql para a razao inteira. Em duas linhas:
    # mission/vision/values_json sao @Lob, logo o Hibernate guarda-os em objeto
    # grande e poe o OID na coluna. Um INSERT em SQL cru mete la o texto, e todas as
    # leituras da identidade passam a falhar com "Bad value for type long", o que
    # bloqueia o percurso BSC inteiro. So a API produz a representacao certa.
    $identityCount = & $psql -h $pgHost -p $pgPort -U $pgUser -d $pgDb -Atc `
        "SELECT count(*) FROM t_institutional_identity WHERE is_active;"

    if ([int]$identityCount -gt 0) {
        Write-Host "-> identidade institucional ativa ja existe ($identityCount), nada a fazer"
    }
    else {
        # No fluxo completo o servico esta parado nesta altura -- o -Migrate mata-o
        # depois do terceiro arranque. Arranca-se aqui so para a chamada a API e
        # volta a parar-se, para o script terminar no mesmo estado em que comecou.
        $bootedForSeed = $false
        if (-not (Get-NetTCPConnection -LocalPort $svcPort -State Listen -ErrorAction SilentlyContinue)) {
            if (-not $java -or -not $jar) {
                throw @"
Falta a identidade institucional e nao consigo arrancar o servico para a criar.
Arranca-o a mao e corre outra vez:  .\scripts\reset-db.ps1 -Seed
"@
            }
            Write-Host '-> a arrancar o servico so para criar a identidade...'
            $seedProc = Start-Process -FilePath $java `
                -ArgumentList '-jar', $jar -WorkingDirectory $repoRoot `
                -RedirectStandardOutput (Join-Path $repoRoot '.logs\reset-boot-seed.log') `
                -RedirectStandardError  (Join-Path $repoRoot '.logs\reset-boot-seed.log.err') `
                -PassThru -NoNewWindow
            $bootedForSeed = $true

            $elapsed = 0
            while ($elapsed -lt 180) {
                Start-Sleep -Seconds 3; $elapsed += 3
                if (Get-NetTCPConnection -LocalPort $svcPort -State Listen -ErrorAction SilentlyContinue) { break }
            }
            if (-not (Get-NetTCPConnection -LocalPort $svcPort -State Listen -ErrorAction SilentlyContinue)) {
                if (-not $seedProc.HasExited) { Stop-Process -Id $seedProc.Id -Force -ErrorAction SilentlyContinue }
                throw 'O servico nao chegou a escutar na porta. Ve .logs\reset-boot-seed.log'
            }
        }

        # O corpo vai por FICHEIRO e nao por argumento: os acentos portugueses
        # partem-se na linha de comandos e o servico responde
        # "JSON parse error: Invalid UTF-8 middle byte".
        $body = @'
{"cycleYear":2026,"mission":"Assegurar a gestão rigorosa das finanças públicas e promover o desenvolvimento empresarial.","vision":"Ser uma administração financeira de referência, transparente e orientada para resultados.","values":["Rigor","Transparência","Responsabilidade","Orientação para resultados"],"versionComment":"Seed inicial para testes funcionais"}
'@
        $tmp = Join-Path ([System.IO.Path]::GetTempPath()) 'sipprog-identity.json'
        [System.IO.File]::WriteAllText($tmp, $body, (New-Object System.Text.UTF8Encoding $false))

        Write-Host '-> a criar a identidade institucional pela API...'
        $code = & curl.exe -s -o "$tmp.out" -w '%{http_code}' -m 60 `
            -X POST "http://localhost:$svcPort/api/v1/strategy/identities" `
            -H 'Content-Type: application/json; charset=utf-8' `
            --data-binary "@$tmp"

        $out = if (Test-Path "$tmp.out") { Get-Content "$tmp.out" -Raw } else { '' }
        Remove-Item $tmp, "$tmp.out" -ErrorAction SilentlyContinue

        if ($bootedForSeed -and -not $seedProc.HasExited) {
            Stop-Process -Id $seedProc.Id -Force -ErrorAction SilentlyContinue
            Start-Sleep -Seconds 2
        }

        if ($code -ne '201') { throw "A criacao da identidade devolveu $code em vez de 201. Resposta: $out" }
        Write-Host 'Identidade institucional criada.' -ForegroundColor Green
    }
}
