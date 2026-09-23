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

Quando a suite corre numa copia isolada (por causa do servidor Java do VS Code, que
escreve no mesmo target/ -- ver o handoff), apontar para os relatorios dessa copia:
    python scripts/verificar_handoff.py --relatorios <copia>/target/surefire-reports

Tambem apanha o que envelhece FORA do "Current state" (visto num teste a frio, 2026-09-23):
numeros repetidos noutras seccoes (Tests run, passos, ahead), frases de "proximo passo" fora do
"Next step", a data "Updated" mais antiga do que o ultimo commit de codigo, e o bloco seguinte da
bateria. Para provar o proprio verificador contra um handoff alterado: --handoff <ficheiro>.
"""
import io
import json
import os
import re
import subprocess
import sys

HANDOFF = ".claude/resume/rhFix.md"
if "--handoff" in sys.argv:
    HANDOFF = sys.argv[sys.argv.index("--handoff") + 1]
RELATORIOS = "target/surefire-reports"
if "--relatorios" in sys.argv:
    RELATORIOS = sys.argv[sys.argv.index("--relatorios") + 1]
    # A pasta temporaria das sessoes passa facilmente dos 260 caracteres do Windows.
    if os.name == "nt":
        RELATORIOS = "\\\\?\\" + os.path.abspath(RELATORIOS)
JAVA = "src/main/java/cv/igrp/RH_Service/"
MIGRACOES = "src/main/resources/db/migration"

if not os.path.isfile(HANDOFF):
    print("Nao encontrei " + HANDOFF + " -- correr a partir da raiz do projecto.")
    sys.exit(2)

s = io.open(HANDOFF, encoding="utf-8").read()

# A seccao do framework de jobs cita caminhos do inss_core_service, que nao existem
# aqui -- e o proprio handoff avisa disso. Fica fora da verificacao de caminhos.
_i = s.find("### Framework de jobs")
# Acaba no titulo seguinte, seja "###" ou "##".
_f = min([x for x in (s.find(chr(10) + "### ", _i + 5), s.find(chr(10) + "## ", _i + 5)) if x >= 0], default=-1) if _i >= 0 else -1
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

if os.path.isdir(RELATORIOS):
    tot = fal = err = 0
    for f in os.listdir(RELATORIOS):
        if not f.endswith(".xml"):
            continue
        t = io.open(os.path.join(RELATORIOS, f), encoding="utf-8",
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
    # O mesmo numero repetido noutra seccao (ex.: "esperado: Tests run: 1001") tambem tem de bater.
    for dito in re.findall(r"Tests run:?\s*(\d+)", s):
        verifica("Tests run citado (%s)" % dito, int(dito) == tot, "handoff diz %s, a realidade diz %d" % (dito, tot))
    verifica("suite verde", fal == 0 and err == 0,
             "%d falhas, %d erros no ultimo relatorio" % (fal, err))
else:
    saltadas.append("testes (sem " + RELATORIOS + " -- correr mvn clean test)")

readme = "scripts/testes_funcionais_README.md"
if os.path.isfile(readme):
    r = io.open(readme, encoding="utf-8").read()
    m_r = re.search(r"\*\*(\d+) passos, \d+ OK\*\*", r)
    if m_r:
        afirma("passos da bateria", r"\*\*Bateria funcional: (\d+) passos", int(m_r.group(1)))
        for dito in re.findall(r"(\d+) passos", s):
            verifica("passos citados (%s)" % dito, int(dito) == int(m_r.group(1)),
                     "handoff diz %s, o README diz %s" % (dito, m_r.group(1)))
    # O bloco seguinte da bateria: um a mais do que o ultimo bloco Fnn do script.
    ps1 = "scripts/testes_funcionais.ps1"
    blocos = [int(b) for b in re.findall(r"=========== F(\d+)\b", io.open(ps1, encoding="utf-8").read())] if os.path.isfile(ps1) else []
    if blocos:
        afirma("proximo bloco da bateria", r"o próximo é o \*\*F(\d+)\*\*", max(blocos) + 1)

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

# "ahead N" citado em qualquer lado (ex.: comentario num bloco de comandos).
for dito in re.findall(r"ahead (\d+)", s):
    real = int(shell("git log --oneline origin_git_lab/%s..HEAD | wc -l" % ramo) or 0)
    verifica("ahead citado (%s)" % dito, int(dito) == real, "handoff diz %s, a realidade diz %d" % (dito, real))

# A data do handoff nao pode ser mais antiga do que o ultimo commit de codigo.
m_u = re.search(r"Updated: (\d{4}-\d{2}-\d{2} \d{2}:\d{2})", s)
ultimo = shell("git log -1 --format=%ci -- src")[:16]
if m_u and ultimo:
    verifica("data do handoff", m_u.group(1) >= ultimo,
             "Updated %s, mas o ultimo commit de codigo e de %s" % (m_u.group(1), ultimo))

# "Proximo passo" so no Next step: fora dele e quase sempre uma frase que ja envelheceu.
_n = s.find("## Next step")
fora = s[:_n] if _n >= 0 else s
for frase in re.findall(r"[^\n]*(?:próximo passo|passo seguinte|\(a seguir\))[^\n]*", fora, flags=re.I):
    verifica("proximo passo fora do Next step", False, frase.strip()[:120])

# ---------------------------------------------------------------- 3. estrutura
for titulo in ["## Goal", "## Current state", "## Decisions made", "## Constraints",
               "## Blockers & risks", "## Relevant files", "## How to verify / resume",
               "## Open questions", "## Plano em aberto", "## Next step"]:
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
