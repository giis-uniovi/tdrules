#!/bin/bash

# Container setup for local development environment

# Define the credentials to access each database:
#  - either set the environment variables `TEST_POSTGRES_PWD`, `TEST_ORACLE_PWD` and `TEST_SQLSERVER_PWD`
#  - or set their assignments in a file `setup/environment.properties` as pairs `name=value`
#    (this file is included in .gitignore to avoid storing credentials in the remote repo)

# ensures this script is running in his folder
SCRIPT_DIR=$(readlink -f $0 | xargs dirname)
echo "run command at directory: $SCRIPT_DIR"
cd $SCRIPT_DIR

# Required environment variables can be set in this file
source ./environment.properties

echo "Postgres setup for local environment"
# Note the / in the volume bind, required when running under some windows shells (e.g. mingw)
docker stop test-postgres && docker rm test-postgres
docker run -d -p 5432:5432 --name test-postgres  --restart unless-stopped \
  -e POSTGRES_PASSWORD="$TEST_POSTGRES_PWD" \
  -e TEST_POSTGRES_PWD="$TEST_POSTGRES_PWD" \
  -v /${PWD}/postgres:/docker-entrypoint-initdb.d \
  postgres:14
./wait-container-ready.sh test-postgres "END SETUP!"


echo "Sqlserver setup for local environment"
docker stop test-sqlserver && docker rm test-sqlserver
docker run -d -p 1433:1433 --name test-sqlserver  --restart unless-stopped \
  -e SA_PASSWORD="$TEST_SQLSERVER_PWD" \
  -e TEST_SQLSERVER_PWD="$TEST_SQLSERVER_PWD" \
  -e "ACCEPT_EULA=Y" -e "MSSQL_PID=Developer" \
  -v /${PWD}/sqlserver:/setup.d \
  mcr.microsoft.com/mssql/server:2019-latest
./wait-container-ready.sh test-sqlserver "SQL Server is now ready for client connections"
# SQLServer does not have an on startup script, run it now
docker exec test-sqlserver bash -c "./setup.d/sqlserver-setup.sh"

#/opt/mssql-tools/bin/sqlcmd -S localhost,1433 -U sa -P $TEST_SQLSERVER_PWD -Q "select name from sys.databases"


echo "Oracle setup for local environment"
docker stop test-oracle && docker rm test-oracle
docker run -d -p 1521:1521 --name test-oracle  --restart unless-stopped \
  -e ORACLE_PASSWORD=$TEST_ORACLE_PWD \
  -e TEST_ORACLE_PWD="$TEST_ORACLE_PWD" \
  -v /${PWD}/oracle:/container-entrypoint-initdb.d \
  gvenzl/oracle-free:slim-faststart
./wait-container-ready.sh test-oracle "DATABASE IS READY TO USE!"

#docker exec -it test-oracle sqlplus sampledb/${TEST_ORACLE_PWD}@FREE

echo "Cassandra setup for local environment"
# Uses the default parameters to run container, except that disables gossip protocol to faster startup
docker stop test-cassandra && docker rm test-cassandra
docker run -d -p 9042:9042 --name test-cassandra  --restart unless-stopped \
  -v /${PWD}/cassandra/cassandra-setup.cql:/cassandra-setup.cql \
  -e JVM_OPTS="-Dcassandra.skip_wait_for_gossip_to_settle=0 -Dcassandra.initial_token=0" \
  cassandra:4.1
./wait-container-ready.sh test-cassandra "Created default superuser role 'cassandra'"
docker exec test-cassandra bash -c "cqlsh localhost 9042 -u cassandra -p $TEST_CASSANDRA_PWD -f /cassandra-setup.cql"
