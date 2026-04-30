# Quickstart: Carreiras e Progressão

**Feature**: 003-carreiras-progressao  
**Base URL**: `http://localhost:8091/api/v1/rh`

## Cenários de Teste (Integration)

### Cenário 1 — CRUD completo de Carreiras

```
# 1. Criar carreira
POST /careers
Body: { "code": "TS", "name": "Técnica Superior", "description": "Carreira técnica superior PCFR" }
Expect: 201, body.code == "TS", body.isActive == true

# 2. Listar carreiras
GET /careers
Expect: 200, data.length >= 1

# 3. Filtrar por activas
GET /careers?isActive=true
Expect: 200, todos body.isActive == true

# 4. Consultar por ID
GET /careers/{id}
Expect: 200, body.id == {id}

# 5. Actualizar
PUT /careers/{id}
Body: { "code": "TS", "name": "Técnica Superior (actualizado)", "description": "Desc actualizada" }
Expect: 200, body.name == "Técnica Superior (actualizado)"

# 6. Duplicado deve falhar
POST /careers
Body: { "code": "TS", "name": "Outra TS" }
Expect: 409

# 7. Desactivar
DELETE /careers/{id}
Expect: 200, body.isActive == false

# 8. Reactivar
PUT /careers/{id}/activate
Expect: 200, body.isActive == true

# 9. Não encontrado
GET /careers/00000000-0000-0000-0000-000000000000
Expect: 404
```

---

### Cenário 2 — CRUD completo de Categorias

```
# Pré-requisito: carreira "TS" criada (ver Cenário 1)

# 1. Criar categoria
POST /categories
Body: { "careerId": "{career_id}", "code": "TS1", "name": "Técnico Superior de 1.ª Classe" }
Expect: 201, body.code == "TS1", body.careerId == "{career_id}"

# 2. Mesmo code, carreira diferente — deve ser aceite
POST /careers
Body: { "code": "TA", "name": "Técnica Administrativa" }
# → obter {career2_id}
POST /categories
Body: { "careerId": "{career2_id}", "code": "TS1", "name": "TS1 noutra carreira" }
Expect: 201

# 3. Mesmo code, mesma carreira — deve falhar
POST /categories
Body: { "careerId": "{career_id}", "code": "TS1", "name": "Duplicado" }
Expect: 409

# 4. Listar categorias de uma carreira
GET /careers/{career_id}/categories
Expect: 200, todos os items têm careerId == {career_id}

# 5. Filtrar por careerId
GET /categories?careerId={career_id}
Expect: 200, resultado idêntico ao sub-recurso acima

# 6. Actualizar (apenas name/description)
PUT /categories/{id}
Body: { "name": "TS1 — actualizado", "description": "Desc" }
Expect: 200

# 7. Tentativa de alterar career_id — deve falhar
PUT /categories/{id}
Body: { "careerId": "{career2_id}", "name": "TS1" }
Expect: 400

# 8. Desactivar categoria
DELETE /categories/{id}
Expect: 200, body.isActive == false

# 9. Bloquear desactivação de carreira com categorias activas
# (criar nova categoria activa primeiro)
POST /categories
Body: { "careerId": "{career_id}", "code": "TS2", "name": "Técnico Superior de 2.ª Classe" }
DELETE /careers/{career_id}
Expect: 409
```

---

### Cenário 3 — CRUD completo de Escalões

```
# Pré-requisito: categoria "TS1" criada (ver Cenário 2)

# 1. Criar escalão
POST /grades
Body: { "categoryId": "{category_id}", "gradeNumber": 1, "name": "Escalão 1", "salaryIndex": 100.50 }
Expect: 201, body.gradeNumber == 1, body.salaryIndex == 100.50

# 2. Escalão sem salary_index (opcional)
POST /grades
Body: { "categoryId": "{category_id}", "gradeNumber": 2, "name": "Escalão 2" }
Expect: 201, body.salaryIndex == null

# 3. Duplicado — deve falhar
POST /grades
Body: { "categoryId": "{category_id}", "gradeNumber": 1, "name": "Duplicado" }
Expect: 409

# 4. gradeNumber inválido — deve falhar
POST /grades
Body: { "categoryId": "{category_id}", "gradeNumber": 0, "name": "Inválido" }
Expect: 400

# 5. Listar escalões de categoria (ordenados por gradeNumber)
GET /categories/{category_id}/grades
Expect: 200, items ordenados ascendentemente por gradeNumber

# 6. Actualizar (apenas name/salaryIndex)
PUT /grades/{id}
Body: { "name": "Escalão 1 — actualizado", "salaryIndex": 110.00 }
Expect: 200

# 7. Tentativa de alterar category_id — deve falhar
PUT /grades/{id}
Body: { "categoryId": "outro-uuid", "name": "Escalão 1" }
Expect: 400

# 8. Desactivar escalão
DELETE /grades/{id}
Expect: 200, body.isActive == false

# 9. Bloquear desactivação de categoria com escalões activos
# (o escalão 2 ainda está activo)
DELETE /categories/{category_id}
Expect: 409
```

---

### Cenário 4 — Auditoria

```
# Após criar e actualizar uma carreira:
GET /careers/{id}/history
Expect: 200, lista com pelo menos 2 entradas (INSERT + UPDATE)

# Após criar e actualizar uma categoria:
GET /categories/{id}/history
Expect: 200, lista com revisões

# Após criar e desactivar um escalão:
GET /grades/{id}/history
Expect: 200, lista com revisões incluindo o soft delete
```
