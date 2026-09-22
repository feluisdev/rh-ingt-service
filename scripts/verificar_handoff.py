# -*- coding: utf-8 -*-
"""
Verifica o handoff (.claude/resume/rhFix.md) contra a realidade do repositorio.

Porque existe: um handoff e util na medida em que se pode confiar nele a frio. As
afirmacoes envelhecem sem ninguem dar por isso -- o numero de testes, o numero de
passos da bateria, a proxima migracao livre, um caminho de ficheiro que mudou de
sitio. Este script le as afirmacoes do PROPRIO handoff e compara-as com o que o
repositorio diz agora. Nao tem numeros escritos a mao, de proposito: assim continua
a servir depois de o handoff ser actualizado.

Correr:  python scripts/verificar_handoff.py
Exit 0 = o handoff diz a verdade. Exit 1 = ha afirmacoes a corrigir.

Nota: os testes sao lidos do ultimo relatorio em target/surefire-reports, portanto
correr `mvn -B clean test` antes, senao essa verificacao e saltada.
"""
import io
import json
import os
import re
import subprocess
import sys

HANDOFF = ".claude/resume/rhFix.md"
JAVA = "src/main/java/cv/igrp/RH_Service/"
MIGRACOES = "src/main/resources/db/migration"

if not os.path.isfile(HANDOFF):
    print("Nao encontrei " + HANDOFF + " -- correr a partir da raiz do projecto.")
    sys.exit(2)

s = io.open(HANDOFF, encoding="utf-8").read()

# A seccao do framework de jobs cita caminhos do inss_core_service, que nao existem
# aqui -- e o proprio handoff avisa disso. Fica fora da verificacao de caminhos.
_i = s.find("### Framework de jobs")
_f = s.find("### ", _i + 5) if _i >= 0 else -1
s_caminhos = (s[:_i] + s[_f:]) if _i >= 0 and _f > _i else s

falhas = []
saltadas = []
ok = 0


def verifica(nome, condicao, detalhe=""):
    global ok
    if condicao:
        ok += 1
    else:
        falhas.append((nome, detalhe))


def afirma(nome, padrao, real, como=int):
    """Le uma afirmacao do handoff e compara-a com a realidade.

    Se o handoff nao a fizer, nao e falha -- e uma verificacao que nao se aplica.
    """
    m = re.search(padrao, s)
    if not m:
        saltadas.append(nome + " (o handoff nao o afirma)")
        return
    dito = como(m.group(1))
    verifica(nome, dito == real, "handoff diz %s, a realidade diz %s" % (dito, real))


def shell(cmd):
    return subprocess.run(cmd, shell=True, capture_output=True, text=True).stdout.strip()


# ---------------------------------------------------------------- 1. caminhos
padrao = re.compile(
    r"`((?:colaboradores|parametrizacoes|estrutura|carreiras|shared|sigdi)"
    r"/[A-Za-z0-9_/.]+\.java)(?::(\d+))?`")
vistos = set()
for m in padrao.finditer(s_caminhos):
    rel, linha = m.group(1), m.group(2)
    if (rel, linha) in vistos:
        continue
    vistos.add((rel, linha))
    caminho = JAVA + rel
    existe = os.path.isfile(caminho)
    verifica("ficheiro " + rel, existe, "nao existe: " + caminho)
    if existe and linha:
        total = len(io.open(caminho, encoding="utf-8").read().split("\n"))
        verifica(rel + ":" + linha, int(linha) <= total,
                 "o ficheiro so tem %d linhas" % total)

for rel in re.findall(r"`(scripts/[A-Za-z0-9_./]+)`", s_caminhos):
    verifica("script " + rel, os.path.isfile(rel), "nao existe")
for rel in re.findall(r"`(docs/[A-Za-z0-9_./]+)`", s_caminhos):
    verifica("doc " + rel, os.path.isfile(rel), "nao existe")
