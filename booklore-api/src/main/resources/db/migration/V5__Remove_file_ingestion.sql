DROP TABLE IF EXISTS bookdrop_file;

ALTER TABLE task_cron_configuration DROP CONSTRAINT IF EXISTS task_cron_configuration_task_type_check;
DELETE FROM task_cron_configuration WHERE task_type = 'BOOKDROP_PERIODIC_SCANNING';
ALTER TABLE task_cron_configuration ADD CONSTRAINT task_cron_configuration_task_type_check CHECK (task_type IN ('LIBRARY_RESCAN','UPDATE_BOOK_RECOMMENDATIONS','CLEANUP_DELETED_BOOKS','SYNC_LIBRARY_FILES'));

ALTER TABLE tasks DROP CONSTRAINT IF EXISTS tasks_type_check;
DELETE FROM tasks WHERE type = 'BOOKDROP_PERIODIC_SCANNING';
ALTER TABLE tasks ADD CONSTRAINT tasks_type_check CHECK (type IN ('LIBRARY_RESCAN','UPDATE_BOOK_RECOMMENDATIONS','CLEANUP_DELETED_BOOKS','SYNC_LIBRARY_FILES'));

ALTER TABLE user_permissions DROP COLUMN IF EXISTS permission_upload;
ALTER TABLE user_permissions DROP COLUMN IF EXISTS permission_access_bookdrop;
