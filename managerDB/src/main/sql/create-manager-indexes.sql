
-- add indexes for manager schema tables
-- JOB
DROP INDEX IF EXISTS manager.job_id_index;
CREATE INDEX job_id_index ON manager.job (id);
DROP INDEX IF EXISTS manager.job_bronhouder_id_index;
CREATE INDEX job_bronhouder_id_index ON manager.etljob (bronhouder_id);
DROP INDEX IF EXISTS manager.job_datasettype_id_index;
CREATE INDEX job_datasettype_id_index ON manager.etljob (datasettype_id);
DROP INDEX IF EXISTS manager.job_creatietijd_index;
CREATE INDEX job_creatietijd_index ON manager.job (createtime);

-- JOBLOG
DROP INDEX IF EXISTS manager.joblog_id_index;
CREATE INDEX joblog_id_index ON manager.joblog (id);
DROP INDEX IF EXISTS manager.joblog_job_id_index;
CREATE INDEX joblog_job_id_index ON manager.joblog (job_id);
DROP INDEX IF EXISTS manager.joblog_loglevel_index;
CREATE INDEX joblog_loglevel_index ON manager.joblog (loglevel);
-- -----------------------------------
