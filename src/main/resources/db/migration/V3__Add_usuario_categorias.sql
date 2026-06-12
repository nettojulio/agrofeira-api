CREATE TABLE usuario_categorias
(
    usuario_id UUID        NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    categoria  VARCHAR(50) NOT NULL,
    PRIMARY KEY (usuario_id, categoria)
);
