CREATE TABLE `revinfo` (
    `rev` INTEGER PRIMARY KEY AUTO_INCREMENT,
    `revtstmp` BIGINT(20) NOT NULL,
    `user` VARCHAR(50) DEFAULT '-1',
    `immunization_registry_id` INTEGER NULL,
    `subscription_id` VARCHAR(50) DEFAULT NULL
);

CREATE TABLE `patient_aud` (
  `rev` INTEGER NOT NULL,
  `revtype` TINYINT NOT NULL,
  `patient_id` int(11) NOT NULL,
  `mrn` varchar(125) NOT NULL,
  `facility_id` int(11) NOT NULL,
  `created_date` datetime NULL,
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
  PRIMARY KEY (`patient_id`,`rev`),
    CONSTRAINT `idfk_patient_revinfo_rev_id`
        FOREIGN KEY (`rev`) REFERENCES `revinfo` (`rev`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE `vaccine_aud` (
  `rev` INTEGER NOT NULL,
  `revtype` TINYINT NOT NULL,
  `vaccine_id` int(11) NOT NULL,
  `created_date` datetime NULL,
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
  PRIMARY KEY (`vaccine_id`,`rev`),
  CONSTRAINT `idfk_vaccination_event_revinfo_rev_id`
    FOREIGN KEY (`rev`) REFERENCES `revinfo` (`rev`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `vaccination_event_aud` (
  `rev` INTEGER NOT NULL,
  `revtype` TINYINT NOT NULL,
  `vaccination_event_id` int(11) NOT NULL,
  `administering_clinician_id` int(11) DEFAULT NULL,
  `entering_clinician_id` int(11) DEFAULT NULL,
  `ordering_clinician_id` int(11) DEFAULT NULL,
  `administering_facility_id` int(11) NOT NULL,
  `patient_id` int(11) NOT NULL,
  `vaccine_id` int(11) NOT NULL,
  `primary_source` BOOLEAN DEFAULT NULL,
  PRIMARY KEY (`vaccination_event_id`,`rev`),
  CONSTRAINT `idfk_vaccine_revinfo_rev_id`
    FOREIGN KEY (`rev`) REFERENCES `revinfo` (`rev`)
);