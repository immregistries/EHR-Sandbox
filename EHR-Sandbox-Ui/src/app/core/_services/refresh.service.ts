import { BehaviorSubject, Observable, of, throwError } from 'rxjs';


/**
 * Abstract service to trigger refresh when resources are updated
 */
export abstract class RefreshService {

  /**
   * Global observable used to trigger a refresh for all the lists of patients, when a new patient was created
   */
  private refresh: BehaviorSubject<boolean>;

  public getRefresh(): Observable<boolean> {
    return this.refresh.asObservable();
  }

  public doRefresh(): void{
    this.refresh.next(!this.refresh.value)
    // this.lastRefreshTime = new Date().getTime()
  }


  constructor() {
      this.refresh = new BehaviorSubject<boolean>(false)

    }


}
