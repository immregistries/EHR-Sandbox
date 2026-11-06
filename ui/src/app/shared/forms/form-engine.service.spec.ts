import {TestBed} from '@angular/core/testing';
import {FormArray, FormGroup, Validators} from '@angular/forms';
import {FormCard, FormType} from './form-config';
import {FormEngineService} from './form-engine.service';

interface Sample {
  name?: string;
  birthDate?: Date;
  names?: { nameFirst?: string; nameLast?: string }[];
  kin?: { nameFirst?: string; relationshipKind?: string }[];
}

const CONFIG: FormCard<Sample>[] = [
  {
    title: 'Main',
    fields: [
      {type: FormType.text, title: 'Name', attributeName: 'name', required: true},
      {type: FormType.date, title: 'Birth date', attributeName: 'birthDate'},
      {
        type: FormType.list,
        title: 'Name',
        attributeName: 'names',
        itemDefault: {nameFirst: '', nameLast: ''},
        itemFields: [
          {type: FormType.text, title: 'First', attributeName: 'nameFirst', required: true},
          {type: FormType.text, title: 'Last', attributeName: 'nameLast'},
        ],
      },
      {
        type: FormType.list,
        title: 'Kin',
        attributeName: 'kin',
        itemDefault: {},
        itemFields: [
          // Nested behind `nextOfKin` in the model.
          {type: FormType.text, title: 'First', attributeName: 'nameFirst', path: 'nextOfKin.nameFirst'},
          {type: FormType.text, title: 'Relationship', attributeName: 'relationshipKind'},
        ],
      },
    ],
  },
];

describe('FormEngineService', () => {
  let engine: FormEngineService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    engine = TestBed.inject(FormEngineService);
  });

  it('builds a control per configured field', () => {
    const form = engine.build(CONFIG, {});

    expect(form.get('name')).toBeTruthy();
    expect(form.get('birthDate')).toBeTruthy();
    expect(form.get('names') instanceof FormArray).toBeTrue();
  });

  it('applies required validators from the config', () => {
    const form = engine.build(CONFIG, {});

    expect(form.get('name')?.hasValidator(Validators.required)).toBeTrue();
    expect(form.get('birthDate')?.hasValidator(Validators.required)).toBeFalse();
  });

  it('builds one list entry per item in the value', () => {
    const form = engine.build(CONFIG, {
      names: [{nameFirst: 'Ada', nameLast: 'Lovelace'}, {nameFirst: 'Grace'}],
    });

    const names = form.get('names') as FormArray;

    expect(names.length).toBe(2);
    expect(names.at(0).get('nameLast')?.value).toBe('Lovelace');
  });

  it('reads list entries through their declared path', () => {
    const form = engine.build(CONFIG, {
      kin: [{nextOfKin: {nameFirst: 'Mary'}, relationshipKind: 'MTH'}] as never,
    });

    const kin = form.get('kin') as FormArray;

    expect(kin.at(0).get('nameFirst')?.value).toBe('Mary');
  });

  it('writes list entries back through their declared path', () => {
    const form = engine.build(CONFIG, {
      kin: [{nextOfKin: {nameFirst: 'Mary'}, relationshipKind: 'MTH'}] as never,
    });

    const value = engine.toEntity<Sample>(form, CONFIG) as Record<string, unknown>;
    const kin = (value['kin'] as Record<string, unknown>[])[0];

    expect(kin['nextOfKin']).toEqual({nameFirst: 'Mary'});
    expect(kin['relationshipKind']).toBe('MTH');
  });

  it('drops empty values so the API receives absent fields, not blanks', () => {
    const form = engine.build(CONFIG, {});
    form.get('name')?.setValue('Ada');

    const value = engine.toEntity<Sample>(form, CONFIG) as Record<string, unknown>;

    expect(value['name']).toBe('Ada');
    expect('birthDate' in value).toBeFalse();
  });

  it('lifts required rules without discarding other validators', () => {
    const config: FormCard<Sample>[] = [
      {
        title: 'Main',
        fields: [
          {
            type: FormType.text,
            title: 'Name',
            attributeName: 'name',
            required: true,
            validator: Validators.minLength(3),
          },
        ],
      },
    ];
    const form = engine.build(config, {});

    engine.applyRequiredRules(form, config, true);
    expect(form.get('name')?.hasValidator(Validators.required)).toBeFalse();

    form.get('name')?.setValue('ab');
    expect(form.get('name')?.errors?.['minlength']).toBeTruthy();
  });

  it('restores required rules when the toggle is turned back off', () => {
    const form = engine.build(CONFIG, {});

    engine.applyRequiredRules(form, CONFIG, true);
    engine.applyRequiredRules(form, CONFIG, false);

    expect(form.get('name')?.hasValidator(Validators.required)).toBeTrue();
  });

  it('lifts required rules inside list entries too', () => {
    const form = engine.build(CONFIG, {names: [{nameFirst: ''}]});
    const entry = (form.get('names') as FormArray).at(0) as FormGroup;

    expect(entry.get('nameFirst')?.hasValidator(Validators.required)).toBeTrue();

    engine.applyRequiredRules(form, CONFIG, true);

    expect(entry.get('nameFirst')?.hasValidator(Validators.required)).toBeFalse();
  });

  it('appends an entry seeded from the configured default', () => {
    const form = engine.build(CONFIG, {});
    const names = form.get('names') as FormArray;

    engine.addListItem(names, CONFIG[0].fields[2] as never);

    expect(names.length).toBe(1);
    expect(names.at(0).get('nameFirst')?.value).toBe('');
  });

  it('repopulates an existing form, rebuilding list entries', () => {
    const form = engine.build(CONFIG, {names: [{nameFirst: 'Ada'}]});

    engine.reset(form, CONFIG, {
      name: 'Second',
      names: [{nameFirst: 'Grace'}, {nameFirst: 'Alan'}],
    });

    expect(form.get('name')?.value).toBe('Second');
    expect((form.get('names') as FormArray).length).toBe(2);
    expect(form.pristine).toBeTrue();
  });

  it('clears fields absent from the new value on reset', () => {
    const form = engine.build(CONFIG, {name: 'Ada'});

    engine.reset(form, CONFIG, {});

    expect(form.get('name')?.value).toBeNull();
  });
});

