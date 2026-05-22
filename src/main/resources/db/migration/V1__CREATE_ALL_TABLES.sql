-- ============================================================
-- V1__create_tables.sql
-- Schema completo e consolidado do KORP DB
-- ============================================================


-- ------------------------------------------------------------
-- ROLES
-- ------------------------------------------------------------
CREATE TABLE roles (
                       role_id INT AUTO_INCREMENT NOT NULL,
                       role    VARCHAR(255)       NOT NULL,
                       CONSTRAINT pk_roles PRIMARY KEY (role_id)
);


-- ------------------------------------------------------------
-- USUARIO
-- ------------------------------------------------------------
CREATE TABLE usuario (
                         id_usuario          INT          AUTO_INCREMENT NOT NULL,
                         nome                VARCHAR(250) NOT NULL,
                         email               VARCHAR(120) NOT NULL,
                         senha               VARCHAR(255) NOT NULL,
                         telefone            VARCHAR(20)  NOT NULL,
                         percentual_comissao DECIMAL(5,2) NULL,
                         dt_criacao          DATETIME     NULL,
                         primeiro_acesso     BOOLEAN      NOT NULL DEFAULT TRUE,
                         ativo               BOOLEAN      NOT NULL DEFAULT TRUE,
                         fk_role             INT          NULL,
                         CONSTRAINT pk_usuario       PRIMARY KEY (id_usuario),
                         CONSTRAINT uc_usuario_email UNIQUE (email),
                         CONSTRAINT fk_usuario_role  FOREIGN KEY (fk_role)
                             REFERENCES roles(role_id) ON DELETE SET NULL
);

-- ------------------------------------------------------------
-- CLIENTE
-- ------------------------------------------------------------
CREATE TABLE cliente (
                         id_cliente           INT          AUTO_INCREMENT NOT NULL,
                         razao_social         VARCHAR(150) NOT NULL,
                         nome_fantasia        VARCHAR(150) NULL,
                         cnpj                 VARCHAR(20)  NOT NULL,
                         inscricao_estadual   VARCHAR(20)  NOT NULL,
                         telefone             VARCHAR(20)  NULL,
                         email                VARCHAR(120) NULL,
                         ativo                BOOLEAN      NOT NULL DEFAULT TRUE,
                         fk_vendedor_cadastro INT          NULL,
                         CONSTRAINT pk_cliente          PRIMARY KEY (id_cliente),
                         CONSTRAINT uc_cliente_cnpj     UNIQUE (cnpj),
                         CONSTRAINT fk_cliente_vendedor FOREIGN KEY (fk_vendedor_cadastro)
                             REFERENCES usuario(id_usuario) ON DELETE SET NULL
);


-- ------------------------------------------------------------
-- DISTRIBUIDOR
-- ------------------------------------------------------------
CREATE TABLE distribuidor (
                              id_distribuidor    INT          AUTO_INCREMENT NOT NULL,
                              razao_social       VARCHAR(150) NOT NULL,
                              nome_fantasia      VARCHAR(150) NULL,
                              cnpj               VARCHAR(20)  NOT NULL,
                              inscricao_estadual VARCHAR(20)  NOT NULL,
                              telefone           VARCHAR(20)  NULL,
                              email              VARCHAR(120) NULL,
                              ativo              BOOLEAN      NOT NULL DEFAULT TRUE,
                              CONSTRAINT pk_distribuidor      PRIMARY KEY (id_distribuidor),
                              CONSTRAINT uc_distribuidor_cnpj UNIQUE (cnpj)
);


