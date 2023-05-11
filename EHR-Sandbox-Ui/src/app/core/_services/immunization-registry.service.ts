import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, share } from 'rxjs';
import { ImmunizationRegistry } from '../_model/rest';
import { SettingsService } from './settings.service';
import { RefreshService } from './refresh.service';
import { CurrentSelectedService } from './current-selected.service';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
export class ImmunizationRegistryService extends CurrentSelectedService<ImmunizationRegistry> {
  constructor(private http: HttpClient, private settings: SettingsService) {
    super(new BehaviorSubject<ImmunizationRegistry>({}))
  }

  public readImmRegistries(): Observable<ImmunizationRegistry[]>{
    return this.http.get<ImmunizationRegistry[]>(
      this.settings.getApiUrl() + `/imm-registry`, httpOptions).pipe(share());
  }

  public putImmRegistry(i: ImmunizationRegistry): Observable<ImmunizationRegistry>{
    return this.http.put<ImmunizationRegistry>(
      this.settings.getApiUrl() + `/imm-registry`, i, httpOptions);
  }

  public postImmRegistry(i: ImmunizationRegistry): Observable<ImmunizationRegistry>{
    return this.http.post<ImmunizationRegistry>(
      this.settings.getApiUrl() + `/imm-registry`, i, httpOptions);
  }

}
