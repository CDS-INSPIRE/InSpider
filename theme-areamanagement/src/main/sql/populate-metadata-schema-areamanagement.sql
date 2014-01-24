
--
-- metadata AreaManagement
-- 

-- One serviceprovider for all services of this INSPIRE theme

DELETE FROM metadata.si_keyword where serviceidentification_id IN (select serviceidentification_id from metadata.service where name='view_AM' OR name='download_AM' OR name='viewAM' OR name='downloadAM');
DELETE FROM metadata.si_accessconstraint where serviceidentification_id IN (select serviceidentification_id from metadata.service where name='view_AM' OR name='download_AM' OR name='viewAM' OR name='downloadAM');
DELETE FROM metadata.service_datasetmetadata where service_id IN (select id from metadata.service where name='view_AM' OR name='download_AM' OR name='viewAM' OR name='downloadAM');
DELETE FROM metadata.service where name='view_AM' OR name='download_AM' OR name='viewAM' OR name='downloadAM';
DELETE FROM metadata.serviceidentification where servicepath like '%view_AM%' OR servicepath like '%download_AM%' OR servicepath like '%viewAM%' OR servicepath like '%downloadAM%';
DELETE FROM metadata.extendedcapabilities where metadataurl like '%view_AM%' OR metadataurl like '%download_AM%' OR metadataurl like '%viewAM%' OR metadataurl like '%downloadAM%';

DELETE FROM metadata.si_keyword where serviceidentification_id IN (select serviceidentification_id from metadata.service where name='wms_AM_NL' OR name='wfs_AM_NL');
DELETE FROM metadata.si_accessconstraint where serviceidentification_id IN (select serviceidentification_id from metadata.service where name='wms_AM_NL' OR name='wfs_AM_NL');
DELETE FROM metadata.service_datasetmetadata where service_id IN (select id from metadata.service where name='wms_AM_NL' OR name='wfs_AM_NL');
DELETE FROM metadata.service where name='wms_AM_NL' OR name='wfs_AM_NL';
DELETE FROM metadata.serviceidentification where servicepath like '%wms_AM_NL%' OR servicepath like '%wfs_AM_NL%';
DELETE FROM metadata.extendedcapabilities where metadataurl like '%wms_AM_NL%' OR metadataurl like '%wfs_AM_NL%';

DELETE FROM metadata.sp_deliverypoint where serviceprovider_id IN (select id from metadata.serviceprovider where individualname like '%AreaManagement%');
DELETE FROM metadata.sp_emailaddress where serviceprovider_id IN (select id from metadata.serviceprovider where individualname like '%AreaManagement%');
DELETE FROM metadata.sp_faxnumber where serviceprovider_id IN (select id from metadata.serviceprovider where individualname like '%AreaManagement%');
DELETE FROM metadata.sp_phonenumber where serviceprovider_id IN (select id from metadata.serviceprovider where individualname like '%AreaManagement%');
DELETE FROM metadata.serviceprovider where individualname like '%AreaManagement%';

INSERT INTO metadata.serviceprovider (id,administrativearea,city,contactinstructions,country,hoursofservice,individualname,onlineresource,organizationname,positionname,postalcode,providername,providersite,role)
	VALUES ((select nextval('metadata.hibernate_sequence')), '', '', NULL, '', NULL, 'Functioneel beheerder CDS Inspire, AreaManagement', NULL, 'GBO provincies', 'pointOfContact', '', NULL, NULL, NULL);

-- INSPIRE View and Download service

INSERT INTO metadata.extendedcapabilities (id,metadataurl) VALUES ((select nextval('metadata.hibernate_sequence')), 'view_AM');
INSERT INTO metadata.extendedcapabilities (id,metadataurl) VALUES ((select nextval('metadata.hibernate_sequence')), 'download_AM');

INSERT INTO metadata.serviceidentification (id,abstract,fees,servicetype,servicepath,title) 
	VALUES ((select nextval('metadata.hibernate_sequence')), 'Deze View service heeft betrekking op AreaManagement.', 'no conditions apply', 'WMS', 'AreaManagement/services/view_AM', 'INSPIRE View service voor AreaManagement van de gezamenlijke provincies');
