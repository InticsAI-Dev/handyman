# Handyman Raven – Database Documentation

## 1. Introduction

### 1.1 Purpose of This Document

This document describes the **database architecture, configuration, and operational standards** for the Handyman Raven platform. It is intended to:

- Provide a single source of truth for how the application connects to and uses relational databases (PostgreSQL).
- Document connection modes (legacy credential-based vs. Azure token-based), connection pooling (HikariCP), and configuration via `config.properties`.
- Support onboarding of developers and operations staff, troubleshooting connectivity issues, and planning capacity or compliance (e.g. retention, encryption).

**This document does not replace** schema-level DDL documentation or runbooks for specific pipelines; it focuses on database *access*, *configuration*, and *overview* of scope and conventions.

---

### 1.2 Scope of the Database

The database scope for Handyman Raven includes:

| Aspect | Scope |
|--------|--------|
| **Primary RDBMS** | PostgreSQL (e.g. `intics_zio` or environment-specific database). |
| **Access patterns** | Read/write via **Jdbi** over **HikariCP** connection pools; connection source is either **LEGACY** (per-resource credentials) or **AZURE** (shared pool with Azure AD token authentication). |
| **Schemas in use** | Application code references schemas such as `macro`, `config`, `inbound_config`, `doc_eyecue`, etc. Table and schema names are typically defined per pipeline/action (e.g. output tables, audit tables). |
| **Typical usage** | Pipeline configuration (e.g. `spw_process_config`, `spw_instance_config`), audit and result tables (e.g. `macro.copro_retry_error_audit`, multi-value SOR handling output tables, table extraction results, OCR comparison results), and BSH/config (e.g. `config.spw_bsh_config`). |
| **Out of scope** | Kafka broker internals, object storage, or external Copro/API services; only the RDBMS used by Handyman for persistence and configuration is in scope here. |

---

### 1.3 Intended Audience

This document is written for:

- **Developers** – Integrating new actions or pipelines that read/write to the database; understanding `config.properties` and connection types.
- **DevOps / SRE** – Deploying and tuning connection pools, configuring Azure or legacy credentials, and monitoring database connectivity.
- **Database / Platform teams** – Understanding connection and pool settings for capacity and security reviews.
- **Compliance / Security** – Reviewing data access patterns, encryption-related configuration, and retention considerations.

---

### 1.4 Architectural Overview

#### Connection model

Handyman Raven uses a **single primary RDBMS** (PostgreSQL) for pipeline configuration, audit, and result storage. Access is abstracted through:

1. **Resource name** – Actions refer to a logical resource (e.g. `resourceConn` / resource name) that resolves to a connection.
2. **Connection type** – Driven by `legacy.resource.connection.type` in `config.properties`:
   - **LEGACY** – Each resource name maps to its own connection config (e.g. URL, user, password from a config store). A HikariCP pool is created per such resource using `raven.db.*` (or resource-specific) settings.
   - **AZURE** – A single shared **Azure token–based** HikariCP pool is used for all database access; credentials are Azure AD (client id/secret/tenant) and the JDBC URL is taken from `raven.db.url`.

#### Components

- **ResourceAccess.rdbmsJDBIConn(resourceName)** – Entry point for obtaining a **Jdbi** instance for a given resource. In AZURE mode this returns the shared pool’s Jdbi; in LEGACY mode it returns the resource-specific Jdbi.
- **HandymanRepoImpl** – Uses the same connection type for core repository operations (e.g. pipeline config, audit writes); uses `raven.db.url`, `raven.db.user`, `raven.db.password`, `raven.max.connection` in LEGACY mode.
- **HikariJdbiProvider** – In AZURE mode, initializes and holds the shared HikariCP data source (Azure token–based) and exposes it as Jdbi; reads pool and Azure settings from `config.properties`.

#### Data flow (simplified)

```
Pipeline / Action
       ↓
ResourceAccess.rdbmsJDBIConn(resourceName)
       ↓
LEGACY → Per-resource Jdbi (HikariCP per resource)
   or
AZURE  → HikariJdbiProvider.getJdbi() (single shared pool, token auth)
       ↓
PostgreSQL (e.g. intics_zio)
```

Audit and result tables (e.g. in `macro` or action-specific schemas) are written by individual actions (e.g. MultivalueSorItemHandling, TableExtraction, SectionFiltering, OCR comparison, Copro retry audit).

---

