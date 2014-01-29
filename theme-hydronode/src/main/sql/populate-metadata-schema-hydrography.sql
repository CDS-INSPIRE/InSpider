
--
-- metadata Hydrography
-- 

-- One serviceprovider for all services of this INSPIRE theme
DELETE FROM metadata.si_keyword where serviceidentification_id IN (select serviceidentification_id from metadata.service where name='view_HY' OR name='download_HY');
DELETE FROM metadata.si_accessconstraint where serviceidentification_id IN (select serviceidentification_id from metadata.service where name='view_HY' OR name='download_HY');
DELETE FROM metadata.service_datasetmetadata where service_id IN (select id from metadata.service where name='view_HY' OR name='download_HY');
DELETE FROM metadata.service where name='view_HY' OR name='download_HY';
DELETE FROM metadata.serviceidentification where servicepath like '%view_HY%' OR servicepath like '%download_HY%';
DELETE FROM metadata.extendedcapabilities where metadataurl like '%view_HY%' OR metadataurl like '%download_HY%';

DELETE FROM metadata.si_keyword where serviceidentification_id IN (select serviceidentification_id from metadata.service where name='wms_HY_NL' OR name='wfs_HY_NL');
DELETE FROM metadata.si_accessconstraint where serviceidentification_id IN (select serviceidentification_id from metadata.service where name='wms_HY_NL' OR name='wfs_HY_NL');
DELETE FROM metadata.service_datasetmetadata where service_id IN (select id from metadata.service where name='wms_HY_NL' OR name='wfs_HY_NL');
DELETE FROM metadata.service where name='wms_HY_NL' OR name='wfs_HY_NL';
DELETE FROM metadata.serviceidentification where servicepath like '%wms_HY_NL%' OR servicepath like '%wfs_HY_NL%';
DELETE FROM metadata.extendedcapabilities where metadataurl like '%wms_HY_NL%' OR metadataurl like '%wfs_HY_NL%';

DELETE FROM metadata.sp_deliverypoint where serviceprovider_id IN (select id from metadata.serviceprovider where individualname like '%Hydrography%');
DELETE FROM metadata.sp_emailaddress where serviceprovider_id IN (select id from metadata.serviceprovider where individualname like '%Hydrography%');
DELETE FROM metadata.sp_faxnumber where serviceprovider_id IN (select id from metadata.serviceprovider where individualname like '%Hydrography%');
DELETE FROM metadata.sp_phonenumber where serviceprovider_id IN (select id from metadata.serviceprovider where individualname like '%Hydrography%');
DELETE FROM metadata.serviceprovider where individualname like '%Hydrography%';

INSERT INTO metadata.serviceprovider (id,administrativearea,city,contactinstructions,country,hoursofservice,individualname,onlineresource,organizationname,positionname,postalcode,providername,providersite,role)
	VALUES ((select nextval('metadata.hibernate_sequence')), '', '', NULL, '', NULL, 'Functioneel beheerder, Hydrography', NULL, 'ELF Project', 'pointOfContact', '', NULL, NULL, NULL);

-- INSPIRE View and Download service

INSERT INTO metadata.extendedcapabilities (id,metadataurl) VALUES ((select nextval('metadata.hibernate_sequence')), 'view_HY');
INSERT INTO metadata.extendedcapabilities (id,metadataurl) VALUES ((select nextval('metadata.hibernate_sequence')), 'download_HY');

INSERT INTO metadata.serviceidentification (id,abstract,fees,servicetype,servicepath,title) 
	VALUES ((select nextval('metadata.hibernate_sequence')), 'Deze View service heeft betrekking op Hydrography.', 'no conditions apply', 'WMS', 'Hydrography/services/view_HY', 'INSPIRE View service voor Hydrography');
INSERT INTO metadata.serviceidentification  (id,abstract,fees,servicetype,servicepath,title) 
	VALUES ((select nextval('metadata.hibernate_sequence')), 'Deze Download service heeft betrekking op Hydrography.', 'none', 'WFS', 'Hydrography/services/download_HY', 'INSPIRE Download service voor Hydrography');

INSERT INTO metadata.service (id,description,name,serviceidentification_id,serviceprovider_id, extendedcapabilities_id) VALUES ((select nextval('metadata.hibernate_sequence')), 'omschrijving view_HY', 'view_HY', (select id from metadata.serviceidentification where servicepath='Hydrography/services/view_HY'), (select id from metadata.serviceprovider where individualname like '%Hydrography%'), (select id from metadata.extendedcapabilities where metadataurl='view_HY')); 
INSERT INTO metadata.service (id,description,name,serviceidentification_id,serviceprovider_id, extendedcapabilities_id) VALUES ((select nextval('metadata.hibernate_sequence')), 'omschrijving download_HY', 'download_HY', (select id from metadata.serviceidentification where servicepath='Hydrography/services/download_HY'), (select id from metadata.serviceprovider where individualname like '%Hydrography%'), (select id from metadata.extendedcapabilities where metadataurl='download_HY')); 

INSERT INTO metadata.service_datasetmetadata (service_id, name, namespace, url,"index") VALUES ((select id from metadata.service where name='view_HY'), 'HY.HydroNode', '', 'X', 0);
INSERT INTO metadata.service_datasetmetadata (service_id, name, namespace, url,"index") VALUES ((select id from metadata.service where name='view_HY'), 'HY.WaterCourseLink', '', 'X', 1);

INSERT INTO metadata.si_accessconstraint (serviceidentification_id,accessconstraint,"index") VALUES ((select serviceidentification_id from metadata.service where name='view_HY'), 'anders', 0);

