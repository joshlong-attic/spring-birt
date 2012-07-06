--*******************************************************************************
--**  Copyright (c) 2005 Actuate Corporation.
--**  All rights reserved. This file and the accompanying materials
--**  are made available under the terms of the Eclipse Public License v1.0
--**   which accompanies this distribution, and is available at
--**  http://www.eclipse.org/legal/epl-v10.html
--** 
--**  Contributors:
--**  Actuate Corporation  - initial implementation
--** 
--**  Classic Models Inc. sample database developed as part of the
--**  Eclipse BIRT Project. For more information, see http:\\www.eclipse.org\birt
--** 
--** *******************************************************************************
--** Apachy Derby IJ script to import data to classic models database
--** To run this script:
--** (1) Make sure that derby.jar and derbytools.jar are in current directory or classpath.
--** (2) Make sure that all data files (*.txt) are in the .\datafiles directory.
--** (3) Make sure that create_classicmodels.sql has been run
--** (4) Run command:
--**     java -cp derby.jar;derbytools.jar -Dij.connection.sampledb=jdbc:derby:BirtSample \
--**          org.apache.derby.tools.ij load_classicmodels.sql
--**
--** Note that the The CLOB and BLOB data used in the ProductLines table needs to be uploaded manually.

CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE (
    null,'OFFICES','.\datafiles\Offices.txt',null,null,null,0);

CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE (
    null,'CUSTOMERS','.\datafiles\Customers.txt',null,null,null,0);

CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE (
    null,'ORDERS','.\datafiles\Orders.txt',null,null,null,0);

CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE (
    null,'ORDERDETAILS','.\datafiles\OrderDetails.txt',null,null,null,0);

CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE (
    null,'EMPLOYEES','.\datafiles\Employees.txt',null,null,null,0);

CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE (
    null,'PAYMENTS','.\datafiles\Payments.txt',null,null,null,0);

CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE (
    null,'PRODUCTS','.\datafiles\Products.txt',null,null,null,0);

CALL SYSCS_UTIL.SYSCS_IMPORT_DATA (
    null,'PRODUCTLINES','PRODUCTLINE,TEXTDESCRIPTION',null,'.\datafiles\ProductLines.txt',null,null,null,0);
