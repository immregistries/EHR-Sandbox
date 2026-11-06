import {Injector} from '@angular/core';
import {FormArray, FormBuilder} from '@angular/forms';
import {ComponentHarness, renderComponent} from '../../../testing';
import {FormCard, FormField, FormType} from './form-config';
import {FormEngineService} from './form-engine.service';
import {FormListComponent} from './form-list.component';

function makeEngine(): FormEngineService {
  const injector = Injector.create({
    providers: [{provide: FormBuilder, useValue: new FormBuilder()}, FormEngineService],
  });
  return injector.get(FormEngineService);
}

const IDENTIFIER_FIELD: FormField<never> = {
  type: FormType.list,
  title: 'Identifier',
  attributeName: 'identifiers',
  itemDefault: {value: '', system: '', type: 'MR'},
  itemFields: [
    {type: FormType.text, title: 'Value', attributeName: 'value'},
    {type: FormType.text, title: 'System', attributeName: 'system'},
  ],
} as FormField<never>;

const CONFIG = [{title: 'Identifiers', fields: [IDENTIFIER_FIELD]}] as FormCard<never>[];

/**
 * A repeating field must re-render when the whole form is repopulated —
 * "Fill random" and opening the edit dialog both replace the FormArray's
 * controls wholesale rather than mutating the existing ones.
 */
describe('FormListComponent repopulation', () => {
  function values(h: ComponentHarness<FormListComponent>): string[] {
    return h.queryAll<HTMLInputElement>('input').map((input) => input.value);
  }

  it('renders the entries present when first bound', async () => {
    const engine = makeEngine();
    const form = engine.build(CONFIG, {
      identifiers: [{value: 'MRN-001', system: 'urn:oid:1'}],
    } as never);

    const h = await renderComponent(FormListComponent, {
      inputs: {field: IDENTIFIER_FIELD, array: form.get('identifiers') as FormArray},
    });

    expect(values(h)).toEqual(['MRN-001', 'urn:oid:1']);
  });

  it('re-renders after the form is repopulated with different entries', async () => {
    const engine = makeEngine();
    const form = engine.build(CONFIG, {
      identifiers: [{value: 'MRN-001', system: 'urn:oid:1'}],
    } as never);
    const h = await renderComponent(FormListComponent, {
      inputs: {field: IDENTIFIER_FIELD, array: form.get('identifiers') as FormArray},
    });

    // What "Fill random" does: replace the whole value.
    engine.reset(form, CONFIG, {
      identifiers: [{value: 'RANDOM-999', system: 'urn:oid:9'}],
    } as never);
    h.detectChanges();

    expect(values(h)).toEqual(['RANDOM-999', 'urn:oid:9']);
  });

  it('re-renders when repopulation changes the number of entries', async () => {
    const engine = makeEngine();
    const form = engine.build(CONFIG, {identifiers: [{value: 'MRN-001'}]} as never);
    const h = await renderComponent(FormListComponent, {
      inputs: {field: IDENTIFIER_FIELD, array: form.get('identifiers') as FormArray},
    });
    expect(h.queryAll('.entry').length).toBe(1);

    engine.reset(form, CONFIG, {
      identifiers: [{value: 'A'}, {value: 'B'}, {value: 'C'}],
    } as never);
    h.detectChanges();

    expect(h.queryAll('.entry').length).toBe(3);
  });

  it('empties the list when the new value has no entries', async () => {
    const engine = makeEngine();
    const form = engine.build(CONFIG, {identifiers: [{value: 'MRN-001'}]} as never);
    const h = await renderComponent(FormListComponent, {
      inputs: {field: IDENTIFIER_FIELD, array: form.get('identifiers') as FormArray},
    });

    engine.reset(form, CONFIG, {} as never);
    h.detectChanges();

    expect(h.queryAll('.entry').length).toBe(0);
  });

  it('re-renders after an entry is added', async () => {
    const engine = makeEngine();
    const form = engine.build(CONFIG, {} as never);
    const array = form.get('identifiers') as FormArray;
    const h = await renderComponent(FormListComponent, {
      inputs: {field: IDENTIFIER_FIELD, array},
    });

    h.component.add();
    h.detectChanges();

    expect(h.queryAll('.entry').length).toBe(1);
  });

  it('re-renders after an entry is removed', async () => {
    const engine = makeEngine();
    const form = engine.build(CONFIG, {
      identifiers: [{value: 'A'}, {value: 'B'}],
    } as never);
    const h = await renderComponent(FormListComponent, {
      inputs: {field: IDENTIFIER_FIELD, array: form.get('identifiers') as FormArray},
    });

    h.component.remove(0);
    h.detectChanges();

    expect(h.queryAll('.entry').length).toBe(1);
    expect(values(h)).toEqual(['B', '']);
  });
});

