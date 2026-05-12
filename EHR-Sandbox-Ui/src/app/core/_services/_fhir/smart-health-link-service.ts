import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Observable, of} from 'rxjs';
import {SettingsService} from '../settings.service';
import {FacilityService} from '../facility.service';
import {TenantService} from '../tenant.service';
import {ImmunizationRegistryService} from 'src/app/core/_services/immunization-registry.service';
import {IdUrlVerifyingService} from '../_abstract/id-url-verifying.service';
import {SnackBarService} from '../snack-bar.service';
import {ReceivedHistoryDTO} from '../../_model/dtos';

const httpOptions = {
  headers: new HttpHeaders({'Content-Type': 'application/json'})
};

@Injectable({
  providedIn: 'root'
})
/**
 * Fhir service interacting with the API to parse and serialize resources, and interact with IIS's
 */
export class SmartHealthLinkService extends IdUrlVerifyingService {

  constructor(
    snackBarService: SnackBarService,
    private http: HttpClient,
    private settings: SettingsService,
    private facilityService: FacilityService,
    private tenantService: TenantService,
    private registryService: ImmunizationRegistryService,
    // private service: Service
  ) {
    super(snackBarService)
  }


  shlinkRead(url: string, password?: string, jwk?: string): Observable<string> {
    const registryId = this.registryService.getCurrentId()
    const tenantId: number = this.tenantService.getCurrentId()
    if (this.idsNotValid(tenantId)) {
      return of()
    }
    return this.http.post<string>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/$read-sh-link`,
      url,
      {...httpOptions, params: {'password': password ?? "", 'jwk': jwk ?? ""}});
  }

  importShlinkForPatient(patientId: number, url: string, password?: string, jwk?: string): Observable<ReceivedHistoryDTO> {
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    if (this.idsNotValid(tenantId, facilityId, patientId)) {
      return of()
    }
    return this.http.post<ReceivedHistoryDTO>(
      `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/$import-sh-link`,
      url,
      {...httpOptions, params: {'password': password ?? "", 'jwk': jwk ?? ""}});
  }

}
