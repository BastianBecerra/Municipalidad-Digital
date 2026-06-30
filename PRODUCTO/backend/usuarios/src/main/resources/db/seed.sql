-- =============================================================
--  SEED DE DATOS MOCK – Municipalidad Digital
--  Contraseñas (BCrypt, factor 10):
--    Admin1234!   → $2a$10$jC8.oK4Rdx2aoU.ecQteg.qLONtlM/icTvNcHea8dDNXtbzte2ud6
--    Funcio123!   → $2a$10$CIuhy.Om6YylQUDBZCSQtuV58CwJJzJvdNIM9OD/34cLgxvH3DPka
--    Vecino123!   → $2a$10$iwuKEntyKGNxmF4oTIQuge6l9FjP78aATPRDVye7MF3f6LniTXy0e
--
--  Todos los usuarios también tienen password_clave_unica (mismos hashes).
-- =============================================================

-- ──────────────────────────────────────────────────────────────
-- 1. TERRITORIOS
--    ID 1 → Municipalidad de Providencia         (admins + funcionarios municipales)
--    ID 2 → 2ª Comisaría Carabineros Providencia (funcionarios Carabineros)
--    ID 3 → Brigada Invest. Criminal PDI          (funcionarios PDI)
--    ID 4 → Junta de Vecinos Villa Los Aromos     (vecinos 1-5)
--    ID 5 → Junta de Vecinos Los Quillayes        (vecinos 6-10)
-- ──────────────────────────────────────────────────────────────
INSERT INTO territorios (
    nombre, tipo, numero_unidad_vecinal,
    comuna, region, direccion_sede,
    latitud, longitud,
    limite_norte, limite_sur, limite_este, limite_oeste,
    email, telefono, presidente, descripcion, activo,
    fecha_creacion, ultima_actualizacion
) VALUES
-- ID 1
(
    'Municipalidad de Providencia', 'SECTOR', 'MUN-PRO',
    'Providencia', 'Región Metropolitana', 'Av. Providencia 1234, Providencia',
    -33.4289, -70.6093,
    'Av. Andrés Bello', 'Av. Ossa', 'Av. Tobalaba', 'Av. Pedro de Valdivia',
    'contacto@providencia.cl', '+56227241700',
    'Alcalde de Providencia',
    'Edificio principal de la Municipalidad de Providencia', true,
    NOW(), NOW()
),
-- ID 2
(
    '2ª Comisaría Carabineros de Providencia', 'SECTOR', 'CAR-PRO-02',
    'Providencia', 'Región Metropolitana', 'Av. Italia 1000, Providencia',
    -33.4391, -70.6142,
    'Av. Irarrázaval', 'Av. Grecia', 'Av. Tobalaba', 'Av. Vicuña Mackenna',
    'comisaria2.providencia@carabineros.cl', '+56222226020',
    'Capitán a/c 2ª Comisaría',
    'Cuartel de la 2ª Comisaría de Carabineros de Providencia', true,
    NOW(), NOW()
),
-- ID 3
(
    'Brigada de Investigación Criminal PDI – Providencia', 'SECTOR', 'PDI-BIC-PRO',
    'Providencia', 'Región Metropolitana', 'Av. Suecia 286, Providencia',
    -33.4248, -70.6050,
    'Av. Ricardo Lyon', 'Av. Eliodoro Yáñez', 'Av. Luis Thayer Ojeda', 'Av. Suecia',
    'bic.providencia@pdi.cl', '+56222071800',
    'Jefe BIC Providencia',
    'Brigada de Investigación Criminal de la PDI en la comuna de Providencia', true,
    NOW(), NOW()
),
-- ID 4
(
    'Junta de Vecinos Villa Los Aromos', 'JUNTA_VECINOS', 'UV-01',
    'Providencia', 'Región Metropolitana', 'Av. Los Aromos 1250, Providencia',
    -33.4311, -70.6093,
    'Av. Providencia', 'Calle Los Olmos', 'Av. Irarrázaval', 'Av. Pedro de Valdivia',
    'jjvv.losароmos@providencia.cl', '+56222341100',
    'Carmen Gloria Muñoz',
    'Junta de vecinos del sector norte de Providencia', true,
    NOW(), NOW()
),
-- ID 5
(
    'Junta de Vecinos Los Quillayes', 'JUNTA_VECINOS', 'UV-02',
    'Providencia', 'Región Metropolitana', 'Calle Los Quillayes 340, Providencia',
    -33.4405, -70.6180,
    'Av. Ossa', 'Av. Grecia', 'Av. Tobalaba', 'Av. Ossa',
    'jjvv.losquillayes@providencia.cl', '+56222341200',
    'Roberto Fuentes Díaz',
    'Junta de vecinos del sector sur de Providencia', true,
    NOW(), NOW()
);

