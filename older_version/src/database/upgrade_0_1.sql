DROP DATABASE ehr;
CREATE DATABASE ehr;

USE ehr;


CREATE TABLE `clinician` (
  `clinician_id` int(11) NOT NULL AUTO_INCREMENT,
  `name_last` varchar(250) NOT NULL,
  `name_middle` varchar(250) NOT NULL,
  `name_first` varchar(250) NOT NULL,
  PRIMARY KEY (`clinician_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE `user` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(250) NOT NULL,
  `password` varchar(250) NOT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `immunization_registry` (
  `immunization_registry_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `iis_url` varchar(250) NOT NULL,
  `iis_username` varchar(250) NOT NULL,
  `iis_facility_id` varchar(250) NOT NULL,
  `iis_password` varchar(250) NOT NULL,
  PRIMARY KEY (`immunization_registry_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `user_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `silo` (
  `silo_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `name_display` varchar(250) NOT NULL,
  PRIMARY KEY (`silo_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `silo_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `facility` (
  `facility_id` int(11) NOT NULL AUTO_INCREMENT,
  `silo_id` int(11) NOT NULL,
  `parent_facility_id` int(11),
  `name_display` varchar(250) NOT NULL,
  `location` varchar(250) NOT NULL,
  PRIMARY KEY (`facility_id`),
  KEY `silo_id` (`silo_id`),
  CONSTRAINT `facility_ibfk_1` FOREIGN KEY (`silo_id`) REFERENCES `silo` (`silo_id`),
  CONSTRAINT `facility_ibfk_2` FOREIGN KEY (`parent_facility_id`) REFERENCES `facility` (`facility_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE `patient` (
  `patient_id` int(11) NOT NULL AUTO_INCREMENT,
  `facility_id` int(11) NOT NULL,
  `silo_id` int(11) NOT NULL,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `birth_date` date NOT NULL,
  `name_last` varchar(250) DEFAULT NULL,
  `name_first` varchar(250) DEFAULT NULL,
  `name_middle` varchar(250) DEFAULT NULL,
  `mother_maiden` varchar(250) DEFAULT NULL,
  `sex` varchar(250) DEFAULT NULL,
  `race` varchar(250) DEFAULT NULL,
  `address_line1` varchar(250) DEFAULT NULL,
  `address_line2` varchar(250) DEFAULT NULL,
  `address_city` varchar(250) DEFAULT NULL,
  `address_state` varchar(250) DEFAULT NULL,
  `address_zip` varchar(250) DEFAULT NULL,
  `address_country` varchar(250) DEFAULT NULL,
  `address_county_parish` varchar(250) DEFAULT NULL,
  `phone` varchar(250) DEFAULT NULL,
  `email` varchar(250) DEFAULT NULL,
  `ethnicity` varchar(250) DEFAULT NULL,
  `birth_flag` varchar(1) DEFAULT NULL,
  `birth_order` varchar(250) DEFAULT NULL,
  `death_flag` varchar(1) DEFAULT NULL,
  `death_date` date DEFAULT NULL,
  `publicity_indicator` varchar(250) DEFAULT NULL,
  `publicity_indicator_date` date DEFAULT NULL,
  `protection_indicator` varchar(250) DEFAULT NULL,
  `protection_indicator_date` date DEFAULT NULL,
  `registry_status_indicator` varchar(250) DEFAULT NULL,
  `registry_status_indicator_date` date DEFAULT NULL,
  `guardian_last` varchar(250) DEFAULT NULL,
  `guardian_first` varchar(250) DEFAULT NULL,
  `guardian_middle` varchar(250) DEFAULT NULL,
  `guardian_relationship` varchar(250) DEFAULT NULL,
  PRIMARY KEY (`patient_id`),
  KEY `facility_id` (`facility_id`),
  KEY `silo_id` (`silo_id`),
  CONSTRAINT `patient_ibfk_1` FOREIGN KEY (`facility_id`) REFERENCES `facility` (`facility_id`),
  CONSTRAINT `patient_ibfk_2` FOREIGN KEY (`silo_id`) REFERENCES `silo` (`silo_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `next_of_kin` (
  `next_of_kin_id` int(11) NOT NULL AUTO_INCREMENT,
  `patient_id` int(11) NOT NULL,
  `birth_date` date NOT NULL,
  `name_last` varchar(250) DEFAULT NULL,
  `name_first` varchar(250) DEFAULT NULL,
  `name_middle` varchar(250) DEFAULT NULL,
  `mother_maiden` varchar(250) DEFAULT NULL,
  `sex` varchar(250) DEFAULT NULL,
  `race` varchar(250) DEFAULT NULL,
  `address_line1` varchar(250) DEFAULT NULL,
  `address_line2` varchar(250) DEFAULT NULL,
  `address_city` varchar(250) DEFAULT NULL,
  `address_state` varchar(250) DEFAULT NULL,
  `address_zip` varchar(250) DEFAULT NULL,
  `address_country` varchar(250) DEFAULT NULL,
  `address_county_parish` varchar(250) DEFAULT NULL,
  `phone` varchar(250) DEFAULT NULL,
  `email` varchar(250) DEFAULT NULL,
  `ethnicity` varchar(250) DEFAULT NULL,
  PRIMARY KEY (`next_of_kin_id`),
  KEY `patient_id` (`patient_id`),
  CONSTRAINT `next_of_kin_ibfk_1` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `vaccine` (
  `vaccine_id` int(11) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `administered_date` date NOT NULL,
  `vaccine_cvx_code` varchar(250) NOT NULL,
  `vaccine_ndc_code` varchar(250) DEFAULT NULL,
  `vaccine_mvx_code` varchar(250) DEFAULT NULL,
  `administered_amount` varchar(250) DEFAULT NULL,
  `manufacturer` varchar(250) DEFAULT NULL,
  `information_source` varchar(250) DEFAULT NULL,
  `lot_number` varchar(250) DEFAULT NULL,
  `expiration_date` date DEFAULT NULL,
  `completion_status` varchar(250) DEFAULT NULL,
  `action_code` varchar(250) DEFAULT NULL,
  `refusal_reason_code` varchar(250) DEFAULT NULL,
  `body_site` varchar(250) DEFAULT NULL,
  `body_route` varchar(250) DEFAULT NULL,
  `funding_source` varchar(250) DEFAULT NULL,
  `funding_eligibility` varchar(250) DEFAULT NULL,
  PRIMARY KEY (`vaccine_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `logs_of_modifications` (
  `log_id` int(11) NOT NULL AUTO_INCREMENT,
  `modif_type` varchar(250) NOT NULL,
  `modif_date` date NOT NULL,
  PRIMARY KEY (`log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `vaccination_event` (
  `vaccination_event_id` int(11) NOT NULL AUTO_INCREMENT,
  `administering_clinician_id` int(11) NOT NULL,
  `entering_clinician_id` int(11) NOT NULL,
  `ordering_clinician_id` int(11) NOT NULL,
  `administering_facility_id` int(11) NOT NULL,
  `patient_id` int(11) NOT NULL,
  `vaccine_id` int(11) NOT NULL,
  `log_id` int(11) NOT NULL,
  PRIMARY KEY (`vaccination_event_id`),
  KEY `patient_id` (`patient_id`),
  KEY `administering_clinician_id` (`administering_clinician_id`),
  KEY `entering_clinician_id` (`entering_clinician_id`),
  KEY `ordering_clinician_id` (`ordering_clinician_id`),
  KEY `administering_facility_id` (`administering_facility_id`),
  KEY `vaccine_id` (`vaccine_id`),
  KEY `log_id` (`log_id`),
  CONSTRAINT `vaccination_event_ibfk_1` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`),
  CONSTRAINT `vaccination_event_ibfk_2` FOREIGN KEY (`entering_clinician_id`) REFERENCES `clinician` (`clinician_id`),
  CONSTRAINT `vaccination_event_ibfk_3` FOREIGN KEY (`ordering_clinician_id`) REFERENCES `clinician` (`clinician_id`),
  CONSTRAINT `vaccination_event_ibfk_4` FOREIGN KEY (`administering_clinician_id`) REFERENCES `clinician` (`clinician_id`),
  CONSTRAINT `vaccination_event_ibfk_5` FOREIGN KEY (`administering_facility_id`) REFERENCES `facility` (`facility_id`),
  CONSTRAINT `vaccination_event_ibfk_6` FOREIGN KEY (`vaccine_id`) REFERENCES `vaccine` (`vaccine_id`),
  CONSTRAINT `vaccination_event_ibfk_7` FOREIGN KEY (`log_id`) REFERENCES `logs_of_modifications` (`log_id`)
  
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;