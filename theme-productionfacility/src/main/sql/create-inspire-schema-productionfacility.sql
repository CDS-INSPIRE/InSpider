
create table inspire.production_facility (
	id serial,
 	job_id bigint,
	gfid text,

	inspire_id_namespace text not null,	
	inspire_id_local_id text not null,
	production_facility_id text not null,
	thematic_identifier text,
	thematic_identifier_scheme text,
	function_activity text,
	function_input text,
	function_output text,
	function_description text,
	name text,
	status_nil_reason text,
	status_xsi_nil text,
	status_type text,
	status_description text,
	
	primary key (id),
 	constraint fk_job_id foreign key (job_id) references manager.job (id)
);
SELECT AddGeometryColumn ('inspire','production_facility','geometry',28992,'GEOMETRY',2);
ALTER TABLE inspire.production_facility ALTER COLUMN "geometry" SET NOT NULL;
SELECT AddGeometryColumn ('inspire','production_facility','surface_geometry',28992,'GEOMETRY',2);

CREATE INDEX idx_inspire_production_facility_geometry ON inspire.production_facility USING GIST (geometry);
CREATE INDEX idx_inspire_production_facility_surface_geometry ON inspire.production_facility USING GIST (surface_geometry);

GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON inspire.production_facility TO inspire;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE inspire.production_facility_id_seq TO inspire;
----------------------------------
