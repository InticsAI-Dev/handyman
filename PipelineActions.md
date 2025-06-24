# Handyman Project Documentation

## Overview

### What is Handyman?
Handyman is a Domain-Specific Language (DSL) based automation framework designed for building file processing pipelines. The framework provides a powerful DSL syntax for defining automated workflows that handle both inbound and outbound file processing operations. Built on top of ANTLR for DSL parsing and JDBI for database operations, it enables developers to create complex processing pipelines through a simple, declarative language. The project includes a custom compiler that processes .lmd (Language Markup Definition) files, translating the DSL syntax into executable workflows. The framework's architecture ensures reliable execution of automated workflows with comprehensive audit tracking and configuration management capabilities.

### How Intics Uses Handyman
In Intics, Handyman serves as the core automation engine for file processing operations. It is used to define and execute file processing workflows through .lmd files, which specify the sequence of actions to be performed on incoming and outgoing files. The framework maintains detailed audit logs in database tables, tracking every action and its outcome. Configuration schemas are used to manage environment-specific settings and processing rules. This implementation allows Intics to maintain a complete audit trail of all file processing operations while providing a flexible and maintainable way to define complex file processing workflows.

## Project Structure
```
handyman/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── in/
│   │   │       └── handyman/
│   │   │           └── raven/
│   │   │               ├── actor/         # Actor system components
│   │   │               ├── core/          # Core framework components
│   │   │               ├── exception/     # Exception handling
│   │   │               ├── lambda/        # Lambda functions
│   │   │               ├── lib/           # Library components
│   │   │               └── util/          # Utility classes
│   │   ├── resources/  # Configuration and resource files
│   │   └── antlr/      # ANTLR grammar files for .lmd compilation
│   └── test/          # Test files
├── pom.xml            # Maven configuration
└── README.md         # Project documentation
```

