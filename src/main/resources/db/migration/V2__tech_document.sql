create table tech_document (
    id uuid primary key,
    source_id varchar(100) not null,
    source_url text not null,
    title text not null,
    vendor varchar(100),
    category varchar(50),
    published_at timestamptz,
    collected_at timestamptz not null,
    normalized_content text,
    content_hash varchar(64) not null,
    processing_status varchar(50) not null,
    created_at timestamptz not null default now(),
    unique(content_hash)
);
