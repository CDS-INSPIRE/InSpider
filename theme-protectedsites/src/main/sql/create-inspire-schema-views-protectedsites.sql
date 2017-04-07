-- View: inspire.vw_named_place

-- DROP VIEW inspire.vw_named_place;

CREATE OR REPLACE VIEW inspire.vw_named_place AS 
 SELECT spatial_datasetinfo.code AS ds_code, spatial_datasetinfo.namespace AS ds_namespace, named_place.id, named_place.geometry, named_place.inspire_id_local_id, named_place.inspire_id_namespace
   FROM inspire.named_place, metadata.spatial_datasetinfo
   JOIN metadata.service ON spatial_datasetinfo.service_id = service.id
  WHERE spatial_datasetinfo.type::text = 'WFS'::text AND service.name = 'download_PS'::text AND spatial_datasetinfo.name::text = 'gn:NamedPlace'::text;

ALTER TABLE inspire.vw_named_place
  OWNER TO inspire;


-- View: inspire.vw_protected_site

-- DROP VIEW inspire.vw_protected_site;

CREATE OR REPLACE VIEW inspire.vw_protected_site AS 
 SELECT spatial_datasetinfo.code AS ds_code, spatial_datasetinfo.namespace AS ds_namespace, protected_site.id, protected_site.geometry, protected_site.legal_foundation_date, protected_site.legal_foundation_document, protected_site.inspire_id_local_id, protected_site.inspire_id_namespace
   FROM inspire.protected_site, metadata.spatial_datasetinfo
   JOIN metadata.service ON spatial_datasetinfo.service_id = service.id
  WHERE spatial_datasetinfo.type::text = 'WFS'::text AND service.name = 'download_PS'::text AND spatial_datasetinfo.name::text = 'ps:ProtectedSite'::text;

ALTER TABLE inspire.vw_protected_site
  OWNER TO inspire;
