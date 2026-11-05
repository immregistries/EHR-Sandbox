import {HttpClient, HttpResponse} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {ImmunizationRegistry} from '../models';
import {ApiBase, JSON_HEADERS, TEXT_OPTIONS} from './api.base';

@Injectable({providedIn: 'root'})
export class ImmunizationRegistryService extends ApiBase {
  private readonly http = inject(HttpClient);

  readImmRegistries(): Observable<ImmunizationRegistry[]> {
    return this.http.get<ImmunizationRegistry[]>(this.url('/registry'), JSON_HEADERS);
  }

  checkConnectivity(registryId: number): Observable<string> {
    return this.http.get(this.url(`/registry/${registryId}/$connectivity`), TEXT_OPTIONS);
  }

  checkConnectivityAuth(registryId: number): Observable<string> {
    return this.http.get(this.url(`/registry/${registryId}/$auth`), TEXT_OPTIONS);
  }

  fhirMetadata(registryId: number): Observable<string> {
    return this.http.get(this.url(`/registry/${registryId}/metadata`), TEXT_OPTIONS);
  }

  postImmRegistry(registry: ImmunizationRegistry): Observable<ImmunizationRegistry> {
    return this.http.post<ImmunizationRegistry>(this.url('/registry'), registry, JSON_HEADERS);
  }

  putImmRegistry(registry: ImmunizationRegistry): Observable<ImmunizationRegistry> {
    return this.http.put<ImmunizationRegistry>(this.url('/registry'), registry, JSON_HEADERS);
  }

  deleteImmRegistry(id: number): Observable<HttpResponse<string>> {
    return this.http.delete(this.url(`/registry/${id}`), {
      ...TEXT_OPTIONS,
      observe: 'response',
    });
  }
}
