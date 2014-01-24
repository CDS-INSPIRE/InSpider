
create table inspire.habitat (
	id serial,
 	job_id bigint,
	gfid text,

	inspire_id_namespace text not null,
	inspire_id_local_id text not null,
	habitat_reference_habitat_type_id_code text not null,
	habitat_reference_habitat_type_id_codespace text,	
	habitat_reference_habitat_type_scheme_code text not null,
	habitat_reference_habitat_type_name text,
	local_habitat_name_local_scheme text,
	local_habitat_name_local_name_code text,
	local_habitat_name_local_name_codespace text,
	local_habitat_name_local_name text,
	local_habitat_name_qualifier_local_name text not null,
	habitat_area_covered text not null,	
	
	primary key (id),
 	constraint fk_job_id foreign key (job_id) references manager.job (id)
);
SELECT AddGeometryColumn ('inspire','habitat','geometry',28992,'GEOMETRY',2);
SELECT AddGeometryColumn ('inspire','habitat','geom_simple',28992,'GEOMETRY',2);

CREATE INDEX habitat_geom_simple_idx ON inspire.habitat USING gist(geom_simple);
CREATE INDEX habitat_geometry_idx ON inspire.habitat USING gist(geometry);

ALTER TABLE inspire.habitat ALTER COLUMN "geometry" SET NOT NULL;
ALTER TABLE inspire.habitat ALTER COLUMN "geom_simple" SET NOT NULL;

GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON inspire.habitat TO inspire;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE inspire.habitat_id_seq TO inspire;
----------------------------------
