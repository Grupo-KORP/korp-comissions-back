'CREATE TABLE roles (
                       idRole INT AUTO_INCREMENT PRIMARY KEY,
                       nome VARCHAR(50) NOT NULL
);

CREATE TABLE usuario (
                         idUsuario INT AUTO_INCREMENT PRIMARY KEY,
                         nome VARCHAR(250) NOT NULL,
                         email VARCHAR(120) NOT NULL,
                         telefone VARCHAR(20),
                         percentualComissao DECIMAL(5,2),
                         fkRole INT NOT NULL,
                         FOREIGN KEY (fkRole) REFERENCES roles(idRole)
);

CREATE TABLE cliente (
                         idCliente INT AUTO_INCREMENT PRIMARY KEY,
                         razaoSocial VARCHAR(150) UNIQUE NOT NULL,
                         nomeFantasia VARCHAR(150) NOT NULL,
                         cnpj VARCHAR(20) UNIQUE NOT NULL,
                         telefone VARCHAR(20),
                         email VARCHAR(120) NOT NULL
);

CREATE TABLE distribuidor (
                              idDistribuidor INT AUTO_INCREMENT PRIMARY KEY,
                              razaoSocial VARCHAR(150) UNIQUE NOT NULL,
                              nomeFantasia VARCHAR(150) NOT NULL,
                              cnpj VARCHAR(20) UNIQUE NOT NULL,
                              telefone VARCHAR(20),
                              email VARCHAR(120) NOT NULL
);

CREATE TABLE contato (
                         idContato INT AUTO_INCREMENT PRIMARY KEY,
                         nome VARCHAR(250) NOT NULL,
                         email VARCHAR(120) NOT NULL,
                         telefone VARCHAR(20),
                         fkCliente INT,
                         fkDistribuidor INT,

                         FOREIGN KEY (fkCliente) REFERENCES cliente(idCliente),
                         FOREIGN KEY (fkDistribuidor) REFERENCES distribuidor(idDistribuidor)
);

CREATE TABLE produto (
                         idProduto INT AUTO_INCREMENT PRIMARY KEY,
                         nome VARCHAR(150) NOT NULL,
                         descricao TEXT
);

CREATE TABLE pedido (
                        idPedido INT AUTO_INCREMENT PRIMARY KEY,
                        dataPedido DATE NOT NULL,
                        numeroNotaDistribuidor INT,
                        valorTotalRevenda DECIMAL(10,2) NOT NULL,
                        valorTotalFaturamento DECIMAL(10,2) NOT NULL,
                        statusPedido VARCHAR(50) DEFAULT 'PENDENTE' NOT NULL,
                        frete BOOLEAN,
                        transportadora VARCHAR(150),
                        observacoes VARCHAR(250),
                        fkVendedor INT NOT NULL,
                        fkCliente INT NOT NULL,
                        fkDistribuidor INT NOT NULL,

                        FOREIGN KEY (fkVendedor) REFERENCES usuario(idUsuario),
                        FOREIGN KEY (fkCliente) REFERENCES cliente(idCliente),
                        FOREIGN KEY (fkDistribuidor) REFERENCES distribuidor(idDistribuidor)
);

CREATE TABLE pagamento (
                           idPagamento INT AUTO_INCREMENT PRIMARY KEY,

                           metodoPagamento ENUM(
        'PIX',
        'BOLETO',
        'CARTAO_CREDITO',
        'CARTAO_DEBITO',
        'TRANSFERENCIA',
        'DINHEIRO'
    ) NOT NULL,

                           parcelado BOOLEAN DEFAULT FALSE,
                           quantidadeParcelas INT,

                           fkPedido INT UNIQUE NOT NULL,

                           FOREIGN KEY (fkPedido) REFERENCES pedido(idPedido)
);

CREATE TABLE parcela (
                         idParcela INT AUTO_INCREMENT PRIMARY KEY,
                         numeroParcela INT NOT NULL,
                         valorParcela DECIMAL(10,2) NOT NULL,
                         dataVencimento DATE NOT NULL,
                         dataPagamento DATE,

                         statusParcela ENUM(
        'PENDENTE',
        'PAGO',
        'ATRASADO',
        'CANCELADO'
    ) DEFAULT 'PENDENTE',

                         fkPagamento INT NOT NULL,

                         FOREIGN KEY (fkPagamento) REFERENCES pagamento(idPagamento)
);

CREATE TABLE item_pedido (
                             idItemPedido INT AUTO_INCREMENT PRIMARY KEY,
                             quantidade INT NOT NULL,
                             valor_unitario DECIMAL(10,2) NOT NULL,
                             fkPedido INT NOT NULL,
                             fkProduto INT NOT NULL,

                             FOREIGN KEY (fkPedido) REFERENCES pedido(idPedido),
                             FOREIGN KEY (fkProduto) REFERENCES produto(idProduto)
);


CREATE TABLE comissao (
                          idComissao INT AUTO_INCREMENT PRIMARY KEY,
                          valorComissao DECIMAL(10,2) NOT NULL,

                          statusComissao ENUM(
        'PENDENTE',
        'LIBERADA',
        'PAGA',
        'CANCELADA'
    ) DEFAULT 'PENDENTE',

                          dataPagamento DATE,

                          fkPedido INT NOT NULL,
                          fkParcela INT NOT NULL,
                          fkVendedor INT NOT NULL,

                          FOREIGN KEY (fkPedido) REFERENCES pedido(idPedido),
                          FOREIGN KEY (fkParcela) REFERENCES parcela(idParcela),
                          FOREIGN KEY (fkVendedor) REFERENCES usuario(idUsuario)
);