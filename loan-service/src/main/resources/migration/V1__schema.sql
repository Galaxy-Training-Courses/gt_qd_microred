-- DDL verificado generando el esquema real con
-- Hibernate (drop-and-create + scripts.generation=create)

create sequence loan_requests_SEQ start with 1 increment by 50;
create sequence loan_request_items_SEQ start with 1 increment by 50;

create table loan_requests (
    id bigint not null,
    requester_id bigint not null,
    requested_from date not null,
    requested_until date not null,
    status varchar(20) not null check (status in ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED', 'RETURNED')),
    reason text not null,
    created_at timestamp(6) with time zone not null,
    primary key (id)
);

create table loan_request_items (
    id bigint not null,
    loan_request_id bigint not null,
    object_id bigint not null,
    notes text,
    primary key (id),
    constraint fk_loan_request_items_loan_request foreign key (loan_request_id) references loan_requests (id)
);
