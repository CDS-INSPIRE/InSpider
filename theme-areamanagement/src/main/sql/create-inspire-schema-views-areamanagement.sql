-- View: inspire.vw_area_management

-- DROP VIEW inspire.vw_area_management;

CREATE OR REPLACE VIEW inspire.vw_area_management AS 
 SELECT spatial_datasetinfo.code AS ds_code, spatial_datasetinfo.namespace AS ds_namespace, area_management.id, area_management.job_id, area_management.gfid, area_management.inspire_id_namespace, area_management.inspire_id_local_id, area_management.zonetype_code, area_management.environmental_domain_code, area_management.thematic_id_identifier, area_management.thematic_id_identifier_scheme, area_management.name_spelling, area_management.competent_authority_organisation_name, area_management.legislation_citation_gml_id, area_management.legal_basis_name, area_management.legal_basis_link, area_management.legal_basis_date, area_management.specialised_zone_type_code, area_management.designation_period_begin_designation, area_management.designation_period_end_designation, area_management.designation_period_end_indeterminate, area_management.vergunde_kuubs, area_management.vergunde_diepte, area_management.noise_low_value, area_management.noise_high_value, area_management.geometry, area_management.geom_simple, area_management.specialised_zone_type_codespace
   FROM inspire.area_management, metadata.spatial_datasetinfo
   JOIN metadata.service ON spatial_datasetinfo.service_id = service.id
  WHERE spatial_datasetinfo.type::text = 'WFS'::text AND service.name = 'download_AM'::text AND spatial_datasetinfo.name::text = 'am:ManagementRestrictionOrRegulationZone'::text;

ALTER TABLE inspire.vw_area_management
  OWNER TO inspire;
