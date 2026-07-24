# AGENTS.md

This file provides guidance to coding agents (Claude Code, GitHub Copilot, etc.) when working with code in this repository.

TdRules (Test Data Rules, formerly SQLRules) generates *Full Predicate Coverage* rules and *SQL Mutants* to assess and generate test data for a query, and discovers data-store schemas. It is a multi-module Maven project (Java 8+) published to Maven Central under `io.github.giis-uniovi` (see `tdrules-bom`), with a .NET subset published to NuGet (`TdRules`).

## Critical: generated code (OpenAPI models + Java→C#)

Two kinds of code in this repo are generated and must not be hand-edited:

1. **Model classes from the OpenAPI spec.** The schema/rules model is defined by the OpenAPI spec in `tdrules-model/src/main/resources/api/v4/` (`api.yml` and friends). It generates:
   - the **Java** model package `giis.tdrules.openapi.model` at build time via the `openapi-generator-maven-plugin` (in `tdrules-model/pom.xml`);
   - the **C#** model (`Giis.Tdrules.Openapi`) via `ant openapi` in `net/` (openapi-generator CLI + a `dotnet run` post-process in `net/OpenApiPostprocess` that adds Java-compatible getters/setters).
   To change the model, edit the OpenAPI YAML, not the generated classes.

2. **The rest of the .NET code is converted from Java.** `net/build.xml` (`convert` target) runs [JavaToCSharp](https://github.com/paulirwin/JavaToCSharp) over the Java sources and applies many `replacefilter` substitutions. Files under `net/**/Translated/` carry the header `THIS FILE HAS BEEN AUTOMATICALLY CONVERTED FROM THE JAVA SOURCES. DO NOT EDIT`. Java is the source of truth; regenerate instead of editing `.cs`.

Regenerate + verify the .NET build (this is what CI runs on every push):
```bash
cd net
dotnet tool install JavaToCSharpCli --global   # once
ant openapi convert
dotnet build
```
Several `<replace ... failOnNoReplacements="true"/>` rules in `net/build.xml` deliberately fail the conversion if an expected Java code pattern disappears — that is an alarm that a Java change needs a matching conversion rule, not a bug.

## Build and test (Java)

Run Maven from the repo root (reactor build of all modules). Surefire uses `testFailureIgnore=true`, so `mvn test` exits 0 even on failures — check `*/target/surefire-reports` and the aggregated HTML under `target/site/`.

```bash
mvn test                                   # all modules, unit tests
mvn test -pl tdrules-store-rdb -am -Dtest=TestSqlserverSchema   # single class in one module (+ deps)
```

Tests are partitioned into **scopes** by class-name prefix (see `.github/workflows/test.yml`), because DB-backed tests need a running container:
- **Core / UT** — no database: `mvn test -Dtest='!TestPostgres*,!TestSqlserver*,!TestOracle*,!TestCassandra*,!ZerocodeScriptTestClass*'`
- **Postgres / Sqlserver / Oracle / Cassandra** — one DBMS each, restricted to the RDB modules:
  ```bash
  mvn test -Dtest='TestPostgres*' -pl tdrules-client-rdb,tdrules-store-rdb,tdrules-store-loader -am \
    -Dsurefire.failIfNoSpecifiedTests=false -Duser.timezone=Europe/Madrid
  ```
  `-Duser.timezone=Europe/Madrid` is required for Oracle (avoids `ORA-01882: timezone region not found`).

### Local test databases

`setup/container-setup.sh` starts the DBMS containers used by the DB scopes (postgres:17, mssql 2019, gvenzl/oracle-free, cassandra:4.1). Provide credentials via env vars `TEST_POSTGRES_PWD` / `TEST_SQLSERVER_PWD` / `TEST_ORACLE_PWD` / `TEST_CASSANDRA_PWD`, or a `setup/environment.properties` file (git-ignored). `setup/database.properties` holds the JDBC connection config the tests read.

### Integration tests

`tdrules-it` runs integration tests that need `newman` (`npm install -g newman`) for the generated Postman collections and an embedded `mockserver`. Broader system tests using real SUTs live in a separate repo, [giis-uniovi/tdrules-st-tdg](https://github.com/giis-uniovi/tdrules-st-tdg).

## Build and test (.NET)

`net/TdRules.sln`, netstandard2.0 library, SDK 10. Some tests require SQL Server running (see setup above):
```bash
cd net
dotnet test TdRulesTest/TdRulesTest.csproj
dotnet test TdRulesTestDbProviders/TdRulesTestDbProviders.csproj
dotnet test --filter FullyQualifiedName~TestSqlserver   # subset
```
The .NET side is a **subset**: only the RDB path is implemented/tested (SQL Server); OpenApi schema discovery is Java-only.

## Architecture

Two capabilities: (a) generate FPC rules / SQL mutants for a query against a schema, by calling the remote [TdRules service](https://in2test.lsi.uniovi.es/tdrules/); (b) discover the schema and load test data. Everything is expressed against the shared OpenAPI **model** (`TdSchema`, `TdRules`).

Module dependency graph (from the README):
```
client      ──────────────▶ model
client-oa   ──────────────▶ model
client-rdb  ──▶ store-rdb ─▶ store-shared
client-rdb  ──────────────▶ model
store-loader ─────────────▶ model, store-shared
```
- `tdrules-model` — the OpenAPI-generated models plus IO/serialization and model utilities.
- `tdrules-client` — client API to the TdRules service: `new TdRulesApi().getRules(schema, query, "")`.
- `tdrules-client-oa` — build a `TdSchema` from an OpenAPI spec (`new OaSchemaApi(spec).getSchema()`).
- `tdrules-client-rdb` — build a `TdSchema` from a live JDBC connection (`new DbSchemaApi(conn).getSchema()`); relational-DB dependent.
- `tdrules-store-rdb` — core relational schema discovery (dialect-specific: PostgreSQL, SQL Server, Oracle).
- `tdrules-store-shared` — cross-store shared components (`dtypes`, `ids`, `stypes`).
- `tdrules-store-loader` — load test data via REST API or JDBC; also emits Postman / Zerocode scenarios.
- `tdrules-bom` — bill of materials for all components.
- `tdrules-it` — integration tests. `net/` — the .NET implementation. `setup/` — local DB containers.

## Conventions

- The Maven `source`/`target` is **Java 8**; published snapshots build with Java 8. Keep sources 8-compatible.
- Public API names match across platforms except casing (Java `getSchema()` ↔ C# `GetSchema()`).
- `.github/workflows/test.yml` uses an `if:` guard on the test jobs to avoid double runs for local-branch PRs while allowing forked-repo and dependabot PRs — preserve it when editing the workflow.
