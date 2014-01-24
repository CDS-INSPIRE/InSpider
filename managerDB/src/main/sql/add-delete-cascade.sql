
-- add a constraint to cascade delete attribute mappings when a dataset is deleted
ALTER TABLE manager.attributemapping DROP CONSTRAINT if exists fk194562b2aa794262;
ALTER TABLE manager.attributemapping ADD CONSTRAINT fk194562b2aa794262 FOREIGN KEY (dataset_id)
      REFERENCES manager.dataset (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE;

-- add a constraint to cascade delete datasetfilter when a dataset or filterexpression is deleted  
ALTER TABLE manager.datasetfilter DROP CONSTRAINT fk58e761f0aa794262;
ALTER TABLE manager.datasetfilter DROP CONSTRAINT fk58e761f0c8885408;
ALTER TABLE manager.datasetfilter ADD CONSTRAINT fk58e761f0aa794262 FOREIGN KEY (dataset_id)
      REFERENCES manager.dataset (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE;
ALTER TABLE manager.datasetfilter ADD CONSTRAINT fk58e761f0c8885408 FOREIGN KEY (rootexpression_id)
      REFERENCES manager.filterexpression (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE;
