import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { ImmunizationRegistry } from '../_model/rest';
import { SettingsService } from './settings.service';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
export class ImmunizationRegistryService {
  private refresh: BehaviorSubject<boolean>;
  public getRefresh(): Observable<boolean> {
    return this.refresh.asObservable();
  }
  public doRefresh(): void{
    this.refresh.next(!this.refresh.value)
  }



  private immRegistry: BehaviorSubject<ImmunizationRegistry>;

  public getObservableImmRegistry(): Observable<ImmunizationRegistry> {
    return this.immRegistry.asObservable();
  }

  public getImmRegistry(): ImmunizationRegistry {
    return this.immRegistry.value
  }

  public getImmRegistryId(): number | undefined{
    return this.immRegistry.value.id
  }

  public setImmRegistry(immRegistry: ImmunizationRegistry) {
    this.immRegistry.next(immRegistry)
  }

  constructor(private http: HttpClient, private settings: SettingsService) {
    this.immRegistry = new BehaviorSubject<ImmunizationRegistry>({})
    this.refresh = new BehaviorSubject<boolean>(false)
  }

  public readImmRegistries(): Observable<ImmunizationRegistry[]>{
    return this.http.get<ImmunizationRegistry[]>(
      this.settings.getApiUrl() + `/imm-registry`, httpOptions);
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
