-- audit.action_execution_audit definition

-- Drop table

-- DROP TABLE audit.action_execution_audit;
create schema if not exists audit;
CREATE TABLE IF NOT EXISTS audit.action_execution_audit (
	action_id bigserial NOT NULL,
	created_by int8 NULL,
	created_date timestamp NULL,
	last_modified_by int8 NULL,
	last_modified_date timestamp NULL,
	context_node text NULL,
	execution_status_id int4 NULL,
	lambda_name varchar NULL,
	parent_action_id int8 NULL,
	parent_action_name varchar NULL,
	parent_pipeline_id int8 NULL,
	parent_pipeline_name varchar NULL,
	pipeline_name varchar NULL,
	action_name varchar NULL,
	execution_group_id int4 NULL,
	input_node text NULL,
	log text NULL,
	pipeline_id int8 NULL,
	thread_name varchar NULL,
	root_pipeline_id int8 NULL,
	CONSTRAINT action_execution_audit_pkey PRIMARY KEY (action_id)
);


-- audit.action_execution_status_audit definition

-- Drop table

-- DROP TABLE audit.action_execution_status_audit;

CREATE TABLE IF NOT EXISTS audit.action_execution_status_audit (
	id bigserial NOT NULL,
	created_by int8 NOT NULL,
	created_date timestamp NOT NULL,
	last_modified_by int8 NOT NULL,
	last_modified_date timestamp NOT NULL,
	action_id int8 NOT NULL,
	execution_status_id int4 NOT NULL,
	pipeline_id int8 NOT NULL,
	root_pipeline_id int8 NOT NULL,
	CONSTRAINT action_execution_status_audit_pkey PRIMARY KEY (id)
);


-- audit.copro_audit definition

-- Drop table

-- DROP TABLE audit.copro_audit;

CREATE TABLE IF NOT EXISTS audit.copro_audit (
	id serial4 NOT NULL,
	root_pipeline_id int4 NOT NULL,
	action_id int4 NOT NULL,
	process text NOT NULL,
	"level" text NOT NULL,
	message text NOT NULL,
	created_at timestamp NOT NULL,
	CONSTRAINT copro_audit_pkey PRIMARY KEY (id)
);


-- audit.execution_group definition

-- Drop table

-- DROP TABLE audit.execution_group;

CREATE TABLE IF NOT EXISTS audit.execution_group (
	id int4 NOT NULL,
	value varchar NOT NULL
);


-- audit.execution_status definition

-- Drop table

-- DROP TABLE audit.execution_status;

CREATE TABLE IF NOT EXISTS audit.execution_status (
	id int4 NOT NULL,
	value varchar NOT NULL,
	CONSTRAINT execution_status_pkey PRIMARY KEY (id)
);


-- audit.handyman_exception_audit definition

-- Drop table

-- DROP TABLE audit.handyman_exception_audit;

CREATE TABLE IF NOT EXISTS audit.handyman_exception_audit (
	group_id int4 NULL,
	root_pipeline_id int8 NULL,
	root_pipeline_name varchar NULL,
	pipeline_name varchar NULL,
	action_id int8 NULL,
	action_name varchar NULL,
	exception_info varchar NULL,
	message text NULL,
	process_id int8 NULL,
	created_by int8 NULL,
	created_date timestamp NULL,
	last_modified_by int8 NULL,
	last_modified_date timestamp NULL
);


-- audit.pipeline_execution_audit definition

-- Drop table

-- DROP TABLE audit.pipeline_execution_audit;

CREATE TABLE IF NOT EXISTS audit.pipeline_execution_audit (
	pipeline_id bigserial NOT NULL,
	created_by int8 NULL,
	created_date timestamp NULL,
	last_modified_by int8 NULL,
	last_modified_date timestamp NULL,
	context_node text NULL,
	execution_status_id int4 NULL,
	lambda_name varchar NULL,
	parent_action_id int8 NULL,
	parent_action_name varchar NULL,
	parent_pipeline_id int8 NULL,
	parent_pipeline_name varchar NULL,
	pipeline_name varchar NULL,
	file_content text NULL,
	host_name varchar NULL,
	mode_of_execution varchar NULL,
	pipeline_load_type varchar NULL,
	relative_path varchar NULL,
	request_body text NULL,
	thread_name varchar NULL,
	process_name varchar NULL,
	root_pipeline_id int8 NULL,
	process_id int8 NULL,
	CONSTRAINT pipeline_execution_audit_pkey PRIMARY KEY (pipeline_id)
);


-- audit.pipeline_execution_status_audit definition

-- Drop table

-- DROP TABLE audit.pipeline_execution_status_audit;

CREATE TABLE IF NOT EXISTS audit.pipeline_execution_status_audit (
	id bigserial NOT NULL,
	created_by int8 NULL,
	created_date timestamp NULL,
	last_modified_by int8 NULL,
	last_modified_date timestamp NULL,
	execution_status_id int4 NULL,
	pipeline_id int8 NULL,
	root_pipeline_id int8 NULL,
	CONSTRAINT pipeline_execution_status_audit_pkey PRIMARY KEY (id)
);


