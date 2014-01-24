
-- change all timestamps from without timezone to timezone 
ALTER TABLE manager.job 
    ALTER COLUMN createtime TYPE timestamp with time zone , 
    ALTER COLUMN starttime TYPE timestamp with time zone , 
    ALTER COLUMN finishtime TYPE timestamp with time zone ;
ALTER TABLE manager.joblog 
    ALTER COLUMN "time" TYPE timestamp with time zone ;

SET TIME ZONE UTC;
-- ---------------------------------------------
