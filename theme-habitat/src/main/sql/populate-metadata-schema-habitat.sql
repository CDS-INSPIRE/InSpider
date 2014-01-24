
--
-- metadata HabitatAndBiotopes
-- 

-- One serviceprovider for all services of this INSPIRE theme
DELETE FROM metadata.si_keyword where serviceidentification_id IN (select serviceidentification_id from metadata.service where name='view_HB' OR name='download_HB');
DELETE FROM metadata.si_accessconstraint where serviceidentification_id IN (select serviceidentification_id from metadata.service where name='view_HB' OR name='download_HB');
DELETE FROM metadata.service_datasetmetadata where service_id IN (select id from metadata.service where name='view_HB' OR name='download_HB');
DELETE FROM metadata.service where name='view_HB' OR name='download_HB';
DELETE FROM metadata.serviceidentification where servicepath like '%view_HB%' OR servicepath like '%download_HB%';
DELETE FROM metadata.extendedcapabilities where metadataurl like '%view_HB%' OR metadataurl like '%download_HB%';

DELETE FROM metadata.si_keyword where serviceidentification_id IN (select serviceidentification_id from metadata.service where name='wms_HB_NL' OR name='wfs_HB_NL');
DELETE FROM metadata.si_accessconstraint where serviceidentification_id IN (select serviceidentification_id from metadata.service where name='wms_HB_NL' OR name='wfs_HB_NL');
DELETE FROM metadata.service_datasetmetadata where service_id IN (select id from metadata.service where name='wms_HB_NL' OR name='wfs_HB_NL');
DELETE FROM metadata.service where name='wms_HB_NL' OR name='wfs_HB_NL';
DELETE FROM metadata.serviceidentification where servicepath like '%wms_HB_NL%' OR servicepath like '%wfs_HB_NL%';
DELETE FROM metadata.extendedcapabilities where metadataurl like '%wms_HB_NL%' OR metadataurl like '%wfs_HB_NL%';

DELETE FROM metadata.sp_deliverypoint where serviceprovider_id IN (select id from metadata.serviceprovider where individualname like '%HabitatAndBioTopes%');
DELETE FROM metadata.sp_emailaddress where serviceprovider_id IN (select id from metadata.serviceprovider where individualname like '%HabitatAndBioTopes%');
DELETE FROM metadata.sp_faxnumber where serviceprovider_id IN (select id from metadata.serviceprovider where individualname like '%HabitatAndBioTopes%');
DELETE FROM metadata.sp_phonenumber where serviceprovider_id IN (select id from metadata.serviceprovider where individualname like '%HabitatAndBioTopes%');
DELETE FROM metadata.serviceprovider where individualname like '%HabitatAndBioTopes%';

INSERT INTO metadata.serviceprovider (id,administrativearea,city,contactinstructions,country,hoursofservice,individualname,onlineresource,organizationname,positionname,postalcode,providername,providersite,role)
	VALUES ((select nextval('metadata.hibernate_sequence')), '', '', NULL, '', NULL, 'Functioneel beheerder CDS Inspire, HabitatAndBioTopes', NULL, 'GBO provincies', 'pointOfContact', '', NULL, NULL, NULL);

-- INSPIRE View and Download service

INSERT INTO metadata.extendedcapabilities (id,metadataurl) VALUES ((select nextval('metadata.hibernate_sequence')), 'view_HB');
INSERT INTO metadata.extendedcapabilities (id,metadataurl) VALUES ((select nextval('metadata.hibernate_sequence')), 'download_HB');

INSERT INTO metadata.serviceidentification (id,abstract,fees,servicetype,servicepath,title) 
	VALUES ((select nextval('metadata.hibernate_sequence')), 'Deze View service heeft betrekking op HabitatAndBioTopes.', 'no conditions apply', 'WMS', 'HabitatAndBioTopes/services/view_HB', 'INSPIRE View service voor HabitatAndBioTopes van de gezamenlijke provincies');
INSERT INTO metadata.serviceidentification  (id,abstract,fees,servicetype,servicepath,title) 
	VALUES ((select nextval('metadata.hibernate_sequence')), 'Deze Download service heeft betrekking op HabitatAndBioTopes.', 'none', 'WFS', 'HabitatAndBioTopes/services/download_HB', 'INSPIRE Download service voor HabitatAndBioTopes van de gezamenlijke provincies');

INSERT INTO metadata.service (id,description,name,serviceidentification_id,serviceprovider_id, extendedcapabilities_id) VALUES ((select nextval('metadata.hibernate_sequence')), 'omschrijving view_HB', 'view_HB', (select id from metadata.serviceidentification where servicepath='HabitatAndBioTopes/services/view_HB'), (select id from metadata.serviceprovider where individualname like '%HabitatAndBioTopes%'), (select id from metadata.extendedcapabilities where metadataurl='view_HB')); 
INSERT INTO metadata.service (id,description,name,serviceidentification_id,serviceprovider_id, extendedcapabilities_id) VALUES ((select nextval('metadata.hibernate_sequence')), 'omschrijving download_HB', 'download_HB', (select id from metadata.serviceidentification where servicepath='HabitatAndBioTopes/services/download_HB'), (select id from metadata.serviceprovider where individualname like '%HabitatAndBioTopes%'), (select id from metadata.extendedcapabilities where metadataurl='download_HB')); 

INSERT INTO metadata.service_datasetmetadata (service_id, name, namespace, url,"index") VALUES ((select id from metadata.service where name='view_HB'), 'HB.Habitat', '', 'X', 0);

INSERT INTO metadata.si_accessconstraint (serviceidentification_id,accessconstraint,"index") VALUES ((select serviceidentification_id from metadata.service where name='view_HB'), 'anders', 0);

