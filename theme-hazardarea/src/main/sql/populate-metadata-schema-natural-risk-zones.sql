
--
-- metadata NaturalRiskZones
-- 

-- One serviceprovider for all services of this INSPIRE theme
DELETE FROM metadata.si_keyword where serviceidentification_id IN (select serviceidentification_id from metadata.service where name='view_NZ' OR name='download_NZ');
DELETE FROM metadata.si_accessconstraint where serviceidentification_id IN (select serviceidentification_id from metadata.service where name='view_NZ' OR name='download_NZ');
DELETE FROM metadata.service_datasetmetadata where service_id IN (select id from metadata.service where name='view_NZ' OR name='download_NZ');
DELETE FROM metadata.service where name='view_NZ' OR name='download_NZ';
DELETE FROM metadata.serviceidentification where servicepath like '%view_NZ%' OR servicepath like '%download_NZ%';
DELETE FROM metadata.extendedcapabilities where metadataurl like '%view_NZ%' OR metadataurl like '%download_NZ%';

DELETE FROM metadata.si_keyword where serviceidentification_id IN (select serviceidentification_id from metadata.service where name='wms_NZ_NL' OR name='wfs_NZ_NL');
DELETE FROM metadata.si_accessconstraint where serviceidentification_id IN (select serviceidentification_id from metadata.service where name='wms_NZ_NL' OR name='wfs_NZ_NL');
DELETE FROM metadata.service_datasetmetadata where service_id IN (select id from metadata.service where name='wms_NZ_NL' OR name='wfs_NZ_NL');
DELETE FROM metadata.service where name='wms_NZ_NL' OR name='wfs_NZ_NL';
DELETE FROM metadata.serviceidentification where servicepath like '%wms_NZ_NL%' OR servicepath like '%wfs_NZ_NL%';
DELETE FROM metadata.extendedcapabilities where metadataurl like '%wms_NZ_NL%' OR metadataurl like '%wfs_NZ_NL%';

DELETE FROM metadata.sp_deliverypoint where serviceprovider_id IN (select id from metadata.serviceprovider where individualname like '%NaturalRiskZones%');
DELETE FROM metadata.sp_emailaddress where serviceprovider_id IN (select id from metadata.serviceprovider where individualname like '%NaturalRiskZones%');
DELETE FROM metadata.sp_faxnumber where serviceprovider_id IN (select id from metadata.serviceprovider where individualname like '%NaturalRiskZones%');
DELETE FROM metadata.sp_phonenumber where serviceprovider_id IN (select id from metadata.serviceprovider where individualname like '%NaturalRiskZones%');
DELETE FROM metadata.serviceprovider where individualname like '%NaturalRiskZones%';

INSERT INTO metadata.serviceprovider (id,administrativearea,city,contactinstructions,country,hoursofservice,individualname,onlineresource,organizationname,positionname,postalcode,providername,providersite,role)
	VALUES ((select nextval('metadata.hibernate_sequence')), '', '', NULL, '', NULL, 'Functioneel beheerder CDS Inspire, NaturalRiskZones', NULL, 'GBO provincies', 'pointOfContact', '', NULL, NULL, NULL);

-- INSPIRE View and Download service

INSERT INTO metadata.extendedcapabilities (id,metadataurl) VALUES ((select nextval('metadata.hibernate_sequence')), 'view_NZ');
INSERT INTO metadata.extendedcapabilities (id,metadataurl) VALUES ((select nextval('metadata.hibernate_sequence')), 'download_NZ');

INSERT INTO metadata.serviceidentification (id,abstract,fees,servicetype,servicepath,title) 
	VALUES ((select nextval('metadata.hibernate_sequence')), 'Deze View service heeft betrekking op NaturalRiskZones.', 'no conditions apply', 'WMS', 'NaturalRiskZones/services/view_NZ', 'INSPIRE View service voor NaturalRiskZones van de gezamenlijke provincies');
