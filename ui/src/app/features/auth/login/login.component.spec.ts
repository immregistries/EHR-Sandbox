/**
 * Component exemplar: render through the harness, drive the DOM, assert on
 * what the component dispatched. State comes from overridden selectors, so
 * no reducer runs during a component spec.
 */
import {AuthActions} from '../../../core/store/auth/auth.actions';
import {selectAuthError, selectAuthLoading} from '../../../core/store/auth/auth.selectors';
import {ComponentHarness, recordDispatches, renderComponent, setSelector,} from '../../../../testing';
import {LoginComponent} from './login.component';

describe('LoginComponent', () => {
  let h: ComponentHarness<LoginComponent>;

  beforeEach(async () => {
    h = await renderComponent(LoginComponent);
  });

  it('starts with an invalid, empty form', () => {
    expect(h.component.form.valid).toBeFalse();
  });

  it('does not dispatch while the form is invalid', () => {
    const dispatches = recordDispatches(h.store);

    h.component.submit();

    expect(dispatches.actions).toEqual([]);
  });

  it('dispatches login with the entered credentials', () => {
    const dispatches = recordDispatches(h.store);
    h.component.form.setValue({username: 'tester', password: 'secret'});

    h.component.submit();

    expect(dispatches.actions).toEqual([
      AuthActions.login({user: {username: 'tester', password: 'secret'}}),
    ]);
  });

  it('shows the error returned by the store', () => {
    setSelector(h.store, selectAuthError, 'Bad credentials');
    h.detectChanges();

    expect(h.text('p-message')).toContain('Bad credentials');
  });

  it('renders no error message when there is no error', () => {
    setSelector(h.store, selectAuthError, null);
    h.detectChanges();

    expect(h.query('p-message')).toBeNull();
  });

  it('reflects the loading state on the submit button', () => {
    setSelector(h.store, selectAuthLoading, true);
    h.detectChanges();

    expect(h.component.loading()).toBeTrue();
  });
});