describe('FormListComponent layout', () => {
  it('lays entries out in a single horizontal track', async () => {
    const engine = makeEngine();
    const form = engine.build(CONFIG, {
      identifiers: [{value: 'A'}, {value: 'B'}, {value: 'C'}],
    } as never);

    const h = await renderComponent(FormListComponent, {
      inputs: {field: IDENTIFIER_FIELD, array: form.get('identifiers') as FormArray},
    });

    const tracks = h.queryAll<HTMLElement>('.track');
    expect(tracks.length).toBe(1);
    // Every entry is a child of the one track, so they scroll together.
    expect(tracks[0].querySelectorAll('.entry').length).toBe(3);
  });

  it('keeps the add button beside the track rather than inside it', async () => {
    const engine = makeEngine();
    const form = engine.build(CONFIG, {identifiers: [{value: 'A'}]} as never);

    const h = await renderComponent(FormListComponent, {
      inputs: {field: IDENTIFIER_FIELD, array: form.get('identifiers') as FormArray},
    });

    const actions = h.query<HTMLElement>('.track-actions');
    expect(actions).not.toBeNull();
    expect(actions?.closest('.entry')).toBeNull();
  });

  it('renders no track in single-entry mode', async () => {
    const engine = makeEngine();
    const field = {...IDENTIFIER_FIELD, singleEntry: true} as FormField<never>;
    const config = [{title: 'C', fields: [field]}] as FormCard<never>[];
    const form = engine.build(config, {} as never);

    const h = await renderComponent(FormListComponent, {
      inputs: {field, array: form.get('identifiers') as FormArray},
    });

    expect(h.query('.track')).toBeNull();
    expect(h.query('.entry-grid--single')).not.toBeNull();
  });
});

describe('FormListComponent clear', () => {
  function clearButton(h: ComponentHarness<FormListComponent>): HTMLButtonElement | undefined {
    return h
      .queryAll<HTMLButtonElement>('.list-header button')
      .find((button) => (button.textContent ?? '').includes('Clear'));
  }

  it('shows the list title and entry count in the header', async () => {
    const engine = makeEngine();
    const form = engine.build(CONFIG, {
      identifiers: [{value: 'A'}, {value: 'B'}],
    } as never);

    const h = await renderComponent(FormListComponent, {
      inputs: {field: IDENTIFIER_FIELD, array: form.get('identifiers') as FormArray},
    });

    expect(h.text('.list-title')).toBe('Identifier');
    expect(h.text('.list-count')).toBe('2');
  });

  it('removes every entry when cleared', async () => {
    const engine = makeEngine();
    const form = engine.build(CONFIG, {
      identifiers: [{value: 'A'}, {value: 'B'}, {value: 'C'}],
    } as never);
    const array = form.get('identifiers') as FormArray;

    const h = await renderComponent(FormListComponent, {
      inputs: {field: IDENTIFIER_FIELD, array},
    });

    clearButton(h)?.click();
    h.detectChanges();

    expect(array.length).toBe(0);
    expect(h.queryAll('.entry').length).toBe(0);
  });

  it('leaves the field an empty array, not a removed value', async () => {
    const engine = makeEngine();
    const form = engine.build(CONFIG, {identifiers: [{value: 'A'}]} as never);

    const h = await renderComponent(FormListComponent, {
      inputs: {field: IDENTIFIER_FIELD, array: form.get('identifiers') as FormArray},
    });
    h.component.clear();

    expect(Array.isArray(form.get('identifiers')?.value)).toBeTrue();
    expect(form.get('identifiers')?.value).toEqual([]);
  });

  it('disables clear when the list is already empty', async () => {
    const engine = makeEngine();
    const form = engine.build(CONFIG, {} as never);

    const h = await renderComponent(FormListComponent, {
      inputs: {field: IDENTIFIER_FIELD, array: form.get('identifiers') as FormArray},
    });

    expect(clearButton(h)?.disabled).toBeTrue();
  });

  it('re-enables clear once an entry is added', async () => {
    const engine = makeEngine();
    const form = engine.build(CONFIG, {} as never);

    const h = await renderComponent(FormListComponent, {
      inputs: {field: IDENTIFIER_FIELD, array: form.get('identifiers') as FormArray},
    });
    h.component.add();
    h.detectChanges();

    expect(clearButton(h)?.disabled).toBeFalse();
  });

  it('offers no clear in single-entry mode', async () => {
    const engine = makeEngine();
    const field = {...IDENTIFIER_FIELD, singleEntry: true} as FormField<never>;
    const config = [{title: 'C', fields: [field]}] as FormCard<never>[];
    const form = engine.build(config, {} as never);

    const h = await renderComponent(FormListComponent, {
      inputs: {field, array: form.get('identifiers') as FormArray},
    });

    expect(h.query('.list-header')).toBeNull();
  });
});
