
create table inspire.area_management (
	id serial,
 	job_id bigint,
	gfid text,

	inspire_id_namespace text not null,
	inspire_id_local_id text not null,
	zonetype_code text not null,
	environmental_domain_code text not null,
	thematic_id_identifier text,
	thematic_id_identifier_scheme text,
	name_spelling text,
	competent_authority_organisation_name text,
	legislation_citation_gml_id text,
	legal_basis_name text,
	legal_basis_link text,	
	legal_basis_date date,
	specialised_zone_type_code text,
	specialised_zone_type_codespace text,	
	designation_period_begin_designation timestamp,
	designation_period_end_designation timestamp,
	designation_period_end_indeterminate text,	
	vergunde_kuubs real,
	vergunde_diepte real,
	noise_low_value real,
	noise_high_value real,
	
	primary key (id),
 	constraint fk_job_id foreign key (job_id) references manager.job (id)
);

SELECT AddGeometryColumn ('inspire','area_management','geometry',28992,'GEOMETRY',2);
SELECT AddGeometryColumn ('inspire','area_management','geom_simple',28992,'GEOMETRY',2);

CREATE INDEX area_management_geom_simple_idx ON inspire.area_management USING gist(geom_simple);
CREATE INDEX area_management_geometry_idx ON inspire.area_management USING gist(geometry);

ALTER TABLE inspire.area_management ALTER COLUMN "geometry" SET NOT NULL;
ALTER TABLE inspire.area_management ALTER COLUMN "geom_simple" SET NOT NULL;

GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON inspire.area_management TO inspire;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE inspire.area_management_id_seq TO inspire;