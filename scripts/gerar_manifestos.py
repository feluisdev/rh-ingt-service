"""Gera os manifestos .igrpstudio/ a partir do Java ja escrito (controller, DTO, entidade).

Os controladores em interfaces/rest/ sao "gerados pelo IGRP Studio" a partir dos manifestos; quando o Java
e escrito primeiro, este script escreve o manifesto correspondente para os dois ficarem a par.

Uso:
    python scripts/gerar_manifestos.py <ficheiro.java> [<ficheiro.java> ...]

Cada ficheiro e classificado pelo caminho: interfaces/rest/*Controller.java -> controllers/,
application/dto/*.java -> dto/, infrastructure/persistence/entity/*Entity.java -> models/.
O modulo e o segmento a seguir a RH_Service/. Reescreve o manifesto se ja existir.
"""
import json
import os
import re
import sys

RAIZ = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
JAVA = os.path.join(RAIZ, 'src', 'main', 'java', 'cv', 'igrp', 'RH_Service')
STUDIO = os.path.join(RAIZ, '.igrpstudio')

TIPOS = {
    'String': 'string', 'LocalDate': 'date', 'LocalDateTime': 'datetime', 'LocalTime': 'time',
    'Integer': 'integer', 'int': 'integer', 'Long': 'long', 'long': 'long', 'BigDecimal': 'decimal',
    'Boolean': 'boolean', 'boolean': 'boolean', 'UUID': 'uuid', 'Double': 'decimal', 'double': 'decimal',
    'Short': 'integer', 'short': 'integer', 'YearMonth': 'string', 'Object': 'string',
}
NOMES_RESPOSTA = {'200': 'OK', '201': 'Created', '202': 'Accepted', '204': 'No Content'}

_indice_dto = None


def indice_dto():
    """Nome da classe DTO -> modulo onde vive."""
    global _indice_dto
    if _indice_dto is None:
        _indice_dto = {}
        for base, _, fs in os.walk(JAVA):
            if base.replace('\\', '/').endswith('application/dto'):
                mod = os.path.relpath(base, JAVA).replace('\\', '/').split('/')[0]
                for f in fs:
                    if f.endswith('.java'):
                        _indice_dto.setdefault(f[:-5], mod)
    return _indice_dto


def modulo_de(caminho):
    rel = os.path.relpath(os.path.abspath(caminho), JAVA).replace('\\', '/')
    return rel.split('/')[0]


def sem_comentarios(src):
    src = re.sub(r'/\*.*?\*/', '', src, flags=re.S)
    return re.sub(r'//[^\n]*', '', src)


def gravar(mod, pasta, nome, dados):
    d = os.path.join(STUDIO, mod, pasta)
    os.makedirs(d, exist_ok=True)
    f = os.path.join(d, nome + '.json')
    with open(f, 'w', encoding='utf-8', newline='\n') as h:
        json.dump(dados, h, ensure_ascii=False, indent=2)
        h.write('\n')
    print('manifesto:', os.path.relpath(f, RAIZ))


def atributo_dto(tipo, nome):
    lista = re.match(r'(?:List|Set|Collection)<\s*([\w.]+)\s*>', tipo)
    base = lista.group(1) if lista else tipo
    base = base.split('.')[-1]
    a = {}
    if base in TIPOS:
        a = {'type': TIPOS[base], 'name': nome, 'objectType': 'java'}
    elif base.startswith('Map'):
        a = {'type': 'string', 'name': nome, 'objectType': 'java'}
    elif base in indice_dto():
        a = {'type': base, 'name': nome, 'objectType': 'dto', 'module': indice_dto()[base]}
    else:   # enum ou outro tipo: vai como texto
        a = {'type': 'string', 'name': nome, 'objectType': 'java'}
    if lista:
        a['collectionType'] = 'list'
    a['required'] = False
    return a


