DROP DATABASE ehr;
CREATE DATABASE ehr;
USE ehr



USE EHR;

CREATE TABLE `clinician` (
  `clinician_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(250) NOT NULL,
  PRIMARY KEY (`clinician_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `ehr_user` (
  `ehr_user_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(250) NOT NULL,
  PRIMARY KEY (`ehr_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `patient` (
  `patient_id` int(11) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `patient_name_last` varchar(250) DEFAULT NULL,
  `patient_name_first` varchar(250) DEFAULT NULL,
  `patient_name_middle` varchar(250) DEFAULT NULL,
  `patient_mother_maiden` varchar(250) DEFAULT NULL,
  `patient_birth_date` date NOT NULL,
  `patient_sex` varchar(250) DEFAULT NULL,
  `patient_race` varchar(250) DEFAULT NULL,
  `patient_address_line1` varchar(250) DEFAULT NULL,
  `patient_address_line2` varchar(250) DEFAULT NULL,
  `patient_address_city` varchar(250) DEFAULT NULL,
  `patient_address_state` varchar(250) DEFAULT NULL,
  `patient_address_zip` varchar(250) DEFAULT NULL,
  `patient_address_country` varchar(250) DEFAULT NULL,
  `patient_address_county_parish` varchar(250) DEFAULT NULL,
  `patient_phone` varchar(250) DEFAULT NULL,
  `patient_email` varchar(250) DEFAULT NULL,
  `patient_ethnicity` varchar(250) DEFAULT NULL,
  `patient_birth_flag` varchar(1) DEFAULT NULL,
  `patient_birth_order` varchar(250) DEFAULT NULL,
  `patient_death_flag` varchar(1) DEFAULT NULL,
  `patient_death_date` date DEFAULT NULL,
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
  PRIMARY KEY (`patient_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `ordering_physician` (
  `ordering_physician_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(250) NOT NULL,
  `buying_date` datetime NOT NULL,
  PRIMARY KEY (`ordering_physician_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `vaccine` (
  `vaccine_id` int(11) NOT NULL AUTO_INCREMENT,
  `ordering_physician_id` int(11) NOT NULL,
  `manufacturer` varchar(250) NOT NULL,
  `creation_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `administered_date` date NOT NULL,
  `vaccine_cvx_code` varchar(250) NOT NULL,
  `vaccine_ndc_code` varchar(250) DEFAULT NULL,
  `vaccine_mvx_code` varchar(250) DEFAULT NULL,
  `administered_amount` varchar(250) DEFAULT NULL,
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
  PRIMARY KEY (`vaccine_id`),
  KEY `ordering_physician_id` (`ordering_physician_id`),
  CONSTRAINT `vaccine_ibfk_1` FOREIGN KEY (`ordering_physician_id`) REFERENCES `ordering_physician` (`ordering_physician_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



CREATE TABLE `user` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(250) NOT NULL,
  `password` varchar(250) NOT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `facility` (
  `facility_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `name` varchar(250) NOT NULL,
  `location` varchar(250) NOT NULL,
  PRIMARY KEY (`facility_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `user_facility_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `logs_of_modifications` (
  `log_id` int(11) NOT NULL AUTO_INCREMENT,
  `modif_type` varchar(250) NOT NULL,
  `modif_date` date NOT NULL,
  PRIMARY KEY (`log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `vaccination_event` (
  `vaccination_event_id` int(11) NOT NULL AUTO_INCREMENT,
  `ehr_user_id` int(11) NOT NULL,
  `patient_id` int(11) NOT NULL,
  `vaccine_id` int(11) NOT NULL,
  `facility_id` int(11) NOT NULL,
  `clinician_id` int(11) NOT NULL,
  `log_id` int(11) NOT NULL,
  PRIMARY KEY (`vaccination_event_id`),
  KEY `patient_id` (`patient_id`),
  KEY `ehr_user_id` (`ehr_user_id`),
  KEY `vaccine_id` (`vaccine_id`),
  KEY `facility_id` (`facility_id`),
  KEY `clinician_id` (`clinician_id`),
  KEY `log_id` (`log_id`),
  CONSTRAINT `vaccination_event_ibfk_1` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`),
  CONSTRAINT `vaccination_event_ibfk_2` FOREIGN KEY (`vaccine_id`) REFERENCES `vaccine` (`vaccine_id`),
  CONSTRAINT `vaccination_event_ibfk_3` FOREIGN KEY (`facility_id`) REFERENCES `facility` (`facility_id`),
  CONSTRAINT `vaccination_event_ibfk_4` FOREIGN KEY (`clinician_id`) REFERENCES `clinician` (`clinician_id`),
  CONSTRAINT `vaccination_event_ibfk_5` FOREIGN KEY (`log_id`) REFERENCES `logs_of_modifications` (`log_id`),
  CONSTRAINT `vaccination_event_ibfk_6` FOREIGN KEY (`ehr_user_id`) REFERENCES `ehr_user` (`ehr_user_id`)
  
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;