package org.immregistries.ehr.logic;

import com.github.javafaker.Faker;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.immregistries.codebase.client.CodeMap;
import org.immregistries.codebase.client.generated.Code;
import org.immregistries.codebase.client.generated.LinkTo;
import org.immregistries.codebase.client.reference.CodesetType;
import org.immregistries.ehr.CodeMapManager;
import org.immregistries.ehr.api.entities.*;
import org.immregistries.ehr.api.entities.embedabbles.EhrAddress;
import org.immregistries.ehr.api.entities.embedabbles.EhrIdentifier;
import org.immregistries.ehr.api.entities.embedabbles.EhrPhoneNumber;
import org.immregistries.ehr.api.entities.embedabbles.EhrRace;
import org.immregistries.ehr.api.repositories.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.min;
import static java.lang.Math.random;
import static org.immregistries.codebase.client.reference.CodesetType.VACCINATION_NDC_CODE_UNIT_OF_USE;
import static org.immregistries.ehr.logic.mapping.PatientMapperR5.MRN_SYSTEM;

@Service
public class RandomGenerator {
    private static final Logger logger = LoggerFactory.getLogger(RandomGenerator.class);
    @Autowired
    CodeMapManager codeMapManager;

    @Autowired
    private ResourceIdentificationService resourceIdentificationService;
    @Autowired
    private TenantRepository tenantRepository;

    private static Date between(Date startInclusive, Date endExclusive) {
        long startMillis = startInclusive.getTime();
        long endMillis = endExclusive.getTime();
        long randomMillisSinceEpoch = ThreadLocalRandom
                .current()
                .nextLong(startMillis, endMillis);

        return new Date(randomMillisSinceEpoch);
    }

    public EhrPatient randomPatient(Facility facility) {
        Faker faker = new Faker();

        int randDay = (int) (Math.random() * 30 + 1);
        int randMonth = (int) (Math.random() * 11);
        int randYear = (int) (Math.random() * 121 + 1900);

        Random rand = new Random();

        long aDay = TimeUnit.DAYS.toMillis(1);
        long now = new Date().getTime();
        Date twoYearsAgo = new Date(now - aDay * 365 * 2);

        Date eightyYearsAgo = new Date(now - aDay * 365 * 80);
        Date fourtyYearsAgo = new Date(now - aDay * 365 * 40);
        Date tenDaysAgo = new Date(now - aDay * 10);
        Date fourYearsAgo = new Date(now - aDay * 365 * 4);

        CodeMap codeMap = codeMapManager.getCodeMap();

        Date birthDate = between(eightyYearsAgo, tenDaysAgo);
        Date deathDate = between(fourYearsAgo, tenDaysAgo);
        Date publicityIndicatorDate = between(twoYearsAgo, tenDaysAgo);
        Date protectionIndicatorDate = between(twoYearsAgo, tenDaysAgo);
        Date registryStatusIndicatorDate = between(twoYearsAgo, tenDaysAgo);
        Date regStatusDate = between(twoYearsAgo, tenDaysAgo);

        String lastname = faker.name().lastName();
        int length = lastname.length();
        String mrn = lastname.substring(0, min(length, 4))
                + RandomStringUtils.random(11, true, true);
        String mrnSystem;
        if (facility != null) {
            mrnSystem = resourceIdentificationService.getFacilityPatientIdentifierSystem(facility);
        } else {
            mrnSystem = MRN_SYSTEM;
        }


        EhrPatient ehrPatient = new EhrPatient();
        ehrPatient.setFacility(facility);

        ehrPatient.setNameFirst(faker.name().firstName());
        ehrPatient.setNameLast(lastname);
        ehrPatient.setNameMiddle(faker.name().firstName());

        EhrIdentifier ehrIdentifier = new EhrIdentifier();
        ehrIdentifier.setType("MR");
        ehrIdentifier.setValue(mrn);
        ehrIdentifier.setSystem(mrnSystem);
        ehrPatient.getIdentifiers().add(ehrIdentifier);

        ehrPatient.setBirthDate(birthDate);


        EhrAddress ehrAddress = randomAddress();
        ehrPatient.addAddress(ehrAddress);

        EhrPhoneNumber phoneNumber = randomPhoneNumber();
        ehrPatient.addPhoneNumber(phoneNumber);
        ehrPatient.setEmail(ehrPatient.getNameFirst() + randDay + "@email.com");

        ehrPatient.setBirthFlag("");
        ehrPatient.setBirthOrder("");

        ehrPatient.setDeathFlag("");
        int randomDecision = rand.nextInt(100);
        if (randomDecision < 30) {
            ehrPatient.setDeathDate(deathDate);
        }

        Collection<Code> codeListEthnicity = codeMap.getCodesForTable(CodesetType.PATIENT_ETHNICITY);
        Collection<Code> codeListRace = codeMap.getCodesForTable(CodesetType.PATIENT_RACE);
        Collection<Code> codeListSex = codeMap.getCodesForTable(CodesetType.PATIENT_SEX);

        /**
         * Sex
         */
        {
            int count = 0;
            int randomNumber = (int) (Math.random() * codeListSex.size());
            for (Code code : codeListSex) {
                ehrPatient.setSex(code.getValue());
                count += 1;
                if (randomNumber == count) {
                    break;
                }
            }
        }

        /**
         * Ethnicity
         */
        {
            int count = 0;
            int randomNumber = (int) (Math.random() * codeListEthnicity.size());
            for (Code code : codeListEthnicity) {
                ehrPatient.setEthnicity(code.getValue());
                count += 1;
                if (randomNumber == count) {
                    break;
                }
            }
        }
        /**
         * Race
         */
        {
            int count = 0;
            int randomNumber = (int) (Math.random() * codeListRace.size());
            for (Code code : codeListRace) {
                count += 1;
                if (randomNumber == count) {
                    ehrPatient.addRace(new EhrRace(code.getValue()));
                    break;
                }
            }
        }
//        ehrPatient.setMotherMaiden(faker.name().lastName());

        Collection<Code> codeListGuardian = codeMap.getCodesForTable(CodesetType.PERSON_RELATIONSHIP);
        NextOfKinRelationship nextOfKinRelationship = new NextOfKinRelationship(ehrPatient, randomNextOfKin());
        nextOfKinRelationship.setRelationshipKind("MTH");
        ehrPatient.addNextOfKinRelationship(nextOfKinRelationship);
//        int count = 0;

//        for(Code code : codeListGuardian) {
//            ehrPatient.setGuardianRelationship(code.getValue());
//            count+=1;
//            if(randDay==count) {
//                break;
//            }
//        }

        ehrPatient.setProtectionIndicator("");
        {
            int randomNumber = (int) (Math.random() * 3);
            if (randomNumber > 2) {
                ehrPatient.setProtectionIndicator("Y");
            } else if (randomNumber > 1) {
                ehrPatient.setProtectionIndicator("N");
            }
        }
        if (StringUtils.isNotBlank(ehrPatient.getProtectionIndicator())) {
            ehrPatient.setProtectionIndicatorDate(protectionIndicatorDate);
        }
        ehrPatient.setPublicityIndicator("");
        if (StringUtils.isNotBlank(ehrPatient.getPublicityIndicator())) {
            ehrPatient.setPublicityIndicatorDate(publicityIndicatorDate);
        }
        ehrPatient.setRegistryStatusIndicator("");
        if (StringUtils.isNotBlank(ehrPatient.getRegistryStatusIndicator())) {
            ehrPatient.setRegistryStatusIndicatorDate(registryStatusIndicatorDate);
        }

        ehrPatient.setUpdatedDate(new Date());
        ehrPatient.setCreatedDate(new Date());
        return ehrPatient;
    }