INSERT INTO metadata.si_keyword (serviceidentification_id,codespace,"value","index" ) VALUES ((select serviceidentification_id from metadata.service where name='view_HY'), 'ISO', 'infoMapAccessService', 0);
INSERT INTO metadata.si_keyword (serviceidentification_id,codespace,"value","index" ) VALUES ((select serviceidentification_id from metadata.service where name='view_HY'), 'GEMET - INSPIRE themes, version 1.0', 'Hydrography', 1);
INSERT INTO metadata.si_keyword (serviceidentification_id,codespace,"value","index" ) VALUES ((select serviceidentification_id from metadata.service where name='download_HY'), 'ISO', 'infoFeatureAccessService', 0);
INSERT INTO metadata.si_keyword (serviceidentification_id,codespace,"value","index" ) VALUES ((select serviceidentification_id from metadata.service where name='download_HY'), 'GEMET - INSPIRE themes, version 1.0', 'Hydrography', 1);

-- si-version is not filled

INSERT INTO metadata.sp_deliverypoint (serviceprovider_id,deliverypoint,"index") VALUES ((select serviceprovider_id from metadata.service where name='view_HY'), '', 0);
INSERT INTO metadata.sp_emailaddress (serviceprovider_id, emailaddress,"index") VALUES ((select serviceprovider_id from metadata.service where name='view_HY'), 'inspire@idgis.nl', 0);
INSERT INTO metadata.sp_faxnumber (serviceprovider_id ,faxnumber,"index") VALUES ((select serviceprovider_id from metadata.service where name='view_HY'), '', 0);
INSERT INTO metadata.sp_phonenumber (serviceprovider_id,phonenumber,"index") VALUES ((select serviceprovider_id from metadata.service where name='view_HY'), '', 0);
--------------------------------

-- WMS & WFS with NL / flat structure


INSERT INTO metadata.extendedcapabilities (id,metadataurl) VALUES ((select nextval('metadata.hibernate_sequence')), 'wms_HY_NL');
INSERT INTO metadata.extendedcapabilities (id,metadataurl) VALUES ((select nextval('metadata.hibernate_sequence')), 'wfs_HY_NL');

INSERT INTO metadata.serviceidentification (id,abstract,fees,servicetype,servicepath,title) 
	VALUES ((select nextval('metadata.hibernate_sequence')), 'Deze WMS service heeft betrekking op Hydrography.', 'no conditions apply', 'WMS', 'Hydrography/services/wms_HY_NL', 'WMS service voor Hydrography');
INSERT INTO metadata.serviceidentification  (id,abstract,fees,servicetype,servicepath,title) 
	VALUES ((select nextval('metadata.hibernate_sequence')), 'Deze WFS service heeft betrekking op Hydrography.', 'none', 'WFS', 'Hydrography/services/wfs_HY_NL', 'WFS service voor Hydrography');

INSERT INTO metadata.service (id,description,name,serviceidentification_id,serviceprovider_id, extendedcapabilities_id) VALUES ((select nextval('metadata.hibernate_sequence')), 'omschrijving wms_HY_NL', 'wms_HY_NL', (select id from metadata.serviceidentification where servicepath='Hydrography/services/wms_HY_NL'), (select id from metadata.serviceprovider where individualname like '%Hydrography%'), (select id from metadata.extendedcapabilities where metadataurl='wms_HY_NL')); 
INSERT INTO metadata.service (id,description,name,serviceidentification_id,serviceprovider_id, extendedcapabilities_id) VALUES ((select nextval('metadata.hibernate_sequence')), 'omschrijving wfs_HY_NL', 'wfs_HY_NL', (select id from metadata.serviceidentification where servicepath='Hydrography/services/wfs_HY_NL'), (select id from metadata.serviceprovider where individualname like '%Hydrography%'), (select id from metadata.extendedcapabilities where metadataurl='wfs_HY_NL')); 

INSERT INTO metadata.service_datasetmetadata (service_id, name, namespace, url,"index") VALUES ((select id from metadata.service where name='wms_HY_NL'), 'HY.HydroNode_NL', '', 'X', 0);
INSERT INTO metadata.service_datasetmetadata (service_id, name, namespace, url,"index") VALUES ((select id from metadata.service where name='wms_HY_NL'), 'HY.WaterCourseLink_NL', '', 'X', 1);

INSERT INTO metadata.si_accessconstraint (serviceidentification_id,accessconstraint,"index") VALUES ((select serviceidentification_id from metadata.service where name='wms_HY_NL'), 'anders', 0);

INSERT INTO metadata.si_keyword (serviceidentification_id,codespace,"value","index" ) VALUES ((select serviceidentification_id from metadata.service where name='wms_HY_NL'), 'ISO', 'infoMapAccessService', 0);
INSERT INTO metadata.si_keyword (serviceidentification_id,codespace,"value","index" ) VALUES ((select serviceidentification_id from metadata.service where name='wms_HY_NL'), 'GEMET - INSPIRE themes, version 1.0', 'Hydrography', 1);
INSERT INTO metadata.si_keyword (serviceidentification_id,codespace,"value","index" ) VALUES ((select serviceidentification_id from metadata.service where name='wfs_HY_NL'), 'ISO', 'infoFeatureAccessService', 0);
INSERT INTO metadata.si_keyword (serviceidentification_id,codespace,"value","index" ) VALUES ((select serviceidentification_id from metadata.service where name='wfs_HY_NL'), 'GEMET - INSPIRE themes, version 1.0', 'Hydrography', 1);
