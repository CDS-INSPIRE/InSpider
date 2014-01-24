
--
-- metadata ProductionAndIndustrialFacilities
-- 

-- One serviceprovider for all services of this INSPIRE theme
DELETE FROM metadata.si_keyword where serviceidentification_id IN (select serviceidentification_id from metadata.service where name='view_PF' OR name='download_PF');
DELETE FROM metadata.si_accessconstraint where serviceidentification_id IN (select serviceidentification_id from metadata.service where name='view_PF' OR name='download_PF');
DELETE FROM metadata.service_datasetmetadata where service_id IN (select id from metadata.service where name='view_PF' OR name='download_PF');
DELETE FROM metadata.service where name='view_PF' OR name='download_PF';
DELETE FROM metadata.serviceidentification where servicepath like '%view_PF%' OR servicepath like '%download_PF%';
DELETE FROM metadata.extendedcapabilities where metadataurl like '%view_PF%' OR metadataurl like '%download_PF%';

DELETE FROM metadata.si_keyword where serviceidentification_id IN (select serviceidentification_id from metadata.service where name='wms_PF_NL' OR name='wfs_PF_NL');
DELETE FROM metadata.si_accessconstraint where serviceidentification_id IN (select serviceidentification_id from metadata.service where name='wms_PF_NL' OR name='wfs_PF_NL');
DELETE FROM metadata.service_datasetmetadata where service_id IN (select id from metadata.service where name='wms_PF_NL' OR name='wfs_PF_NL');
DELETE FROM metadata.service where name='wms_PF_NL' OR name='wfs_PF_NL';
DELETE FROM metadata.serviceidentification where servicepath like '%wms_PF_NL%' OR servicepath like '%wfs_PF_NL%';
DELETE FROM metadata.extendedcapabilities where metadataurl like '%wms_PF_NL%' OR metadataurl like '%wfs_PF_NL%';

DELETE FROM metadata.sp_deliverypoint where serviceprovider_id IN (select id from metadata.serviceprovider where individualname like '%ProductionAndIndustrialFacilities%');
DELETE FROM metadata.sp_emailaddress where serviceprovider_id IN (select id from metadata.serviceprovider where individualname like '%ProductionAndIndustrialFacilities%');
DELETE FROM metadata.sp_faxnumber where serviceprovider_id IN (select id from metadata.serviceprovider where individualname like '%ProductionAndIndustrialFacilities%');
DELETE FROM metadata.sp_phonenumber where serviceprovider_id IN (select id from metadata.serviceprovider where individualname like '%ProductionAndIndustrialFacilities%');
DELETE FROM metadata.serviceprovider where individualname like '%ProductionAndIndustrialFacilities%';

INSERT INTO metadata.serviceprovider (id,administrativearea,city,contactinstructions,country,hoursofservice,individualname,onlineresource,organizationname,positionname,postalcode,providername,providersite,role)
	VALUES ((select nextval('metadata.hibernate_sequence')), '', '', NULL, '', NULL, 'Functioneel beheerder CDS Inspire, ProductionAndIndustrialFacilities', NULL, 'GBO provincies', 'pointOfContact', '', NULL, NULL, NULL);

-- INSPIRE View and Download service

INSERT INTO metadata.extendedcapabilities (id,metadataurl) VALUES ((select nextval('metadata.hibernate_sequence')), 'view_PF');
INSERT INTO metadata.extendedcapabilities (id,metadataurl) VALUES ((select nextval('metadata.hibernate_sequence')), 'download_PF');

INSERT INTO metadata.serviceidentification (id,abstract,fees,servicetype,servicepath,title) 
	VALUES ((select nextval('metadata.hibernate_sequence')), 'Deze View service heeft betrekking op ProductionAndIndustrialFacilities.', 'no conditions apply', 'WMS', 'ProductionAndIndustrialFacilities/services/view_PF', 'INSPIRE View service voor ProductionAndIndustrialFacilities van de gezamenlijke provincies');