INSERT INTO metadata.serviceidentification  (id,abstract,fees,servicetype,servicepath,title) 
	VALUES ((select nextval('metadata.hibernate_sequence')), 'Deze Download service heeft betrekking op NaturalRiskZones.', 'none', 'WFS', 'NaturalRiskZones/services/download_NZ', 'INSPIRE Download service voor NaturalRiskZones van de gezamenlijke provincies');

INSERT INTO metadata.service (id,description,name,serviceidentification_id,serviceprovider_id, extendedcapabilities_id) VALUES ((select nextval('metadata.hibernate_sequence')), 'omschrijving view_NZ', 'view_NZ', (select id from metadata.serviceidentification where servicepath='NaturalRiskZones/services/view_NZ'), (select id from metadata.serviceprovider where individualname like '%NaturalRiskZones%'), (select id from metadata.extendedcapabilities where metadataurl='view_NZ')); 
INSERT INTO metadata.service (id,description,name,serviceidentification_id,serviceprovider_id, extendedcapabilities_id) VALUES ((select nextval('metadata.hibernate_sequence')), 'omschrijving download_NZ', 'download_NZ', (select id from metadata.serviceidentification where servicepath='NaturalRiskZones/services/download_NZ'), (select id from metadata.serviceprovider where individualname like '%NaturalRiskZones%'), (select id from metadata.extendedcapabilities where metadataurl='download_NZ')); 

INSERT INTO metadata.service_datasetmetadata (service_id, name, namespace, url,"index") VALUES ((select id from metadata.service where name='view_NZ'), 'NZ.HazardArea', '', 'X', 0);
INSERT INTO metadata.service_datasetmetadata (service_id, name, namespace, url,"index") VALUES ((select id from metadata.service where name='view_NZ'), 'NZ.RiskZone', '', 'X', 1);
INSERT INTO metadata.service_datasetmetadata (service_id, name, namespace, url,"index") VALUES ((select id from metadata.service where name='view_NZ'), 'NZ.ExposedElement', '', 'X', 2);

INSERT INTO metadata.si_accessconstraint (serviceidentification_id,accessconstraint,"index") VALUES ((select serviceidentification_id from metadata.service where name='view_NZ'), 'anders', 0);

INSERT INTO metadata.si_keyword (serviceidentification_id,codespace,"value","index" ) VALUES ((select serviceidentification_id from metadata.service where name='view_NZ'), 'ISO', 'infoMapAccessService', 0);
INSERT INTO metadata.si_keyword (serviceidentification_id,codespace,"value","index" ) VALUES ((select serviceidentification_id from metadata.service where name='view_NZ'), 'GEMET - INSPIRE themes, version 1.0', 'Habitat and Biotopes', 1);
INSERT INTO metadata.si_keyword (serviceidentification_id,codespace,"value","index" ) VALUES ((select serviceidentification_id from metadata.service where name='download_NZ'), 'ISO', 'infoFeatureAccessService', 0);
INSERT INTO metadata.si_keyword (serviceidentification_id,codespace,"value","index" ) VALUES ((select serviceidentification_id from metadata.service where name='download_NZ'), 'GEMET - INSPIRE themes, version 1.0', 'Habitat and Biotopes', 1);

-- si-version is not filled

INSERT INTO metadata.sp_deliverypoint (serviceprovider_id,deliverypoint,"index") VALUES ((select serviceprovider_id from metadata.service where name='view_NZ'), '', 0);

INSERT INTO metadata.sp_emailaddress (serviceprovider_id, emailaddress,"index") VALUES ((select serviceprovider_id from metadata.service where name='view_NZ'), 'inspire@gbo-provincie.nl', 0);

INSERT INTO metadata.sp_faxnumber (serviceprovider_id ,faxnumber,"index") VALUES ((select serviceprovider_id from metadata.service where name='view_NZ'), '', 0);

INSERT INTO metadata.sp_phonenumber (serviceprovider_id,phonenumber,"index") VALUES ((select serviceprovider_id from metadata.service where name='view_NZ'), '', 0);
--------------------------------

-- WMS & WFS with NL / flat structure


