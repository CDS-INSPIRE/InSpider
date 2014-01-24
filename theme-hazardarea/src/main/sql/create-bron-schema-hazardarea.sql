
create table bron.hazard_area (
	id serial,
 	job_id bigint,
	gfid text,

	inspire_id_dataset_code text not null,
	inspire_id_local_id text not null,
	hazard_area_id text not null,
	determination_method text not null,
	type_of_hazard_hazard_category text not null,
	likelihood_of_occurrence_assement_method_name text,
	likelihood_of_occurrence_assement_method_link text,	
	likelihood_of_occurrence_qualitative_likelihood text,
	likelihood_of_occurrence_quantitative_likelihood_probability decimal,
	likelihood_of_occurrence_quantitative_likelihood_return_period decimal,
	
	primary key (id),
 	constraint fk_job_id foreign key (job_id) references manager.job (id)
);
SELECT AddGeometryColumn ('bron','hazard_area','geometry',28992,'GEOMETRY',2);
ALTER TABLE bron.hazard_area ALTER COLUMN "geometry" SET NOT NULL;

CREATE INDEX idx_bron_hazard_area_geometry ON bron.hazard_area USING GIST (geometry);

GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON bron.hazard_area TO inspire;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE bron.hazard_area_id_seq TO inspire;
----------------------------------
