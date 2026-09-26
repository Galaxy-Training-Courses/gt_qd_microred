-- Datos deterministas para %dev y %test:
-- ids fijos para que la colección Postman pueda referenciarlos directamente.

-- El volumen (paginación real) lo aporta DataSeeder con Datafaker, solo en %dev.
insert into users (id, name, email, neighborhood, status, created_at) values
    (1, 'Ana Torres', 'ana.torres@example.com', 'Miraflores', 'ACTIVE', '2026-01-01T00:00:00Z'),
    (2, 'Luis Paredes', 'luis.paredes@example.com', 'Barranco', 'ACTIVE', '2026-01-01T00:00:00Z');

insert into objects (id, owner_id, name, description, category, condition, status, created_at) values
    (1, 1, 'Escalera telescópica', 'Aluminio, 3.8 m', 'HERRAMIENTAS', 'GOOD', 'AVAILABLE', '2026-01-01T00:00:00Z'),
    (2, 1, 'Proyector portátil', 'HDMI y control remoto', 'ELECTRONICA', 'NEW', 'AVAILABLE', '2026-01-01T00:00:00Z'),
    (3, 2, 'Carpa 4 personas', 'Impermeable, con piso', 'CAMPING', 'USED', 'AVAILABLE', '2026-01-01T00:00:00Z');

-- Los ids se insertan a mano, así que la secuencia queda desalineada con
-- el máximo real: se adelanta explícitamente para que el próximo insert
-- hecho por Hibernate/Panache no colisione con estas filas.
select setval('users_SEQ', 1000);
select setval('objects_SEQ', 1000);
