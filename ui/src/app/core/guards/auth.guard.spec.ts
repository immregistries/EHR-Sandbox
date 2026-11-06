import {TestBed} from '@angular/core/testing';
import {ActivatedRouteSnapshot, GuardResult, provideRouter, RouterStateSnapshot, UrlTree,} from '@angular/router';
import {MockStore} from '@ngrx/store/testing';
import {firstValueFrom, isObservable, Observable} from 'rxjs';
import {appStateFactory, provideTestStore} from '../../../testing';
import {AuthActions} from '../store/auth/auth.actions';
import {initialState as authInitial} from '../store/auth/auth.reducer';
import {authGuard} from './auth.guard';

function runGuard(url = '/t/1/f/5/dashboard'): Promise<GuardResult> {
  const result = TestBed.runInInjectionContext(() =>
    authGuard({} as ActivatedRouteSnapshot, {url} as RouterStateSnapshot),
  );
  return isObservable(result)
    ? firstValueFrom(result as Observable<GuardResult>)
    : Promise.resolve(result as GuardResult);
}

describe('authGuard', () => {
  it('lets an authenticated session through', async () => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        ...provideTestStore({
          auth: {...authInitial, authenticated: true, username: 'tester', callState: 'loaded'},
        }),
      ],
    });

    expect(await runGuard()).toBeTrue();
  });

  it('redirects to login once the session is known to be absent', async () => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        ...provideTestStore({auth: {...authInitial, callState: 'loaded'}}),
      ],
    });

    const result = await runGuard('/t/1/f/5/dashboard');

    expect(result instanceof UrlTree).toBeTrue();
    expect(String(result)).toContain('/login');
    expect(String(result)).toContain('redirectTo');
  });

  /**
   * The regression: on a reload the store starts empty while the session
   * probe is still in flight. Deciding immediately logs the user out.
   */
  it('waits for the session probe rather than redirecting on reload', async () => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        ...provideTestStore({auth: {...authInitial, callState: 'init'}}),
      ],
    });
    const store = TestBed.inject(MockStore);

    let settled: GuardResult | undefined;
    void runGuard().then((result) => (settled = result));
    await Promise.resolve();

    expect(settled)
      .withContext('guard must not decide while the probe is outstanding')
      .toBeUndefined();

    // The probe reports a live session.
    store.setState(
      appStateFactory({
        auth: {...authInitial, authenticated: true, username: 'tester', callState: 'loaded'},
      }),
    );
    await Promise.resolve();

    expect(settled).toBeTrue();
  });

  it('redirects when the probe reports no session', async () => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        ...provideTestStore({auth: {...authInitial, callState: 'init'}}),
      ],
    });
    const store = TestBed.inject(MockStore);

    const pending = runGuard();
    store.setState(appStateFactory({auth: {...authInitial, callState: 'loaded'}}));

    expect(String(await pending)).toContain('/login');
  });
});

describe('authGuard after a fresh login', () => {
  /**
   * The regression: the guard used to re-probe unconditionally. On a backend
   * whose /auth/user does not confirm a just-created session, that probe
   * overwrote the logged-in state and bounced the user back to /login.
   */
  it('admits a session established by login without re-probing', async () => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        ...provideTestStore({
          auth: {...authInitial, authenticated: true, username: 'tester', callState: 'loaded'},
        }),
      ],
    });
    const store = TestBed.inject(MockStore);
    const dispatched: { type: string }[] = [];
    spyOn(store, 'dispatch').and.callFake(((action: { type: string }) => {
      dispatched.push(action);
    }) as never);

    expect(await runGuard()).toBeTrue();
    expect(dispatched.map((action) => action.type)).not.toContain(
      AuthActions.restoreSession.type,
    );
  });

  it('starts a probe only when the session is unknown', async () => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        ...provideTestStore({auth: {...authInitial, callState: 'init'}}),
      ],
    });
    const store = TestBed.inject(MockStore);
    const dispatched: { type: string }[] = [];
    spyOn(store, 'dispatch').and.callFake(((action: { type: string }) => {
      dispatched.push(action);
    }) as never);

    void runGuard();
    await Promise.resolve();

    expect(dispatched.map((action) => action.type)).toContain(
      AuthActions.restoreSession.type,
    );
  });

  it('does not probe again while one is already running', async () => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        ...provideTestStore({auth: {...authInitial, callState: 'loading'}}),
      ],
    });
    const store = TestBed.inject(MockStore);
    const dispatched: { type: string }[] = [];
    spyOn(store, 'dispatch').and.callFake(((action: { type: string }) => {
      dispatched.push(action);
    }) as never);

    void runGuard();
    await Promise.resolve();

    expect(dispatched).toEqual([]);
  });
});
