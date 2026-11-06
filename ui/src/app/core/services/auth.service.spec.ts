import {firstValueFrom} from 'rxjs';
import {createHttpHarness, HttpHarness} from '../../../testing';
import {AuthService} from './auth.service';

/**
 * The backend's /auth/user answers with a bare username as plain text and
 * 204 No Content when not authenticated. Requesting it as JSON throws on the
 * raw string, which logged the user out on every refresh.
 */
describe('AuthService', () => {
  let h: HttpHarness<AuthService>;

  beforeEach(() => {
    h = createHttpHarness(AuthService);
  });

  afterEach(() => h.verify());

  it('reads the logged-in user as text, not JSON', async () => {
    const result = firstValueFrom(h.service.checkLoggedUser());

    const req = h.expectOne('GET', '/ehr/auth/user');
    expect(req.request.responseType).toBe('text');
    req.flush('tester');

    expect(await result).toBe('tester');
  });

  it('survives a bare username that is not valid JSON', async () => {
    const result = firstValueFrom(h.service.checkLoggedUser());

    h.expectOne('GET', '/ehr/auth/user').flush('tester', {
      status: 200,
      statusText: 'OK',
    });

    await expectAsync(result).toBeResolvedTo('tester');
  });

  it('returns an empty body for 204 No Content', async () => {
    const result = firstValueFrom(h.service.checkLoggedUser());

    h.expectOne('GET', '/ehr/auth/user').flush(null, {status: 204, statusText: 'No Content'});

    expect(await result).toBeFalsy();
  });

  it('posts credentials to /auth', async () => {
    const result = firstValueFrom(h.service.login({username: 'tester', password: 'secret'}));

    const req = h.expectOne('POST', '/ehr/auth');
    expect(req.request.body).toEqual({username: 'tester', password: 'secret'});
    req.flush({id: 1, username: 'tester', accessToken: 'jwt', tokenType: 'Bearer'});

    expect((await result).body?.accessToken).toBe('jwt');
  });
});