### 1.5 Naming Conventions & Standards

| Convention | Description |
|------------|-------------|
| **Schema names** | Lowercase, descriptive: `macro` (pipeline/audit data), `config` (configuration and BSH), `inbound_config`, `doc_eyecue`, etc. |
| **Table names** | Snake_case; e.g. `copro_retry_error_audit`, `multi_value_sor_item_audit`, `sor_meta_consolidated_audit`, `ocr_text_comparison_result`, `spw_process_config`, `spw_instance_config`, `spw_bsh_config`. |
| **Config keys** | Dot-separated, lowercase with category prefix: `raven.db.*`, `azure.identity.*`, `kafka.ssl.*`, `hikari.*`, `protegrity.*`, etc. |
| **Resource names** | Logical names used in code (e.g. `intics_zio_db_conn`); in LEGACY mode these resolve to stored connection configs; in AZURE mode they are not used for URL selection (single URL from `raven.db.url`). |

Consistent naming improves discoverability and aligns with existing references in code (e.g. `macro.copro_retry_error_audit`, `config.spw_bsh_config`).

---

### 1.6 Data Retention & Compliance Overview

- **Retention** – Data retention is not enforced by Handyman Raven itself; it is the responsibility of the database and platform policies (e.g. table-level retention jobs, archival, purges). Actions write to configured audit and result tables; retention rules should be defined per schema/table and executed by DB/platform automation.
- **Encryption** – The application supports:
  - **In-transit**: Prefer TLS for JDBC (e.g. `ssl=true` or equivalent in `raven.db.url` where required).
  - **At-rest**: Handled by the database and infrastructure; Handyman does not implement at-rest encryption itself.
  - **Application-level**: Optional item-wise encryption (e.g. Protegrity/AES) for specific fields; see **Section 2** for `aes.secretKey`, `protegrity.enc.api.url`, `protegrity.dec.api.url`. Pipeline-level flags (e.g. from context) control when encryption/decryption is applied.
- **Credentials** – In LEGACY mode, credentials are supplied via config (`raven.db.user`, `raven.db.password`). In AZURE mode, authentication uses Azure AD (client id/secret/tenant); passwords in `config.properties` should be protected (e.g. secret manager, restricted file permissions).
- **Compliance** – Specific regulatory requirements (e.g. GDPR, HIPAA) must be addressed at the environment level (access control, audit logging, retention, encryption). This document only outlines how the application uses the database and configuration so that those controls can be designed and audited.

---

## 2. Configuration Reference: config.properties

The application is configured primarily via `config.properties` (e.g. under `src/main/resources/config.properties`). The following sections list and explain each variable. Values shown are examples; production values (especially secrets) must be set per environment.

---

### 2.1 Database Connection (Raven)

| Variable | Description | Example / Notes |
|----------|-------------|------------------|
| **raven.db.url** | JDBC URL for the primary PostgreSQL database. Used in both LEGACY and AZURE modes for the main Handyman connection. | `jdbc:postgresql://localhost:5432/intics_zio` |
| **raven.db.user** | Database username. In LEGACY mode used for login; in AZURE mode may be used as the Azure AD principal name for token-based auth. | `anandh.andrews` |
| **raven.db.password** | Database password. Used in LEGACY mode; in AZURE mode may be unused if pure token auth is used. | Keep in secret manager in production. |
| **raven.max.connection** | Maximum number of connections in the pool when using LEGACY mode (HandymanRepoImpl). | `500` |

---

### 2.2 Resource Connection Type

| Variable | Description | Example / Notes |
|----------|-------------|------------------|
| **legacy.resource.connection.type** | Determines how Jdbi connections are obtained: **LEGACY** = per-resource credentials/pools; **AZURE** = single shared Azure token–based pool (HikariJdbiProvider). | `LEGACY` or `AZURE` |

---

### 2.3 Kafka (SSL & Authentication)

