/**
 * Effect exemplar: feed a finite actions stream, assert the emitted actions.
 * The service is a jasmine spy object; the store comes from the harness so
 * effects reading route context (refresh$) see real router selectors.
 */
import {HttpResponse} from '@angular/common/http';
import {Action} from '@ngrx/store';
import {Observable, of, throwError} from 'rxjs';
import {
  aFacility,
  AppStateOverrides,
  createEffectsHarness,
  expectEmission,
  expectNoEmission,
} from '../../../../testing';
import {Facility} from '../../models';
import {FacilityService} from '../../services';
import {FacilityActions} from './facility.actions';
import {FacilityEffects} from './facility.effects';

describe('FacilityEffects', () => {
  let facilityService: jasmine.SpyObj<FacilityService>;

  beforeEach(() => {
    facilityService = jasmine.createSpyObj<FacilityService>('FacilityService', [
      'readFacilities',
      'readFacility',
      'postFacility',
      'putFacility',
    ]);
  });

  function harness(actions: Observable<Action>, state: AppStateOverrides = {}) {
    return createEffectsHarness(FacilityEffects, {
      actions,
      state,
      providers: [{provide: FacilityService, useValue: facilityService}],
    });
  }

  it('emits success with the loaded facilities', async () => {
    const facilities = [aFacility({id: 1}), aFacility({id: 2})];
    facilityService.readFacilities.and.returnValue(of(facilities));

    const {effects} = harness(of(FacilityActions.loadFacilities({tenantId: 7})));

    await expectEmission(
      effects.loadFacilities$,
      FacilityActions.loadFacilitiesSuccess({facilities}),
    );
    expect(facilityService.readFacilities).toHaveBeenCalledWith(7);
  });

  it('maps a failed request to a failure action instead of throwing', async () => {
    facilityService.readFacilities.and.returnValue(throwError(() => new Error('offline')));

    const {effects} = harness(of(FacilityActions.loadFacilities({tenantId: 7})));

    await expectEmission(
      effects.loadFacilities$,
      FacilityActions.loadFacilitiesFailure({error: 'offline'}),
    );
  });

  it('refreshes using the tenant id currently in the url', async () => {
    const {effects} = harness(of(FacilityActions.refresh()), {route: {tenantId: 42}});

    await expectEmission(effects.refresh$, FacilityActions.loadFacilities({tenantId: 42}));
  });

  it('ignores a refresh when no tenant is selected', async () => {
    const {effects} = harness(of(FacilityActions.refresh()), {route: {}});

    await expectNoEmission(effects.refresh$);
  });

  it('falls back to the submitted facility when the API returns no body', async () => {
    const facility = aFacility({id: 3});
    facilityService.postFacility.and.returnValue(
      of(new HttpResponse<Facility>({body: null})),
    );

    const {effects} = harness(
      of(FacilityActions.createFacility({tenantId: 1, facility})),
    );

    await expectEmission(effects.createFacility$, FacilityActions.createFacilitySuccess({facility}));
  });
});