INSERT INTO metadata.serviceidentification  (id,abstract,fees,servicetype,servicepath,title) 
	VALUES ((select nextval('metadata.hibernate_sequence')), 'Deze Download service heeft betrekking op AreaManagement.', 'none', 'WFS', 'AreaManagement/services/download_AM', 'INSPIRE Download service voor AreaManagement van de gezamenlijke provincies');

INSERT INTO metadata.service (id,description,name,serviceidentification_id,serviceprovider_id, extendedcapabilities_id) VALUES ((select nextval('metadata.hibernate_sequence')), 'omschrijving view_AM', 'view_AM', (select id from metadata.serviceidentification where servicepath='AreaManagement/services/view_AM'), (select id from metadata.serviceprovider where individualname like '%AreaManagement%'), (select id from metadata.extendedcapabilities where metadataurl='view_AM')); 
INSERT INTO metadata.service (id,description,name,serviceidentification_id,serviceprovider_id, extendedcapabilities_id) VALUES ((select nextval('metadata.hibernate_sequence')), 'omschrijving download_AM', 'download_AM', (select id from metadata.serviceidentification where servicepath='AreaManagement/services/download_AM'), (select id from metadata.serviceprovider where individualname like '%AreaManagement%'), (select id from metadata.extendedcapabilities where metadataurl='download_AM')); 

INSERT INTO metadata.service_datasetmetadata (service_id, name, namespace, url,"index") VALUES ((select id from metadata.service where name='view_AM'), 'AM.BathingWaters', '', 'X', 0);
INSERT INTO metadata.service_datasetmetadata (service_id, name, namespace, url,"index") VALUES ((select id from metadata.service where name='view_AM'), 'AM.DrinkingWaterProtectionArea', '', 'X', 1);
INSERT INTO metadata.service_datasetmetadata (service_id, name, namespace, url,"index") VALUES ((select id from metadata.service where name='view_AM'), 'AM.NoiseRestrictionZone', '', 'X', 2);
INSERT INTO metadata.service_datasetmetadata (service_id, name, namespace, url,"index") VALUES ((select id from metadata.service where name='view_AM'), 'AM.ProspectingAndMiningPermitArea', '', 'X', 3);

INSERT INTO metadata.si_accessconstraint (serviceidentification_id,accessconstraint,"index") VALUES ((select serviceidentification_id from metadata.service where name='view_AM'), 'anders', 0);

INSERT INTO metadata.si_keyword (serviceidentification_id,codespace,"value","index" ) VALUES ((select serviceidentification_id from metadata.service where name='view_AM'), 'ISO', 'infoMapAccessService', 0);
INSERT INTO metadata.si_keyword (serviceidentification_id,codespace,"value","index" ) VALUES ((select serviceidentification_id from metadata.service where name='view_AM'), 'GEMET - INSPIRE themes, version 1.0', 'Habitat and Biotopes', 1);
INSERT INTO metadata.si_keyword (serviceidentification_id,codespace,"value","index" ) VALUES ((select serviceidentification_id from metadata.service where name='download_AM'), 'ISO', 'infoFeatureAccessService', 0);
INSERT INTO metadata.si_keyword (serviceidentification_id,codespace,"value","index" ) VALUES ((select serviceidentification_id from metadata.service where name='download_AM'), 'GEMET - INSPIRE themes, version 1.0', 'Habitat and Biotopes', 1);

-- si-version is not filled

INSERT INTO metadata.sp_deliverypoint (serviceprovider_id,deliverypoint,"index") VALUES ((select serviceprovider_id from metadata.service where name='view_AM'), '', 0);

INSERT INTO metadata.sp_emailaddress (serviceprovider_id, emailaddress,"index") VALUES ((select serviceprovider_id from metadata.service where name='view_AM'), 'inspire@gbo-provincie.nl', 0);

INSERT INTO metadata.sp_faxnumber (serviceprovider_id ,faxnumber,"index") VALUES ((select serviceprovider_id from metadata.service where name='view_AM'), '', 0);

INSERT INTO metadata.sp_phonenumber (serviceprovider_id,phonenumber,"index") VALUES ((select serviceprovider_id from metadata.service where name='view_AM'), '', 0);
--------------------------------

-- WMS & WFS with NL / flat structure