-- audit.pipeline_summary_audit definition

-- Drop table

-- DROP TABLE audit.pipeline_summary_audit;

CREATE TABLE IF NOT EXISTS audit.pipeline_summary_audit (
	summary_id bigserial NOT NULL,
	process_id int8 NOT NULL,
	created_on timestamp NULL,
	module_name varchar NULL,
	macro_process varchar NULL,
	files_processed_count int8 NULL DEFAULT 0,
	papers_processed_count int8 NULL DEFAULT 0,
	files_failed_count int8 NULL DEFAULT 0,
	papers_failed_count int8 NULL DEFAULT 0,
	start_time timestamp NULL,
	end_time timestamp NULL,
	time_taken int8 NULL,
	average_time int8 NULL,
	group_id int8 NOT NULL,
	root_pipeline_id int8 NOT NULL,
	created_user_id varchar NOT NULL,
	template_id varchar NULL,
	tenant_id varchar NULL,
	CONSTRAINT pipeline_summary_audit_pk PRIMARY KEY (summary_id)
);


-- audit.statement_execution_audit definition

-- Drop table

-- DROP TABLE audit.statement_execution_audit;

CREATE TABLE IF NOT EXISTS audit.statement_execution_audit (
	statement_id bigserial NOT NULL,
	created_by int8 NULL,
	created_date timestamp NULL,
	last_modified_by int8 NULL,
	last_modified_date timestamp NULL,
	action_id int8 NULL,
	rows_processed int4 NULL,
	rows_read int4 NULL,
	rows_written int4 NULL,
	statement_content text NULL,
	time_taken float8 NULL,
	root_pipeline_id int8 NULL,
	CONSTRAINT statement_execution_audit_pkey PRIMARY KEY (statement_id)
);






create schema if not exists audit;


-- config.spw_common_config definition

-- Drop table

-- DROP TABLE config.spw_common_config;

CREATE TABLE IF NOT EXISTS config.spw_common_config (
	id int8 NOT NULL,
	variable varchar(150) NOT NULL,
	value text NOT NULL,
	active bool NOT NULL,
	created_by int8 NOT NULL,
	created_date timestamp NOT NULL,
	last_modified_by int8 NOT NULL,
	last_modified_date timestamp NOT NULL,
	"version" int4 NOT NULL,
	CONSTRAINT spw_common_config_pkey PRIMARY KEY (id)
);


-- config.spw_instance_config definition

-- Drop table

-- DROP TABLE config.spw_instance_config;

CREATE TABLE IF NOT EXISTS config.spw_instance_config (
	id int8 NOT NULL DEFAULT nextval('config.instance_id'::regclass),
	"instance" varchar(150) NOT NULL,
	process varchar(150) NOT NULL,
	variable varchar(150) NOT NULL,
	value text NOT NULL,
	active bool NOT NULL,
	created_by int8 NOT NULL,
	created_date timestamp NOT NULL,
	last_modified_by int8 NOT NULL,
	last_modified_date timestamp NOT NULL,
	"version" int4 NOT NULL,
	CONSTRAINT spw_instance_config_pkey PRIMARY KEY (id)
);


-- config.spw_process_config definition

-- Drop table

-- DROP TABLE config.spw_process_config;

CREATE TABLE IF NOT EXISTS config.spw_process_config (
	id int8 NOT NULL,
	process varchar(150) NOT NULL,
	variable varchar(150) NOT NULL,
	value varchar(500) NOT NULL,
	active bool NOT NULL,
	created_by int8 NOT NULL,
	created_date timestamp NOT NULL,
	last_modified_by int8 NOT NULL,
	last_modified_date timestamp NOT NULL,
	"version" int4 NOT NULL,
	CONSTRAINT spw_process_config_pkey PRIMARY KEY (id)
);


-- config.spw_resource_config definition

-- Drop table

-- DROP TABLE config.spw_resource_config;

CREATE TABLE IF NOT EXISTS config.spw_resource_config (
	id int8 NOT NULL,
	config_name varchar NOT NULL,
	resource_url text NOT NULL,
	active bool NOT NULL,
	user_name varchar NOT NULL,
	"password" varchar NOT NULL,
	driver_class varchar NOT NULL,
	host varchar NOT NULL,
	port varchar NOT NULL,
	database_name varchar NOT NULL,
	created_by int8 NOT NULL,
	created_date timestamp NOT NULL,
	last_modified_by int8 NOT NULL,
	last_modified_date timestamp NOT NULL,
	"version" int4 NOT NULL,
	CONSTRAINT spw_resource_config_pkey PRIMARY KEY (id)
);


INSERT INTO config.spw_process_config
(id, process, variable, value, active, created_by, created_date, last_modified_by, last_modified_date, "version")
VALUES(1, 'SYS_PACKAGE', 'raven.java.lib', 'in.handyman.raven.lib', true, 1, now(), 1,now(), 1);