| Variable | Description | Example / Notes |
|----------|-------------|------------------|
| **kafka.authentication.sasl.ssl.include** | Indicates SASL/SSL usage for Kafka (e.g. inclusion of certs). | `certs` |
| **kafka.ssl.truststore.type** | Kafka client truststore type. | `JKS` |
| **kafka.ssl.keystore.type** | Kafka client keystore type. | `JKS` |
| **kafka.ssl.truststore.location** | Path to Kafka truststore file. | `/etc/kafka/secrets/kafka.truststore.jks` |
| **kafka.ssl.truststore.password** | Truststore password. | Keep secret. |
| **kafka.ssl.keystore.location** | Path to Kafka keystore file. | `/etc/kafka/secrets/kafka.keystore.jks` |
| **kafka.ssl.keystore.password** | Keystore password. | Keep secret. |
| **kafka.ssl.key.password** | Key password (often same as keystore). | Keep secret. |
| **kafka.ssl.endpoint.identification.algorithm** | Endpoint identification algorithm for Kafka SSL; empty to disable hostname verification if required. | `` (empty) or e.g. `https` |
| **kafka.ssl.acks** | Kafka producer acks setting. | `all` |
| **kafka.ssl.api.retries** | Kafka producer retries. | `3` |
| **kafka.ssl.request.timeout.ms** | Kafka request timeout (ms). | `30000` |
| **kafka.ssl.delivery.timeout.ms** | Kafka delivery timeout (ms). | `300000` |
| **kafka.ssl.linger.ms** | Kafka producer linger (ms). | `500` |

---

### 2.4 Azure Identity (Token-Based DB & HikariCP)

Used when `legacy.resource.connection.type=AZURE` for database authentication and optional token refresh.

| Variable | Description | Example / Notes |
|----------|-------------|------------------|
| **azure.identity.tenantId** | Azure AD tenant ID. | Required for token-based auth. |
| **azure.identity.clientId** | Azure AD application (client) ID. | Required for token-based auth. |
| **azure.identity.clientSecret** | Azure AD client secret. | Keep secret. |
| **azure.token.scope** | OAuth scope for the token (e.g. for Azure SQL/PostgreSQL). | Set per Azure setup. |
| **azure.identity.refresh.minutes** | Interval (minutes) for refreshing Azure identity/token. | `55` |

**HikariCP pool (Azure mode):**

| Variable | Description | Example / Notes |
|----------|-------------|------------------|
| **azure.identity.hcp.minimum.idle** | Minimum idle connections in the shared pool. | `100` |
| **azure.identity.hcp.max.pool.size** | Maximum pool size for the shared Azure data source. | `200` (code default may use 300 if unset) |
| **azure.identity.hcp.conn.timeout** | Connection timeout (ms). | `30000` |
| **azure.identity.hcp.idle.timeout** | Idle timeout (ms). | `35000` |
| **azure.identity.hcp.max.lifetime** | Max lifetime of a connection in the pool (ms). | `45000` |
| **azure.identity.hcp.application.name** | Application name sent to the database (e.g. for identification in DB logs). | `Intics-vulcan-HCP-App` |

---

### 2.5 HikariCP Metrics (Application-Level)

| Variable | Description | Example / Notes |
|----------|-------------|------------------|
| **hikari.metrics.log.interval.seconds** | Interval in seconds at which HikariCP metrics (active/idle/total connections, threads awaiting) are logged. | `60` |
| **hikari.metrics.log.enabled** | Whether HikariCP metrics logging is enabled. | `true` |

---

### 2.6 Encryption (Application-Level)

Used for optional field-level encryption/decryption (e.g. Protegrity or AES).

| Variable | Description | Example / Notes |
|----------|-------------|------------------|
| **aes.secretKey** | Base64-encoded AES secret key for symmetric encryption when using AES implementation. | Keep secret; rotate per policy. |
| **protegrity.enc.api.url** | Protegrity encryption API endpoint (when using Protegrity for encrypt). | `http://localhost:8190/vulcan/api/encryption/encrypt` |
| **protegrity.dec.api.url** | Protegrity decryption API endpoint (when using Protegrity for decrypt). | `http://localhost:8190/vulcan/api/encryption/decrypt` |

Note: Some pipeline contexts may override encryption URLs or behavior via action context (e.g. in tests); production typically relies on these config values or environment-specific overrides.

---

## 3. Summary

- **Sections 1.1–1.6** define the purpose, scope, audience, architecture, naming, and retention/compliance overview for the Handyman Raven database.
- **Section 2** documents every variable in `config.properties`: database connection, resource connection type, Kafka SSL/producer, Azure identity and HikariCP, HikariCP metrics, and encryption-related settings.

For schema-level details (table definitions, indexes, FKs), refer to the database DDL or schema documentation maintained for your environment. For pipeline-specific tables (e.g. output tables per action), refer to the action or pipeline configuration and code.
