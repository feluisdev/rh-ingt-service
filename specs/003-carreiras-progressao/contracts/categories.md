# Contrato REST: Categories

**Base URL**: `api/v1/rh/categories`  
**Controller**: `CategoryController` (gerado por IGRP Studio)

---

## GET /categories

Lista categorias com filtros opcionais.

**Query params**: `careerId` (UUID, opcional), `isActive` (boolean, opcional)  
**Response 200**:
```json
{
  "data": [
    {
      "id": "uuid",
      "careerId": "uuid",
      "careerName": "Técnica Superior",
      "code": "TS1",
      "name": "Técnico Superior de 1.ª Classe",
      "description": null,
      "isActive": true,
      "createdDate": "2026-04-30T10:00:00",
      "createdBy": "admin"
    }
  ],
  "totalElements": 1
}
```

---

## GET /categories/{id}

Consulta uma categoria por ID.

**Response 200**: objeto CategoryResponse  
**Response 404**: categoria não encontrada

---

## GET /categories/{id}/grades

Lista os escalões de uma categoria, ordenados por `gradeNumber` ascendente.

**Response 200**: lista de GradeResponse  
**Response 404**: categoria não encontrada

---

## POST /categories

Cria uma nova categoria.

**Request body**:
```json
{
  "careerId": "uuid",
  "code": "TS1",
  "name": "Técnico Superior de 1.ª Classe",
  "description": null,
  "isActive": true
}
```

**Validações**:
- `careerId`: obrigatório, deve referenciar uma carreira existente e activa
- `code`: obrigatório, máx. 50 caracteres, único dentro da carreira (`careerId`)
- `name`: obrigatório, máx. 150 caracteres

**Response 201**: CategoryResponse criado  
**Response 404**: careerId não existe  
**Response 409**: par (careerId, code) duplicado

---

## PUT /categories/{id}

Actualiza uma categoria existente. Os campos `careerId` e `code` são **imutáveis** — se fornecidos, são ignorados (ou rejeitados com 400 se diferirem dos valores originais).

**Request body**:
```json
{
  "name": "Técnico Superior de 1.ª Classe",
  "description": "Descrição actualizada"
}
```

**Response 200**: CategoryResponse actualizado  
**Response 400**: tentativa de alterar `careerId` ou `code`  
**Response 404**: categoria não encontrada

---

## DELETE /categories/{id}

Desactiva uma categoria (soft delete — `is_active = false`).

**Response 200**: CategoryResponse desactivado  
**Response 404**: categoria não encontrada  
**Response 409**: categoria tem escalões activos

---

## PUT /categories/{id}/activate

Reactiva uma categoria.

**Response 200**: CategoryResponse reactivado  
**Response 404**: categoria não encontrada  
**Response 409**: categoria já está activa