describe('FormEngineService single-entry lists', () => {
  let engine: FormEngineService;

  const CONFIG_SINGLE: FormCard<Sample>[] = [
    {
      title: 'Main',
      fields: [
        {
          type: FormType.list,
          title: 'Name',
          attributeName: 'names',
          singleEntry: true,
          itemDefault: {nameFirst: ''},
          itemFields: [{type: FormType.text, title: 'First', attributeName: 'nameFirst'}],
        },
      ],
    },
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({});
    engine = TestBed.inject(FormEngineService);
  });

  it('always has exactly one entry, even with no value', () => {
    const form = engine.build(CONFIG_SINGLE, {});

    expect((form.get('names') as FormArray).length).toBe(1);
  });

  it('keeps only the first entry when the value has several', () => {
    const form = engine.build(CONFIG_SINGLE, {
      names: [{nameFirst: 'Ada'}, {nameFirst: 'Grace'}],
    });
    const names = form.get('names') as FormArray;

    expect(names.length).toBe(1);
    expect(names.at(0).get('nameFirst')?.value).toBe('Ada');
  });

  it('still emits an array, so the API contract is unchanged', () => {
    const form = engine.build(CONFIG_SINGLE, {names: [{nameFirst: 'Ada'}]});

    const value = engine.toEntity<Sample>(form, CONFIG_SINGLE) as Record<string, unknown>;

    expect(Array.isArray(value['names'])).toBeTrue();
    expect(value['names']).toEqual([{nameFirst: 'Ada'}]);
  });

  it('keeps exactly one entry across a reset', () => {
    const form = engine.build(CONFIG_SINGLE, {});

    engine.reset(form, CONFIG_SINGLE, {});

    expect((form.get('names') as FormArray).length).toBe(1);
  });
});
