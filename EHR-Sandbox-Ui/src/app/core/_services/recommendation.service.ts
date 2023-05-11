import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ImmunizationRecommendation } from 'fhir/r5';
import { map, Observable, of } from 'rxjs';
import { FacilityService } from './facility.service';
import { SettingsService } from './settings.service';
import { TenantService } from './tenant.service';


const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};
@Injectable({
  providedIn: 'root'
})
export class RecommendationService {

  constructor(private http: HttpClient,
    private settings: SettingsService,
    private facilityService: FacilityService,
    private tenantService: TenantService) {

  }
  readRecommendations(patientId: number): Observable<ImmunizationRecommendation[]>{
    const tenantId: number = this.tenantService.getCurrentId()
    const facilityId: number = this.facilityService.getCurrentId()
    if (tenantId > 0 && facilityId > 0 && patientId > 0){
      return this.http.get<string[]>(
        `${this.settings.getApiUrl()}/tenants/${tenantId}/facilities/${facilityId}/patients/${patientId}/recommendations`,
        httpOptions)
          .pipe(map((array: string[]) => {return array.map((json) => { return (JSON.parse(json) as ImmunizationRecommendation)})}));
    } else {
      return of([])
    }
  }



}
