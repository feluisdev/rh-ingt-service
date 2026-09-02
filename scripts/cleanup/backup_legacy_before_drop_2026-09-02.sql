--
-- PostgreSQL database dump
--

-- Dumped from database version 17.5
-- Dumped by pg_dump version 17.5

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: t_employee_professional_assignments_aud; Type: TABLE DATA; Schema: audit_schema; Owner: postgres
--

INSERT INTO audit_schema.t_employee_professional_assignments_aud (id, rev, revtype, created_by, created_date, last_modified_by, last_modified_date, career_id, cargo_id, category_id, data_fim, data_inicio, funcionario_id, function_id, grade_id, is_current, unidade_organica_id) VALUES ('2b5e342b-8541-4bc1-a1da-0da481fd5904', 1253, 0, 'anonymousUser', '2026-05-20 11:03:49.542928', 'anonymousUser', '2026-05-20 11:03:49.542928', NULL, '12433bcc-e4e3-435f-845c-7717d89facdb', NULL, NULL, '2025-05-20', 'aa261725-ee0f-4c61-85b3-595d0953cd58', NULL, NULL, true, 'd55418e7-f34e-44fc-a96d-97fc355ca01f');
INSERT INTO audit_schema.t_employee_professional_assignments_aud (id, rev, revtype, created_by, created_date, last_modified_by, last_modified_date, career_id, cargo_id, category_id, data_fim, data_inicio, funcionario_id, function_id, grade_id, is_current, unidade_organica_id) VALUES ('e790c40b-d987-4a86-bc31-b33a6fb108f4', 1254, 0, 'anonymousUser', '2026-05-20 11:06:44.88145', 'anonymousUser', '2026-05-20 11:06:44.88145', 'a567155e-c000-480e-876d-bd783feff497', '12433bcc-e4e3-435f-845c-7717d89facdb', 'b873943f-48d7-4b18-b5c5-573f375b6c0f', NULL, '2025-05-20', '8463e480-2422-48c5-884f-7c51f2a38e9f', NULL, 'f241273a-a738-4657-80cd-39c0d74dc107', true, 'd55418e7-f34e-44fc-a96d-97fc355ca01f');
INSERT INTO audit_schema.t_employee_professional_assignments_aud (id, rev, revtype, created_by, created_date, last_modified_by, last_modified_date, career_id, cargo_id, category_id, data_fim, data_inicio, funcionario_id, function_id, grade_id, is_current, unidade_organica_id) VALUES ('f65fe457-1f78-4588-ba7f-a9522ca65571', 1603, 0, 'anonymousUser', '2026-05-26 21:44:00.825095', 'anonymousUser', '2026-05-26 21:44:00.825095', 'a567155e-c000-480e-876d-bd783feff497', '12433bcc-e4e3-435f-845c-7717d89facdb', 'b873943f-48d7-4b18-b5c5-573f375b6c0f', NULL, '2024-03-01', '2f27bc00-62e3-4175-88f2-8be377e65c6f', NULL, 'f241273a-a738-4657-80cd-39c0d74dc107', true, 'd55418e7-f34e-44fc-a96d-97fc355ca01f');
INSERT INTO audit_schema.t_employee_professional_assignments_aud (id, rev, revtype, created_by, created_date, last_modified_by, last_modified_date, career_id, cargo_id, category_id, data_fim, data_inicio, funcionario_id, function_id, grade_id, is_current, unidade_organica_id) VALUES ('a4662934-7c5c-486d-ada8-ced2e2d1abdd', 1604, 0, 'anonymousUser', '2026-05-26 21:44:47.223524', 'anonymousUser', '2026-05-26 21:44:47.223524', 'a567155e-c000-480e-876d-bd783feff497', '12433bcc-e4e3-435f-845c-7717d89facdb', 'b873943f-48d7-4b18-b5c5-573f375b6c0f', NULL, '2024-03-01', '3304a9f7-03f8-48de-be6d-74f24f9c3d1f', NULL, 'f241273a-a738-4657-80cd-39c0d74dc107', true, 'd55418e7-f34e-44fc-a96d-97fc355ca01f');
INSERT INTO audit_schema.t_employee_professional_assignments_aud (id, rev, revtype, created_by, created_date, last_modified_by, last_modified_date, career_id, cargo_id, category_id, data_fim, data_inicio, funcionario_id, function_id, grade_id, is_current, unidade_organica_id) VALUES ('842b10aa-bad7-4efe-8dbf-65c5d8cbac74', 1652, 0, 'anonymousUser', '2026-05-26 22:16:15.431847', 'anonymousUser', '2026-05-26 22:16:15.431847', 'a567155e-c000-480e-876d-bd783feff497', '12433bcc-e4e3-435f-845c-7717d89facdb', 'b873943f-48d7-4b18-b5c5-573f375b6c0f', NULL, '2025-01-01', '3f0a68c6-d2b4-43ae-9eab-938f6026dff6', NULL, 'f241273a-a738-4657-80cd-39c0d74dc107', true, 'd55418e7-f34e-44fc-a96d-97fc355ca01f');
INSERT INTO audit_schema.t_employee_professional_assignments_aud (id, rev, revtype, created_by, created_date, last_modified_by, last_modified_date, career_id, cargo_id, category_id, data_fim, data_inicio, funcionario_id, function_id, grade_id, is_current, unidade_organica_id) VALUES ('c9aca2a1-eaee-4dd8-92d1-1df83ebb2bf0', 1653, 0, 'anonymousUser', '2026-05-26 22:16:15.753039', 'anonymousUser', '2026-05-26 22:16:15.753039', 'a567155e-c000-480e-876d-bd783feff497', '12433bcc-e4e3-435f-845c-7717d89facdb', 'b873943f-48d7-4b18-b5c5-573f375b6c0f', NULL, '2025-01-01', 'e482f90d-eff7-4fdb-a759-9c5617a2985a', NULL, 'f241273a-a738-4657-80cd-39c0d74dc107', true, 'd55418e7-f34e-44fc-a96d-97fc355ca01f');
INSERT INTO audit_schema.t_employee_professional_assignments_aud (id, rev, revtype, created_by, created_date, last_modified_by, last_modified_date, career_id, cargo_id, category_id, data_fim, data_inicio, funcionario_id, function_id, grade_id, is_current, unidade_organica_id) VALUES ('f727a9bc-b7cb-4f40-8aa2-1529adb9ff56', 1654, 0, 'anonymousUser', '2026-05-26 22:16:15.945381', 'anonymousUser', '2026-05-26 22:16:15.945381', 'a567155e-c000-480e-876d-bd783feff497', '12433bcc-e4e3-435f-845c-7717d89facdb', 'b873943f-48d7-4b18-b5c5-573f375b6c0f', NULL, '2025-01-01', '3e91ed7a-6694-46b4-8033-febad3edec4f', NULL, 'f241273a-a738-4657-80cd-39c0d74dc107', true, 'd55418e7-f34e-44fc-a96d-97fc355ca01f');
INSERT INTO audit_schema.t_employee_professional_assignments_aud (id, rev, revtype, created_by, created_date, last_modified_by, last_modified_date, career_id, cargo_id, category_id, data_fim, data_inicio, funcionario_id, function_id, grade_id, is_current, unidade_organica_id) VALUES ('d6b68704-5436-4fa8-9be5-308662565f5a', 1852, 0, 'anonymousUser', '2026-05-28 11:26:09.353575', 'anonymousUser', '2026-05-28 11:26:09.353575', NULL, '12433bcc-e4e3-435f-845c-7717d89facdb', NULL, NULL, '2026-05-28', 'da130a3b-beb1-475f-8ecd-4a3ef7a79ae5', NULL, NULL, true, 'd55418e7-f34e-44fc-a96d-97fc355ca01f');


