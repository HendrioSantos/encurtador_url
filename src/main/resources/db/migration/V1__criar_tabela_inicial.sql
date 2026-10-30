CREATE TABLE url (
    id BIGSERIAL PRIMARY KEY,
    url_original TEXT NOT NULL,
    codigo_encurtado VARCHAR(10) UNIQUE,
    data_criacao TIMESTAMP NOT NULL
);