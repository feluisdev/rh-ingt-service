"""Verifica se a documentacao v5 (docs/funcionarios/v5) reflecte a aplicacao.

Uso (na raiz do projecto):
    PYTHONIOENCODING=utf-8 python scripts/verificar_docs.py [--detalhe]

O que confere (ambito nao-sigdi: /api/v1/rh/** e /documento/**):
  1. api_guide.md <-> openapi.json: cada caminho da API aparece no guia, e cada caminho que o guia
     cita existe na API. O guia abrevia ('.../unidade-atual'), por isso um caminho conta como
     documentado quando o guia tem o caminho inteiro ou um sufixo dele com pelo menos dois
     segmentos e acabado num segmento literal.
  2. modelo_relacional.html <-> codigo: cada tabela @Table dos modulos RH aparece no modelo, e
     cada t_... do modelo existe no codigo ou nas migracoes.
  3. Regras BR-*: cada identificador citado nos documentos esta definido no regras_negocio.html
     (os intervalos 'BR-X-01 a BR-X-05' contam os extremos).
  4. HTML: etiquetas equilibradas e ancoras internas (href="#...") que existem.
  5. Referencias a seccoes do guia ('api_guide.md §6.14', '§6.14' dentro do guia) que existem.
  6. Leis: os artigos citados da Lei n.o 20/X/2023 e do DL n.o 3/2010 nao passam do ultimo artigo
     do diploma (so se os textos .lei20.txt / .dl3.txt estiverem na raiz).
  7. Data da ultima alteracao no cabecalho de cada documento (nos HTML, dentro do <header>).
  8. Ecras da apresentacao: cada caminho em <span class="rota"> existe no openapi.json, e cada
     data-campo existe no esquema do data-dto mais proximo (os ecras desenham-se dos DTOs).
  9. Ligacoes entre documentos (href="x.html", "x.md", "x.json") apontam para ficheiros que existem.
 10. A coluna Origem do catalogo de regras: cada Classe e Classe.metodo citado existe no codigo.

Sai com 1 se houver falhas.
"""
import html.parser
import json
import os
import re
import sys

RAIZ = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
V5 = os.path.join(RAIZ, 'docs', 'funcionarios', 'v5')
JAVA = os.path.join(RAIZ, 'src', 'main', 'java', 'cv', 'igrp', 'RH_Service')
MIGR = os.path.join(RAIZ, 'src', 'main', 'resources', 'db')
MODULOS_RH = ('colaboradores', 'estrutura', 'carreiras', 'parametrizacoes', 'shared', 'recrutamento', 'formacao')
DETALHE = '--detalhe' in sys.argv

falhas = []
avisos = []


def ler(caminho):
    with open(caminho, encoding='utf-8') as f:
        return f.read()


def falha(grupo, texto):
    falhas.append((grupo, texto))


def norm(p):
    p = p.replace('…', '...')
    p = re.sub(r'\{[^}/]*\}', '{}', p)
    p = re.sub(r'^/api/v1/rh', '', p)
    return p.rstrip('/')


# ------------------------------------------------------------------ 1. guia <-> openapi
api = json.loads(ler(os.path.join(V5, 'openapi.json')))
guia = ler(os.path.join(V5, 'api_guide.md'))
guia_n = re.sub(r'\{[^}/\s]*\}', '{}', guia.replace('…', '...'))
# alternativas do guia: /api/v1/rh/{careers|categories|grades} -> expandir
for m in re.finditer(r'/\{([a-z-]+(?:\|[a-z-]+)+)\}', guia):
    for alt in m.group(1).split('|'):
        guia_n += '\n/' + alt

caminhos_api = {}
for p, ops in api['paths'].items():
    if not (p.startswith('/api/v1/rh') or p.startswith('/documento')):
        continue
    caminhos_api[norm(p)] = sorted(k.upper() for k in ops if k in ('get', 'post', 'put', 'patch', 'delete'))


