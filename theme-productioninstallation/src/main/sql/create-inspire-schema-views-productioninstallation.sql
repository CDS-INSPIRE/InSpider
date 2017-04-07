-- View: inspire.vw_production_installation

-- DROP VIEW inspire.vw_production_installation;

CREATE OR REPLACE VIEW inspire.vw_production_installation AS 
 SELECT spatial_datasetinfo.code AS ds_code, spatial_datasetinfo.namespace AS ds_namespace, production_installation.id, production_installation.job_id, production_installation.gfid, production_installation.inspire_id_namespace, production_installation.inspire_id_local_id, production_installation.production_facility_id, production_installation.production_installation_id, production_installation.thematic_identifier, production_installation.thematic_identifier_scheme, production_installation.name, production_installation.description, production_installation.status_nil_reason, production_installation.status_xsi_nil, production_installation.status_type, production_installation.status_description, production_installation.type, production_installation.type_codespace, production_installation.point_geometry, production_installation.surface_geometry, production_installation.line_geometry
   FROM inspire.production_installation, metadata.spatial_datasetinfo
   JOIN metadata.service ON spatial_datasetinfo.service_id = service.id
  WHERE spatial_datasetinfo.type::text = 'WFS'::text AND service.name = 'download_PF'::text AND spatial_datasetinfo.name::text = 'pf:ProductionInstallation'::text;

ALTER TABLE inspire.vw_production_installation
  OWNER TO inspire;
