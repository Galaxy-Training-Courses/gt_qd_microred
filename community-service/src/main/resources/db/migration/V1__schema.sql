-- Esquema equivalente al que generaría Hibernate con drop-and-create

-- INCREMENT BY 50 no es arbitrario: es el tamaño de lote que usa el
-- optimizer "pooled" por defecto de Hibernate para @Id de PanacheEntity.
-- Si aquí se pusiera INCREMENT BY 1, Hibernate asignaría IDs duplicados.
create sequence users_SEQ start with 1 increment by 50;
create sequence objects_SEQ start with 1 increment by 50;

create table users (
    id bigint not null,
    name varchar(100) not null,
    email varchar(150) not null unique,
    neighborhood varchar(100) not null,
    status varchar(20) not null check (status in ('ACTIVE', 'INACTIVE')),
    created_at timestamp(6) with time zone not null,
    primary key (id)
);

create table objects (
    id bigint not null,
    owner_id bigint not null,
    name varchar(150) not null,
    description text not null,
    category varchar(50) not null,
    condition varchar(20) not null check (condition in ('NEW', 'GOOD', 'USED', 'WORN')),
    status varchar(20) not null check (status in ('AVAILABLE', 'RESERVED', 'UNAVAILABLE')),
    created_at timestamp(6) with time zone not null,
    primary key (id),
    constraint fk_objects_owner foreign key (owner_id) references users (id)
);
