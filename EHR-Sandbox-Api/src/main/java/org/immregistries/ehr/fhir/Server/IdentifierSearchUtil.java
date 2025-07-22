package org.immregistries.ehr.fhir.Server;

import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.param.TokenParamModifier;
import org.apache.commons.lang3.StringUtils;
import org.immregistries.ehr.api.entities.EhrEntityWithIdentifiers;
import org.immregistries.ehr.api.entities.Facility;
import org.immregistries.ehr.api.entities.embedabbles.EhrIdentifier;
import org.immregistries.ehr.api.repositories.IIdentifierSearchRepository;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class IdentifierSearchUtil {

    /**
     * TODO support modifiers
     *
     * @param theIdentifier
     * @param facility
     * @param size
     * @param searchRepository
     * @return
     */
    @NotNull
    public static Stream<EhrEntityWithIdentifiers> getStream(TokenAndListParam theIdentifier, Facility facility, int size, IIdentifierSearchRepository searchRepository) {
        Stream<EhrEntityWithIdentifiers> ehrEntityStream;
        TokenParam identifier = theIdentifier.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0);

        /*
         * TODO get first identifier with null modifier ?
         */
        int ini = 0;
        Iterable<EhrEntityWithIdentifiers> ehrEntityIterable;
        if (identifier.getModifier() == null) {
            ini = 1;
            ehrEntityIterable = getIterableFromSearchOneIdentifier(facility, searchRepository, identifier);
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

    private static Iterable<EhrEntityWithIdentifiers> getIterableFromSearchOneIdentifier(Facility facility, IIdentifierSearchRepository searchRepository, TokenParam identifier) {
        Iterable<EhrEntityWithIdentifiers> ehrEntityIterable;
        if (StringUtils.isNoneBlank(identifier.getSystem(), identifier.getValue())) {
            ehrEntityIterable = searchRepository.findByFacilityIdAndIdentifier(facility.getId(), identifier.getSystem(), identifier.getValue());
        } else if (StringUtils.isNotBlank(identifier.getSystem())) {
            ehrEntityIterable = searchRepository.findByFacilityIdAndIdentifierSystem(facility.getId(), identifier.getSystem());
        } else {
            ehrEntityIterable = searchRepository.findByFacilityIdAndIdentifierValue(facility.getId(), identifier.getValue());
        }
        return ehrEntityIterable;
    }

    @NotNull
    private static Stream<EhrEntityWithIdentifiers> filterStreamWithTokenParam(TokenParam tokenParam, Stream<EhrEntityWithIdentifiers> ehrEntityStream) {
        final Predicate<EhrIdentifier> predicate;
        Predicate<EhrIdentifier> predicate1;
        if (StringUtils.isNoneBlank(tokenParam.getSystem(), tokenParam.getValue())) {
            predicate1 = ehrIdentifier ->
                    StringUtils.equals(ehrIdentifier.getSystem(), tokenParam.getSystem())
                            && StringUtils.equals(ehrIdentifier.getValue(), tokenParam.getValue());
        } else if (StringUtils.isNotBlank(tokenParam.getSystem())) {
            predicate1 = ehrIdentifier -> StringUtils.equals(ehrIdentifier.getSystem(), tokenParam.getSystem());
        } else {
            predicate1 = ehrIdentifier -> StringUtils.equals(ehrIdentifier.getValue(), tokenParam.getValue());
        }

        if (tokenParam.getModifier().equals(TokenParamModifier.NOT)) {
            predicate1 = Predicate.not(predicate1);
        }

        predicate = predicate1;
        ehrEntityStream = ehrEntityStream
                .filter(ehrEntity -> ehrEntity.getIdentifiers()
                        .stream().anyMatch(predicate));
        return ehrEntityStream;
    }
}
