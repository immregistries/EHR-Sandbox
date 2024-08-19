import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, share } from 'rxjs';
import { ImmunizationRegistry } from '../_model/rest';
import { SettingsService } from './settings.service';
import { CurrentSelectedWithIdService } from './current-selected-with-id.service';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
export class ImmunizationRegistryService extends CurrentSelectedWithIdService<ImmunizationRegistry> {

  private _registriesCached?: ImmunizationRegistry[] | undefined;
  public get registriesCached(): ImmunizationRegistry[] | undefined {
    return this._registriesCached;
  }

  constructor(private http: HttpClient, private settings: SettingsService) {
    super(new BehaviorSubject<ImmunizationRegistry>({}))
    this.getRefresh().subscribe(() => {
      this.readImmRegistries()
        .subscribe((facilities) => {
          this._registriesCached = facilities
        })
    })
  }

  public readImmRegistries(): Observable<ImmunizationRegistry[]> {
    return this.http.get<ImmunizationRegistry[]>(
      this.settings.getApiUrl() + `/registry`, httpOptions).pipe(share());
  }

  public checkConnectivity(registryId: number | undefined): Observable<string> {
    return this.http.get(
      this.settings.getApiUrl() + `/registry/${registryId}/$connectivity`,
      { ...httpOptions, responseType: 'text' }).pipe(share());
  }
  public checkConnectivityAuth(registryId: number | undefined): Observable<string> {
    return this.http.get(
      this.settings.getApiUrl() + `/registry/${registryId}/$auth`,
      { ...httpOptions, responseType: 'text' }).pipe(share());
  }

  public deleteImmRegistry(id: number): Observable<HttpResponse<string>> {
    return this.http.delete<HttpResponse<string>>(
      this.settings.getApiUrl() + `/registry/${id}`, httpOptions);
  }

  public putImmRegistry(registry: ImmunizationRegistry): Observable<ImmunizationRegistry> {
    return this.http.put<ImmunizationRegistry>(
      this.settings.getApiUrl() + `/registry`, registry, httpOptions);
  }

  public postImmRegistry(registry: ImmunizationRegistry): Observable<ImmunizationRegistry> {
    return this.http.post<ImmunizationRegistry>(
      this.settings.getApiUrl() + `/registry`, registry, httpOptions);
  }

}