-- ──────────────────────────────────────────────────────────────
-- 2. ADMINISTRADORES (2)
--    territorio_id = 1 (Municipalidad de Providencia)
-- ──────────────────────────────────────────────────────────────
INSERT INTO usuarios (
    nombres, apellido_paterno, apellido_materno, rut,
    fecha_nacimiento, genero, email, telefono,
    direccion, comuna, region,
    password, password_clave_unica, rol, activo,
    territorio_id, fecha_registro, ultima_actualizacion
) VALUES
(
    'Sebastián Andrés', 'Rojas', 'Vidal', '14.567.890-3',
    '1985-03-12', 'MASCULINO',
    'admin.sebastian@municipalidad.cl', '+56912345601',
    'Av. Providencia 1234 Of. 301', 'Providencia', 'Región Metropolitana',
    '$2a$10$jC8.oK4Rdx2aoU.ecQteg.qLONtlM/icTvNcHea8dDNXtbzte2ud6',
    '$2a$10$jC8.oK4Rdx2aoU.ecQteg.qLONtlM/icTvNcHea8dDNXtbzte2ud6',
    'ADMIN', true,
    1, NOW(), NOW()
),
(
    'Valentina Isabel', 'Morales', 'Bravo', '16.234.567-8',
    '1990-07-25', 'FEMENINO',
    'admin.valentina@municipalidad.cl', '+56912345602',
    'Av. Providencia 1234 Of. 302', 'Providencia', 'Región Metropolitana',
    '$2a$10$jC8.oK4Rdx2aoU.ecQteg.qLONtlM/icTvNcHea8dDNXtbzte2ud6',
    '$2a$10$jC8.oK4Rdx2aoU.ecQteg.qLONtlM/icTvNcHea8dDNXtbzte2ud6',
    'ADMIN', true,
    1, NOW(), NOW()
);

-- ──────────────────────────────────────────────────────────────
-- 3. FUNCIONARIOS (10)
--    Municipalidad  → territorio_id = 1
--    Carabineros    → territorio_id = 2
--    PDI            → territorio_id = 3
-- ──────────────────────────────────────────────────────────────
INSERT INTO usuarios (
    nombres, apellido_paterno, apellido_materno, rut,
    fecha_nacimiento, genero, email, telefono,
    direccion, comuna, region,
    password, password_clave_unica, rol, activo,
    territorio_id, fecha_registro, ultima_actualizacion
) VALUES

