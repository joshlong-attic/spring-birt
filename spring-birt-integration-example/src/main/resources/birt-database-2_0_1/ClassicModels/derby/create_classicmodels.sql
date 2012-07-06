--*******************************************************************************
--**  Copyright (c) 2005 Actuate Corporation.
--**  All rights reserved. This file and the accompanying materials
--**  are made available under the terms of the Eclipse Public License v1.0
--**  which accompanies this distribution, and is available at
--**  http://www.eclipse.org/legal/epl-v10.html
--** 
--**  Contributors:
--**  Actuate Corporation  - initial implementation
--** 
--**  Classic Models Inc. sample database developed as part of the
--**  Eclipse BIRT Project. For more information, see http:\\www.eclipse.org\birt
--** 
--** *******************************************************************************
--** Apachy Derby IJ script to create BIRT Sample Database
--** To run this script:
--** (1) Make sure that derby.jar and derbytools.jar are in current directory or classpath
--** (2) Run command:
--**     java -cp derby.jar;derbytools.jar -Dij.connection.sampledb=jdbc:derby:BirtSample;create=true \
--**          org.apache.derby.tools.ij create_classicmodels.sql
--**

CREATE TABLE Offices(
	officeCode VARCHAR(10),
	city VARCHAR(50),
	phone VARCHAR(50),
	addressLine1 VARCHAR(50),
	addressLine2 VARCHAR(50),
	state VARCHAR(50),
	country VARCHAR(50),
	postalCode VARCHAR(15),
	territory VARCHAR(10)
);

CREATE UNIQUE INDEX offices_pk ON Offices ( officeCode );

CREATE TABLE Customers(
	customerNumber INTEGER,
	customerName VARCHAR(50),
	contactLastName VARCHAR(50),
	contactFirstName VARCHAR(50),
	phone VARCHAR(50),
	addressLine1 VARCHAR(50),
	addressLine2 VARCHAR(50),
	city VARCHAR(50),
	state VARCHAR(50),
	postalCode VARCHAR(15),
	country VARCHAR(50),
	salesRepEmployeeNumber INTEGER,
	creditLimit DOUBLE
);

CREATE UNIQUE INDEX customers_pk ON Customers( customerNumber );

CREATE TABLE Orders(
	orderNumber INTEGER,
	orderDate DATE,
	requiredDate DATE,
	shippedDate DATE,
	status VARCHAR(15),
	comments LONG VARCHAR,
	customerNumber INTEGER 
);
CREATE UNIQUE INDEX orders_pk ON Orders( orderNumber );
CREATE INDEX orders_cutomer ON Orders( customerNumber );

CREATE TABLE OrderDetails(
	orderNumber INTEGER,
	productCode VARCHAR(15),
	quantityOrdered INTEGER,
	priceEach DOUBLE,
	orderLineNumber SMALLINT);
CREATE UNIQUE INDEX orderDetails_pk ON OrderDetails( orderNumber, productCode );

CREATE TABLE Employees(
	employeeNumber INTEGER,
	lastName VARCHAR(50),
	firstName VARCHAR(50),
	extension VARCHAR(10),
	email VARCHAR(100),
	officeCode VARCHAR(10),
	reportsTo INTEGER,
	jobTitle VARCHAR(50) 
);
cREATE UNIQUE INDEX employees_pk ON Employees( employeeNumber );

CREATE TABLE Payments(
	customerNumber INTEGER,
	checkNumber VARCHAR(50),
	paymentDate DATE,
	amount DOUBLE 
);
CREATE UNIQUE INDEX payments_pk ON Payments( customerNumber, checkNumber );

CREATE TABLE Products(
	productCode VARCHAR(15),
	productName VARCHAR(70),
	productLine VARCHAR(50),
	productScale VARCHAR(10),
	productVendor VARCHAR(50),
	productDescription LONG VARCHAR,
	quantityInStock INTEGER,
	buyPrice DOUBLE,
	MSRP DOUBLE
);
CREATE UNIQUE INDEX products_pk ON Products( productCode );

CREATE TABLE ProductLines(
	productLine VARCHAR(50),
	textDescription VARCHAR(4000),
	htmlDescription CLOB,
	image BLOB
);
CREATE UNIQUE INDEX productLines_pk on ProductLines( productLine );