-- ------------------------------------------------------------
-- ENDERECO
-- ------------------------------------------------------------
CREATE TABLE endereco (
                          id_endereco     INT          AUTO_INCREMENT NOT NULL,
                          logradouro      VARCHAR(200) NOT NULL,
                          numero          VARCHAR(20)  NULL,
                          complemento     VARCHAR(100) NULL,
                          bairro          VARCHAR(100) NULL,
                          cidade          VARCHAR(100) NOT NULL,
                          estado          CHAR(2)      NOT NULL,
                          cep             VARCHAR(10)  NOT NULL,
                          tipo_endereco   ENUM('LOCAL', 'ENTREGA', 'COBRANCA') NOT NULL,
                          fk_cliente      INT          NULL,
                          fk_distribuidor INT          NULL,
                          CONSTRAINT pk_endereco              PRIMARY KEY (id_endereco),
                          CONSTRAINT fk_endereco_cliente      FOREIGN KEY (fk_cliente)
                              REFERENCES cliente(id_cliente) ON DELETE CASCADE,
                          CONSTRAINT fk_endereco_distribuidor FOREIGN KEY (fk_distribuidor)
                              REFERENCES distribuidor(id_distribuidor) ON DELETE CASCADE
);


-- ------------------------------------------------------------
-- CONTATO
-- ------------------------------------------------------------
CREATE TABLE contato (
                         id_contato           INT          AUTO_INCREMENT NOT NULL,
                         nome                 VARCHAR(250) NOT NULL,
                         email                VARCHAR(120) NULL,
                         telefone             VARCHAR(20)  NULL,
                         ativo                BOOLEAN      NOT NULL DEFAULT TRUE,
                         fk_cliente           INT          NULL,
                         fk_distribuidor      INT          NULL,
                         fk_vendedor_cadastro INT          NULL,
                         CONSTRAINT pk_contato              PRIMARY KEY (id_contato),
                         CONSTRAINT fk_contato_cliente      FOREIGN KEY (fk_cliente)
                             REFERENCES cliente(id_cliente) ON DELETE CASCADE,
                         CONSTRAINT fk_contato_distribuidor FOREIGN KEY (fk_distribuidor)
                             REFERENCES distribuidor(id_distribuidor) ON DELETE CASCADE,
                         CONSTRAINT fk_contato_vendedor     FOREIGN KEY (fk_vendedor_cadastro)
                             REFERENCES usuario(id_usuario) ON DELETE SET NULL
);


-- ------------------------------------------------------------
-- PRODUTO
-- ------------------------------------------------------------
CREATE TABLE pedido (
                        id_pedido                INT          AUTO_INCREMENT NOT NULL,
                        data_pedido              DATE         NOT NULL,
                        numero_nota_distribuidor VARCHAR(20)  NULL,
                        valor_total_distr        DECIMAL(10,2) NOT NULL,
                        valor_total_cliente      DECIMAL(10,2) NOT NULL,
                        status_pedido            VARCHAR(50)  NOT NULL DEFAULT 'PENDENTE',
                        frete                    BOOLEAN      NULL,
                        transportadora           VARCHAR(150) NULL,
                        observacoes              VARCHAR(250) NULL,
                        ativo                    BOOLEAN      NOT NULL DEFAULT TRUE,
                        fk_vendedor              INT          NOT NULL,
                        fk_cliente               INT          NOT NULL,
                        fk_distribuidor          INT          NOT NULL,
                        CONSTRAINT pk_pedido              PRIMARY KEY (id_pedido),
                        CONSTRAINT fk_pedido_vendedor     FOREIGN KEY (fk_vendedor)
                            REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
                        CONSTRAINT fk_pedido_cliente      FOREIGN KEY (fk_cliente)
                            REFERENCES cliente(id_cliente) ON DELETE RESTRICT,
                        CONSTRAINT fk_pedido_distribuidor FOREIGN KEY (fk_distribuidor)
                            REFERENCES distribuidor(id_distribuidor) ON DELETE RESTRICT
);


-- ------------------------------------------------------------
-- PEDIDO
-- ------------------------------------------------------------
CREATE TABLE produto (
                         id_produto           INT          AUTO_INCREMENT NOT NULL,
                         nome                 VARCHAR(150) NOT NULL,
                         codigo_produto       VARCHAR(20)        NULL,
                         ativo                BOOLEAN            NOT NULL DEFAULT TRUE,
                         CONSTRAINT pk_produto          PRIMARY KEY (id_produto)
);


