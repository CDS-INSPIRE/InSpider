
create table bron.habitat (
	id serial,
 	job_id bigint,
	gfid text,

	inspire_id_dataset_code text not null,
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
SELECT AddGeometryColumn ('bron','habitat','geometry',28992,'GEOMETRY',2);
ALTER TABLE bron.habitat ALTER COLUMN "geometry" SET NOT NULL;

CREATE INDEX idx_bron_habitat_geometry ON bron.habitat USING GIST (geometry);

GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON bron.habitat TO inspire;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE bron.habitat_id_seq TO inspire;
----------------------------------
