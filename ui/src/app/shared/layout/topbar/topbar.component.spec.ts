import {AuthActions} from '../../../core/store/auth/auth.actions';
import {selectUsername} from '../../../core/store/auth/auth.selectors';
import {PatientActions} from '../../../core/store/patient/patient.actions';
import {ComponentHarness, recordDispatches, renderComponent, setSelector} from '../../../../testing';
import {TopbarComponent} from './topbar.component';

describe('TopbarComponent', () => {
  let h: ComponentHarness<TopbarComponent>;

  beforeEach(async () => {
    h = await renderComponent(TopbarComponent, {state: {route: {tenantId: 1, facilityId: 2}}});
  });

  it('shows the signed-in username', () => {
    setSelector(h.store, selectUsername, 'tester');
    h.detectChanges();

    expect(h.text('.username')).toContain('tester');
  });

  it('dispatches a refresh of the current patient list', () => {
    const dispatches = recordDispatches(h.store);

    h.component.refresh();

    expect(dispatches.actions).toEqual([PatientActions.refresh()]);
  });

  it('dispatches logout', () => {
    const dispatches = recordDispatches(h.store);

    h.component.logout();

    expect(dispatches.actions).toEqual([AuthActions.logout()]);
  });

  it('emits a toggle when the menu button is pressed', () => {
    let toggled = 0;
    h.component.menuToggle.subscribe(() => (toggled += 1));

    h.click('button[aria-label="Toggle navigation"]');

    expect(toggled).toBe(1);
  });
});
