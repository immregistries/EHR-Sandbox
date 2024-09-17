import { BehaviorSubject, Observable, of, share } from 'rxjs';
import { ObjectWithID } from '../../_model/rest';
import { CurrentSelectedService } from './current-selected.service';
import { SnackBarService } from '../snack-bar.service';

export class CurrentSelectedWithIdService<T extends ObjectWithID> extends CurrentSelectedService<T> {


  public getCurrentId(): number {
    return this.current.value?.id ?? -1
  }

  constructor(subject: BehaviorSubject<T>, snackBarService: SnackBarService) {
    super(subject, snackBarService)
  }

}
