import {Feedback} from '../../core/models';
import {FormCard, FormType, hl7LocationLabel, hl7LocationMatches} from './form-config';
import {collectWarnings, unmatchedWarnings} from './form-warnings';

const CONFIG: FormCard<Record<string, unknown>>[] = [
  {
    title: 'Main',
    fields: [
      {
        type: FormType.date,
        title: 'Birth date',
        attributeName: 'birthDate',
        hl7Location: {segmentId: 'PID', componentNumber: 7},
      },
      {
        type: FormType.list,
        title: 'Name',
        attributeName: 'names',
        itemFields: [
          {
            type: FormType.text,
            title: 'Last',
            attributeName: 'nameLast',
            hl7Location: {segmentId: 'PID', fieldPosition: 5, componentNumber: 1},
          },
        ],
      },
    ],
  },
];

function feedback(partial: Partial<Feedback>): Feedback {
  return {content: 'Problem', severity: 'WARN', ...partial};
}

describe('form warnings', () => {
  it('attaches feedback to the field at its HL7 location', () => {
    const warnings = collectWarnings(CONFIG, [
      feedback({content: 'Birth date in the future', hl7Locations: [{segmentId: 'PID', componentNumber: 7}]}),
    ]);

    expect(warnings['birthDate']).toEqual(['WARN: Birth date in the future']);
  });

  it('attaches feedback to the right entry of a repeating field', () => {
    const warnings = collectWarnings(CONFIG, [
      feedback({
        content: 'Last name missing',
        hl7Locations: [{segmentId: 'PID', fieldPosition: 5, componentNumber: 1, fieldRepetition: 2}],
      }),
    ]);

    const names = warnings['names'] as Record<number, Record<string, string[]>>;

    // Repetitions are 1-based in HL7; the second entry is index 1.
    expect(names[1]['nameLast']).toEqual(['WARN: Last name missing']);
  });

  it('treats an absent repetition as the first entry', () => {
    const warnings = collectWarnings(CONFIG, [
      feedback({hl7Locations: [{segmentId: 'PID', fieldPosition: 5, componentNumber: 1}]}),
    ]);

    const names = warnings['names'] as Record<number, Record<string, string[]>>;

    expect(names[0]['nameLast'].length).toBe(1);
  });

  it('ignores feedback for a different segment', () => {
    const warnings = collectWarnings(CONFIG, [
      feedback({hl7Locations: [{segmentId: 'RXA', componentNumber: 7}]}),
    ]);

    expect(warnings).toEqual({});
  });

  it('reports feedback that matches no field, rather than losing it', () => {
    const orphan = feedback({hl7Locations: [{segmentId: 'ORC', fieldPosition: 3}]});

    expect(unmatchedWarnings(CONFIG, [orphan])).toEqual([orphan]);
  });

  it('returns nothing when there is no feedback', () => {
    expect(collectWarnings(CONFIG, [])).toEqual({});
  });
});

describe('hl7 locations', () => {
  it('renders the PID-5.1 notation used in feedback', () => {
    expect(hl7LocationLabel({segmentId: 'PID', fieldPosition: 5, componentNumber: 1})).toBe('PID-5.1');
    expect(hl7LocationLabel({segmentId: 'PID', componentNumber: 7})).toBe('PID-7');
    expect(hl7LocationLabel(undefined)).toBe('');
  });

  it('matches locations that address the same place', () => {
    expect(
      hl7LocationMatches(
        {segmentId: 'PID', fieldPosition: 5, componentNumber: 1},
        {segmentId: 'PID', fieldPosition: 5, componentNumber: 1},
      ),
    ).toBeTrue();
  });

  it('does not match across segments', () => {
    expect(
      hl7LocationMatches({segmentId: 'PID', fieldPosition: 5}, {segmentId: 'NK1', fieldPosition: 5}),
    ).toBeFalse();
  });
});
