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

describe('authInterceptor remote registry failures', () => {
  let http: HttpClient;
  let mock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        ...provideTestStore({}),
        {provide: TokenStorageService, useValue: {getToken: () => 'jwt-abc'}},
      ],
    });
    http = TestBed.inject(HttpClient);
    mock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => mock.verify());

  function dispatchedTypes(): string[] {
    const store = TestBed.inject(MockStore);
    const seen: string[] = [];
    spyOn(store, 'dispatch').and.callFake(((action: { type: string }) => {
      seen.push(action.type);
    }) as never);
    return seen;
  }

  /**
   * A 401 from a registry connectivity check means the *remote IIS* rejected
   * its configured credentials — signing the user out of this app would be
   * wrong, and connection checks are expected to fail.
   */
  const REMOTE_URLS = [
    '/ehr/registry/1/$auth',
    '/ehr/registry/1/$connectivity',
    '/ehr/registry/1/metadata',
    '/ehr/tenants/1/fhir-client?registryId=1',
  ];

  for (const url of REMOTE_URLS) {
    it(`keeps the session when ${url} returns 401`, () => {
      const seen = dispatchedTypes();

      http.get(url).subscribe({error: () => undefined});
      mock.expectOne(url).flush({}, {status: 401, statusText: 'Unauthorized'});

      expect(seen).toEqual([]);
    });
  }

  it('still ends the session when an ordinary endpoint returns 401', () => {
    const seen = dispatchedTypes();

    http.get('/ehr/tenants/1/facilities').subscribe({error: () => undefined});
    mock
      .expectOne('/ehr/tenants/1/facilities')
      .flush({}, {status: 401, statusText: 'Unauthorized'});

    expect(seen).toContain(AuthActions.sessionExpired.type);
  });
});
