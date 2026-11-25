-- Prazo/condição de entrega do pedido (ex.: "Imediata", "15 dias"), informado pelo vendedor.
-- Um valor para o pedido inteiro. Pedidos antigos ficam sem valor (nunca foi gravado antes).
ALTER TABLE pedido ADD COLUMN entrega VARCHAR(100) NULL;