-- === Municipalidad (4) — territorio_id = 1 ===
(
    'Felipe Ignacio', 'Castro', 'Herrera', '12.345.678-9',
    '1988-05-10', 'MASCULINO',
    'f.castro@municipalidad.cl', '+56912345610',
    'Av. Providencia 1234 Of. 101', 'Providencia', 'Región Metropolitana',
    '$2a$10$CIuhy.Om6YylQUDBZCSQtuV58CwJJzJvdNIM9OD/34cLgxvH3DPka',
    '$2a$10$CIuhy.Om6YylQUDBZCSQtuV58CwJJzJvdNIM9OD/34cLgxvH3DPka',
    'FUNCIONARIO', true,
    1, NOW(), NOW()
),
(
    'Camila Fernanda', 'González', 'Pizarro', '17.890.123-4',
    '1993-11-22', 'FEMENINO',
    'c.gonzalez@municipalidad.cl', '+56912345611',
    'Av. Providencia 1234 Of. 102', 'Providencia', 'Región Metropolitana',
    '$2a$10$CIuhy.Om6YylQUDBZCSQtuV58CwJJzJvdNIM9OD/34cLgxvH3DPka',
    '$2a$10$CIuhy.Om6YylQUDBZCSQtuV58CwJJzJvdNIM9OD/34cLgxvH3DPka',
    'FUNCIONARIO', true,
    1, NOW(), NOW()
),
(
    'Diego Alejandro', 'Muñoz', 'Sánchez', '15.678.901-2',
    '1987-02-14', 'MASCULINO',
    'd.munoz@municipalidad.cl', '+56912345612',
    'Av. Providencia 1234 Of. 103', 'Providencia', 'Región Metropolitana',
    '$2a$10$CIuhy.Om6YylQUDBZCSQtuV58CwJJzJvdNIM9OD/34cLgxvH3DPka',
    '$2a$10$CIuhy.Om6YylQUDBZCSQtuV58CwJJzJvdNIM9OD/34cLgxvH3DPka',
    'FUNCIONARIO', true,
    1, NOW(), NOW()
),
(
    'Natalia Paz', 'Vargas', 'Cuevas', '19.012.345-6',
    '1995-08-30', 'FEMENINO',
    'n.vargas@municipalidad.cl', '+56912345613',
    'Av. Providencia 1234 Of. 104', 'Providencia', 'Región Metropolitana',
    '$2a$10$CIuhy.Om6YylQUDBZCSQtuV58CwJJzJvdNIM9OD/34cLgxvH3DPka',
    '$2a$10$CIuhy.Om6YylQUDBZCSQtuV58CwJJzJvdNIM9OD/34cLgxvH3DPka',
    'FUNCIONARIO', true,
    1, NOW(), NOW()
),

-- === Carabineros (3) — territorio_id = 2 ===
(
    'Carlos Eduardo', 'Pérez', 'Riquelme', '13.456.789-0',
    '1986-04-18', 'MASCULINO',
    'c.perez@carabineros.cl', '+56912345614',
    'Av. Italia 1000, 2ª Comisaría', 'Providencia', 'Región Metropolitana',
    '$2a$10$CIuhy.Om6YylQUDBZCSQtuV58CwJJzJvdNIM9OD/34cLgxvH3DPka',
    '$2a$10$CIuhy.Om6YylQUDBZCSQtuV58CwJJzJvdNIM9OD/34cLgxvH3DPka',
    'FUNCIONARIO', true,
    2, NOW(), NOW()
),
(
    'Andrea Soledad', 'López', 'Tapia', '18.234.567-1',
    '1991-09-05', 'FEMENINO',
    'a.lopez@carabineros.cl', '+56912345615',
    'Av. Italia 1000, 2ª Comisaría', 'Providencia', 'Región Metropolitana',
    '$2a$10$CIuhy.Om6YylQUDBZCSQtuV58CwJJzJvdNIM9OD/34cLgxvH3DPka',
    '$2a$10$CIuhy.Om6YylQUDBZCSQtuV58CwJJzJvdNIM9OD/34cLgxvH3DPka',
    'FUNCIONARIO', true,
    2, NOW(), NOW()
),
(
    'Rodrigo Esteban', 'Silva', 'Flores', '11.234.567-8',
    '1983-12-01', 'MASCULINO',
    'r.silva@carabineros.cl', '+56912345616',
    'Av. Italia 1000, 2ª Comisaría', 'Providencia', 'Región Metropolitana',
    '$2a$10$CIuhy.Om6YylQUDBZCSQtuV58CwJJzJvdNIM9OD/34cLgxvH3DPka',
    '$2a$10$CIuhy.Om6YylQUDBZCSQtuV58CwJJzJvdNIM9OD/34cLgxvH3DPka',
    'FUNCIONARIO', true,
    2, NOW(), NOW()
),

