-- View: inspire.vw_habitat

-- DROP VIEW inspire.vw_habitat;

CREATE OR REPLACE VIEW inspire.vw_habitat AS 
 SELECT spatial_datasetinfo.code AS ds_code, spatial_datasetinfo.namespace AS ds_namespace, habitat.id, habitat.job_id, habitat.gfid, habitat.inspire_id_local_id, habitat.habitat_reference_habitat_type_id_code, habitat.habitat_reference_habitat_type_scheme_code, habitat.habitat_reference_habitat_type_name, habitat.local_habitat_name_local_scheme, habitat.local_habitat_name_local_name_code, habitat.local_habitat_name_local_name, habitat.local_habitat_name_qualifier_local_name, habitat.habitat_area_covered, habitat.geometry, habitat.geom_simple, habitat.inspire_id_namespace, habitat.habitat_reference_habitat_type_id_codespace, habitat.local_habitat_name_local_name_codespace
   FROM inspire.habitat, metadata.spatial_datasetinfo
   JOIN metadata.service ON spatial_datasetinfo.service_id = service.id
  WHERE spatial_datasetinfo.type::text = 'WFS'::text AND service.name = 'download_HB'::text AND spatial_datasetinfo.name::text = 'hb:Habitat'::text;

ALTER TABLE inspire.vw_habitat
  OWNER TO inspire;
