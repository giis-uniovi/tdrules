# Execution of sqlserver commands after startup
# A little bit flaky in GitHub, check first connection and wait if fails
echo "-- Try first connection"
if ! /opt/mssql-tools18/bin/sqlcmd -C -S localhost,1433 -U sa -P $TEST_SQLSERVER_PWD -l 30 -Q "select name from sys.databases"; then
  echo "Failure to connect from sa, wait some time"
  sleep 4
  echo "End sleep, going to setup"
  #this seems to solve login failure, but sometimes: Error: Process completed with exit code 137.
fi
echo "-- Begin setup"
/opt/mssql-tools18/bin/sqlcmd -C -S localhost,1433 -U sa -P $TEST_SQLSERVER_PWD -l 30  <<-EOSQL
  CREATE LOGIN tdclirdb WITH PASSWORD = '$TEST_SQLSERVER_PWD', CHECK_POLICY=OFF, CHECK_EXPIRATION=OFF, DEFAULT_LANGUAGE=spanish;
  GO
  CREATE DATABASE tdclirdb
  GO
  USE [tdclirdb]
  CREATE USER [tdclirdb] FOR LOGIN [tdclirdb] --WITH DEFAULT_SCHEMA=[dbo]
  EXEC sp_addrolemember 'db_owner', 'tdclirdb'
  GO
  ----
  CREATE LOGIN tdstorerdb2 WITH PASSWORD = '$TEST_SQLSERVER_PWD', CHECK_POLICY=OFF, CHECK_EXPIRATION=OFF, DEFAULT_LANGUAGE=spanish;
  GO
  CREATE DATABASE tdstorerdb2
  GO
  USE [tdstorerdb2]
  CREATE USER [tdstorerdb2] FOR LOGIN [tdstorerdb2] --WITH DEFAULT_SCHEMA=[dbo]
  EXEC sp_addrolemember 'db_owner', 'tdstorerdb2'
  GO
  ----
  CREATE LOGIN tdloadrdb WITH PASSWORD = '$TEST_SQLSERVER_PWD', CHECK_POLICY=OFF, CHECK_EXPIRATION=OFF, DEFAULT_LANGUAGE=spanish;
  GO
  CREATE DATABASE tdloadrdb
  GO
  USE [tdloadrdb]
  CREATE USER [tdloadrdb] FOR LOGIN [tdloadrdb] --WITH DEFAULT_SCHEMA=[dbo]
  EXEC sp_addrolemember 'db_owner', 'tdloadrdb'
  GO
EOSQL
echo "-- END SETUP!"