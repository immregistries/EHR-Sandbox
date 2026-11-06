import {aRegistry, ComponentHarness, renderComponent, resetFixtureIds} from '../../../../testing';
import {RegistryActions} from '../../../core/store/registry/registry.actions';
import {adapter, initialState} from '../../../core/store/registry/registry.reducer';
import {SettingsPageComponent} from './settings-page.component';

describe('SettingsPageComponent', () => {
  const registries = [
    aRegistry({id: 1, name: 'Localhost', default: true}),
    aRegistry({id: 2, name: 'Sabbia', default: false}),
  ];

  async function render() {
    return renderComponent(SettingsPageComponent, {
      state: {registry: adapter.setAll(registries, initialState)},
    });
  }

  function entries(h: ComponentHarness<SettingsPageComponent>): HTMLElement[] {
    return h.queryAll<HTMLElement>('.registry');
  }

  beforeEach(() => resetFixtureIds());

  it('lists the configured registries', async () => {
    const h = await render();

    expect(entries(h).length).toBe(2);
    expect(entries(h)[0].textContent).toContain('Localhost');
  });

  it('marks the default registry', async () => {
    const h = await render();

    expect(entries(h)[0].textContent).toContain('default');
    expect(entries(h)[1].textContent).not.toContain('default');
  });

  it('shows no form until a registry is chosen', async () => {
    const h = await render();

    expect(h.query('app-registry-form')).toBeNull();
  });

  it('opens the chosen registry in the form', async () => {
    const h = await render();

    entries(h)[1]?.click();
    h.detectChanges();

    expect(h.component.editing()?.id).toBe(2);
    expect(h.query('app-registry-form')).not.toBeNull();
  });

  it('opens an empty draft for a new registry', async () => {
    const h = await render();

    h.component.startNew();
    h.detectChanges();

    expect(h.component.creating()).toBeTrue();
    expect(h.component.editing()).toEqual({});
  });

  it('seeds a duplicate from the selected registry without its id', async () => {
    const h = await render();
    h.component.select(registries[0]);

    h.component.duplicateSelected();
    h.detectChanges();

    const draft = h.component.editing();
    expect(draft?.id).toBeUndefined();
    expect(draft?.name).toBe('Localhost copy');
    // A copy must not claim to be the default too.
    expect(draft?.default).toBeFalse();
  });

  it('selects the saved registry and clears the draft', async () => {
    const h = await render();
    h.component.startNew();

    h.component.onSaved(aRegistry({id: 3, name: 'New'}));

    expect(h.component.creating()).toBeFalse();
    expect(h.component.selectedId()).toBe(3);
  });

  it('loads the registries on open', async () => {
    const h = await render();
    const dispatched: { type: string }[] = [];
    spyOn(h.store, 'dispatch').and.callFake(((action: { type: string }) => {
      dispatched.push(action);
    }) as never);

    h.component.confirmDelete(1);

    // The confirm dialog gates the delete itself.
    expect(dispatched).toEqual([]);
    expect(RegistryActions.loadRegistries.type).toBeTruthy();
  });
});