def dto(caminho):
    src = sem_comentarios(open(caminho, encoding='utf-8').read())
    classe = re.search(r'class\s+(\w+)', src).group(1)
    corpo = src[src.index('{', src.index('class ' + classe)) + 1:]
    attrs = []
    for m in re.finditer(r'^\s*private\s+(?!static)(?:final\s+)?([\w.<>, ]+?)\s+(\w+)\s*(?:=[^;]*)?;', corpo, re.M):
        attrs.append(atributo_dto(m.group(1).strip(), m.group(2)))
    nome = re.sub(r'DTO$', '', classe)
    gravar(modulo_de(caminho), 'dto', classe, {
        'type': 'dto', 'name': nome, 'template': 'classic', 'module': modulo_de(caminho), 'attributes': attrs})


def schema_de_tipo(tipo, mod_padrao):
    tipo = tipo.strip()
    lista = re.match(r'(?:List|Set|Collection)<\s*([\w.]+)\s*>', tipo)
    base = (lista.group(1) if lista else tipo).split('.')[-1]
    if base in ('byte[]', 'String', 'Resource'):
        return None
    return {'type': base, 'objectType': 'dto', 'name': 'data', 'collectionType': 'list' if lista else 'none',
            'module': indice_dto().get(base, mod_padrao)}


def controller(caminho):
    src_c = open(caminho, encoding='utf-8').read()
    src = sem_comentarios(src_c)
    mod = modulo_de(caminho)
    classe = re.search(r'class\s+(\w+)', src).group(1)
    base = re.search(r'@RequestMapping\(\s*(?:path\s*=\s*|value\s*=\s*)?"([^"]*)"', src)
    tag = re.search(r'@Tag\([^)]*?description\s*=\s*"((?:[^"\\]|\\.)*)"', src, re.S)
    acoes = []
    padrao = re.compile(r'@(Get|Post|Put|Patch|Delete)Mapping\(([^)]*)\)(.*?)public\s+ResponseEntity<(.+?)>\s+(\w+)\s*\((.*?)\)\s*\{', re.S)
    for m in padrao.finditer(src):
        verbo, args_map, anot, ret, metodo, params = m.groups()
        caminho_m = re.search(r'"([^"]*)"', args_map)
        produces = re.search(r'produces\s*=\s*"([^"]+)"', args_map)
        a = {'actionName': metodo, 'method': verbo.upper(), 'path': caminho_m.group(1) if caminho_m else ''}
        summ = re.search(r'@Operation\([^)]*?summary\s*=\s*"((?:[^"\\]|\\.)*)"', anot, re.S)
        if summ:
            a['summary'] = summ.group(1)
        pvs, rps, body = [], [], None
        for p in re.split(r',\s*(?=@(?:PathVariable|RequestParam|RequestBody|RequestHeader|ModelAttribute))', params.strip()):
            p = p.strip()
            if not p:
                continue
            pv = re.match(r'@PathVariable(?:\([^)]*\))?\s+([\w<>]+)\s+(\w+)', p)
            rp = re.match(r'@RequestParam\(([^)]*)\)\s+(?:@[\w.]+(?:\((?:[^()]|\([^()]*\))*\))?\s+)*([\w<>]+)\s+(\w+)', p)
            rb = re.match(r'@RequestBody(?:\([^)]*\))?\s+([\w<>.]+)\s+(\w+)', p)
            if pv:
                pvs.append({'name': pv.group(2), 'type': TIPOS.get(pv.group(1), 'string')})
            elif rp:
                nome = re.search(r'(?:value|name)\s*=\s*"([^"]+)"', rp.group(1)) or re.search(r'"([^"]+)"', rp.group(1))
                req = not re.search(r'required\s*=\s*false', rp.group(1))
                rps.append({'type': TIPOS.get(rp.group(2), 'string'), 'name': nome.group(1) if nome else rp.group(3),
                            'isRequired': req})
            elif rb:
                t = rb.group(1).split('.')[-1]
                body = {'content': {'application/json': {'schema': {
                    'type': re.sub(r'DTO$', '', t), 'collectionType': 'none', 'name': 'data', 'objectType': 'dto',
                    'module': indice_dto().get(t, mod)}}}}
        if pvs:
            a['pathVariables'] = pvs
        if rps:
            a['requestParams'] = rps
        if body:
            a['requestBody'] = body
        codigo = re.search(r'@ApiResponse\(\s*responseCode\s*=\s*"(\d+)"', anot)
        codigo = codigo.group(1) if codigo else '200'
        sch = schema_de_tipo(ret, mod)
        if produces and 'csv' in produces.group(1):
            conteudo = {'text/csv': {'schema': {'type': 'string', 'collectionType': 'none'}}}
        elif produces and 'pdf' in produces.group(1):
            conteudo = {'application/pdf': {'schema': {'type': 'string', 'collectionType': 'none'}}}
        elif sch:
            conteudo = {'application/json': {'schema': sch}}
        else:
            conteudo = {'application/json': {'schema': {'type': 'string', 'collectionType': 'none'}}}
        a['responses'] = {codigo: {'name': NOMES_RESPOSTA.get(codigo, 'OK'), 'content': conteudo}}
        acoes.append(a)
    nome = re.sub(r'Controller$', '', classe)
    gravar(mod, 'controllers', classe, {
        'type': 'controller', 'name': nome, 'module': mod, 'basePath': base.group(1) if base else '',
        'description': tag.group(1) if tag else '', 'actions': acoes})


