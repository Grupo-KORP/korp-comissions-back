CREATE TABLE roles (
                       role_id INT AUTO_INCREMENT NOT NULL,
                       role VARCHAR(255),
                       CONSTRAINT pk_roles PRIMARY KEY (role_id)
);

CREATE TABLE usuario (
                         id_usuario  INT AUTO_INCREMENT NOT NULL,
                         nome VARCHAR(250) NOT NULL,
                         email VARCHAR(120) NOT NULL,
                         senha VARCHAR(255) NOT NULL,
                         telefone VARCHAR(255) NOT NULL,
                         percentual_comissao DECIMAL(5,2) NULL,
                         dt_criacao DATETIME NULL,
                         fk_role INT NULL,
                         CONSTRAINT pk_usuario PRIMARY KEY (id_usuario),
                         CONSTRAINT uc_usuario_email UNIQUE (email),
                         CONSTRAINT FK_USUARIO_ON_FKROLE FOREIGN KEY (fk_role) REFERENCES roles(role_id)
);

CREATE TABLE cliente (
                         id_cliente INT AUTO_INCREMENT NOT NULL,
                         razao_social VARCHAR(150) NOT NULL,
                         nome_fantasia VARCHAR(150) NULL,
                         cnpj VARCHAR(20) NOT NULL,
                         telefone VARCHAR(20) NULL,
                         email VARCHAR(120) NULL,
                         CONSTRAINT pk_cliente PRIMARY KEY (id_cliente),
                         CONSTRAINT uc_cliente_cnpj UNIQUE (cnpj)
);

CREATE TABLE distribuidor (
                              id_distribuidor INT AUTO_INCREMENT NOT NULL,
                              razao_social VARCHAR(150) NOT NULL,
                              nome_fantasia VARCHAR(150) NULL,
                              cnpj VARCHAR(20) NOT NULL,
                              telefone VARCHAR(20) NULL,
                              email VARCHAR(120) NULL,
                              CONSTRAINT pk_distribuidor PRIMARY KEY (id_distribuidor),
                              CONSTRAINT uc_distribuidor_cnpj UNIQUE (cnpj)
);

CREATE TABLE contato (
                         id_contato INT AUTO_INCREMENT NOT NULL,
                         nome VARCHAR(250) NOT NULL,
                         email VARCHAR(120) NULL,
                         telefone VARCHAR(20) NULL,
                         fk_cliente INT NULL,
                         fk_distribuidor INT NULL,
                         CONSTRAINT pk_contato PRIMARY KEY (id_contato),
                         CONSTRAINT FK_CONTATO_ON_FK_CLIENTE FOREIGN KEY (fk_cliente) REFERENCES cliente(id_cliente),
                         CONSTRAINT FK_CONTATO_ON_FK_DISTRIBUIDOR FOREIGN KEY (fk_distribuidor) REFERENCES distribuidor(id_distribuidor)
);

CREATE TABLE produto (
                         id_produto INT AUTO_INCREMENT PRIMARY KEY,
                         nome VARCHAR(150) NOT NULL,
                         descricao TEXT
);

CREATE TABLE pedido (
                        id_pedido INT AUTO_INCREMENT PRIMARY KEY,
                        data_pedido DATE NOT NULL,
                        numero_nota_distribuidor INT,
                        valor_total_revenda DECIMAL(10,2) NOT NULL,
                        valor_total_faturamento DECIMAL(10,2) NOT NULL,
                        status_pedido VARCHAR(50) DEFAULT 'PENDENTE' NOT NULL,
                        frete BOOLEAN,
                        transportadora VARCHAR(150),
                        observacoes VARCHAR(250),
                        fk_vendedor INT NOT NULL,
                        fk_cliente INT NOT NULL,
                        fk_distribuidor INT NOT NULL,
                        FOREIGN KEY (fk_vendedor) REFERENCES usuario(id_usuario),
                        FOREIGN KEY (fk_cliente) REFERENCES cliente(id_cliente),
                        FOREIGN KEY (fk_distribuidor) REFERENCES distribuidor(id_distribuidor)
);

CREATE TABLE pagamento (
                           id_pagamento INT AUTO_INCREMENT PRIMARY KEY,
                           metodo_pagamento VARCHAR(30) NOT NULL,
                           parcelado BOOLEAN DEFAULT FALSE,
                           quantidade_parcelas INT,
                           fk_pedido INT UNIQUE NOT NULL,
                           FOREIGN KEY (fk_pedido) REFERENCES pedido(id_pedido)
);

CREATE TABLE parcela (
                         id_parcela INT AUTO_INCREMENT PRIMARY KEY,
                         numero_parcela INT NOT NULL,
                         valor_parcela DECIMAL(10,2) NOT NULL,
                         data_vencimento DATE NOT NULL,
                         data_pagamento DATE,
                         status_parcela VARCHAR(20) DEFAULT 'PENDENTE',
                         fk_pagamento INT NOT NULL,
                         FOREIGN KEY (fk_pagamento) REFERENCES pagamento(id_pagamento)
);

CREATE TABLE item_pedido (
                             id_item_pedido INT AUTO_INCREMENT PRIMARY KEY,
                             quantidade INT NOT NULL,
                             valor_unitario DECIMAL(10,2) NOT NULL,
                             fk_pedido INT NOT NULL,
                             fk_produto INT NOT NULL,
                             FOREIGN KEY (fk_pedido) REFERENCES pedido(id_pedido),
                             FOREIGN KEY (fk_produto) REFERENCES produto(id_produto)
);

CREATE TABLE comissao (
                          id_comissao INT AUTO_INCREMENT PRIMARY KEY,
                          valor_comissao DECIMAL(10,2) NOT NULL,
                          status_comissao VARCHAR(20) DEFAULT 'PENDENTE',
                          data_pagamento DATE,
                          fk_pedido INT NOT NULL,
                          fk_parcela INT NOT NULL,
                          fk_vendedor INT NOT NULL,
                          FOREIGN KEY (fk_pedido) REFERENCES pedido(id_pedido),
                          FOREIGN KEY (fk_parcela) REFERENCES parcela(id_parcela),
                          FOREIGN KEY (fk_vendedor) REFERENCES usuario(id_usuario)
);