-- === PDI (3) — territorio_id = 3 ===
(
    'Marcelo Patricio', 'Torres', 'Contreras', '14.678.901-3',
    '1984-07-20', 'MASCULINO',
    'm.torres@pdi.cl', '+56912345617',
    'Av. Suecia 286, BIC Providencia', 'Providencia', 'Región Metropolitana',
    '$2a$10$CIuhy.Om6YylQUDBZCSQtuV58CwJJzJvdNIM9OD/34cLgxvH3DPka',
    '$2a$10$CIuhy.Om6YylQUDBZCSQtuV58CwJJzJvdNIM9OD/34cLgxvH3DPka',
    'FUNCIONARIO', true,
    3, NOW(), NOW()
),
(
    'Patricia Eugenia', 'Ramírez', 'Ortiz', '20.123.456-7',
    '1997-03-15', 'FEMENINO',
    'p.ramirez@pdi.cl', '+56912345618',
    'Av. Suecia 286, BIC Providencia', 'Providencia', 'Región Metropolitana',
    '$2a$10$CIuhy.Om6YylQUDBZCSQtuV58CwJJzJvdNIM9OD/34cLgxvH3DPka',
    '$2a$10$CIuhy.Om6YylQUDBZCSQtuV58CwJJzJvdNIM9OD/34cLgxvH3DPka',
    'FUNCIONARIO', true,
    3, NOW(), NOW()
),
(
    'Jorge Luis', 'Reyes', 'Navarro', ' ',
    '1989-01-08', 'MASCULINO',
    'j.reyes@pdi.cl', '+56912345619',
    'Av. Suecia 286, BIC Providencia', 'Providencia', 'Región Metropolitana',
    '$2a$10$CIuhy.Om6YylQUDBZCSQtuV58CwJJzJvdNIM9OD/34cLgxvH3DPka',
    '$2a$10$CIuhy.Om6YylQUDBZCSQtuV58CwJJzJvdNIM9OD/34cLgxvH3DPka',
    'FUNCIONARIO', true,
    3, NOW(), NOW()
);

