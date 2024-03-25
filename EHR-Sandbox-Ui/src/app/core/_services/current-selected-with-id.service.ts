import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';

import { BehaviorSubject, Observable, of, share } from 'rxjs';
import { SettingsService } from './settings.service';
import { RefreshService } from './refresh.service';
import { ObjectWithID } from '../_model/rest';
import { CurrentSelectedService } from './current-selected.service';

export class CurrentSelectedWithIdService<T extends ObjectWithID> extends CurrentSelectedService<T> {


  public getCurrentId(): number {
    return this.current.value?.id ?? -1
  }

  constructor(subject : BehaviorSubject<T>
    ) {
      super(subject)
   }

}
