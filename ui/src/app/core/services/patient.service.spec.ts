/**
 * API service exemplar: assert the exact url, method and body of each call.
 * These tests are the contract against the backend's route shape.
 */
import {firstValueFrom} from 'rxjs';
import {aPatient, createHttpHarness, HttpHarness, resetFixtureIds} from '../../../testing';
import {PatientService} from './patient.service';

describe('PatientService', () => {
  let h: HttpHarness<PatientService>;

  beforeEach(() => {
    resetFixtureIds();
    h = createHttpHarness(PatientService);
  });

  afterEach(() => h.verify());

  it('reads the patients of a facility', async () => {
    const patients = [aPatient({id: 1})];
    const result = firstValueFrom(h.service.readPatients(1, 2));

    h.expectOne('GET', '/ehr/tenants/1/facilities/2/patients').flush(patients);

    expect(await result).toEqual(patients);
  });

  it('reads a single patient', async () => {
    const patient = aPatient({id: 3});
    const result = firstValueFrom(h.service.readPatient(1, 2, 3));

    h.expectOne('GET', '/ehr/tenants/1/facilities/2/patients/3').flush(patient);

    expect(await result).toEqual(patient);
  });

  it('posts a new patient to the facility collection', async () => {
    const patient = aPatient({id: undefined});
    const result = firstValueFrom(h.service.postPatient(1, 2, patient));

    const req = h.expectOne('POST', '/ehr/tenants/1/facilities/2/patients');
    expect(req.request.body).toEqual(patient);
    req.flush('55');

    expect((await result).body).toBe('55');
  });

  it('puts an updated patient to the collection url, not the item url', async () => {
    const patient = aPatient({id: 3});
    const result = firstValueFrom(h.service.putPatient(1, 2, patient));

    h.expectOne('PUT', '/ehr/tenants/1/facilities/2/patients').flush(patient);

    expect(await result).toEqual(patient);
  });

  it('requests a facility-scoped random patient when ids are given', async () => {
    const patient = aPatient({id: 9});
    const result = firstValueFrom(h.service.readRandom(1, 2));

    h.expectOne('GET', '/ehr/tenants/1/facilities/2/$random_patient').flush(patient);

    expect(await result).toEqual(patient);
  });

  it('falls back to the global random patient endpoint without ids', async () => {
    const patient = aPatient({id: 9});
    const result = firstValueFrom(h.service.readRandom());

    h.expectOne('GET', '/ehr/$random_patient').flush(patient);

    expect(await result).toEqual(patient);
  });
});
