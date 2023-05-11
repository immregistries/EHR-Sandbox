import { Injectable } from '@angular/core';
import { FacilityService } from '../../_services/facility.service';
import { ImmunizationRegistryService } from '../../_services/immunization-registry.service';
import { TenantService } from '../../_services/tenant.service';
import { JwtResponse } from '../security';
const TOKEN_KEY = 'auth-token';
const USER_KEY = 'auth-user';
@Injectable({
  providedIn: 'root'
})
export class TokenStorageService {
  constructor( private facilityService : FacilityService,
    private tenantService: TenantService,
    private immService: ImmunizationRegistryService) { }
  signOut(): void {
    window.sessionStorage.clear();
    this.facilityService.setCurrent({id:-1})
    this.tenantService.setCurrent({id:-1})
    this.immService.setCurrent({})
  }
  public saveToken(token: string): void {
    window.sessionStorage.removeItem(TOKEN_KEY);
    window.sessionStorage.setItem(TOKEN_KEY, token);
  }
  public getToken(): string | null {
    return window.sessionStorage.getItem(TOKEN_KEY);
  }
  public saveUser(user: JwtResponse): void {
    window.sessionStorage.removeItem(USER_KEY);
    window.sessionStorage.setItem(USER_KEY, JSON.stringify(user));
  }
  public getUser(): any {
    const user = window.sessionStorage.getItem(USER_KEY);
    if (user) {
      return JSON.parse(user);
    }
    return {};
  }
}
