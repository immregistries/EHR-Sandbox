/**
 * HTTP service testing harness.
 *
 *   const h = createHttpHarness(PatientService);
 *   const result = firstValueFrom(h.service.readPatients(1, 2));
 *   h.expectOne('GET', '/ehr/tenants/1/facilities/2/patients').flush([patient]);
 *
 * The API root is stubbed to a fixed '/ehr', so expected urls are written in
 * full and the tenant/facility path shape is asserted exactly.
 */
import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting, TestRequest,} from '@angular/common/http/testing';
import {Provider, Type} from '@angular/core';
import {TestBed} from '@angular/core/testing';
import {SettingsService} from '../app/core/services';

export const TEST_API_URL = '/ehr';

export interface HttpHarness<T> {
  service: T;
  httpMock: HttpTestingController;

  /** Asserts exactly one request matching method and url, and returns it. */
  expectOne(method: string, url: string): TestRequest;

  /** Asserts no request was made to the url. */
  expectNone(url: string): void;

  /** Call in afterEach — fails the spec on unexpected outstanding requests. */
  verify(): void;
}

export function createHttpHarness<T>(
  serviceType: Type<T>,
  providers: Provider[] = [],
): HttpHarness<T> {
  TestBed.configureTestingModule({
    providers: [
      provideHttpClient(),
      provideHttpClientTesting(),
      {
        provide: SettingsService,
        useValue: {
          getApiUrl: () => TEST_API_URL,
          getEnvConfig: () => ({apiUrl: TEST_API_URL}),
        },
      },
      serviceType,
      ...providers,
    ],
  });

  const httpMock = TestBed.inject(HttpTestingController);

  return {
    service: TestBed.inject(serviceType),
    httpMock,
    expectOne(method: string, url: string): TestRequest {
      const req = httpMock.expectOne(
        (candidate) => candidate.method === method && candidate.urlWithParams === url,
        `${method} ${url}`,
      );
      return req;
    },
    expectNone(url: string): void {
      httpMock.expectNone((candidate) => candidate.urlWithParams === url);
    },
    verify(): void {
      httpMock.verify();
    },
  };
}
