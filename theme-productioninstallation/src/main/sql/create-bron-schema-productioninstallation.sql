
create table bron.production_installation (
	id serial,
 	job_id bigint,
	gfid text,

	inspire_id_dataset_code text not null,
	inspire_id_local_id text not null,
	production_facility_id text not null,
	production_installation_id text not null,
	thematic_identifier text,
	thematic_identifier_scheme text,
	name text,
	description text,
	status_type text,
	status_description text,		
	"type" text,
	type_codespace text,
	
	primary key (id),
 	constraint fk_job_id foreign key (job_id) references manager.job (id)
);
SELECT AddGeometryColumn ('bron','production_installation','point_geometry',28992,'GEOMETRY',2);
SELECT AddGeometryColumn ('bron','production_installation','surface_geometry',28992,'GEOMETRY',2);
SELECT AddGeometryColumn ('bron','production_installation','line_geometry',28992,'GEOMETRY',2);

CREATE INDEX idx_bron_production_installation_point_geometry ON bron.production_installation USING GIST (point_geometry);
CREATE INDEX idx_bron_production_installation_surface_geometry ON bron.production_installation USING GIST (surface_geometry);
CREATE INDEX idx_bron_production_installation_line_geometry ON bron.production_installation USING GIST (line_geometry);

GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON bron.production_installation TO inspire;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE bron.production_installation_id_seq TO inspire;
----------------------------------