INSERT INTO metadata.extendedcapabilities (id,metadataurl) VALUES ((select nextval('metadata.hibernate_sequence')), 'wms_NZ_NL');
INSERT INTO metadata.extendedcapabilities (id,metadataurl) VALUES ((select nextval('metadata.hibernate_sequence')), 'wfs_NZ_NL');

INSERT INTO metadata.serviceidentification (id,abstract,fees,servicetype,servicepath,title) 
	VALUES ((select nextval('metadata.hibernate_sequence')), 'Deze WMS service heeft betrekking op NaturalRiskZones.', 'no conditions apply', 'WMS', 'NaturalRiskZones/services/wms_NZ_NL', 'WMS service voor NaturalRiskZones van de gezamenlijke provincies');
INSERT INTO metadata.serviceidentification  (id,abstract,fees,servicetype,servicepath,title) 
	VALUES ((select nextval('metadata.hibernate_sequence')), 'Deze WFS service heeft betrekking op NaturalRiskZones.', 'none', 'WFS', 'NaturalRiskZones/services/wfs_NZ_NL', 'WFS service voor NaturalRiskZones van de gezamenlijke provincies');

INSERT INTO metadata.service (id,description,name,serviceidentification_id,serviceprovider_id, extendedcapabilities_id) VALUES ((select nextval('metadata.hibernate_sequence')), 'omschrijving wms_NZ_NL', 'wms_NZ_NL', (select id from metadata.serviceidentification where servicepath='NaturalRiskZones/services/wms_NZ_NL'), (select id from metadata.serviceprovider where individualname like '%NaturalRiskZones%'), (select id from metadata.extendedcapabilities where metadataurl='wms_NZ_NL')); 
INSERT INTO metadata.service (id,description,name,serviceidentification_id,serviceprovider_id, extendedcapabilities_id) VALUES ((select nextval('metadata.hibernate_sequence')), 'omschrijving wfs_NZ_NL', 'wfs_NZ_NL', (select id from metadata.serviceidentification where servicepath='NaturalRiskZones/services/wfs_NZ_NL'), (select id from metadata.serviceprovider where individualname like '%NaturalRiskZones%'), (select id from metadata.extendedcapabilities where metadataurl='wfs_NZ_NL')); 

INSERT INTO metadata.service_datasetmetadata (service_id, name, namespace, url,"index") VALUES ((select id from metadata.service where name='wms_NZ_NL'), 'NZ.HazardArea', '', 'X', 0);
INSERT INTO metadata.service_datasetmetadata (service_id, name, namespace, url,"index") VALUES ((select id from metadata.service where name='wms_NZ_NL'), 'NZ.RiskZone', '', 'X', 1);
INSERT INTO metadata.service_datasetmetadata (service_id, name, namespace, url,"index") VALUES ((select id from metadata.service where name='wms_NZ_NL'), 'NZ.ExposedElement', '', 'X', 2);

INSERT INTO metadata.si_accessconstraint (serviceidentification_id,accessconstraint,"index") VALUES ((select serviceidentification_id from metadata.service where name='wms_NZ_NL'), 'anders', 0);

INSERT INTO metadata.si_keyword (serviceidentification_id,codespace,"value","index" ) VALUES ((select serviceidentification_id from metadata.service where name='wms_NZ_NL'), 'ISO', 'infoMapAccessService', 0);
INSERT INTO metadata.si_keyword (serviceidentification_id,codespace,"value","index" ) VALUES ((select serviceidentification_id from metadata.service where name='wms_NZ_NL'), 'GEMET - INSPIRE themes, version 1.0', 'Habitat and Biotopes', 1);
INSERT INTO metadata.si_keyword (serviceidentification_id,codespace,"value","index" ) VALUES ((select serviceidentification_id from metadata.service where name='wfs_NZ_NL'), 'ISO', 'infoFeatureAccessService', 0);
INSERT INTO metadata.si_keyword (serviceidentification_id,codespace,"value","index" ) VALUES ((select serviceidentification_id from metadata.service where name='wfs_NZ_NL'), 'GEMET - INSPIRE themes, version 1.0', 'Habitat and Biotopes', 1);