INSERT INTO metadata.extendedcapabilities (id,metadataurl) VALUES ((select nextval('metadata.hibernate_sequence')), 'wms_AM_NL');
INSERT INTO metadata.extendedcapabilities (id,metadataurl) VALUES ((select nextval('metadata.hibernate_sequence')), 'wfs_AM_NL');

INSERT INTO metadata.serviceidentification (id,abstract,fees,servicetype,servicepath,title) 
	VALUES ((select nextval('metadata.hibernate_sequence')), 'Deze WMS service heeft betrekking op AreaManagement.', 'no conditions apply', 'WMS', 'AreaManagement/services/wms_AM_NL', 'WMS service voor AreaManagement van de gezamenlijke provincies');
INSERT INTO metadata.serviceidentification  (id,abstract,fees,servicetype,servicepath,title) 
	VALUES ((select nextval('metadata.hibernate_sequence')), 'Deze WFS service heeft betrekking op AreaManagement.', 'none', 'WFS', 'AreaManagement/services/wfs_AM_NL', 'WFS service voor AreaManagement van de gezamenlijke provincies');

INSERT INTO metadata.service (id,description,name,serviceidentification_id,serviceprovider_id, extendedcapabilities_id) VALUES ((select nextval('metadata.hibernate_sequence')), 'omschrijving wms_AM_NL', 'wms_AM_NL', (select id from metadata.serviceidentification where servicepath='AreaManagement/services/wms_AM_NL'), (select id from metadata.serviceprovider where individualname like '%AreaManagement%'), (select id from metadata.extendedcapabilities where metadataurl='wms_AM_NL')); 
INSERT INTO metadata.service (id,description,name,serviceidentification_id,serviceprovider_id, extendedcapabilities_id) VALUES ((select nextval('metadata.hibernate_sequence')), 'omschrijving wfs_AM_NL', 'wfs_AM_NL', (select id from metadata.serviceidentification where servicepath='AreaManagement/services/wfs_AM_NL'), (select id from metadata.serviceprovider where individualname like '%AreaManagement%'), (select id from metadata.extendedcapabilities where metadataurl='wfs_AM_NL')); 

INSERT INTO metadata.service_datasetmetadata (service_id, name, namespace, url,"index") VALUES ((select id from metadata.service where name='wms_AM_NL'), 'AM.BathingWaters', '', 'X', 0);
INSERT INTO metadata.service_datasetmetadata (service_id, name, namespace, url,"index") VALUES ((select id from metadata.service where name='wms_AM_NL'), 'AM.DrinkingWaterProtectionArea', '', 'X', 1);
INSERT INTO metadata.service_datasetmetadata (service_id, name, namespace, url,"index") VALUES ((select id from metadata.service where name='wms_AM_NL'), 'AM.NoiseRestrictionZone', '', 'X', 2);
INSERT INTO metadata.service_datasetmetadata (service_id, name, namespace, url,"index") VALUES ((select id from metadata.service where name='wms_AM_NL'), 'AM.ProspectingAndMiningPermitArea', '', 'CX', 3);

INSERT INTO metadata.si_accessconstraint (serviceidentification_id,accessconstraint,"index") VALUES ((select serviceidentification_id from metadata.service where name='wms_AM_NL'), 'anders', 0);

INSERT INTO metadata.si_keyword (serviceidentification_id,codespace,"value","index" ) VALUES ((select serviceidentification_id from metadata.service where name='wms_AM_NL'), 'ISO', 'infoMapAccessService', 0);
INSERT INTO metadata.si_keyword (serviceidentification_id,codespace,"value","index" ) VALUES ((select serviceidentification_id from metadata.service where name='wms_AM_NL'), 'GEMET - INSPIRE themes, version 1.0', 'Habitat and Biotopes', 1);
INSERT INTO metadata.si_keyword (serviceidentification_id,codespace,"value","index" ) VALUES ((select serviceidentification_id from metadata.service where name='wfs_AM_NL'), 'ISO', 'infoFeatureAccessService', 0);
INSERT INTO metadata.si_keyword (serviceidentification_id,codespace,"value","index" ) VALUES ((select serviceidentification_id from metadata.service where name='wfs_AM_NL'), 'GEMET - INSPIRE themes, version 1.0', 'Habitat and Biotopes', 1);
