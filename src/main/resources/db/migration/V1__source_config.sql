create table source_config (
    id varchar(100) primary key,
    source_type varchar(50) not null,
    vendor varchar(100),
    category varchar(50) not null,
    endpoint text not null,
    enabled boolean not null default true,
    schedule varchar(100),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);
