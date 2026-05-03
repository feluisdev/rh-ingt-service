# Contrato REST: Careers

**Base URL**: `api/v1/rh/careers`  
**Controller**: `CareerController` (gerado por IGRP Studio)

---

## GET /careers

Lista carreiras com filtro opcional.

**Query params**: `isActive` (boolean, opcional)  
**Response 200**:
```json
{
  "data": [
    {
      "id": "uuid",
      "code": "TS",
      "name": "Técnica Superior",
      "description": "Carreira técnica superior do PCFR",
      "isActive": true,
      "createdDate": "2026-04-30T10:00:00",
      "createdBy": "admin"
    }
  ],
  "totalElements": 1
}
```

---

## GET /careers/{id}

Consulta uma carreira por ID.

**Response 200**: objeto CareerResponse  
**Response 404**: carreira não encontrada

---

## GET /careers/{id}/categories

Lista as categorias de uma carreira.

**Response 200**: lista de CategoryResponse  
**Response 404**: carreira não encontrada

---

## POST /careers

Cria uma nova carreira.

**Request body**:
```json
{
  "code": "TS",
  "name": "Técnica Superior",
  "description": "Carreira técnica superior do PCFR",
  "isActive": true
}
```

**Validações**:
- `code`: obrigatório, máx. 50 caracteres, único globalmente
- `name`: obrigatório, máx. 150 caracteres

**Response 201**: CareerResponse criado  
**Response 409**: code duplicado

---

## PUT /careers/{id}

Actualiza uma carreira existente.

**Request body**: mesmo esquema de POST (todos os campos editáveis)  
**Response 200**: CareerResponse actualizado  
**Response 404**: carreira não encontrada  
**Response 409**: code já em uso por outra carreira

---

## DELETE /careers/{id}

Desactiva uma carreira (soft delete — `is_active = false`).

**Response 200**: CareerResponse desactivado  
**Response 404**: carreira não encontrada  
**Response 409**: carreira tem categorias activas

---

## PUT /careers/{id}/activate

Reactiva uma carreira.

**Response 200**: CareerResponse reactivado  
**Response 404**: carreira não encontrada  
**Response 409**: carreira já está activa