def documentado(pn):
    if pn in guia_n and re.search(re.escape(pn) + r'(?![\w-])', guia_n):
        return True
    seg = pn.strip('/').split('/')
    for i in range(1, len(seg) - 1):
        suf = '/'.join(seg[i:])
        if seg[-1] in ('{}',) or len(seg[i:]) < 2:
            continue
        if re.search(r'/' + re.escape(suf) + r'(?![\w-])', guia_n):
            return True
    # caminho acabado em {}: aceitar o sufixo com o {} final
    if seg[-1] == '{}' and len(seg) >= 3:
        suf = '/'.join(seg[-3:])
        if re.search(r'/' + re.escape(suf) + r'(?![\w-])', guia_n):
            return True
        suf = '/'.join(seg[-2:])
        if seg[-2] != '{}' and re.search(r'/' + re.escape(suf) + r'(?![\w-])', guia_n):
            return True
    return False


def coberto(pn):
    if documentado(pn):
        return True
    # padrao CRUD da seccao 2 do guia: /X/{id} e /X/combobox contam se /X estiver documentado
    base = re.sub(r'/(\{\}|combobox)$', '', pn)
    if base != pn and base in caminhos_api and documentado(base):
        return True
    # o CSV: o guia escreve '(e .csv)' na linha do caminho base
    if pn.endswith('.csv'):
        ultimo = pn[:-4].rsplit('/', 1)[-1]
        return any(ultimo in l and '.csv' in l for l in guia_n.splitlines())
    return False


sem_doc = [p for p in sorted(caminhos_api) if not coberto(p)]
for p in sem_doc:
    falha('guia: caminho da API sem documentacao', p + '  ' + ' '.join(caminhos_api[p]))

# caminhos citados no guia que nao existem na API (so os absolutos: /api/v1/rh/... ou /funcionarios/...)
raizes = sorted({p.strip('/').split('/')[0] for p in caminhos_api})
# a seccao dos endpoints removidos cita o que ja nao existe -- e tem de nao existir
m13 = re.search(r'^## 13\..*?(?=^## |\Z)', guia_n, re.M | re.S)
removidos_txt = m13.group(0) if m13 else ''
guia_vivo = guia_n.replace(removidos_txt, '')
for m in re.finditer(r'`(?:(?:POST|GET|PUT|PATCH|DELETE)/?)+\s+(/[\w{}\-/]+)', removidos_txt):
    if norm(m.group(1)) in caminhos_api:
        falha('guia: caminho dado como removido que ainda existe', norm(m.group(1)))
citados = set()
for m in re.finditer(r'(?<![\w.])(/api/v1/rh)?(/(?:' + '|'.join(map(re.escape, raizes)) + r')(?:/[\w{}\-.]+)*)', guia_vivo):
    c = m.group(2).rstrip('.').split('?')[0]
    if '...' in c or c.endswith('.csv') and norm(c) in caminhos_api:
        continue
    citados.add(norm(c))
for c in sorted(citados):
    if c in caminhos_api:
        continue
    # prefixo de um caminho existente (ex.: '/catalogs' como familia) e aceite
    if any(p.startswith(c + '/') for p in caminhos_api):
        continue
    falha('guia: caminho citado que nao existe na API', c)

# ------------------------------------------------------------------ 2. modelo relacional <-> codigo
modelo = ler(os.path.join(V5, 'modelo_relacional.html'))
tabelas_codigo = set()
for base, _, fs in os.walk(JAVA):
    rel = os.path.relpath(base, JAVA).split(os.sep)[0]
    if rel not in MODULOS_RH:
        continue
    for f in fs:
        if f.endswith('.java'):
            t = ler(os.path.join(base, f))
            for m in re.finditer(r'@(?:Table|CollectionTable|JoinTable)\s*\(\s*name\s*=\s*"(\w+)"', t):
                tabelas_codigo.add(m.group(1).lower())
tabelas_migr = set()
for base, _, fs in os.walk(MIGR):
    for f in fs:
        if f.endswith('.sql'):
            for m in re.finditer(r'create\s+table\s+(?:if\s+not\s+exists\s+)?(?:\w+\.)?(\w+)', ler(os.path.join(base, f)), re.I):
                tabelas_migr.add(m.group(1).lower())
tabelas_modelo = {t.lower() for t in re.findall(r'\bt_[a-z0-9_]+\b', modelo)}
for t in sorted(tabelas_codigo - tabelas_modelo):
    falha('modelo relacional: tabela do codigo em falta', t)
# as tabelas legadas que o modelo da como removidas (no mesmo paragrafo) nao tem de existir
legadas = set()
for par in re.findall(r'<(?:p|div|td|li|footer)[^>]*>(?:(?!</?(?:p|div|td|li|footer)[ >]).)*?removid.*?</(?:p|div|td|li|footer)>', modelo, re.S | re.I):
    legadas |= {t.lower() for t in re.findall(r'\bt_[a-z0-9_]+\b', par)}
