-- Código do pedido no formato DDMMAA-P01: data de criação + sequência do dia no sistema todo.
-- O número é gravado na criação e nunca muda (excluir um pedido não renumera os outros).
ALTER TABLE pedido ADD COLUMN numero_dia INT NULL;

-- Pedidos existentes: numerados pela ordem de criação dentro de cada dia.
UPDATE pedido p
    JOIN (
        SELECT id_pedido,
               ROW_NUMBER() OVER (PARTITION BY data_pedido ORDER BY id_pedido) AS numero
        FROM pedido
    ) ordem ON ordem.id_pedido = p.id_pedido
SET p.numero_dia = ordem.numero;

ALTER TABLE pedido MODIFY COLUMN numero_dia INT NOT NULL;

-- Impede dois pedidos com o mesmo código, inclusive quando criados ao mesmo tempo.
ALTER TABLE pedido ADD CONSTRAINT uc_pedido_data_numero UNIQUE (data_pedido, numero_dia);
