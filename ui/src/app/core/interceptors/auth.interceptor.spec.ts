import {HttpClient, provideHttpClient, withInterceptors} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {TestBed} from '@angular/core/testing';
import {MockStore} from '@ngrx/store/testing';
import {provideTestStore} from '../../../testing';
import {TokenStorageService} from '../services';
import {AuthActions} from '../store/auth/auth.actions';
import {authInterceptor} from './auth.interceptor';

describe('authInterceptor', () => {
  let http: HttpClient;
  let mock: HttpTestingController;
  let token: string | null;

  beforeEach(() => {
    token = 'jwt-abc';
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        ...provideTestStore({}),
        {provide: TokenStorageService, useValue: {getToken: () => token}},
      ],
    });
    http = TestBed.inject(HttpClient);
    mock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => mock.verify());

  it('sends the stored token as a bearer header', () => {
    http.get('/ehr/tenants').subscribe();

    const req = mock.expectOne('/ehr/tenants');
    expect(req.request.headers.get('Authorization')).toBe('Bearer jwt-abc');
    req.flush([]);
  });

  it('sends no authorization header when there is no token', () => {
    token = null;
    http.get('/ehr/tenants').subscribe();

    const req = mock.expectOne('/ehr/tenants');
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush([]);
  });

  it('ends the session when the API rejects the token', () => {
    const store = TestBed.inject(MockStore);
    const dispatched: { type: string }[] = [];
    spyOn(store, 'dispatch').and.callFake(((action: { type: string }) => {
      dispatched.push(action);
    }) as never);

    http.get('/ehr/tenants').subscribe({error: () => undefined});
    mock.expectOne('/ehr/tenants').flush({}, {status: 401, statusText: 'Unauthorized'});

    expect(dispatched.map((a) => a.type)).toContain(AuthActions.sessionExpired.type);
  });

  it('does not end the session when the probe itself 401s', () => {
    const store = TestBed.inject(MockStore);
    const dispatched: { type: string }[] = [];
    spyOn(store, 'dispatch').and.callFake(((action: { type: string }) => {
      dispatched.push(action);
    }) as never);

    // restoreSession handles this case; a redirect here would loop.
    http.get('/ehr/auth/user').subscribe({error: () => undefined});
    mock.expectOne('/ehr/auth/user').flush({}, {status: 401, statusText: 'Unauthorized'});

    expect(dispatched).toEqual([]);
  });
});