## Table of Contents
1. [Core Components](#core-components)
2. [Action Classes](#action-classes)
3. [Grammar and Parser](#grammar-and-parser)
4. [Context Variables](#context-variables)
5. [API Integration](#api-integration)
6. [Security](#security)

## Core Components

### HandymanException
Located in `src/main/java/in/handyman/raven/exception/HandymanException.java`
Custom exception handling class for the Handyman project that provides robust error handling and logging capabilities. This class extends the standard Exception class to provide context-aware error messages and integrates with the audit system for tracking and monitoring. It supports detailed error reporting with stack traces and custom error codes.

**Key Features:**
- Custom exception handling
- Exception logging and tracking
- Audit system integration
- Context-aware error messages

**Usage:**
```java
throw new HandymanException("Error message", cause, action);
```

### ResourceAccess
Manages database and resource connections in the Handyman framework. This class provides a centralized way to handle database connections, connection pooling, and resource management. It supports both Azure and legacy database systems, with built-in connection pooling using HikariCP for optimal performance.

**Key Features:**
- Database connection pooling using HikariCP
- Azure and legacy database support
- JDBI connection management
- Resource configuration management

**Important Methods:**
- `rdbmsConn(String resourceName)`: Creates HikariDataSource connection
- `rdbmsJDBIConn(String resourceName)`: Creates JDBI connection

### SecurityEngine
Manages encryption and security operations in the Handyman framework. This class provides a unified interface for all encryption operations, supporting multiple encryption methods and algorithms. It integrates with external security services and provides a flexible architecture for adding new encryption methods.

**Key Features:**
- Multiple encryption methods:
  - AES-256 (default)
  - Protegrity API encryption
  - Protegrity direct encryption
- Configurable encryption policies
- Integration with InticsIntegrity for data protection

### ConfigEncryptionUtils
Handles configuration encryption/decryption in the Handyman framework. This utility class manages the encryption and decryption of configuration values, particularly sensitive information like passwords and API keys. It uses AES-256 encryption by default and supports encrypted value wrapping.

**Key Features:**
- AES-256 based configuration encryption
- ENC() wrapped encrypted values
- Secure password management
- Configuration property decryption

## Action Classes

### InboundBatchDataConsumer
Handles inbound data processing with batch processing capabilities. This class implements the consumer pattern for processing large volumes of incoming data efficiently. It includes built-in error handling, retry mechanisms, and batch size optimization.

**Features:**
- Batch data processing
- Consumer pattern implementation
- Error handling and logging
- Data validation

### AgenticPaperFilterAction
Filters and processes paper documents with advanced capabilities. This class provides intelligent document processing features including text extraction, content validation, and batch processing. It supports multiple document formats and includes OCR capabilities.

**Features:**
- Document filtering
- Text extraction
- Content validation
- Batch processing support

### AlchemyAuthTokenAction
Manages authentication tokens for Alchemy services. This class handles the generation, validation, and refresh of authentication tokens for Alchemy API services. It includes secure token storage and automatic token refresh mechanisms.

**Features:**
- Token generation
- Token validation
- Secure token storage
- Token refresh mechanism

### AlchemyInfoAction
Processes Alchemy service information. This class manages the retrieval and processing of information from Alchemy services. It includes data validation, error handling, and response processing capabilities.

**Features:**
- Service information retrieval
- Data validation
- Error handling
- Response processing

### AlchemyKvpResponseAction
Handles Key-Value Pair responses from Alchemy. This class processes and validates KVP responses from Alchemy services. It includes data transformation capabilities and comprehensive error handling.

**Features:**
- KVP processing
- Response validation
- Data transformation
- Error handling

### AssetInfoAction
Manages asset information and metadata. This class handles the processing and management of asset metadata, including file information extraction and validation. It supports multiple asset types and formats.

**Features:**
- Asset metadata processing
- File information extraction
- Asset validation
- Error handling

### AuthTokenAction
General authentication token management. This class provides a generic interface for managing authentication tokens across different services. It includes token generation, validation, and secure storage capabilities.

**Features:**
- Token generation
- Token validation
- Secure storage
- Token refresh

### AutoRotationAction
Handles document auto-rotation. This class provides intelligent document rotation capabilities based on content analysis. It includes orientation detection and batch processing support.

**Features:**
- Image rotation
- Orientation detection
- Batch processing
- Error handling

### ControlDataComparisonAction
Compares control data with validation. This class provides comprehensive data comparison capabilities with detailed difference detection and reporting. It supports multiple data formats and comparison methods.

**Features:**
- Data comparison
- Validation
- Difference detection
- Reporting

### ConvertExcelToDatabaseAction
Converts Excel data to database format. This class handles the conversion of Excel data to database format with support for multiple Excel versions and database types. It includes data validation and error handling.

**Features:**
- Excel parsing
- Data transformation
- Database insertion
- Error handling

### CreateDirectoryAction
Directory creation and management. This class provides secure directory creation and management capabilities. It includes permission management and path validation.

**Features:**
- Directory creation
- Path validation
- Permission management
- Error handling

### CreateZipAction
ZIP file creation and management. This class handles the creation and management of ZIP archives with support for compression and encryption. It includes progress tracking and error handling.

**Features:**
- File compression
- Archive management
- Error handling
- Progress tracking

### DbBackupEaseAction
Database backup management. This class provides comprehensive database backup capabilities with validation and error handling. It supports multiple database types and backup formats.

**Features:**
- Backup creation
- Backup validation
- Error handling
- Progress tracking

### DbDataDartAction
Database data dart operations. This class handles data extraction, transformation, and loading operations for databases. It includes comprehensive error handling and validation.

**Features:**
- Data extraction
- Transformation
- Loading
- Error handling

### DecryptInticsEncAction
Decryption operations. This class provides secure decryption capabilities for Intics encrypted data. It includes key management and security validation.

**Features:**
- Data decryption
- Key management
- Error handling
- Security validation

### DeleteFileDirectoryAction
File and directory deletion. This class provides secure file and directory deletion capabilities with permission checking and logging. It includes safety checks and error handling.

**Features:**
- Safe deletion
- Permission checking
- Error handling
- Logging

### EpisodeOfCoverageAction
Episode of coverage processing. This class handles the processing and validation of coverage episodes. It includes data validation and reporting capabilities.

**Features:**
- Coverage validation
- Data processing
- Error handling
- Reporting

### FolderDeleteByProcessAction
Process-based folder deletion. This class provides process-aware folder deletion capabilities with tracking and logging. It includes safety checks and error handling.

**Features:**
- Process tracking
- Safe deletion
- Error handling
- Logging

### ImportCsvToDBAction
CSV to database import. This class handles the import of CSV data to databases with validation and error handling. It supports multiple CSV formats and database types.

**Features:**
- CSV parsing
- Data validation
- Database insertion
- Error handling

### KafkaPublishAction
Kafka message publishing. This class provides Kafka message publishing capabilities with topic management and error handling. It includes message validation and logging.

**Features:**
- Message publishing
- Topic management
- Error handling
- Logging

### MultitudeAction
Multiple operation handling. This class provides capabilities for handling multiple operations in parallel with progress tracking. It includes comprehensive error handling and logging.

**Features:**
- Batch processing
- Parallel execution
- Error handling
- Progress tracking

### OutboundDeliveryNotifyAction
Outbound delivery notification. This class handles outbound delivery notifications with status tracking and error handling. It includes notification validation and logging.

**Features:**
- Notification sending
- Status tracking
- Error handling
- Logging

### OutboundKvpResponseAction
Outbound KVP response handling. This class processes outbound KVP responses with validation and error handling. It includes data transformation and logging capabilities.

**Features:**
- Response processing
- Data validation
- Error handling
- Logging

### PaperItemizerAction
Paper document itemization. This class provides document splitting and content extraction capabilities. It includes progress tracking and error handling.

**Features:**
- Document splitting
- Content extraction
- Error handling
- Progress tracking

### ProductResponseAction
Product response handling. This class processes product responses with validation and error handling. It includes data transformation and logging capabilities.

**Features:**
- Response processing
- Data validation
- Error handling
- Logging

### RadonKvpAction
Radon KVP processing. This class handles Radon KVP processing with validation and error handling. It includes data transformation and logging capabilities.

**Features:**
- KVP processing
- Data validation
- Error handling
- Logging

### ScalarAdapterAction
Scalar data adaptation. This class provides data transformation and type conversion capabilities. It includes validation and error handling.

**Features:**
- Data transformation
- Type conversion
- Error handling
- Validation

### TransformAction
Data transformation. This class provides general data transformation capabilities with format conversion and validation. It includes comprehensive error handling.

**Features:**
- Data conversion
- Format transformation
- Error handling
- Validation

## Grammar and Parser

### Raven.g4
Defines the grammar for the Raven language. This grammar file specifies the syntax and structure of the Raven language, including process definitions, action definitions, and expression handling. It provides a formal specification for parsing and interpreting Raven scripts.

**Components:**
1. Process Structure
2. Action Definitions
3. Expression Handling
4. File Operations
5. Data Processing

**Features:**
- Process definition
- Action definitions
- Expression handling
- Error handling
- File operations
- Data processing operations

## Context Variables

### Common Context Variables
- `read.batch.size`: Default batch size for reading operations
- `write.batch.size`: Default batch size for writing operations
- `okhttp.client.timeout`: HTTP client timeout
- `replicate.request.api.token`: API token for replicate requests
- `copro.request.agentic.paper.filter.extraction.handler.name`: Handler name for paper filter extraction
- `agentic.paper.filter.consumer.API.count`: Number of consumer APIs
- `triton.request.activator`: Triton request activation flag
- `preprocess.agentic.paper.filter.model.name`: Model name for paper filter preprocessing
- `pipeline.copro.api.process.file.format`: File format for processing
- `page.content.min.length.threshold`: Minimum content length threshold

### Encryption Context Variables
- `ENCRYPT_AGENTIC_FILTER_OUTPUT`: Flag for encrypting filter output
- `ENCRYPT_REQUEST_RESPONSE`: Flag for encrypting request/response
- `ENCRYPT_ITEM_WISE_ENCRYPTION`: Flag for item-wise encryption
- `DEFAULT_ENCRYPTION_ALGORITHM`: Default encryption algorithm
- `PROTEGRITY_ENCRYPT_URL`: Protegrity encryption URL
- `PROTEGRITY_DECRYPT_URL`: Protegrity decryption URL

## API Integration

### External API Integration
- Protegrity API for encryption/decryption
- Alchemy API for document processing
- Kafka for message publishing
- Database connections (JDBC)
- File system operations

### Internal API Integration
- Security Engine for encryption
- Resource Access for database operations
- Exception handling system
- Logging system
- Configuration management

## Security

### Encryption
- AES-256 encryption for sensitive data
- Protegrity API integration for enterprise encryption
- Secure key management
- Encrypted configuration handling

### Authentication
- Token-based authentication
- API key management
- Secure credential storage
- Access control

### Data Protection
- Secure data transmission
- Encrypted storage
- Access logging
- Audit trails