-- ──────────────────────────────────────────────────────────────
-- 4. VECINOS (10)
--    Los Aromos    → territorio_id = 4  (vecinos 1-5)
--    Los Quillayes → territorio_id = 5  (vecinos 6-10)
-- ──────────────────────────────────────────────────────────────
INSERT INTO usuarios (
    nombres, apellido_paterno, apellido_materno, rut,
    fecha_nacimiento, genero, email, telefono,
    direccion, comuna, region,
    password, password_clave_unica, rol, activo,
    territorio_id, fecha_registro, ultima_actualizacion
) VALUES
(
    'Ana María', 'Soto', 'Fuentes', '12.789.456-1',
    '1980-06-15', 'FEMENINO',
    'ana.soto@gmail.com', '+56912345620',
    'Av. Los Aromos 432, Dpto 3B', 'Providencia', 'Región Metropolitana',
    '$2a$10$iwuKEntyKGNxmF4oTIQuge6l9FjP78aATPRDVye7MF3f6LniTXy0e',
    '$2a$10$iwuKEntyKGNxmF4oTIQuge6l9FjP78aATPRDVye7MF3f6LniTXy0e',
    'VECINO', true,
    4, NOW(), NOW()
),
(
    'Luis Hernán', 'Espinoza', 'Medina', '13.901.234-5',
    '1975-11-28', 'MASCULINO',
    'luis.espinoza@hotmail.com', '+56912345621',
    'Calle Los Cerezos 128', 'Providencia', 'Región Metropolitana',
    '$2a$10$iwuKEntyKGNxmF4oTIQuge6l9FjP78aATPRDVye7MF3f6LniTXy0e',
    '$2a$10$iwuKEntyKGNxmF4oTIQuge6l9FjP78aATPRDVye7MF3f6LniTXy0e',
    'VECINO', true,
    4, NOW(), NOW()
),
(
    'Claudia Beatriz', 'Jiménez', 'Rojas', '18.567.890-2',
    '1992-04-03', 'FEMENINO',
    'claudia.jimenez@gmail.com', '+56912345622',
    'Av. Los Aromos 560, Casa 2', 'Providencia', 'Región Metropolitana',
    '$2a$10$iwuKEntyKGNxmF4oTIQuge6l9FjP78aATPRDVye7MF3f6LniTXy0e',
    '$2a$10$iwuKEntyKGNxmF4oTIQuge6l9FjP78aATPRDVye7MF3f6LniTXy0e',
    'VECINO', true,
    4, NOW(), NOW()
),
(
    'Marco Antonio', 'Herrera', 'Álvarez', '15.123.678-4',
    '1968-09-17', 'MASCULINO',
    'marco.herrera@yahoo.cl', '+56912345623',
    'Pasaje El Roble 23', 'Providencia', 'Región Metropolitana',
    '$2a$10$iwuKEntyKGNxmF4oTIQuge6l9FjP78aATPRDVye7MF3f6LniTXy0e',
    '$2a$10$iwuKEntyKGNxmF4oTIQuge6l9FjP78aATPRDVye7MF3f6LniTXy0e',
    'VECINO', true,
    4, NOW(), NOW()
),
(
    'Gabriela Alejandra', 'Núñez', 'Castillo', '19.345.678-9',
    '1998-01-22', 'FEMENINO',
    'gabriela.nunez@gmail.com', '+56912345624',
    'Calle Los Tilos 87, Dpto 1A', 'Providencia', 'Región Metropolitana',
    '$2a$10$iwuKEntyKGNxmF4oTIQuge6l9FjP78aATPRDVye7MF3f6LniTXy0e',
    '$2a$10$iwuKEntyKGNxmF4oTIQuge6l9FjP78aATPRDVye7MF3f6LniTXy0e',
    'VECINO', true,
    4, NOW(), NOW()
),
(
    'Tomás Esteban', 'Palacios', 'Vega', '14.234.890-7',
    '1982-08-09', 'MASCULINO',
    'tomas.palacios@outlook.com', '+56912345625',
    'Calle Los Quillayes 218', 'Providencia', 'Región Metropolitana',
    '$2a$10$iwuKEntyKGNxmF4oTIQuge6l9FjP78aATPRDVye7MF3f6LniTXy0e',
    '$2a$10$iwuKEntyKGNxmF4oTIQuge6l9FjP78aATPRDVye7MF3f6LniTXy0e',
    'VECINO', true,
    5, NOW(), NOW()
),
(
    'Sandra Lorena', 'Cárdenas', 'Meza', '16.012.345-0',
    '1976-05-30', 'FEMENINO',
    'sandra.cardenas@gmail.com', '+56912345626',
    'Calle Los Quillayes 350, Casa 5', 'Providencia', 'Región Metropolitana',
    '$2a$10$iwuKEntyKGNxmF4oTIQuge6l9FjP78aATPRDVye7MF3f6LniTXy0e',
    '$2a$10$iwuKEntyKGNxmF4oTIQuge6l9FjP78aATPRDVye7MF3f6LniTXy0e',
    'VECINO', true,
    5, NOW(), NOW()
),
(
    'Patricio Javier', 'Díaz', 'Guerrero', '17.456.789-3',
    '1990-12-14', 'MASCULINO',
    'patricio.diaz@hotmail.cl', '+56912345627',
    'Av. Grecia 445, Dpto 7C', 'Providencia', 'Región Metropolitana',
    '$2a$10$iwuKEntyKGNxmF4oTIQuge6l9FjP78aATPRDVye7MF3f6LniTXy0e',
    '$2a$10$iwuKEntyKGNxmF4oTIQuge6l9FjP78aATPRDVye7MF3f6LniTXy0e',
    'VECINO', true,
    5, NOW(), NOW()
),
(
    'Francisca Elena', 'Aguilar', 'Moreno', '20.678.901-4',
    '2000-07-07', 'FEMENINO',
    'francisca.aguilar@gmail.com', '+56912345628',
    'Pasaje Las Camelias 12', 'Providencia', 'Región Metropolitana',
    '$2a$10$iwuKEntyKGNxmF4oTIQuge6l9FjP78aATPRDVye7MF3f6LniTXy0e',
    '$2a$10$iwuKEntyKGNxmF4oTIQuge6l9FjP78aATPRDVye7MF3f6LniTXy0e',
    'VECINO', true,
    5, NOW(), NOW()
),
(
    'Gonzalo Ignacio', 'Mendoza', 'Sepúlveda', '11.890.234-6',
    '1965-03-25', 'MASCULINO',
    'gonzalo.mendoza@yahoo.cl', '+56912345629',
    'Av. Tobalaba 892', 'Providencia', 'Región Metropolitana',
    '$2a$10$iwuKEntyKGNxmF4oTIQuge6l9FjP78aATPRDVye7MF3f6LniTXy0e',
    '$2a$10$iwuKEntyKGNxmF4oTIQuge6l9FjP78aATPRDVye7MF3f6LniTXy0e',
    'VECINO', true,
    5, NOW(), NOW()
);