    public NextOfKin randomNextOfKin() {
        Faker faker = new Faker();
        NextOfKin nextOfKin = new NextOfKin();
        nextOfKin.setNameFirst(faker.name().firstName());
        nextOfKin.setNameLast(faker.name().lastName());
        nextOfKin.setNameMiddle(faker.name().firstName());
//        nextOfKin.setRelationship("MTH");
        return nextOfKin;
    }

    public VaccinationEvent randomVaccinationEvent(EhrPatient patient, Tenant tenant, Facility facility) {
        VaccinationEvent vaccinationEvent = new VaccinationEvent();
        vaccinationEvent.setPatient(patient);
        vaccinationEvent.setAdministeringFacility(facility);

        Vaccine vaccine = randomVaccine();
        vaccinationEvent.setVaccine(vaccine);
        vaccinationEvent.setPrimarySource(true);

        vaccinationEvent.setEnteringClinician(randomClinician(tenant));
        vaccinationEvent.setOrderingClinician(randomClinician(tenant));
        vaccinationEvent.setAdministeringClinician(randomClinician(tenant));
        return vaccinationEvent;
    }

    public Vaccine randomVaccine() {
        Date currentDate = new Date();
        Date instant = new Date();
        int randomN = (int) (Math.random() * 9);
        int randDay = (int) (Math.random() * 31);
        int randMonth = (int) (Math.random() * 11);
        int randYear = (int) (Math.random() * 20);
        Date randomDate = new Date((int) (currentDate.getYear() + randYear + 1), randMonth, randDay);

        Vaccine vaccine = new Vaccine();

        vaccine.setAdministeredDate(currentDate);
        vaccine.setCreatedDate(currentDate);
        vaccine.setExpirationDate(randomDate);
        vaccine.setUpdatedDate(currentDate);


        vaccine.setAdministeredAmount(randomN + ".5");
        vaccine.setActionCode("A");
        vaccine.setCompletionStatus("CP");
        vaccine.setLotNumber(Integer.toString(randomN));
        vaccine.setRefusalReasonCode("");

        CodeMap codeMap = codeMapManager.getCodeMap();
        Collection<Code> codeListCVX = codeMap.getCodesForTable(CodesetType.VACCINATION_CVX_CODE);
        Collection<Code> codeListMVX = codeMap.getCodesForTable(CodesetType.VACCINATION_MANUFACTURER_CODE);
        Collection<Code> codeListNDC = codeMap.getCodesForTable(VACCINATION_NDC_CODE_UNIT_OF_USE);
        Collection<Code> codeListInfSource = codeMap.getCodesForTable(CodesetType.VACCINATION_INFORMATION_SOURCE);
        Collection<Code> codeListBodyRoute = codeMap.getCodesForTable(CodesetType.BODY_ROUTE);
        Collection<Code> codeListBodySite = codeMap.getCodesForTable(CodesetType.BODY_SITE);
        Collection<Code> codeListFundingSource = codeMap.getCodesForTable(CodesetType.VACCINATION_FUNDING_SOURCE);
        Collection<Code> codeListFinancialStatus = codeMap.getCodesForTable(CodesetType.FINANCIAL_STATUS_CODE);

        int randomIndex = (int) (random() * codeListCVX.size());
        Code cvxCode = randomCode(codeListCVX);
        vaccine.setVaccineCvxCode(cvxCode.getValue());

        Code ndcCode = null;
        if (cvxCode.getReference() != null && !cvxCode.getReference().getLinkTo().isEmpty()) {
            Optional<LinkTo> optionalLinkTo = cvxCode.getReference().getLinkTo().stream().filter((linkTo -> linkTo.getCodeset().equals(CodesetType.VACCINATION_NDC_CODE_UNIT_OF_USE.name()))).findFirst();
            if (optionalLinkTo.isPresent()) {
                ndcCode = codeListNDC.stream().filter((code -> code.getValue().equals(optionalLinkTo.get().getValue()))).findFirst().get();
            } else {
//                ndcCode
            }
        }

        if (ndcCode != null) {
            vaccine.setVaccineNdcCode(ndcCode.getValue());
        }

        Code mvxCode = randomCode(codeListMVX);
        vaccine.setVaccineMvxCode(mvxCode.getValue());

        Code bodyRouteCode = randomCode(codeListBodyRoute);
        vaccine.setBodyRoute(bodyRouteCode.getValue());


        Code bodyRouteSite = randomCode(codeListBodySite);
        vaccine.setBodySite(bodyRouteSite.getValue());

        vaccine.setFinancialStatus(randomCode(codeListFinancialStatus).getValue());

        vaccine.setInformationSource("00");

        Code fundingSourceCode = randomCode(codeListFundingSource);
        vaccine.setFundingSource(fundingSourceCode.getValue());


        return vaccine;
    }

