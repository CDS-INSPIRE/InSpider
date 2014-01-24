
create table bron.area_management (
	id serial,
 	job_id bigint,
	gfid text,

	inspire_id_dataset_code text not null,
	inspire_id_local_id text not null,
	zonetype_code text not null,
	environmental_domain_code text not null,
	thematic_id_identifier text,
	thematic_id_identifier_scheme text,
	name_spelling text,
	competent_authority_organisation_name text,
	legal_basis_name text,
	legal_basis_link text,
	legal_basis_date date,
	specialised_zone_type_code text,
	specialised_zone_type_codespace text,	
	designation_period_begin_designation timestamp,
	designation_period_end_designation timestamp,	
	vergunde_kuubs real,
	vergunde_diepte real,
	noise_low_value real,
	noise_high_value real,
	
	primary key (id),
 	constraint fk_job_id foreign key (job_id) references manager.job (id)
);
SELECT AddGeometryColumn ('bron','area_management','geometry',28992,'GEOMETRY',2);
ALTER TABLE bron.area_management ALTER COLUMN "geometry" SET NOT NULL;

CREATE INDEX idx_bron_area_management_geometry ON bron.area_management USING GIST (geometry);
CREATE INDEX idx_bron_area_management_zonetype_code ON bron.area_management (zonetype_code);

GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON bron.area_management TO inspire;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE bron.area_management_id_seq TO inspire;
