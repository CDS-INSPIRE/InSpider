
-- GRANT METADATA SCHEMA
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON metadata.Service TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON metadata.ExtendedCapabilities TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON metadata.ServiceIdentification TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON metadata.ServiceProvider TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON metadata.service_datasetmetadata TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON metadata.si_accessconstraint TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON metadata.si_keyword TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON metadata.si_version TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON metadata.sp_deliverypoint TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON metadata.sp_emailaddress TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON metadata.sp_faxnumber TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON metadata.sp_phonenumber TO inspire;

GRANT USAGE, SELECT, UPDATE ON SEQUENCE metadata.hibernate_sequence TO inspire;

GRANT USAGE ON SCHEMA metadata TO inspire;
-- ----------------------------------
