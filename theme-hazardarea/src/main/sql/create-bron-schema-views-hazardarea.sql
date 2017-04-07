-- View: bron.vw_hazard_area

-- DROP VIEW bron.vw_hazard_area;

CREATE OR REPLACE VIEW bron.vw_hazard_area AS 
 SELECT spatial_datasetinfo.code AS ds_code, spatial_datasetinfo.namespace AS ds_namespace, hazard_area.id, hazard_area.job_id, hazard_area.gfid, hazard_area.inspire_id_dataset_code, hazard_area.inspire_id_local_id, hazard_area.hazard_area_id, hazard_area.determination_method, hazard_area.type_of_hazard_hazard_category, hazard_area.likelihood_of_occurrence_assement_method_name, hazard_area.likelihood_of_occurrence_assement_method_link, hazard_area.likelihood_of_occurrence_qualitative_likelihood, hazard_area.likelihood_of_occurrence_quantitative_likelihood_probability, hazard_area.likelihood_of_occurrence_quantitative_likelihood_return_period, hazard_area.geometry
   FROM bron.hazard_area, metadata.spatial_datasetinfo
   JOIN metadata.service ON spatial_datasetinfo.service_id = service.id
  WHERE spatial_datasetinfo.type::text = 'WFS'::text AND service.name = 'download_NZ'::text AND spatial_datasetinfo.name::text = 'app:HazardArea_NL'::text;

ALTER TABLE bron.vw_hazard_area
  OWNER TO inspire;
