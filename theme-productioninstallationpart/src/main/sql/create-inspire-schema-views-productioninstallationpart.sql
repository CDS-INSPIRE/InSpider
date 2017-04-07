-- View: inspire.vw_production_installation_part

-- DROP VIEW inspire.vw_production_installation_part;

CREATE OR REPLACE VIEW inspire.vw_production_installation_part AS 
 SELECT spatial_datasetinfo.code AS ds_code, spatial_datasetinfo.namespace AS ds_namespace, production_installation_part.id, production_installation_part.job_id, production_installation_part.gfid, production_installation_part.inspire_id_namespace, production_installation_part.inspire_id_local_id, production_installation_part.production_installation_id, production_installation_part.name, production_installation_part.status_nil_reason, production_installation_part.status_xsi_nil, production_installation_part.status_type, production_installation_part.status_description, production_installation_part.type, production_installation_part.type_codespace, production_installation_part.technique, production_installation_part.point_geometry
   FROM inspire.production_installation_part, metadata.spatial_datasetinfo
   JOIN metadata.service ON spatial_datasetinfo.service_id = service.id
  WHERE spatial_datasetinfo.type::text = 'WFS'::text AND service.name = 'download_PF'::text AND spatial_datasetinfo.name::text = 'pf:ProductionInstallationPart'::text;

ALTER TABLE inspire.vw_production_installation_part
  OWNER TO inspire;
