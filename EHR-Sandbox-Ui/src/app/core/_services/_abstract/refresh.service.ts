import { BehaviorSubject, Observable, of, throwError } from 'rxjs';
import { SnackBarService } from '../snack-bar.service';
import { IdUrlVerifyingService } from './id-url-verifying.service';


/**
 * Abstract service to trigger refresh when resources are updated
 */
export abstract class RefreshService extends IdUrlVerifyingService {

  /**
   * Global observable used to trigger a refresh for all the lists of patients, when a new patient was created
   */
  private refresh: BehaviorSubject<boolean>;

  public getRefresh(): Observable<boolean> {
    return this.refresh.asObservable();
  }

  public doRefresh(): void {
    this.refresh.next(!this.refresh.value)
    // this.lastRefreshTime = new Date().getTime()
  }


  constructor(snackBarService: SnackBarService) {
    super(snackBarService);
    this.refresh = new BehaviorSubject<boolean>(false)
  }


}
