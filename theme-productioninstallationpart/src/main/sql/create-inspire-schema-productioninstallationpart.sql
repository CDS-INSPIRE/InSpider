
create table inspire.production_installation_part (
	id serial,
 	job_id bigint,
	gfid text,

	inspire_id_namespace text not null,	
	inspire_id_local_id text not null,
	production_installation_id text not null,
	name text,
	status_nil_reason text,
	status_xsi_nil text,
	status_type text,
	status_description text,
	"type" text,
	type_codespace text,
	technique text,
	
	primary key (id),
 	constraint fk_job_id foreign key (job_id) references manager.job (id)
);
SELECT AddGeometryColumn ('inspire','production_installation_part','point_geometry',28992,'GEOMETRY',2);

CREATE INDEX idx_inspire_production_installation_part_point_geometry ON inspire.production_installation_part USING GIST (point_geometry);

GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON inspire.production_installation_part TO inspire;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE inspire.production_installation_part_id_seq TO inspire;

----------------------------------
