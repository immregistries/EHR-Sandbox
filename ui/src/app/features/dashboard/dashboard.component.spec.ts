import {Router} from '@angular/router';
import {PatientActions} from '../../core/store/patient/patient.actions';
import {adapter as patientAdapter, initialState as patientInitial} from '../../core/store/patient/patient.reducer';
import {VaccinationActions} from '../../core/store/vaccination/vaccination.actions';
import {aPatient, ComponentHarness, recordDispatches, renderComponent,} from '../../../testing';
import {DashboardComponent} from './dashboard.component';

describe('DashboardComponent', () => {
  const patients = [aPatient({id: 1}), aPatient({id: 2})];

  async function render(route: Record<string, number>): Promise<ComponentHarness<DashboardComponent>> {
    return renderComponent(DashboardComponent, {
      state: {
        route,
        patient: patientAdapter.setAll(patients, patientInitial),
      },
    });
  }

  it('loads the facility patients from the route context', async () => {
    const h = await render({tenantId: 4, facilityId: 9});
    const dispatches = recordDispatches(h.store);

    h.component.refreshPatients();

    expect(dispatches.actions).toEqual([PatientActions.refresh()]);
  });

  it('renders a row per patient', async () => {
    const h = await render({tenantId: 4, facilityId: 9});

    expect(h.queryAll('tbody tr').length).toBeGreaterThanOrEqual(patients.length);
  });

  it('prompts for a patient when none is selected', async () => {
    const h = await render({tenantId: 4, facilityId: 9});

    expect(h.text('.panel--vaccinations .placeholder')).toContain('Select a patient');
  });

  it('titles the immunization panel with the selected patient', async () => {
    const h = await render({tenantId: 4, facilityId: 9, patientId: 1});

    expect(h.component.vaccinationsTitle()).toContain('Ada');
  });

  it('navigates when a patient is selected, instead of mutating state', async () => {
    const h = await render({tenantId: 4, facilityId: 9});
    const router = h.fixture.debugElement.injector.get(Router);
    const navigate = spyOn(router, 'navigate');

    h.component.selectPatient(patients[1]);

    expect(navigate).toHaveBeenCalledWith(['/t', 4, 'f', 9, 'dashboard', 'patients', 2]);
  });

  it('dispatches a vaccination refresh', async () => {
    const h = await render({tenantId: 4, facilityId: 9, patientId: 1});
    const dispatches = recordDispatches(h.store);

    h.component.refreshVaccinations();

    expect(dispatches.actions).toEqual([VaccinationActions.refresh()]);
  });
});
