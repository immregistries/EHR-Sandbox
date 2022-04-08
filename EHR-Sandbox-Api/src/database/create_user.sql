CREATE USER
  'ehr_web'@'localhost' IDENTIFIED WITH mysql_native_password
                                   BY 'SharkBaitHooHaHa';

GRANT ALL ON ehr.* TO 'ehr_web'@'localhost';
