import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class SettingsService {
  apiUrl;

  constructor() {
    this.apiUrl = "localhost:9091/ehr-sandbox"
  }

  getApiUrl() {
    return this.apiUrl
  }

  setApiUrl(url: string) {
    this.apiUrl = url
    return this.apiUrl
  }
}