INSERT INTO metadata.serviceidentification  (id,abstract,fees,servicetype,servicepath,title) 
	VALUES ((select nextval('metadata.hibernate_sequence')), 'Deze Download service heeft betrekking op ProductionAndIndustrialFacilities.', 'none', 'WFS', 'ProductionAndIndustrialFacilities/services/download_PF', 'INSPIRE Download service voor ProductionAndIndustrialFacilities van de gezamenlijke provincies');

INSERT INTO metadata.service (id,description,name,serviceidentification_id,serviceprovider_id, extendedcapabilities_id) VALUES ((select nextval('metadata.hibernate_sequence')), 'omschrijving view_PF', 'view_PF', (select id from metadata.serviceidentification where servicepath='ProductionAndIndustrialFacilities/services/view_PF'), (select id from metadata.serviceprovider where individualname like '%ProductionAndIndustrialFacilities%'), (select id from metadata.extendedcapabilities where metadataurl='view_PF')); 
INSERT INTO metadata.service (id,description,name,serviceidentification_id,serviceprovider_id, extendedcapabilities_id) VALUES ((select nextval('metadata.hibernate_sequence')), 'omschrijving download_PF', 'download_PF', (select id from metadata.serviceidentification where servicepath='ProductionAndIndustrialFacilities/services/download_PF'), (select id from metadata.serviceprovider where individualname like '%ProductionAndIndustrialFacilities%'), (select id from metadata.extendedcapabilities where metadataurl='download_PF')); 

INSERT INTO metadata.service_datasetmetadata (service_id, name, namespace, url,"index") VALUES ((select id from metadata.service where name='view_PF'), 'PF.ProductionFacility', '', 'X', 0);
INSERT INTO metadata.service_datasetmetadata (service_id, name, namespace, url,"index") VALUES ((select id from metadata.service where name='view_PF'), 'PF.ProductionInstallation', '', 'X', 1);
INSERT INTO metadata.service_datasetmetadata (service_id, name, namespace, url,"index") VALUES ((select id from metadata.service where name='view_PF'), 'PF.ProductionInstallationPart', '', 'X', 2);

INSERT INTO metadata.si_accessconstraint (serviceidentification_id,accessconstraint,"index") VALUES ((select serviceidentification_id from metadata.service where name='view_PF'), 'anders', 0);

INSERT INTO metadata.si_keyword (serviceidentification_id,codespace,"value","index" ) VALUES ((select serviceidentification_id from metadata.service where name='view_PF'), 'ISO', 'infoMapAccessService', 0);
INSERT INTO metadata.si_keyword (serviceidentification_id,codespace,"value","index" ) VALUES ((select serviceidentification_id from metadata.service where name='view_PF'), 'GEMET - INSPIRE themes, version 1.0', 'Habitat and Biotopes', 1);
INSERT INTO metadata.si_keyword (serviceidentification_id,codespace,"value","index" ) VALUES ((select serviceidentification_id from metadata.service where name='download_PF'), 'ISO', 'infoFeatureAccessService', 0);
INSERT INTO metadata.si_keyword (serviceidentification_id,codespace,"value","index" ) VALUES ((select serviceidentification_id from metadata.service where name='download_PF'), 'GEMET - INSPIRE themes, version 1.0', 'Habitat and Biotopes', 1);

-- si-version is not filled

INSERT INTO metadata.sp_deliverypoint (serviceprovider_id,deliverypoint,"index") VALUES ((select serviceprovider_id from metadata.service where name='view_PF'), '', 0);

INSERT INTO metadata.sp_emailaddress (serviceprovider_id, emailaddress,"index") VALUES ((select serviceprovider_id from metadata.service where name='view_PF'), 'inspire@gbo-provincie.nl', 0);

INSERT INTO metadata.sp_faxnumber (serviceprovider_id ,faxnumber,"index") VALUES ((select serviceprovider_id from metadata.service where name='view_PF'), '', 0);

INSERT INTO metadata.sp_phonenumber (serviceprovider_id,phonenumber,"index") VALUES ((select serviceprovider_id from metadata.service where name='view_PF'), '', 0);
--------------------------------

-- WMS & WFS with NL / flat structure