for rel in re.findall(r"`(db/(?:seed|migration)/[A-Za-z0-9_./]+)`", s_caminhos):
    verifica("recurso " + rel, os.path.isfile("src/main/resources/" + rel),
             "nao existe: src/main/resources/" + rel)
for v in set(re.findall(r"\*\*`(V\d+)`\*\*", s)):
    verifica("migracao " + v,
             any(f.startswith(v + "__") for f in os.listdir(MIGRACOES)),
             "nao ha ficheiro " + v + "__*.sql")

# ---------------------------------------------------------------- 2. numeros
ramo = shell("git rev-parse --abbrev-ref HEAD")
afirma("commits por enviar",
       r"\*\*(\d+) commits locais por enviar\*\*",
       int(shell("git log --oneline origin_git_lab/%s..HEAD | wc -l" % ramo) or 0))

numeros = sorted(int(f.split("__")[0][1:]) for f in os.listdir(MIGRACOES)
                 if re.match(r"^V\d+__", f))
afirma("proxima migracao livre", r"Próxima livre: \*\*V(\d+)\*\*", max(numeros) + 1)

if os.path.isdir("target/surefire-reports"):
    tot = fal = err = 0
    for f in os.listdir("target/surefire-reports"):
        if not f.endswith(".xml"):
            continue
        t = io.open("target/surefire-reports/" + f, encoding="utf-8",
                    errors="replace").read(4000)
        for chave in ("tests", "failures", "errors"):
            m = re.search(chave + r'="(\d+)"', t)
            if not m:
                continue
            if chave == "tests":
                tot += int(m.group(1))
            elif chave == "failures":
                fal += int(m.group(1))
            else:
                err += int(m.group(1))
    afirma("total de testes", r"\*\*Testes: (\d+), 0 falhas", tot)
    verifica("suite verde", fal == 0 and err == 0,
             "%d falhas, %d erros no ultimo relatorio" % (fal, err))
else:
    saltadas.append("testes (sem target/surefire-reports -- correr mvn clean test)")

readme = "scripts/testes_funcionais_README.md"
if os.path.isfile(readme):
    r = io.open(readme, encoding="utf-8").read()
    m_r = re.search(r"\*\*(\d+) passos, \d+ OK\*\*", r)
    if m_r:
        afirma("passos da bateria", r"\*\*Bateria funcional: (\d+) passos", int(m_r.group(1)))

api = "docs/funcionarios/v5/openapi.json"
if os.path.isfile(api):
    d = json.load(io.open(api, encoding="utf-8"))
    afirma("caminhos no openapi", r"\*\*`openapi\.json`\*\*: (\d+) caminhos", len(d["paths"]))
    afirma("esquemas no openapi", r"caminhos, (\d+) esquemas", len(d["components"]["schemas"]))

afirma("jobs @Scheduled",
       r"\*\*(Sete|Seis|Oito|Cinco|Nove) jobs `@Scheduled`\*\*",
       int(shell("grep -rl '@Scheduled' --include=*.java src/main | wc -l") or 0),
       como=lambda palavra: {"Cinco": 5, "Seis": 6, "Sete": 7, "Oito": 8,
                             "Nove": 9}[palavra])

# ---------------------------------------------------------------- 3. estrutura
for titulo in ["## Goal", "## Current state", "## Decisions made", "## Constraints",
               "## Blockers & risks", "## Relevant files", "## How to verify / resume",
               "## Open questions", "## Next step"]:
    verifica("seccao " + titulo, s.count(titulo) == 1,
             "aparece %d vezes" % s.count(titulo))

# ---------------------------------------------------------------- resultado
print("VERIFICACOES OK: %d" % ok)
for nome in saltadas:
    print("  saltada: " + nome)
if falhas:
    print("\nFALHAS: %d" % len(falhas))
    for nome, detalhe in falhas:
        print("  - %s  -> %s" % (nome, detalhe))
    sys.exit(1)
print("FALHAS: 0")
