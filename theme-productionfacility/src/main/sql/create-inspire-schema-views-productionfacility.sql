-- View: inspire.vw_production_facility

-- DROP VIEW inspire.vw_production_facility;

CREATE OR REPLACE VIEW inspire.vw_production_facility AS 
 SELECT spatial_datasetinfo.code AS ds_code, spatial_datasetinfo.namespace AS ds_namespace, production_facility.id, production_facility.job_id, production_facility.gfid, production_facility.inspire_id_namespace, production_facility.inspire_id_local_id, production_facility.production_facility_id, production_facility.thematic_identifier, production_facility.thematic_identifier_scheme, production_facility.function_activity, production_facility.function_input, production_facility.function_output, production_facility.function_description, production_facility.name, production_facility.status_nil_reason, production_facility.status_xsi_nil, production_facility.status_type, production_facility.status_description, production_facility.geometry, production_facility.surface_geometry
   FROM inspire.production_facility, metadata.spatial_datasetinfo
   JOIN metadata.service ON spatial_datasetinfo.service_id = service.id
  WHERE spatial_datasetinfo.type::text = 'WFS'::text AND service.name = 'download_PF'::text AND spatial_datasetinfo.name::text = 'pf:ProductionFacility'::text;

ALTER TABLE inspire.vw_production_facility
  OWNER TO inspire;
