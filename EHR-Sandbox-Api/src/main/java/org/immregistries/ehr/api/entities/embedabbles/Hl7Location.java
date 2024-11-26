package org.immregistries.ehr.api.entities.embedabbles;


import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Embeddable
/**
 * Copied from MQE code to be embedded
 */
public class Hl7Location implements Comparable<Hl7Location> {

    private int line = 0; //-1;
    @Size(max = 5)
    private String segmentId = "";
    private int segmentSequence = 0;
    private int fieldPosition = 0;
    private int fieldRepetition = 1;
    private int componentNumber = 0;
    private int subComponentNumber = 0;

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    private final Pattern p = Pattern.compile("^(MSH|PID|MSA|PD1)");

    boolean expectOneSegmentId() {
        return p.matcher(this.segmentId).find();
    }

    public String getFieldLoc() {
        return segmentId + (segmentSequence > 1 ? "[" + segmentSequence + "]" : "") + (fieldPosition > 0 ? "-" + fieldPosition : "");
    }

    /*
    No field repetitions, or segment indexes.
     */
    public String getBaseReference() {
        return segmentId + "-" + fieldPosition
                + (componentNumber > 0 ? "." + componentNumber
                + (subComponentNumber > 0 ? "." + subComponentNumber : "") : "");
    }

    public Hl7Location(Hl7Location that) {
        this.line = that.line;
        this.segmentId = that.segmentId;
        this.segmentSequence = that.segmentSequence;
        this.fieldPosition = that.fieldPosition;
        this.fieldRepetition = that.fieldRepetition;
        this.componentNumber = that.componentNumber;
        this.subComponentNumber = that.subComponentNumber;
    }

    public String getAbbreviated() {
        String s = segmentId;
        boolean expectSingleSegment = this.expectOneSegmentId();
        if ((expectSingleSegment && segmentSequence > 1)
                || (!expectSingleSegment && segmentSequence >= 1)
        ) {
            s += "[" + segmentSequence + "]";
        }
        if (fieldPosition > 0) {
            s += "-" + fieldPosition;
            if (fieldRepetition > 1) {
                s += "[" + fieldRepetition + "]";
            }
            if (componentNumber > 1 || subComponentNumber > 1) {
                s += "." + componentNumber;
                if (subComponentNumber > 1) {
                    s += "." + subComponentNumber;
                }
            }
        }
        return s;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Hl7Location that = (Hl7Location) o;
        return line == that.line &&
                segmentSequence == that.segmentSequence &&
                fieldPosition == that.fieldPosition &&
                fieldRepetition == that.fieldRepetition &&
                componentNumber == that.componentNumber &&
                subComponentNumber == that.subComponentNumber &&
                Objects.equals(segmentId, that.segmentId);
    }

    @Override
    public int hashCode() {

        return Objects
                .hash(line, segmentId, segmentSequence, fieldPosition, fieldRepetition, componentNumber,
                        subComponentNumber);
    }

    @Override
    public String toString() {
        return segmentId + "[" + (segmentSequence) + "]"
                + "-" + fieldPosition
                + "[" + fieldRepetition + "]"
                + "." + componentNumber
                + "." + subComponentNumber;
    }

    public boolean hasSegmentId() {
        return !StringUtils.isBlank(segmentId);
    }

    public boolean hasFieldPosition() {
        return fieldPosition > 0;
    }

    public boolean hasComponentNumber() {
        return componentNumber > 0;
    }

    public boolean hasSubComponentNumber() {
        return subComponentNumber > 0;
    }

    public Hl7Location() {
        // default
    }

    /**
     * this regex can be seen and tested at
     https://regex101.com/r/i8Jujl/7
     *
     */
//  private static final String regex = "(\\w\\w\\w)\\[?(\\d*)]?-?(\\d*)[\\[~]?(\\d*)]?[.-]?(\\d*)[.-]?(\\d*)";

    /**
     * this regex can be seen and tested at
     * https://regex101.com/r/i8Jujl/8
     * <p>
     * it works with any of these formats:
     * RXA[2]-5[2].5.5
     * RXA#2-5[2].5.5
     * RXA[2]-5#2.5.5
     * RXA#2-5#2.5.5
     */
    private final static String REGEX = "(\\w\\w\\w)\\[?#?(\\d*)]?-?(\\d*)[\\[~#]?(\\d*)]?[.-]?(\\d*)[.-]?(\\d*)";

