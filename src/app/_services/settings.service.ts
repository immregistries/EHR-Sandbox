import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class SettingsService {
  private apiUrl: string;

  constructor() {
    this.apiUrl = "http://localhost:8080"
  }

  public getApiUrl(): string {
    return this.apiUrl
  }

  public setApiUrl(url: string) {
    this.apiUrl = url
    return this.apiUrl
  }
}