--
-- Data for Name: t_employee_unit_assignments_aud; Type: TABLE DATA; Schema: audit_schema; Owner: postgres
--



--
-- Data for Name: t_cargo; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.t_cargo (id, created_by, created_date, last_modified_by, last_modified_date, codigo, descricao, estado, nivel_hierarquico, nome, salario_base) VALUES ('11111111-1111-1111-1111-111111111111', 'system', '2026-05-01 06:33:31.172968', NULL, NULL, 'CT-001', NULL, 'A', 1, 'Cargo de Teste', 50000.00);


--
-- Data for Name: t_contrato_entity; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: t_documento; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: t_employee_professional_assignments; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: t_employee_unit_assignments; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: t_professional_situation; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.t_professional_situation (id, code, description, is_active, created_date, created_by, last_modified_date, last_modified_by, counts_seniority, eligible_for_progression) VALUES ('9544f0df-ac98-40e2-91c9-e026c4e15cd2', 'COMISSIONADO', 'Comissionado', true, '2026-04-29 23:18:54.582902+00', 'system', NULL, NULL, true, true);
INSERT INTO public.t_professional_situation (id, code, description, is_active, created_date, created_by, last_modified_date, last_modified_by, counts_seniority, eligible_for_progression) VALUES ('95948733-a666-47aa-9bf6-a17a6a31763f', 'ESTAGIARIO', 'Estagiário', true, '2026-04-29 23:18:54.582902+00', 'system', NULL, NULL, true, true);
INSERT INTO public.t_professional_situation (id, code, description, is_active, created_date, created_by, last_modified_date, last_modified_by, counts_seniority, eligible_for_progression) VALUES ('24c64ac7-6259-4c99-80c5-e3c1990d69d6', 'DESTACADO', 'Destacado', true, '2026-04-29 23:18:54.582902+00', 'system', NULL, NULL, true, true);
INSERT INTO public.t_professional_situation (id, code, description, is_active, created_date, created_by, last_modified_date, last_modified_by, counts_seniority, eligible_for_progression) VALUES ('4a6fd74e-47e0-40fa-b504-bdd7d1c81b5c', 'REQUISITADO', 'Requisitado', true, '2026-04-29 23:18:54.582902+00', 'system', NULL, NULL, true, true);
INSERT INTO public.t_professional_situation (id, code, description, is_active, created_date, created_by, last_modified_date, last_modified_by, counts_seniority, eligible_for_progression) VALUES ('7d33f792-2383-4df1-a642-6718d01dccfc', 'EFETIVO', 'Efectivo (actualizado)', true, '2026-04-29 23:18:54.582902+00', 'system', '2026-04-30 15:16:30.008377+00', 'anonymousUser', true, true);
INSERT INTO public.t_professional_situation (id, code, description, is_active, created_date, created_by, last_modified_date, last_modified_by, counts_seniority, eligible_for_progression) VALUES ('0e48870e-f196-400b-8f72-a6fa13932ed3', 'CONTRATADO', 'Activo (editado)', true, '2026-04-29 23:18:54.582902+00', 'system', '2026-05-03 17:30:24.0676+00', 'anonymousUser', true, true);
INSERT INTO public.t_professional_situation (id, code, description, is_active, created_date, created_by, last_modified_date, last_modified_by, counts_seniority, eligible_for_progression) VALUES ('c4562ad6-728a-4432-a5fa-89e65e35dd4c', 'TST_PS', NULL, false, '2026-05-03 17:49:44.722664+00', 'anonymousUser', '2026-05-03 17:49:44.788271+00', 'anonymousUser', true, true);


--
-- PostgreSQL database dump complete
--

