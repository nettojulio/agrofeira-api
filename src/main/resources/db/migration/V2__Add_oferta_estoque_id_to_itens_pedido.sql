ALTER TABLE itens_pedido
    ADD COLUMN oferta_estoque_id UUID REFERENCES ofertas_estoque (id) ON DELETE SET NULL;

CREATE INDEX idx_itens_pedido_oferta_estoque_id ON itens_pedido (oferta_estoque_id);