for t in sorted(tabelas_modelo - tabelas_codigo - tabelas_migr - legadas):
    if t.endswith('_aud'):
        continue
    falha('modelo relacional: tabela citada que nao existe', t)

# ------------------------------------------------------------------ 3. regras BR-*
regras = ler(os.path.join(V5, 'regras_negocio.html'))
definidas = set(re.findall(r'<td class="id">\s*(BR-[A-Z0-9-]+?[a-z]?)\s*</td>', regras))
if not definidas:
    falha('regras', 'nenhuma regra encontrada em <td class="id">')
padrao_br = r'BR-[A-Z]+(?:-[A-Z]+)*-\d+[a-z]?'
docs = {f: ler(os.path.join(V5, f)) for f in os.listdir(V5) if f.endswith(('.md', '.html'))}
for f, t in docs.items():
    citadas = set(re.findall(padrao_br, t))
    for c in sorted(citadas - definidas):
        falha('regras: ' + f + ' cita regra nao definida', c)
    # intervalos 'BR-X-01 a BR-X-05' / 'BR-X-01..05'
    for m in re.finditer(r'(BR-[A-Z]+(?:-[A-Z]+)*-)(\d+)\s*(?:a|\.\.|–|-)\s*(?:BR-[A-Z]+(?:-[A-Z]+)*-)?(\d+)\b', t):
        pref, a, b = m.group(1), int(m.group(2)), int(m.group(3))
        if b <= a or b - a > 40:
            continue
        for n in (a, b):
            for cand in (pref + '%02d' % n, pref + str(n)):
                if cand in definidas:
                    break
            else:
                falha('regras: ' + f + ' intervalo com extremo nao definido', pref + str(n))

# ------------------------------------------------------------------ 4. HTML
VAZIAS = {'br', 'hr', 'img', 'meta', 'link', 'input', 'col', 'wbr', 'source', 'area', 'base', 'path', 'rect', 'circle', 'line', 'polyline', 'polygon', 'ellipse', 'use', 'stop'}
OPCIONAIS = {'p', 'li', 'td', 'th', 'tr', 'thead', 'tbody', 'dt', 'dd', 'option'}


class Html(html.parser.HTMLParser):
    def __init__(self):
        super().__init__()
        self.pilha, self.ids, self.hrefs, self.erros = [], set(), set(), []

    def handle_starttag(self, tag, attrs):
        a = dict(attrs)
        if 'id' in a:
            self.ids.add(a['id'])
        if a.get('href', '').startswith('#') and len(a['href']) > 1:
            self.hrefs.add(a['href'][1:])
        if tag not in VAZIAS:
            self.pilha.append((tag, self.getpos()[0]))

    def handle_startendtag(self, tag, attrs):
        a = dict(attrs)
        if 'id' in a:
            self.ids.add(a['id'])

    def handle_endtag(self, tag):
        if tag in VAZIAS:
            return
        while self.pilha and self.pilha[-1][0] != tag and self.pilha[-1][0] in OPCIONAIS:
            self.pilha.pop()
        if self.pilha and self.pilha[-1][0] == tag:
            self.pilha.pop()
        else:
            self.erros.append('</%s> sem abertura (linha %d)' % (tag, self.getpos()[0]))


for f, t in docs.items():
    if not f.endswith('.html'):
        continue
    h = Html()
    h.feed(t)
    for e in h.erros[:10]:
        falha('html: ' + f, e)
    resto = [x for x in h.pilha if x[0] not in OPCIONAIS and x[0] not in ('html', 'body', 'head')]
    for tag, linha in resto[:10]:
        falha('html: ' + f, '<%s> por fechar (linha %d)' % (tag, linha))
    for a in sorted(h.hrefs - h.ids):
        falha('html: ' + f + ' ancora inexistente', '#' + a)

# ------------------------------------------------------------------ 5. seccoes do guia
seccoes = set(re.findall(r'^#{2,4}\s+(\d+(?:\.\d+)*)\.?\s', guia, re.M))
for f, t in docs.items():
    if f == 'api_guide.md':
        refs = re.findall(r'§\s?(\d+(?:\.\d+)*)', t)
    else:
        refs = re.findall(r'api_guide(?:\.md)?\)?\s*(?:,\s*)?§\s?(\d+(?:\.\d+)*)', t)
    for r in sorted(set(refs)):
        if r not in seccoes:
            falha('seccoes: ' + f + ' cita seccao do guia que nao existe', '§' + r)

