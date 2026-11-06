import {FormCard, FormType} from './form-config';
import {activeFlavorKeys, applyFlavors} from './form-flavors';

const CONFIG: FormCard<Record<string, unknown>>[] = [
  {
    title: 'Name',
    fields: [
      {
        type: FormType.list,
        title: 'Name',
        attributeName: 'names',
        itemFields: [
          {type: FormType.text, title: 'First', attributeName: 'nameFirst'},
          {type: FormType.code, title: 'Type', attributeName: 'nameType'},
        ],
        flavors: {SINGLENAME: {singleEntry: true, omitItemFields: ['nameType']}},
      },
      {
        type: FormType.text,
        title: 'Mother maiden name',
        attributeName: 'motherMaiden',
        flavors: {NOPARENT: {omit: true}},
      },
      {type: FormType.text, title: 'Email', attributeName: 'email'},
    ],
  },
];

describe('activeFlavorKeys', () => {
  it('reads flavors from the tenant name, as the backend encodes them', () => {
    const active = activeFlavorKeys('SINGLENAME Hospital', ['SINGLENAME', 'PAIN_PERDU']);

    expect([...active]).toEqual(['SINGLENAME']);
  });

  it('matches case-insensitively', () => {
    expect([...activeFlavorKeys('singlename clinic', ['SINGLENAME'])]).toEqual(['SINGLENAME']);
  });

  it('returns nothing for a plain tenant name', () => {
    expect(activeFlavorKeys('General Hospital', ['SINGLENAME']).size).toBe(0);
  });

  it('tolerates a missing tenant name', () => {
    expect(activeFlavorKeys(undefined, ['SINGLENAME']).size).toBe(0);
  });
});

describe('applyFlavors', () => {
  it('returns the config untouched when no flavor is active', () => {
    expect(applyFlavors(CONFIG, new Set())).toBe(CONFIG);
  });

  it('collapses a list to a single entry under SINGLENAME', () => {
    const [card] = applyFlavors(CONFIG, new Set(['SINGLENAME']));

    expect(card.fields[0].singleEntry).toBeTrue();
  });

  it('drops the sub-fields the flavor omits', () => {
    const [card] = applyFlavors(CONFIG, new Set(['SINGLENAME']));

    expect(card.fields[0].itemFields?.map((f) => f.attributeName)).toEqual(['nameFirst']);
  });

  it('leaves other fields alone', () => {
    const [card] = applyFlavors(CONFIG, new Set(['SINGLENAME']));

    expect(card.fields.map((f) => f.attributeName)).toEqual([
      'names',
      'motherMaiden',
      'email',
    ]);
  });

  it('removes a field a flavor omits entirely', () => {
    const [card] = applyFlavors(CONFIG, new Set(['NOPARENT']));

    expect(card.fields.map((f) => f.attributeName)).toEqual(['names', 'email']);
  });

  it('does not mutate the source config', () => {
    applyFlavors(CONFIG, new Set(['SINGLENAME']));

    expect(CONFIG[0].fields[0].itemFields?.length).toBe(2);
    expect(CONFIG[0].fields[0].singleEntry).toBeUndefined();
  });
});
