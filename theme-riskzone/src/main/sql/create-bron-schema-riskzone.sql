
create table bron.risk_zone (
	id serial,
 	job_id bigint,
	gfid text,

	inspire_id_dataset_code text not null,
	inspire_id_local_id text not null,
	hazard_area_id text not null,
	determinationMethod text not null,
	type_of_hazard_hazard_category text not null,
	likelihood_of_occurrence_assement_method_name text,
	likelihood_of_occurrence_assement_method_link text,	
	likelihood_of_occurrence_qualitative_likelihood text,
	likelihood_of_occurrence_quantitative_likelihood_probability decimal,
	likelihood_of_occurrence_quantitative_likelihood_return_period decimal,
	
	primary key (id),
 	constraint fk_job_id foreign key (job_id) references manager.job (id)
);
SELECT AddGeometryColumn ('bron','risk_zone','geometry',28992,'GEOMETRY',2);
ALTER TABLE bron.risk_zone ALTER COLUMN "geometry" SET NOT NULL;

CREATE INDEX idx_bron_risk_zone_geometry ON bron.risk_zone USING GIST (geometry);

GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON bron.risk_zone TO inspire;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE bron.risk_zone_id_seq TO inspire;
----------------------------------