# ------------------------------------------------------------------ 6. leis
def ultimo_artigo(txt):
    ns = [int(n) for n in re.findall(r'Artigo\s+(\d+)\.?\s*[º°o]', txt)]
    return max(ns) if ns else None


leis = {}
for nome, ficheiro, padrao in (('Lei 20/X/2023', '.lei20.txt', r'Lei\s+(?:n\.?\s*[º°o]\s*)?20/X/2023'),
                               ('DL 3/2010', '.dl3.txt', r'(?:DL|Decreto-Lei)\s+(?:n\.?\s*[º°o]\s*)?3/2010')):
    c = os.path.join(RAIZ, ficheiro)
    if os.path.exists(c):
        leis[nome] = (ultimo_artigo(ler(c)), padrao)
    else:
        avisos.append('sem ' + ficheiro + ': artigos de ' + nome + ' nao conferidos')
for f, t in docs.items():
    texto = re.sub(r'<[^>]+>', ' ', t)
    for nome, (ultimo, padrao) in leis.items():
        if not ultimo:
            continue
        # 'art. 38.º ... da Lei n.º 20/X/2023' ou 'Lei n.º 20/X/2023, art. 38.º' na mesma frase curta
        for m in re.finditer(r'arts?\.\s*(\d+)\.?\s*[º°o][^.;\n]{0,60}?' + padrao + '|' + padrao + r'[^;\n]{0,25}?arts?\.\s*(\d+)\.?\s*[º°o]', texto):
            n = int(m.group(1) or m.group(2))
            if n > ultimo:
                falha('leis: ' + f, '%s art. %d > ultimo artigo (%d)' % (nome, n, ultimo))

# ------------------------------------------------------------------ 7. data da ultima alteracao
# Cada documento diz no cabecalho 'Última alteração: AAAA-MM-DD'. A data nao pode ficar atras do
# ultimo commit que tocou no ficheiro, e um ficheiro com alteracoes por gravar tem de trazer a de hoje.
import datetime
import subprocess


def git(*args):
    try:
        return subprocess.run(['git', *args], cwd=RAIZ, capture_output=True, text=True, encoding='utf-8').stdout.strip()
    except OSError:
        return ''


hoje = datetime.date.today().isoformat()
for f, t in sorted(docs.items()):
    fim_cab = t.find('</header>')
    cabeca = t[:fim_cab] if f.endswith('.html') and fim_cab > 0 else '\n'.join(t.splitlines()[:60])
    m = re.search(r'Última alteração:?\s*(?:<[^>]+>\s*)*(\d{4}-\d{2}-\d{2})', cabeca)
    if not m:
        falha('data: ' + f, "sem 'Última alteração: AAAA-MM-DD' no cabecalho")
        continue
    data = m.group(1)
    rel = os.path.relpath(os.path.join(V5, f), RAIZ).replace(os.sep, '/')
    do_commit = git('log', '-1', '--format=%cs', '--', rel)
    alterado = bool(git('status', '--porcelain', '--', rel))
    if alterado and data != hoje:
        falha('data: ' + f, 'alterado e por gravar, mas diz %s (hoje e %s)' % (data, hoje))
    elif not alterado and do_commit and data < do_commit:
        falha('data: ' + f, 'diz %s, mas o ultimo commit e de %s' % (data, do_commit))

