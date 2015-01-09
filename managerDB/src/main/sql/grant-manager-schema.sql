
-- GRANT MANAGER SCHEMA
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON manager.Bronhouder TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON manager.Dataset TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON manager.DatasetType TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON manager.Job TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON manager.EtlJob TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON manager.JobLog TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON manager.JobType TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON manager.Thema TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON manager.bronhouder_geometry TO inspire;

GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON manager.bronhouderthema TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON manager.codelistmapping TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON manager.datasetfilter TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON manager.filterexpression TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON manager.MetadataDocument TO inspire;


GRANT USAGE, SELECT, UPDATE ON SEQUENCE manager.hibernate_sequence TO inspire;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE manager.joblog_sequence TO inspire;
GRANT SELECT ON manager.Dataset TO nagios;
GRANT SELECT ON manager.Job TO nagios;
GRANT SELECT ON manager.EtlJob TO nagios;
GRANT SELECT ON manager.Bronhouder TO nagios;
GRANT SELECT ON manager.DatasetType TO nagios;
GRANT USAGE ON SCHEMA manager TO inspire;
GRANT USAGE ON SCHEMA manager TO nagios;

GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON manager.AttributeMapping TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON manager.MappingOperation TO inspire;

GRANT SELECT ON manager.AttributeMapping TO nagios;
GRANT SELECT ON manager.MappingOperation TO nagios;

--grant select on manager.job_info to inspire;
grant select on manager.job_validated to inspire;
grant select on manager.job_last to inspire;
grant select on manager.job_last_import to inspire;
grant select on manager.job_last_validate to inspire;
grant select on manager.joblog_overview to inspire;
--grant select on manager.joblog_report to inspire;
--grant select on manager.joblog_report_last to inspire;
--grant select on manager.joblog_report_last_import to inspire;
--grant select on manager.joblog_report_last_validate to inspire;
-- ----------------------------------
