# Execution of postgres commands at startup
# note the first line specifying the shel is removed to avoid provelem with line breaks in windows
echo "-- Begin setup"
sqlplus system/${TEST_ORACLE_PWD}@XE  <<-EOSQL
  create user sampledb identified by "$TEST_ORACLE_PWD" ACCOUNT UNLOCK;
  grant connect to sampledb;
  grant create session to sampledb;
  grant resource to sampledb;
  grant create table to sampledb; 
  grant create procedure to sampledb; 
  grant create view  to sampledb; 
EOSQL
echo "-- End setup"