    public Clinician randomClinician(Integer tenantId) {
        return randomClinician(tenantRepository.findById(tenantId).get());
    }

    public Clinician randomClinician(Tenant tenant) {
        Faker faker = new Faker();
        Clinician clinician = new Clinician();
        clinician.setTenant(tenant);
        clinician.setNameFirst(faker.name().firstName());
        clinician.setNameLast(faker.name().lastName());
        clinician.setNameMiddle(faker.name().firstName());

        clinician.setQualification("MD");
        double randomNumber = random();
        if (randomNumber > 0.8) {
            clinician.setQualification("RN");
        }
        if (randomNumber > 0.6) {
            clinician.setQualification("MA");
        }
        if (randomNumber > 0.4) {
            clinician.setQualification("PN");
        }
        if (randomNumber > 0.2) {
            clinician.setQualification("NP");
        }
        EhrIdentifier ehrIdentifier = new EhrIdentifier();
        ehrIdentifier.setValue(RandomStringUtils.random(15, true, true));
        clinician.getIdentifiers().add(ehrIdentifier);
        EhrAddress ehrAddress = randomAddress();
        clinician.addAddress(ehrAddress);

        EhrPhoneNumber ehrPhoneNumber = randomPhoneNumber();
        clinician.addPhoneNumber(ehrPhoneNumber);
        return clinician;
    }

    private EhrPhoneNumber randomPhoneNumber() {
        Faker faker = new Faker();
        EhrPhoneNumber phoneNumber = new EhrPhoneNumber(faker.phoneNumber().extension() + faker.phoneNumber().subscriberNumber(6));
        phoneNumber.setUse("PRN");
        phoneNumber.setType("CP");
        return phoneNumber;

    }

    private EhrAddress randomAddress() {
        Faker faker = new Faker();
        EhrAddress ehrAddress = new EhrAddress();
        ehrAddress.setAddressLine1(faker.address().streetAddress());
        ehrAddress.setAddressCity(faker.address().city());
        ehrAddress.setAddressCountry("USA");
        ehrAddress.setAddressState(faker.address().stateAbbr());
        ehrAddress.setAddressZip(faker.address().zipCode());
        try {
            ehrAddress.setAddressCountyParish(faker.address().countyByZipCode(ehrAddress.getAddressZip()));
        } catch (RuntimeException e) {

        }
        return ehrAddress;
    }

    private Code randomCode(Collection<Code> collection) {
        return collection.stream().skip((long) (random() * collection.size())).findFirst().get();
    }
}
