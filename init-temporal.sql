CREATE DATABASE IF NOT EXISTS temporal_visibility;

GRANT ALL PRIVILEGES ON temporal.* TO 'temporal'@'%';
GRANT ALL PRIVILEGES ON temporal_visibility.* TO 'temporal'@'%';
FLUSH PRIVILEGES;
