# Execution of postgres commands at startup
# note the first line specifying the shell is removed to avoid problems with line breaks in windows
echo "-- Begin setup"
sqlplus system/${TEST_ORACLE_PWD}@XE  <<-EOSQL
  create user tdstorerdb2 identified by "$TEST_ORACLE_PWD" ACCOUNT UNLOCK;
  grant connect to tdstorerdb2;
  grant create session to tdstorerdb2;
  grant resource to tdstorerdb2;
  grant create table to tdstorerdb2; 
  grant create procedure to tdstorerdb2; 
  grant create view to tdstorerdb2;
  -- migration from 11 to 21 requires granting access to tablespace
  grant unlimited tablespace to tdstorerdb2;
  
  -- special databases (0 and 1) to check visibility of schemas
  create user tdstorerdb0 identified by "$TEST_ORACLE_PWD" ACCOUNT UNLOCK;
  grant connect to tdstorerdb0;
  grant create session to tdstorerdb0;
  grant resource to tdstorerdb0;
  grant create table to tdstorerdb0; 
  grant create procedure to tdstorerdb0; 
  grant create view  to tdstorerdb0; 
  grant dba to tdstorerdb0;
  grant unlimited tablespace to tdstorerdb0;
  
  create user tdstorerdb1 identified by "$TEST_ORACLE_PWD" ACCOUNT UNLOCK;
  grant connect to tdstorerdb1;
  grant create session to tdstorerdb1;
  grant resource to tdstorerdb1;
  grant create table to tdstorerdb1; 
  grant create procedure to tdstorerdb1; 
  grant create view  to tdstorerdb1; 
  grant dba to tdstorerdb1;
  grant unlimited tablespace to tdstorerdb1;
----
  create user tdloadrdb identified by "$TEST_ORACLE_PWD" ACCOUNT UNLOCK;
  grant connect to tdloadrdb;
  grant create session to tdloadrdb;
  grant resource to tdloadrdb;
  grant create table to tdloadrdb; 
  grant create procedure to tdloadrdb; 
  grant create view to tdloadrdb;
  grant unlimited tablespace to tdloadrdb;
 
EOSQL
echo "-- End setup"
