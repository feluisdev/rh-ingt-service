# Contrato REST: Grades

**Base URL**: `api/v1/rh/grades`  
**Controller**: `GradeController` (gerado por IGRP Studio)

---

## GET /grades

Lista escalões com filtros opcionais.

**Query params**: `categoryId` (UUID, opcional), `isActive` (boolean, opcional)  
**Response 200**:
```json
{
  "data": [
    {
      "id": "uuid",
      "categoryId": "uuid",
      "categoryName": "Técnico Superior de 1.ª Classe",
      "gradeNumber": 1,
      "name": "Escalão 1",
      "salaryIndex": 100.50,
      "isActive": true,
      "createdDate": "2026-04-30T10:00:00",
      "createdBy": "admin"
    }
  ],
  "totalElements": 1
}
```

---

## GET /grades/{id}

Consulta um escalão por ID.

**Response 200**: objeto GradeResponse  
**Response 404**: escalão não encontrado

---

## POST /grades

Cria um novo escalão.

**Request body**:
```json
{
  "categoryId": "uuid",
  "gradeNumber": 1,
  "name": "Escalão 1",
  "salaryIndex": 100.50,
  "isActive": true
}
```

**Validações**:
- `categoryId`: obrigatório, deve referenciar uma categoria existente e activa
- `gradeNumber`: obrigatório, inteiro ≥ 1, único dentro da categoria
- `name`: obrigatório, máx. 150 caracteres
- `salaryIndex`: opcional, decimal (12,2)

**Response 201**: GradeResponse criado  
**Response 400**: `gradeNumber` < 1  
**Response 404**: categoryId não existe  
**Response 409**: par (categoryId, gradeNumber) duplicado

---

## PUT /grades/{id}

Actualiza um escalão existente. Os campos `categoryId` e `gradeNumber` são **imutáveis** — rejeitados com 400 se diferirem dos valores originais.

**Request body**:
```json
{
  "name": "Escalão 1 — actualizado",
  "salaryIndex": 110.00
}
```

**Response 200**: GradeResponse actualizado  
**Response 400**: tentativa de alterar `categoryId` ou `gradeNumber`  
**Response 404**: escalão não encontrado

---

## DELETE /grades/{id}

Desactiva um escalão (soft delete — `is_active = false`).

**Response 200**: GradeResponse desactivado  
**Response 404**: escalão não encontrado  
**Response 409**: escalão referenciado em enquadramento profissional activo

---

## PUT /grades/{id}/activate

Reactiva um escalão.

**Response 200**: GradeResponse reactivado  
**Response 404**: escalão não encontrado  
**Response 409**: escalão já está activo
