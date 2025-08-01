package org.immregistries.ehr.fhir.Server;

import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.param.TokenParamModifier;
import org.apache.commons.lang3.StringUtils;
import org.immregistries.ehr.api.entities.EhrEntityWithIdentifiers;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.embedabbles.EhrIdentifier;
import org.immregistries.ehr.api.repositories.IIdentifierSearchRepository;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class IdentifierSearchUtil {
    private final static Logger logger = LoggerFactory.getLogger(IdentifierSearchUtil.class);

    /**
     * TODO support modifiers
     *
     * @param theIdentifier
     * @param facility
     * @param searchRepository
     * @return
     */
    @NotNull
    public static Stream<EhrEntityWithIdentifiers> getStream(TokenAndListParam theIdentifier, Facility facility, IIdentifierSearchRepository searchRepository) {
        int size = theIdentifier.size();
        Stream<EhrEntityWithIdentifiers> ehrEntityStream;
        List<TokenParam> tokenParams = new ArrayList<>(5);
        TokenParam tokenForSearch = null;

        for (TokenOrListParam tokenOrListParam : theIdentifier.getValuesAsQueryTokens()) {

            for (TokenParam tokenParam : tokenOrListParam.getValuesAsQueryTokens()) {
                if (tokenForSearch == null && (tokenParam.getModifier() == null || TokenParamModifier.OF_TYPE.equals(tokenParam.getModifier()))) {
                    tokenForSearch = tokenParam;
                } else {
                    tokenParams.add(tokenParam);
                }
            }
        }
        logger.info("TokenAndList {} TokenOrListParam 0 {}", theIdentifier.size(), theIdentifier.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().size());

        /*
         * TODO get first identifier with null modifier ?
         */
        int ini = 0;
        Iterable<EhrEntityWithIdentifiers> ehrEntityIterable;
        if (tokenForSearch != null) {
            ini = 1;
            ehrEntityIterable = getIterableFromSearchOneIdentifier(facility, searchRepository, tokenForSearch);
        } else {
            ehrEntityIterable = searchRepository.findByFacilityId(facility.getId());
        }

        ehrEntityStream = StreamSupport.stream(ehrEntityIterable.spliterator(), false);
        for (int i = ini; i < size; i++) {
            TokenParam tokenParam = theIdentifier.getValuesAsQueryTokens().get(i).getValuesAsQueryTokens().get(0);
            ehrEntityStream = filterStreamWithTokenParam(tokenParam, ehrEntityStream);
        }
        return ehrEntityStream;
    }

    private static Iterable<EhrEntityWithIdentifiers> getIterableFromSearchOneIdentifier(Facility facility, IIdentifierSearchRepository searchRepository, TokenParam tokenParam) {
        Iterable<EhrEntityWithIdentifiers> ehrEntityIterable;
        String value = tokenParam.getValue();
        String system = tokenParam.getSystem();

        if (TokenParamModifier.OF_TYPE.equals(tokenParam.getModifier())) {
            String[] strings = tokenParam.getValue().split("\\|");
            String type = strings[0];
            value = strings[1];
//            logger.info("Of type {}  value: {} system: {} type: {}", tokenParam, value, system, type);

            if (StringUtils.isNoneBlank(tokenParam.getSystem(), tokenParam.getValue())) {
                ehrEntityIterable = searchRepository.findByFacilityIdAndIdentifier(facility.getId(), system, value, type);
            } else if (StringUtils.isNotBlank(tokenParam.getSystem())) {
                ehrEntityIterable = searchRepository.findByFacilityIdAndIdentifierSystemAndIdentifierType(facility.getId(), system, type);
            } else {
                ehrEntityIterable = searchRepository.findByFacilityIdAndIdentifierValueAndIdentifierType(facility.getId(), value, type);
            }
        } else {
            if (StringUtils.isNoneBlank(system, value)) {
                ehrEntityIterable = searchRepository.findByFacilityIdAndIdentifier(facility.getId(), system, value);
            } else if (StringUtils.isNotBlank(system)) {
                ehrEntityIterable = searchRepository.findByFacilityIdAndIdentifierSystem(facility.getId(), system);
            } else {
                ehrEntityIterable = searchRepository.findByFacilityIdAndIdentifierValue(facility.getId(), value);
            }
        }

        return ehrEntityIterable;
    }

    @NotNull
    private static Stream<EhrEntityWithIdentifiers> filterStreamWithTokenParam(TokenParam tokenParam, Stream<EhrEntityWithIdentifiers> ehrEntityStream) {


        Predicate<EhrIdentifier> predicate;

//        if (TokenParamModifier.OF_TYPE.equals(tokenParam.getModifier())) {
//            predicate1 = Predicate.not(predicate1);
//        }

        String system = tokenParam.getSystem();
        String value = tokenParam.getValue();

        Predicate<EhrIdentifier> predicateType = null;
//        https://www.hl7.org/fhir/search.html#modifieroftype
        if (TokenParamModifier.OF_TYPE.equals(tokenParam.getModifier())) {
            String[] strings = tokenParam.getValue().split("\\|");
            String type = strings[0];
            value = strings[1];
//            logger.info("Of type {}  value: {} system: {} type: {}", tokenParam, value, system, type);
            predicateType = ehrIdentifier -> StringUtils.equals(ehrIdentifier.getType(), type);
        }

        
        String finalValue = value;
        if (StringUtils.isNoneBlank(system, value)) {
            predicate = ehrIdentifier ->
                    StringUtils.equals(ehrIdentifier.getSystem(), system)
                            && StringUtils.equals(ehrIdentifier.getValue(), finalValue);
        } else if (StringUtils.isNotBlank(system)) {
            predicate = ehrIdentifier -> StringUtils.equals(ehrIdentifier.getSystem(), system);
        } else {
            predicate = ehrIdentifier -> StringUtils.equals(ehrIdentifier.getValue(), finalValue);
        }

        if (null != predicateType) {
            predicate = predicate.and(predicateType);
        }

        if (TokenParamModifier.NOT.equals(tokenParam.getModifier())) {
            predicate = Predicate.not(predicate);
        }

        final Predicate<EhrIdentifier> finalPredicate = predicate;
        ehrEntityStream = ehrEntityStream
                .filter(ehrEntity -> ehrEntity.getIdentifiers()
                        .stream().anyMatch(finalPredicate));
        return ehrEntityStream;
    }
}
