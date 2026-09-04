create table notion_publish (
    id uuid primary key,
    document_id uuid,
    publish_type varchar(50) not null,
    notion_page_id varchar(200),
    notion_page_url text,
    status varchar(50) not null,
    idempotency_key varchar(200) not null,
    retry_count integer not null default 0,
    error_message text,
    published_at timestamptz,
    created_at timestamptz not null default now(),
    unique(idempotency_key)
);