INSERT INTO metadata.si_keyword (serviceidentification_id,codespace,"value","index" ) VALUES ((select serviceidentification_id from metadata.service where name='view_HB'), 'ISO', 'infoMapAccessService', 0);
INSERT INTO metadata.si_keyword (serviceidentification_id,codespace,"value","index" ) VALUES ((select serviceidentification_id from metadata.service where name='view_HB'), 'GEMET - INSPIRE themes, version 1.0', 'Habitat and Biotopes', 1);
INSERT INTO metadata.si_keyword (serviceidentification_id,codespace,"value","index" ) VALUES ((select serviceidentification_id from metadata.service where name='download_HB'), 'ISO', 'infoFeatureAccessService', 0);
INSERT INTO metadata.si_keyword (serviceidentification_id,codespace,"value","index" ) VALUES ((select serviceidentification_id from metadata.service where name='download_HB'), 'GEMET - INSPIRE themes, version 1.0', 'Habitat and Biotopes', 1);

-- si-version is not filled

INSERT INTO metadata.sp_deliverypoint (serviceprovider_id,deliverypoint,"index") VALUES ((select serviceprovider_id from metadata.service where name='view_HB'), '', 0);

INSERT INTO metadata.sp_emailaddress (serviceprovider_id, emailaddress,"index") VALUES ((select serviceprovider_id from metadata.service where name='view_HB'), 'inspire@gbo-provincie.nl', 0);

INSERT INTO metadata.sp_faxnumber (serviceprovider_id ,faxnumber,"index") VALUES ((select serviceprovider_id from metadata.service where name='view_HB'), '', 0);

INSERT INTO metadata.sp_phonenumber (serviceprovider_id,phonenumber,"index") VALUES ((select serviceprovider_id from metadata.service where name='view_HB'), '', 0);
--------------------------------

-- WMS & WFS with NL / flat structure


INSERT INTO metadata.extendedcapabilities (id,metadataurl) VALUES ((select nextval('metadata.hibernate_sequence')), 'wms_HB_NL');
INSERT INTO metadata.extendedcapabilities (id,metadataurl) VALUES ((select nextval('metadata.hibernate_sequence')), 'wfs_HB_NL');

INSERT INTO metadata.serviceidentification (id,abstract,fees,servicetype,servicepath,title) 
	VALUES ((select nextval('metadata.hibernate_sequence')), 'Deze WMS service heeft betrekking op HabitatAndBioTopes.', 'no conditions apply', 'WMS', 'HabitatAndBioTopes/services/wms_HB_NL', 'WMS service voor HabitatAndBioTopes van de gezamenlijke provincies');
INSERT INTO metadata.serviceidentification  (id,abstract,fees,servicetype,servicepath,title) 
	VALUES ((select nextval('metadata.hibernate_sequence')), 'Deze WFS service heeft betrekking op HabitatAndBioTopes.', 'none', 'WFS', 'HabitatAndBioTopes/services/wfs_HB_NL', 'WFS service voor HabitatAndBioTopes van de gezamenlijke provincies');

INSERT INTO metadata.service (id,description,name,serviceidentification_id,serviceprovider_id, extendedcapabilities_id) VALUES ((select nextval('metadata.hibernate_sequence')), 'omschrijving wms_HB_NL', 'wms_HB_NL', (select id from metadata.serviceidentification where servicepath='HabitatAndBioTopes/services/wms_HB_NL'), (select id from metadata.serviceprovider where individualname like '%HabitatAndBioTopes%'), (select id from metadata.extendedcapabilities where metadataurl='wms_HB_NL')); 
INSERT INTO metadata.service (id,description,name,serviceidentification_id,serviceprovider_id, extendedcapabilities_id) VALUES ((select nextval('metadata.hibernate_sequence')), 'omschrijving wms_HB_NL', 'wfs_HB_NL', (select id from metadata.serviceidentification where servicepath='HabitatAndBioTopes/services/wfs_HB_NL'), (select id from metadata.serviceprovider where individualname like '%HabitatAndBioTopes%'), (select id from metadata.extendedcapabilities where metadataurl='wfs_HB_NL')); 

INSERT INTO metadata.service_datasetmetadata (service_id, name, namespace, url,"index") VALUES ((select id from metadata.service where name='wms_HB_NL'), 'HB.Habitat', '', 'X', 0);

INSERT INTO metadata.si_accessconstraint (serviceidentification_id,accessconstraint,"index") VALUES ((select serviceidentification_id from metadata.service where name='wms_HB_NL'), 'anders', 0);

INSERT INTO metadata.si_keyword (serviceidentification_id,codespace,"value","index" ) VALUES ((select serviceidentification_id from metadata.service where name='wms_HB_NL'), 'ISO', 'infoMapAccessService', 0);
INSERT INTO metadata.si_keyword (serviceidentification_id,codespace,"value","index" ) VALUES ((select serviceidentification_id from metadata.service where name='wms_HB_NL'), 'GEMET - INSPIRE themes, version 1.0', 'Habitat and Biotopes', 1);
INSERT INTO metadata.si_keyword (serviceidentification_id,codespace,"value","index" ) VALUES ((select serviceidentification_id from metadata.service where name='wfs_HB_NL'), 'ISO', 'infoFeatureAccessService', 0);
INSERT INTO metadata.si_keyword (serviceidentification_id,codespace,"value","index" ) VALUES ((select serviceidentification_id from metadata.service where name='wfs_HB_NL'), 'GEMET - INSPIRE themes, version 1.0', 'Habitat and Biotopes', 1);
