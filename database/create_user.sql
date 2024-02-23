CREATE USER
  'ehr_web' IDENTIFIED WITH mysql_native_password
                                   BY 'SharkBaitHooHaHa';

GRANT ALL ON ehr.* TO 'ehr_web';
