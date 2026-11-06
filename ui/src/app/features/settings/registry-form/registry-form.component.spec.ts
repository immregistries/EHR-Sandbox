import {of, throwError} from 'rxjs';
import {aRegistry, ComponentHarness, renderComponent, resetFixtureIds} from '../../../../testing';
import {ImmunizationRegistry} from '../../../core/models';
import {ImmunizationRegistryService} from '../../../core/services';
import {RegistryFormComponent} from './registry-form.component';
import {REGISTRY_FORM_CONFIG} from './registry-form.config';

describe('RegistryFormComponent', () => {
  let h: ComponentHarness<RegistryFormComponent>;

  beforeEach(async () => {
    resetFixtureIds();
    h = await renderComponent(RegistryFormComponent, {});
  });

  it('renders the configured sections', () => {
    const titles = h.queryAll<HTMLElement>('app-form-card h3').map((el) => el.textContent?.trim());

    expect(titles).toEqual(REGISTRY_FORM_CONFIG.map((card) => card.title));
  });

  it('requires a nickname', () => {
    expect(h.component.form().get('name')?.hasError('required')).toBeTrue();
  });

  it('offers neither fill random nor lifting required rules', () => {
    // Settings describe local configuration, not data sent to a registry.
    const labels = h.queryAll<HTMLElement>('button').map((b) => b.textContent ?? '');

    expect(labels.some((l) => l.includes('Fill random'))).toBeFalse();
    expect(h.query('p-toggleswitch')).toBeNull();
  });

  it('blocks saving without a nickname', () => {
    let saved = 0;
    h.component.saved.subscribe(() => (saved += 1));

    h.component.submit();

    expect(saved).toBe(0);
  });
});

describe('RegistryFormComponent saving', () => {
  const existing = aRegistry({id: 4, name: 'Localhost', default: false});

  async function render(registry?: ImmunizationRegistry) {
    resetFixtureIds();
    const h = await renderComponent(RegistryFormComponent, {
      ...(registry ? {inputs: {registry}} : {}),
    });
    const service = h.fixture.debugElement.injector.get(ImmunizationRegistryService);
    return {h, service};
  }

  it('creates a registry that has no id yet', async () => {
    const {h, service} = await render();
    const post = spyOn(service, 'postImmRegistry').and.returnValue(of(aRegistry({id: 7})));
    h.component.form().get('name')?.setValue('New IIS');

    h.component.submit();

    expect(post).toHaveBeenCalled();
    expect((post.calls.mostRecent().args[0] as ImmunizationRegistry).name).toBe('New IIS');
  });

  it('updates an existing registry, keeping its id', async () => {
    const {h, service} = await render(existing);
    const put = spyOn(service, 'putImmRegistry').and.returnValue(of(existing));

    h.component.submit();

    expect((put.calls.mostRecent().args[0] as ImmunizationRegistry).id).toBe(4);
  });

  it('sends the default flag from the checkbox, not the form', async () => {
    const {h, service} = await render(existing);
    const put = spyOn(service, 'putImmRegistry').and.returnValue(of(existing));
    h.component.isDefault.set(true);

    h.component.submit();

    expect((put.calls.mostRecent().args[0] as ImmunizationRegistry).default).toBeTrue();
  });

  it('asks for a connection check only when requested', async () => {
    const {h, service} = await render(existing);
    spyOn(service, 'putImmRegistry').and.returnValue(of(existing));
    let checks = 0;
    h.component.checkRequested.subscribe(() => (checks += 1));

    h.component.submit();
    expect(checks).toBe(0);

    h.component.submit(true);
    expect(checks).toBe(1);
  });

  it('surfaces a save failure instead of closing silently', async () => {
    const {h, service} = await render(existing);
    spyOn(service, 'putImmRegistry').and.returnValue(
      throwError(() => ({error: {message: 'Server refused'}})),
    );

    h.component.submit();

    expect(h.component.saveError()).toBe('Server refused');
    expect(h.component.saving()).toBeFalse();
  });

  it('emits the id to delete, leaving confirmation to the page', async () => {
    const {h} = await render(existing);
    const deleted: number[] = [];
    h.component.deleted.subscribe((id) => deleted.push(id));

    h.component.remove();

    expect(deleted).toEqual([4]);
  });
});
