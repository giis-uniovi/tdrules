# Execution of postgres commands at startup
# note the first line specifying the shell is removed to avoid problems with line breaks in windows
echo "-- Begin setup"
psql -v ON_ERROR_STOP=1   <<-EOSQL
  CREATE USER tdclirdb with encrypted password '$TEST_POSTGRES_PWD';
  CREATE DATABASE tdclirdb OWNER tdclirdb;

  CREATE USER tdstorerdb2 with encrypted password '$TEST_POSTGRES_PWD';
  CREATE DATABASE tdstorerdb2 OWNER tdstorerdb2;

  CREATE USER tdloadrdb with encrypted password '$TEST_POSTGRES_PWD';
  CREATE DATABASE tdloadrdb OWNER tdloadrdb;
EOSQL
echo "-- END SETUP!"