INSERT INTO metadata.extendedcapabilities (id,metadataurl) VALUES ((select nextval('metadata.hibernate_sequence')), 'wms_PF_NL');
INSERT INTO metadata.extendedcapabilities (id,metadataurl) VALUES ((select nextval('metadata.hibernate_sequence')), 'wfs_PF_NL');

INSERT INTO metadata.serviceidentification (id,abstract,fees,servicetype,servicepath,title) 
	VALUES ((select nextval('metadata.hibernate_sequence')), 'Deze WMS service heeft betrekking op ProductionAndIndustrialFacilities.', 'no conditions apply', 'WMS', 'ProductionAndIndustrialFacilities/services/wms_PF_NL', 'WMS service voor ProductionAndIndustrialFacilities van de gezamenlijke provincies');
INSERT INTO metadata.serviceidentification  (id,abstract,fees,servicetype,servicepath,title) 
	VALUES ((select nextval('metadata.hibernate_sequence')), 'Deze WFS service heeft betrekking op ProductionAndIndustrialFacilities.', 'none', 'WFS', 'ProductionAndIndustrialFacilities/services/wfs_PF_NL', 'WFS service voor ProductionAndIndustrialFacilities van de gezamenlijke provincies');

INSERT INTO metadata.service (id,description,name,serviceidentification_id,serviceprovider_id, extendedcapabilities_id) VALUES ((select nextval('metadata.hibernate_sequence')), 'omschrijving wms_PF_NL', 'wms_PF_NL', (select id from metadata.serviceidentification where servicepath='ProductionAndIndustrialFacilities/services/wms_PF_NL'), (select id from metadata.serviceprovider where individualname like '%ProductionAndIndustrialFacilities%'), (select id from metadata.extendedcapabilities where metadataurl='wms_PF_NL')); 
INSERT INTO metadata.service (id,description,name,serviceidentification_id,serviceprovider_id, extendedcapabilities_id) VALUES ((select nextval('metadata.hibernate_sequence')), 'omschrijving wfs_PF_NL', 'wfs_PF_NL', (select id from metadata.serviceidentification where servicepath='ProductionAndIndustrialFacilities/services/wfs_PF_NL'), (select id from metadata.serviceprovider where individualname like '%ProductionAndIndustrialFacilities%'), (select id from metadata.extendedcapabilities where metadataurl='wfs_PF_NL')); 

INSERT INTO metadata.service_datasetmetadata (service_id, name, namespace, url,"index") VALUES ((select id from metadata.service where name='wms_PF_NL'), 'PF.ProductionFacility_NL', '', 'X', 0);
INSERT INTO metadata.service_datasetmetadata (service_id, name, namespace, url,"index") VALUES ((select id from metadata.service where name='wms_PF_NL'), 'PF.ProductionInstallation_NL', '', 'X', 1);
INSERT INTO metadata.service_datasetmetadata (service_id, name, namespace, url,"index") VALUES ((select id from metadata.service where name='wms_PF_NL'), 'PF.ProductionInstallationPart_NL', '', 'X', 2);

INSERT INTO metadata.si_accessconstraint (serviceidentification_id,accessconstraint,"index") VALUES ((select serviceidentification_id from metadata.service where name='wms_PF_NL'), 'anders', 0);

INSERT INTO metadata.si_keyword (serviceidentification_id,codespace,"value","index" ) VALUES ((select serviceidentification_id from metadata.service where name='wms_PF_NL'), 'ISO', 'infoMapAccessService', 0);
INSERT INTO metadata.si_keyword (serviceidentification_id,codespace,"value","index" ) VALUES ((select serviceidentification_id from metadata.service where name='wms_PF_NL'), 'GEMET - INSPIRE themes, version 1.0', 'Habitat and Biotopes', 1);
INSERT INTO metadata.si_keyword (serviceidentification_id,codespace,"value","index" ) VALUES ((select serviceidentification_id from metadata.service where name='wfs_PF_NL'), 'ISO', 'infoFeatureAccessService', 0);
INSERT INTO metadata.si_keyword (serviceidentification_id,codespace,"value","index" ) VALUES ((select serviceidentification_id from metadata.service where name='wfs_PF_NL'), 'GEMET - INSPIRE themes, version 1.0', 'Habitat and Biotopes', 1);
