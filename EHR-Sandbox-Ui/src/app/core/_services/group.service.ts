import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Group } from 'fhir/r5';
import { map, Observable, of } from 'rxjs';
import { FacilityService } from './facility.service';
import { SettingsService } from './settings.service';
import { TenantService } from './tenant.service';
import { ImmunizationRegistryService } from './immunization-registry.service';


const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};
@Injectable({
  providedIn: 'root'
})
export class GroupService {

  constructor(private http: HttpClient,
    private settings: SettingsService,
    private facilityService: FacilityService,
    private tenantService: TenantService,
    private immunizationRegistryService: ImmunizationRegistryService) {

  }
  readGroups(): Observable<Group[]>{
    const tenantId: number = this.tenantService.getTenantId()
    const facilityId: number = this.facilityService.getFacilityId()
    const immRegistryId: number | undefined = this.immunizationRegistryService.getImmRegistryId()
    if (tenantId > 0 && facilityId > 0 && immRegistryId && immRegistryId > 0){
      return this.http.get<string[]>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/imm-registry/${immRegistryId}/groups`,
        httpOptions)
          .pipe(map((array: string[]) => {return array.map((json) => { return (JSON.parse(json) as Group)})}));
    } else {
      return of([])
    }
  }



}
