import {Router} from '@angular/router';
import {aFacility, ComponentHarness, renderComponent, resetFixtureIds} from '../../../../testing';
import {adapter, initialState} from '../../../core/store/facility/facility.reducer';
import {FacilityListComponent} from './facility-list.component';

describe('FacilityListComponent', () => {
  const facilities = [
    aFacility({id: 5, nameDisplay: 'North Clinic', type: 'clinic', feedbacksCount: 1}),
    aFacility({id: 6, nameDisplay: 'South Clinic', type: 'clinic', childrenCount: 2}),
  ];

  async function render(route: Record<string, number> = {tenantId: 3}) {
    return renderComponent(FacilityListComponent, {
      state: {route, facility: adapter.setAll(facilities, initialState)},
    });
  }

  function rows(h: ComponentHarness<FacilityListComponent>): HTMLElement[] {
    return h.queryAll<HTMLElement>('tr.facility-row');
  }

  beforeEach(() => resetFixtureIds());

  it('lists the facilities of the tenant in the url', async () => {
    const h = await render();

    expect(rows(h).length).toBe(2);
    expect(rows(h)[0].textContent).toContain('North Clinic');
  });

  it('marks the facility currently in the url as active', async () => {
    const h = await render({tenantId: 3, facilityId: 6});

    const active = h.queryAll<HTMLElement>('tr.row--active');

    expect(active.length).toBe(1);
    expect(active[0].textContent).toContain('South Clinic');
  });

  it('selects a facility by navigating into its dashboard', async () => {
    const h = await render();
    const router = h.fixture.debugElement.injector.get(Router);
    const navigate = spyOn(router, 'navigate');

    h.component.select(facilities[0]);

    expect(navigate).toHaveBeenCalledWith(['/t', 3, 'f', 5, 'dashboard']);
  });

  it('selects a facility when its row is clicked', async () => {
    const h = await render();
    const router = h.fixture.debugElement.injector.get(Router);
    const navigate = spyOn(router, 'navigate');

    rows(h)[1]?.click();

    expect(navigate).toHaveBeenCalledWith(['/t', 3, 'f', 6, 'dashboard']);
  });

  it('declares a sortable field for every data column', async () => {
    const h = await render();

    const sortable = h.queryAll<HTMLElement>('thead th.p-datatable-sortable-column');

    expect(sortable.length).toBe(h.component.columns.length);
  });
});
