create table mail_delivery (
    id uuid primary key,
    delivery_type varchar(50) not null,
    subject text not null,
    recipient text not null,
    notion_page_url text,
    status varchar(50) not null,
    idempotency_key varchar(200) not null,
    retry_count integer not null default 0,
    error_message text,
    sent_at timestamptz,
    created_at timestamptz not null default now(),
    unique(idempotency_key)
);
