-- Table: metadata.spatial_datasetinfo

DROP TABLE IF EXISTS metadata.spatial_datasetinfo;

CREATE TABLE metadata.spatial_datasetinfo
(
  id bigint NOT NULL,
  type character varying,
  service_id bigint NOT NULL,
  name character varying,
  code character varying,
  namespace character varying,
  index integer NOT NULL,
  CONSTRAINT spatial_datasetinfo_pkey PRIMARY KEY (id),
  CONSTRAINT spatial_datasetinfo_service_id_fkey FOREIGN KEY (service_id)
      REFERENCES metadata.service (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT check_type CHECK (type::text = 'WFS'::text OR type::text = 'WMS'::text)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE metadata.spatial_datasetinfo
  OWNER TO postgres;

 GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON metadata.spatial_datasetinfo TO inspire;
 
-- Index: metadata.spatial_datasetinfo_id_index

-- DROP INDEX metadata.spatial_datasetinfo_id_index;

CREATE UNIQUE INDEX spatial_datasetinfo_id_index
  ON metadata.spatial_datasetinfo
  USING btree
  (id);

-- Index: metadata.spatial_datasetinfo_name_idx

-- DROP INDEX metadata.spatial_datasetinfo_name_idx;

CREATE INDEX spatial_datasetinfo_name_idx
  ON metadata.spatial_datasetinfo
  USING btree
  (name COLLATE pg_catalog."default");

-- Index: metadata.spatial_datasetinfo_serviceid_index

-- DROP INDEX metadata.spatial_datasetinfo_serviceid_index;

CREATE INDEX spatial_datasetinfo_serviceid_index
  ON metadata.spatial_datasetinfo
  USING btree
  (service_id);

  

  
-- Table: metadata.spatial_datasetinfotemp

DROP TABLE IF EXISTS metadata.spatial_datasetinfotemp;

CREATE TABLE metadata.spatial_datasetinfotemp
(
  id bigint NOT NULL,
  service_type character varying,
  service_name character varying,
  feature_name character varying,
  code character varying,
  namespace character varying,
  xml_namespace character varying,
  url text,
  index integer NOT NULL,
  CONSTRAINT spatial_datasetinfotemp_pkey PRIMARY KEY (id),
  CONSTRAINT check_type CHECK (service_type::text = 'WFS'::text OR service_type::text = 'WMS'::text)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE metadata.spatial_datasetinfotemp
  OWNER TO postgres;

GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON metadata.spatial_datasetinfotemp TO inspire;
  
INSERT INTO metadata.spatial_datasetinfotemp (id ,service_type, service_name, feature_name, code, namespace, xml_namespace, url, index) VALUES
(1,'WFS','download_AM','am:ManagementRestrictionOrRegulationZone','dummy','dummy','http://inspire.ec.europa.eu/schemas/am/3.0rc2', 'http://www.nationaalgeoregister.nl/geonetwork/srv/dut/csw?Service=CSW&Request=GetRecordById&Version=2.0.2&id=071a5652-c513-4722-bb45-bf522bb4349f&outputSchema=http://www.isotc211.org/2005/gmd&elementSetName=full', 0),
(2,'WFS','download_AF','app:Stalgroep','dummy','dummy','http://www.ipo.nl/InSpider', 'http://www.nationaalgeoregister.nl/geonetwork/srv/dut/csw?Service=CSW&Request=GetRecordById&Version=2.0.2&id=4784dc9b-62bf-4c2a-a25c-b59b049b4956&outputSchema=http://www.isotc211.org/2005/gmd&elementSetName=full', 0),
(3,'WFS','download_EF','app:Monsterpunt','dummy','dummy','http://www.ipo.nl/InSpider', 'http://www.nationaalgeoregister.nl/geonetwork/srv/dut/csw?Service=CSW&Request=GetRecordById&Version=2.0.2&id=ebd6ea46-ee7b-432f-83c4-058c889a790d&outputSchema=http://www.isotc211.org/2005/gmd&elementSetName=full', 0),
(4,'WFS','download_EF','app:Monster','dummy','dummy','http://www.ipo.nl/InSpider', 'http://www.nationaalgeoregister.nl/geonetwork/srv/dut/csw?Service=CSW&Request=GetRecordById&Version=2.0.2&id=ebd6ea46-ee7b-432f-83c4-058c889a790d&outputSchema=http://www.isotc211.org/2005/gmd&elementSetName=full', 1),
(5,'WFS','download_EF','app:Monsterresultaat','dummy','dummy','http://www.ipo.nl/InSpider', 'http://www.nationaalgeoregister.nl/geonetwork/srv/dut/csw?Service=CSW&Request=GetRecordById&Version=2.0.2&id=ebd6ea46-ee7b-432f-83c4-058c889a790d&outputSchema=http://www.isotc211.org/2005/gmd&elementSetName=full', 2),
(6,'WFS','download_HB','hb:Habitat','dummy','dummy', 'http://inspire.ec.europa.eu/schemas/hb/3.0rc3', 'http://www.nationaalgeoregister.nl/geonetwork/srv/dut/csw?Service=CSW&Request=GetRecordById&Version=2.0.2&id=702060c1-b3fd-44ec-8cc6-d4249d150110&outputSchema=http://www.isotc211.org/2005/gmd&elementSetName=full', 0),
(7,'WFS','download_NZ','app:HazardArea_NL','dummy','dummy','http://www.ipo.nl/InSpider', 'http://www.nationaalgeoregister.nl/geonetwork/srv/dut/csw?Service=CSW&Request=GetRecordById&Version=2.0.2&id=00e8746f-5031-413e-8343-9203c8ea82ee&outputSchema=http://www.isotc211.org/2005/gmd&elementSetName=full', 0),
(8,'WFS','download_PF','pf:ProductionFacility','dummy','dummy', 'http://inspire.ec.europa.eu/schemas/pf/3.0rc3', 'http://www.nationaalgeoregister.nl/geonetwork/srv/dut/csw?Service=CSW&Request=GetRecordById&Version=2.0.2&id=3094a1c4-3dd6-4e8e-9929-4a63dd66992e&outputSchema=http://www.isotc211.org/2005/gmd&elementSetName=full', 0),
(9,'WFS','download_PF','pf:ProductionInstallation','dummy','dummy','http://inspire.ec.europa.eu/schemas/pf/3.0rc3', 'http://www.nationaalgeoregister.nl/geonetwork/srv/dut/csw?Service=CSW&Request=GetRecordById&Version=2.0.2&id=3094a1c4-3dd6-4e8e-9929-4a63dd66992e&outputSchema=http://www.isotc211.org/2005/gmd&elementSetName=full', 1),
(10,'WFS','download_PF','pf:ProductionInstallationPart','dummy','dummy','http://inspire.ec.europa.eu/schemas/pf/3.0rc3', 'http://www.nationaalgeoregister.nl/geonetwork/srv/dut/csw?Service=CSW&Request=GetRecordById&Version=2.0.2&id=3094a1c4-3dd6-4e8e-9929-4a63dd66992e&outputSchema=http://www.isotc211.org/2005/gmd&elementSetName=full', 2),
(11,'WFS','download_PS','gn:NamedPlace','dummy','dummy','urn:x-inspire:specification:gmlas:GeographicalNames:3.0', 'http://www.nationaalgeoregister.nl/geonetwork/srv/dut/csw?Service=CSW&Request=GetRecordById&Version=2.0.2&id=c04fc102-1bb3-497f-83b2-427ddca70cff&outputSchema=http://www.isotc211.org/2005/gmd&elementSetName=full', 0),
(12,'WFS','download_PS','ps:ProtectedSite','dummy','dummy', 'urn:x-inspire:specification:gmlas:ProtectedSites:3.0', 'http://www.nationaalgeoregister.nl/geonetwork/srv/dut/csw?Service=CSW&Request=GetRecordById&Version=2.0.2&id=c04fc102-1bb3-497f-83b2-427ddca70cff&outputSchema=http://www.isotc211.org/2005/gmd&elementSetName=full', 1),
(13,'WMS','view_AM','AM.BathingWaters','dummy','dummy','','',1),
(14,'WMS','view_AM','AM.DrinkingWaterProtectionArea','dummy','dummy','','',2),
(15,'WMS','view_AM','AM.NoiseRestrictionZone','dummy','dummy','','',0),
(16,'WMS','view_AM','AM.ProspectingAndMiningPermitArea','dummy','dummy','','',3),
(17,'WMS','view_AF','AF.BestandVeehouderijbedrijven.bedrijf','dummy','dummy','','',1),
(18,'WMS','view_AF','AF.BestandVeehouderijbedrijven.gebouw','dummy','dummy','','',0),
(19,'WMS','view_AF','AF.BestandVeehouderijbedrijven.emissiepunt','dummy','dummy','','',2),
(20,'WMS','view_EF','EF.Monsterpunt','dummy','dummy','','',0),
(21,'WMS','view_HB','HB.Habitat','dummy','dummy','','',0),
(22,'WMS','view_NZ','NZ.HazardArea','dummy','dummy','','',0),
(23,'WMS','view_PF','PF.ProductionFacility','dummy','dummy','','',0),
(24,'WMS','view_PF','PF.ProductionInstallation','dummy','dummy','','',2),
(25,'WMS','view_PF','PF.ProductionInstallationPart','dummy','dummy','','',1),
(26,'WMS','view_PS','PS.ProtectedSite','dummy','dummy','','',7),
(27,'WMS','view_PS','PS.ProtectedSiteNatureConservation','dummy','dummy','','',8),
(28,'WMS','view_PS','PS.ProtectedSiteArchaeological','dummy','dummy','','',9),
(29,'WMS','view_PS','PS.ProtectedSiteCultural','dummy','dummy','','',10),
(30,'WMS','view_PS','PS.ProtectedSiteEcological','dummy','dummy','','',11),
(31,'WMS','view_PS','PS.ProtectedSiteLandscape','dummy','dummy','','',12),
(32,'WMS','view_PS','PS.ProtectedSiteEnvironment','dummy','dummy','','',13),
(33,'WMS','view_PS','PS.ProtectedSiteGeological','dummy','dummy','','',0),
(34,'WMS','view_PS','PS.ProtectedSiteAardkundigeWaarden','dummy','dummy','','',6),
(35,'WMS','view_PS','PS.ProtectedSiteEcologischeHoofdstructuur','dummy','dummy','','',5),
(36,'WMS','view_PS','PS.ProtectedSiteNationaleLandschappen','dummy','dummy','','',4),
(37,'WMS','view_PS','PS.ProtectedSiteProvincialeMonumenten','dummy','dummy','','',3),
(38,'WMS','view_PS','PS.ProtectedSiteStilteGebieden','dummy','dummy','','',2),
(39,'WMS','view_PS','PS.ProtectedSiteWAVGebieden','dummy','dummy','','',1);


INSERT INTO metadata.spatial_datasetinfo (id, type, service_id, name, code, namespace, index) 
SELECT sdt.id, sdt.service_type, s.id, sdt.feature_name, sdt.code, sdt.namespace, sdt.index FROM metadata.service as s 
INNER JOIN metadata.spatial_datasetinfotemp as sdt on sdt.service_name = s.name;

DELETE FROM metadata.service_datasetmetadata as sdm1
WHERE sdm1.service_id IN (SELECT DISTINCT sdm2.service_id FROM metadata.service_datasetmetadata as sdm2
INNER JOIN metadata.spatial_datasetinfotemp as sdt on sdm2.namespace = sdt.xml_namespace
WHERE sdt.service_type = 'WFS');

INSERT INTO metadata.service_datasetmetadata(service_id, name, namespace, url, index)
SELECT s.id, sdt.feature_name, sdt.xml_namespace, sdt.url, sdt.index FROM metadata.service as s 
INNER JOIN metadata.spatial_datasetinfotemp as sdt on sdt.service_name = s.name
Where sdt.service_type = 'WFS';

DROP TABLE IF EXISTS metadata.spatial_datasetinfotemp;