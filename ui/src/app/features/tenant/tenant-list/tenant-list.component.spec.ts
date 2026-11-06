import {Router} from '@angular/router';
import {aTenant, ComponentHarness, renderComponent, resetFixtureIds} from '../../../../testing';
import {adapter, initialState} from '../../../core/store/tenant/tenant.reducer';
import {TenantListComponent} from './tenant-list.component';

describe('TenantListComponent', () => {
  const tenants = [
    aTenant({id: 1, nameDisplay: 'Demo Tenant', alias: 'demo'}),
    aTenant({id: 2, nameDisplay: 'PAIN_PERDU Lab'}),
  ];

  async function render(route: Record<string, number> = {}) {
    return renderComponent(TenantListComponent, {
      state: {
        route,
        tenant: {
          ...adapter.setAll(tenants, initialState),
          flavors: [{key: 'PAIN_PERDU', description: 'Keeps deprecated codes', hidden: false}],
        },
      },
    });
  }

  function rowText(h: ComponentHarness<TenantListComponent>): string[] {
    return h.queryAll<HTMLElement>('tr.tenant-row').map((tr) => (tr.textContent ?? '').trim());
  }

  beforeEach(() => resetFixtureIds());

  it('lists the available tenants', async () => {
    const h = await render();

    expect(rowText(h).length).toBe(2);
    expect(rowText(h)[0]).toContain('Demo Tenant');
  });

  it('derives flavor tags from the tenant name, as the backend does', async () => {
    const h = await render();

    expect(h.component.rows()[0].flavors).toEqual([]);
    expect(h.component.rows()[1].flavors).toEqual(['PAIN_PERDU']);
  });

  it('marks the tenant currently in the url as active', async () => {
    const h = await render({tenantId: 2});

    const active = h.queryAll<HTMLElement>('tr.row--active');

    expect(active.length).toBe(1);
    expect(active[0].textContent).toContain('PAIN_PERDU Lab');
  });

  it('selects a tenant by navigating, so the choice lives in the url', async () => {
    const h = await render();
    const router = h.fixture.debugElement.injector.get(Router);
    const navigate = spyOn(router, 'navigate');

    h.component.select(tenants[1]);

    expect(navigate).toHaveBeenCalledWith(['/t', 2, 'facilities']);
  });

  it('selects a tenant when its row is clicked', async () => {
    const h = await render();
    const router = h.fixture.debugElement.injector.get(Router);
    const navigate = spyOn(router, 'navigate');

    h.queryAll<HTMLElement>('tr.tenant-row')[0]?.click();

    expect(navigate).toHaveBeenCalledWith(['/t', 1, 'facilities']);
  });

  it('declares a sortable field for every data column', async () => {
    const h = await render();

    const sortable = h.queryAll<HTMLElement>('thead th.p-datatable-sortable-column');

    expect(sortable.length).toBe(h.component.columns.length);
  });
});
