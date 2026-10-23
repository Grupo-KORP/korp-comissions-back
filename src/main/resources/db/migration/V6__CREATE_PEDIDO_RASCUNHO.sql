CREATE TABLE pedido_rascunho (
    id_rascunho       INT          AUTO_INCREMENT NOT NULL,
    fk_vendedor       INT          NOT NULL,
    dados             LONGTEXT     NOT NULL,
    versao_schema     INT          NOT NULL DEFAULT 1,
    nome_cliente      VARCHAR(150) NULL,
    nome_distribuidor VARCHAR(150) NULL,
    qtd_produtos      INT          NOT NULL DEFAULT 0,
    metodo_pagamento  VARCHAR(20)  NULL,
    versao            INT          NOT NULL DEFAULT 0,
    dt_criacao        DATETIME     NOT NULL,
    dt_atualizacao    DATETIME     NOT NULL,
    CONSTRAINT pk_pedido_rascunho PRIMARY KEY (id_rascunho),
    CONSTRAINT fk_rascunho_vendedor FOREIGN KEY (fk_vendedor)
        REFERENCES usuario(id_usuario) ON DELETE CASCADE
);

CREATE INDEX idx_rascunho_vendedor_atualizacao ON pedido_rascunho (fk_vendedor, dt_atualizacao);