    private static final Pattern pattern = Pattern.compile(REGEX);

    /**
     * Expected format: segment[sequence]-field[fieldrep].component.subcomponent
     *
     * @param hl7Reference
     */
    public Hl7Location(String hl7Reference, int lineNumber) {
        this(hl7Reference);
        this.line = lineNumber;
    }

    public Hl7Location(String hl7Reference, int lineNumber, int segmentSequence) {
        this(hl7Reference);
        this.line = lineNumber;
        this.segmentSequence = segmentSequence;
    }

    public Hl7Location(String hl7Reference) {
        if (StringUtils.isNotBlank(hl7Reference)) {
            Matcher matcher = pattern.matcher(hl7Reference);
            if (matcher.find()) {
                //got it!
                String segment = matcher.group(1);
                String segIndex = matcher.group(2);
                String field = matcher.group(3);
                String fieldRep = matcher.group(4);
                String component = matcher.group(5);
                String subcomponent = matcher.group(6);

                this.segmentId = segment;
                this.segmentSequence = StringUtils.isNumeric(segIndex) ? Integer.parseInt(segIndex) : 1;
                this.fieldPosition = StringUtils.isNumeric(field) ? Integer.parseInt(field) : 0;
                this.fieldRepetition = StringUtils.isNumeric(fieldRep) ? Integer.parseInt(fieldRep) : 1;
                this.componentNumber = StringUtils.isNumeric(component) ? Integer.parseInt(component) : 1;
                this.subComponentNumber =
                        StringUtils.isNumeric(subcomponent) ? Integer.parseInt(subcomponent) : 1;

            } else {
                //don't got it!
                throw new RuntimeException(
                        "Can't find any HL7 information in location provided : " + hl7Reference);
            }
        }
    }

    public String getSegmentId() {
        return segmentId;
    }

    public void setSegmentId(String segmentId) {
        this.segmentId = segmentId;
    }

    public int getSegmentSequence() {
        return segmentSequence;
    }

    public void setSegmentSequence(int segmentSequence) {
        this.segmentSequence = segmentSequence;
    }

    public int getFieldPosition() {
        return fieldPosition;
    }

    public void setFieldPosition(int fieldPosition) {
        this.fieldPosition = fieldPosition;
    }

    public int getFieldRepetition() {
        return fieldRepetition;
    }

    public void setFieldRepetition(int fieldRepetition) {
        this.fieldRepetition = fieldRepetition;
    }

    public int getComponentNumber() {
        return componentNumber;
    }

    public void setComponentNumber(int componentNumber) {
        this.componentNumber = componentNumber;
    }

    public int getSubComponentNumber() {
        return subComponentNumber;
    }

    public void setSubComponentNumber(int subComponentNumber) {
        this.subComponentNumber = subComponentNumber;
    }

    @Override
    public int compareTo(Hl7Location o) {
        if (o == null) {
            return 1;
        }

        if ((this.line != o.line)) {
            return Integer.compare(this.line, o.line);
        }

        if (!this.segmentId.equals(o.segmentId)) {
            return this.segmentId.compareTo(o.segmentId);
        }

        if (this.segmentSequence != o.segmentSequence) {
//      if (this.segmentId.equals(o.segmentId) && this.segmentSequence != o.segmentSequence) {
            return Integer.compare(this.segmentSequence, o.segmentSequence);
        }

        if (this.fieldPosition != o.fieldPosition) {
            return Integer.compare(this.fieldPosition, o.fieldPosition);
        }

        if (this.fieldRepetition != o.fieldRepetition) {
            return Integer.compare(this.fieldRepetition, o.fieldRepetition);
        }

        if (this.componentNumber != o.componentNumber) {
            return Integer.compare(this.componentNumber, o.componentNumber);
        }

        if (this.subComponentNumber != o.subComponentNumber) {
            return Integer.compare(this.subComponentNumber, o.subComponentNumber);
        }

        return 0;
    }
}
