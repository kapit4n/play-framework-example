# --- !Downs
drop table IF EXISTS bancos;
drop table IF EXISTS user;
drop table IF EXISTS association;
drop table IF EXISTS module;
drop table IF EXISTS account;
drop table IF EXISTS productor;
drop table IF EXISTS transaction;
drop table IF EXISTS transactionDetail;
drop table IF EXISTS proveedor;
drop table IF EXISTS reportes;
drop table IF EXISTS product;
drop table IF EXISTS productInv;
drop table IF EXISTS discountReport;
drop table IF EXISTS discountDetail;
drop table IF EXISTS requestRow;
drop table IF EXISTS productRequest;
drop table IF EXISTS requestRowProductor;
drop table IF EXISTS logEntry;
drop table IF EXISTS measure;
drop table IF EXISTS company;
drop table IF EXISTS roles;
drop table IF EXISTS userRole;

## --- !Ups
create table association (
  id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) not null,
  average double,
  pleno double,
  excedent double,
  total double
);

create table module (
  id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) not null,
  president VARCHAR(100),
  description VARCHAR(250),
  associationId INT(6),
  associationName VARCHAR(100)
);

create table measure (
  id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100),
  quantity double,
  description VARCHAR(250),
  measureId INT,
  measureName VARCHAR(100)
);

create table account (
  id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(30),
  name VARCHAR(100),
  type VARCHAR(30),
  parent INT(6),
  negativo VARCHAR(30),
  description VARCHAR(250),
  child boolean,
  debit double,
  credit double,
  createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

create table transaction (
  id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  date VARCHAR(30),
  type VARCHAR(30),
  description VARCHAR(250),
  createdBy INT,
  createdByName VARCHAR(100),
  autorizedBy INT,
  autorizedByName VARCHAR(100), 
  receivedBy INT,
  receivedByName VARCHAR(100), 
  updatedBy INT(6),
  updatedByName VARCHAR(100),
  createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

create table company (
  id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100),
  president VARCHAR(50),
  description VARCHAR(2505)
);

create table logEntry (
  id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  action VARCHAR(100),
  tableName1 VARCHAR(100),
  userId INT(6),
  userName VARCHAR(255),
  description VARCHAR(255),
  createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

create table transactionDetail (
  id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  transaction INT,
  account INT,
  debit double,
  credit double,
  transactionDate VARCHAR(30),
  accountCode VARCHAR(30),
  accountName VARCHAR(100),
  createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  createdBy INT,
  createdByName VARCHAR(100)
);

create table bancos (
  id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) not null,
  tipo VARCHAR(30) not null,
  currentMoney VARCHAR(30),
  typeMoney VARCHAR(30)
);

create table product (
  id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) not null,
  cost double DEFAULT 0,
  totalValue double DEFAULT 0,
  percent double DEFAULT 0,
  price double DEFAULT 0,
  description VARCHAR(250) DEFAULT '',
  measureId INT DEFAULT 0,
  measureName VARCHAR(100) DEFAULT '',
  currentAmount INT  DEFAULT 0,
  proveedorId INT DEFAULT 0,
  proveedorName VARCHAR(100) DEFAULT '',
  proveedorCode VARCHAR(50) DEFAULT '',
  type VARCHAR(50) DEFAULT '',
  createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

create table productor (
  id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) not null,
  carnet INT not null,
  telefono INT,
  direccion VARCHAR(100),
  account VARCHAR(30),
  module INT,
  moduleName VARCHAR(100),
  associationName VARCHAR(100),
  associationId INT,
  acopio INT,
  status VARCHAR(30),
  promedio INT,
  pleno INT,
  excedent INT,
  totalDebt double,
  numberPayment INT,
  position VARCHAR(30),
  createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

create table proveedor (
  id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) not null,
  telefono INT,
  direccion VARCHAR(100),
  contacto VARCHAR(100),
  account INT,
  createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

create table reportes (
  id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  monto INT not null,
  account INT not null,
  cliente INT not null,
  createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

create table user (
  id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) not null,
  carnet INT not null,
  telefono INT,
  direccion VARCHAR(30),
  sueldo INT,
  type VARCHAR(30),
  login VARCHAR(30),
  password VARCHAR(30),
  createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

create table userRole (
  id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  userId INT,
  roleName VARCHAR(100),
  roleCode VARCHAR(50)
);

create table roles (
  id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  roleName VARCHAR(100),
  roleCode VARCHAR(50)
);

create table productRequest (
  id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  date VARCHAR(30),
  veterinario INT,
  veterinarioname VARCHAR(100),
  storekeeper INT,
  storekeeperName VARCHAR(100),
  user INT,
  userName VARCHAR(100),
  module INT,
  moduleName VARCHAR(100),
  totalPrice double,
  paid double,
  credit double,
  paidDriver double,
  creditDriver double,
  status VARCHAR(30),
  detail VARCHAR(250),
  type VARCHAR(30),
  createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  createdBy INT,
  createdByName VARCHAR(100),
  acceptedBy INT,
  acceptedByName VARCHAR(100),
  acceptedAt Date
);

create table requestRow (
  id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  requestId INT,
  productId INT,
  productName VARCHAR(100),
  productorId INT,
  productorName VARCHAR(100),
  quantity INT,
  price double,
  totalPrice double,
  paid double,
  credit double,
  paidDriver double,
  creditDriver double,
  measureId INT,
  measureName VARCHAR(100),
  status VARCHAR(30),
  createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  createdBy INT,
  createdByName VARCHAR(100)
);

create table requestRowProductor (
  id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  requestRowId INT,
  productId INT,
  productName VARCHAR(100),
  productorId INT,
  productorName VARCHAR(100),
  measureId INT,
  measureName VARCHAR(100),
  quantity INT,
  price double,
  totalPrice double,
  paid double,
  credit double,
  status VARCHAR(30),
  type VARCHAR(30),
  createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  createdBy INT,
  createdByName VARCHAR(100),
  payType VARCHAR(20)
);

create table productInv (
  id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  productId INT,
  proveedorId INT,
  measureId INT,
  productName VARCHAR(60),
  proveedorName VARCHAR(60),
  measureName VARCHAR(60),
  amount INT  DEFAULT 0,
  amountLeft INT  DEFAULT 0,
  cost_unit double  DEFAULT 0,
  total_cost double  DEFAULT 0,
  createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

create table discountReport (
  id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  startDate VARCHAR(30),
  endDate VARCHAR(30),
  status VARCHAR(30),
  total double,
  user_id INT,
  createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

create table discountDetail (
  id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  discountReport INT,
  requestRow INT,
  productorId INT,
  productorName VARCHAR(100),
  status VARCHAR(30),
  discount double
);