-- ------------------------------------------------------------
-- ITEM_PEDIDO
-- ------------------------------------------------------------
CREATE TABLE item_pedido (
                             id_item_pedido    INT          AUTO_INCREMENT NOT NULL,
                             quantidade        INT          NOT NULL,
                             vlr_unit_distr    DECIMAL(10,2) NOT NULL,
                             vlr_total_distr   DECIMAL(10,2) NOT NULL,
                             vlr_unit_cliente  DECIMAL(10,2) NOT NULL,
                             vlr_total_cliente DECIMAL(10,2) NOT NULL,
                             fk_pedido         INT          NOT NULL,
                             fk_produto        INT          NOT NULL,
                             CONSTRAINT pk_item_pedido  PRIMARY KEY (id_item_pedido),
                             CONSTRAINT fk_item_pedido  FOREIGN KEY (fk_pedido)
                                 REFERENCES pedido(id_pedido) ON DELETE CASCADE,
                             CONSTRAINT fk_item_produto FOREIGN KEY (fk_produto)
                                 REFERENCES produto(id_produto) ON DELETE RESTRICT
);


-- ------------------------------------------------------------
-- PAGAMENTO
-- ------------------------------------------------------------
CREATE TABLE pagamento (
                           id_pagamento        INT         AUTO_INCREMENT NOT NULL,
                           metodo_pagamento    VARCHAR(30) NOT NULL,
                           parcelado           BOOLEAN     NOT NULL DEFAULT FALSE,
                           quantidade_parcelas INT         NULL,
                           fk_pedido           INT         NOT NULL,
                           CONSTRAINT pk_pagamento        PRIMARY KEY (id_pagamento),
                           CONSTRAINT uc_pagamento_pedido UNIQUE (fk_pedido),
                           CONSTRAINT fk_pagamento_pedido FOREIGN KEY (fk_pedido)
                               REFERENCES pedido(id_pedido) ON DELETE CASCADE
);


-- ------------------------------------------------------------
-- PARCELA
-- ------------------------------------------------------------
CREATE TABLE parcela (
                         id_parcela      INT          AUTO_INCREMENT NOT NULL,
                         numero_parcela  INT          NOT NULL,
                         valor_parcela   DECIMAL(10,2) NOT NULL,
                         data_vencimento DATE         NOT NULL,
                         data_pagamento  DATE         NULL,
                         status_parcela  VARCHAR(20)  NOT NULL DEFAULT 'PENDENTE',
                         fk_pagamento    INT          NOT NULL,
                         CONSTRAINT pk_parcela       PRIMARY KEY (id_parcela),
                         CONSTRAINT fk_parcela_pagto FOREIGN KEY (fk_pagamento)
                             REFERENCES pagamento(id_pagamento) ON DELETE CASCADE
);


-- ------------------------------------------------------------
-- COMISSAO
-- ------------------------------------------------------------
CREATE TABLE comissao (
                          id_comissao     INT          AUTO_INCREMENT NOT NULL,
                          valor_comissao  DECIMAL(10,2) NOT NULL,
                          status_comissao VARCHAR(20)  NOT NULL DEFAULT 'PENDENTE',
                          data_pagamento  DATE         NULL,
                          fk_pedido       INT          NOT NULL,
                          fk_parcela      INT          NOT NULL,
                          fk_vendedor     INT          NOT NULL,
                          CONSTRAINT pk_comissao          PRIMARY KEY (id_comissao),
                          CONSTRAINT fk_comissao_pedido   FOREIGN KEY (fk_pedido)
                              REFERENCES pedido(id_pedido) ON DELETE CASCADE,
                          CONSTRAINT fk_comissao_parcela  FOREIGN KEY (fk_parcela)
                              REFERENCES parcela(id_parcela) ON DELETE CASCADE,
                          CONSTRAINT fk_comissao_vendedor FOREIGN KEY (fk_vendedor)
                              REFERENCES usuario(id_usuario) ON DELETE RESTRICT
);