# ------------------------------------------------------------------ 8. ecras: rotas e campos
esquemas = api.get('components', {}).get('schemas', {})
todas_rotas = {norm(p) for p in api['paths']}
n_rotas = n_campos = 0
for f, t in sorted(docs.items()):
    if not f.endswith('.html'):
        continue
    for m in re.finditer(r'<span class="rota">([^<]+)</span>', t):
        r = html.unescape(m.group(1)).strip()
        r = re.sub(r'^(GET|POST|PUT|PATCH|DELETE)\s+', '', r).split('?')[0].replace('…', '...')
        if '...' in r:
            continue
        n_rotas += 1
        if norm(r) not in todas_rotas:
            falha('ecras: ' + f + ' rota que nao existe na API', r)
    for d in sorted(set(re.findall(r'data-dto="([^"]+)"', t)) - set(esquemas)):
        falha('ecras: ' + f + ' DTO que nao existe no openapi.json', d)
    # campos: percorre as etiquetas com a pilha dos data-dto abertos
    pilha = []
    for m in re.finditer(r'<(/?)(\w+)([^>]*)>', t):
        fecho, tag, attrs = m.groups()
        if tag in VAZIAS:
            continue
        if fecho:
            while pilha and pilha[-1][0] != tag:
                pilha.pop()
            if pilha:
                pilha.pop()
            continue
        dto = re.search(r'data-dto="([^"]+)"', attrs)
        pilha.append((tag, dto.group(1) if dto else None))
        campo = re.search(r'data-campo="([^"]+)"', attrs)
        if not campo:
            continue
        n_campos += 1
        dono = next((d for _, d in reversed(pilha) if d), None)
        if not dono:
            falha('ecras: ' + f + ' campo sem data-dto', campo.group(1))
        elif dono in esquemas and campo.group(1) not in (esquemas[dono].get('properties') or {}):
            falha('ecras: ' + f + ' campo que o DTO nao tem', dono + '.' + campo.group(1))

# ------------------------------------------------------------------ 9. ligacoes entre documentos
for f, t in sorted(docs.items()):
    for m in re.finditer(r'(?:href="|\]\()([\w.-]+\.(?:html|md|json))(?:#[^")]*)?[")]', t):
        if not os.path.exists(os.path.join(V5, m.group(1))):
            falha('ligacoes: ' + f + ' aponta para ficheiro que nao existe', m.group(1))

# ------------------------------------------------------------------ 10. origem das regras no codigo
# a coluna Origem do catalogo cita Classe ou Classe.metodo: tem de existir no codigo (fora do sigdi)
fontes = {}
for base, _, fs in os.walk(JAVA):
    if os.sep + 'sigdi' in base:
        continue
    for f in fs:
        if f.endswith('.java'):
            fontes.setdefault(f[:-5], []).append(ler(os.path.join(base, f)))
n_origens = 0
for m in re.finditer(r'<td class="id">(BR-[\w-]+)</td>.*?<td class="src">(.*?)</td>', regras, re.S):
    br, src = m.group(1), re.sub(r'<[^>]+>', '', m.group(2))
    src = re.sub(r'\S*\*\S*|@\w+|\([^)]*\)', ' ', src)  # nomes com * (familias), anotacoes, notas
    for cls, met in re.findall(r'\b([A-Z][a-z][A-Za-z0-9]+)(?:\.([a-z][A-Za-z0-9]*))?', src):
        n_origens += 1
        # o catalogo abrevia ('Create/UpdateJobCommandHandler', 'GetMeLeaveBalances'): basta o prefixo de uma classe
        donos = [c for c in fontes if c == cls] or [c for c in fontes if c.startswith(cls)]
        if not donos:
            falha('regras: origem com classe que nao existe', '%s -> %s' % (br, cls))
        elif met and not re.search(r'\b' + re.escape(met) + r'\s*\(', ''.join(''.join(fontes[c]) for c in donos)):
            falha('regras: origem com metodo que nao existe', '%s -> %s.%s' % (br, cls, met))

# ------------------------------------------------------------------ resultado
print('caminhos da API (RH): %d | documentados: %d' % (len(caminhos_api), len(caminhos_api) - len(sem_doc)))
print('tabelas RH no codigo: %d | no modelo: %d' % (len(tabelas_codigo), len(tabelas_modelo)))
print('regras definidas: %d' % len(definidas))
print('ecras: %d rotas e %d campos conferidos' % (n_rotas, n_campos))
print('origens das regras conferidas no codigo: %d' % n_origens)
for a in avisos:
    print('aviso: ' + a)
grupos = {}
for g, t in falhas:
    grupos.setdefault(g, []).append(t)
for g, ts in grupos.items():
    print('\n%s (%d)' % (g, len(ts)))
    for t in (ts if DETALHE else ts[:15]):
        print('  - ' + t)
    if not DETALHE and len(ts) > 15:
        print('  ... mais %d (--detalhe)' % (len(ts) - 15))
print('\nFALHAS: %d' % len(falhas))
sys.exit(1 if falhas else 0)