def entidade(caminho):
    src = sem_comentarios(open(caminho, encoding='utf-8').read())
    mod = modulo_de(caminho)
    classe = re.search(r'class\s+(\w+)', src).group(1)
    tabela = re.search(r'@Table\(\s*name\s*=\s*"(\w+)"', src).group(1)
    attrs = []
    corpo = src[src.index('{', src.index('class ' + classe)) + 1:]
    padrao = re.compile(r'((?:@[\w.]+(?:\((?:[^()]|\([^()]*\))*\))?\s*)*)private\s+([\w.<>, ]+?)\s+(\w+)\s*(?:=[^;]*)?;', re.S)
    for m in padrao.finditer(corpo):
        anots, tipo, nome = m.group(1), m.group(2).strip(), m.group(3)
        if 'static' in tipo or '@Transient' in anots:
            continue
        rel = re.search(r'@(ManyToOne|OneToOne)', anots)
        if rel:
            jc = re.search(r'@JoinColumn\([^)]*name\s*=\s*"(\w+)"', anots)
            attrs.append({'name': nome, 'type': 'relation', 'relation': {
                'type': rel.group(1), 'entity': tipo.split('.')[-1],
                'fetchType': 'lazy' if 'LAZY' in anots else 'eager',
                'joinColumn': jc.group(1) if jc else nome + '_id', 'module': mod}})
            continue
        if re.search(r'@(OneToMany|ManyToMany|ElementCollection)', anots):
            continue
        base = tipo.split('.')[-1]
        a = {'name': nome, 'type': TIPOS.get(base, 'string'), 'objectType': 'java',
             'primaryKey': '@Id' in anots}
        col = re.search(r'@Column\(([^)]*)\)', anots)
        a['nullable'] = not (col and re.search(r'nullable\s*=\s*false', col.group(1))) and '@Id' not in anots
        ln = col and re.search(r'length\s*=\s*(\d+)', col.group(1))
        if ln:
            a['length'] = int(ln.group(1))
        if 'columnDefinition = "text"' in anots or '@Lob' in anots:
            a['type'] = 'text'
        if 'SqlTypes.JSON' in anots:
            a['type'] = 'text'
        attrs.append(a)
    gravar(mod, 'models', classe, {
        'type': 'model', 'module': mod, 'name': classe, 'tableName': tabela,
        'audit': '@Audited' in src, 'crud': False, 'revision': '@Audited' in src, 'attributes': attrs})


def main(args):
    for f in args:
        p = f.replace('\\', '/')
        if '/interfaces/rest/' in p and p.endswith('Controller.java'):
            controller(f)
        elif '/application/dto/' in p:
            dto(f)
        elif '/persistence/entity/' in p and p.endswith('Entity.java'):
            entidade(f)
        else:
            print('ignorado (nao e controller, DTO nem entidade):', f)


if __name__ == '__main__':
    main(sys.argv[1:])
