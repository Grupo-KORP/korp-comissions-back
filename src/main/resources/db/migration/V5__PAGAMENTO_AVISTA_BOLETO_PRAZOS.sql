ALTER TABLE pedido ADD COLUMN metodo_pagamento VARCHAR(20) NULL;
ALTER TABLE parcela ADD COLUMN prazo_dias INT NULL;

-- Só existem À vista e Boleto; qualquer outro método antigo vira À vista.
UPDATE pagamento SET metodo_pagamento = 'AVISTA' WHERE metodo_pagamento <> 'BOLETO';

UPDATE pedido p
    JOIN pagamento pg ON pg.fk_pedido = p.id_pedido
SET p.metodo_pagamento = pg.metodo_pagamento;

-- Pedidos já finalizados: prazo aproximado pela distância entre o pedido e o vencimento.
UPDATE parcela pa
    JOIN pagamento pg ON pg.id_pagamento = pa.fk_pagamento
    JOIN pedido p ON p.id_pedido = pg.fk_pedido
SET pa.prazo_dias = GREATEST(DATEDIFF(pa.data_vencimento, p.data_pedido), 0);
