import {HttpHeaders} from '@angular/common/http';
import {inject} from '@angular/core';
import {SettingsService} from './settings.service';

export const JSON_HEADERS = {
  headers: new HttpHeaders({'Content-Type': 'application/json'}),
};

export const TEXT_OPTIONS = {
  headers: new HttpHeaders({'Content-Type': 'application/json'}),
  responseType: 'text' as const,
};

/**
 * Base for every API service: resolves urls against the configured API root.
 * Services are stateless — tenant/facility ids are always explicit arguments,
 * the "currently selected" context lives in the NgRx store.
 */
export abstract class ApiBase {
  protected readonly settings = inject(SettingsService);

  protected url(path: string): string {
    return `${this.settings.getApiUrl()}${path}`;
  }

  protected tenantUrl(tenantId: number, path = ''): string {
    return this.url(`/tenants/${tenantId}${path}`);
  }

  protected facilityUrl(tenantId: number, facilityId: number, path = ''): string {
    return this.tenantUrl(tenantId, `/facilities/${facilityId}${path}`);
  }

  protected patientUrl(tenantId: number, facilityId: number, patientId: number, path = ''): string {
    return this.facilityUrl(tenantId, facilityId, `/patients/${patientId}${path}`);
  }
}
