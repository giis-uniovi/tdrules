-- Just a single keyspace and table for smoke tests
CREATE KEYSPACE IF NOT EXISTS tdstorerdb2 WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : '1' };
CREATE TABLE tdstorerdb2.stypes (id int PRIMARY KEY, txt VARCHAR, dat DATE);
