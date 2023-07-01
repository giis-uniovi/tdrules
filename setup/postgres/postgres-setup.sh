# Execution of postgres commands at startup
# note the first line specifying the shel is removed to avoid provelem with line breaks in windows
echo "-- Begin setup"
psql -v ON_ERROR_STOP=1   <<-EOSQL
  CREATE USER sampledb with encrypted password '$TEST_POSTGRES_PWD';
  CREATE DATABASE sampledb;
EOSQL
echo "-- END SETUP!"