import { BehaviorSubject, Observable, of, share } from 'rxjs';
import { RefreshService } from './refresh.service';

export class CurrentSelectedService<T> extends RefreshService {
  protected current!: BehaviorSubject<T>;
  private lastRefreshTime: number;

  public getLastRefreshTime(): number {
    return this.lastRefreshTime;
  }

  public updateLastRefreshtime(): number {
    this.lastRefreshTime = new Date().getTime()
    return this.lastRefreshTime;
  }


  public getCurrentObservable(): Observable<T> {
    return this.current.asObservable();
  }

  public getCurrent(): T {
    return this.current.value
  }

  public setCurrent(value: T) {
    this.current.next(value)
    this.updateLastRefreshtime()
  }

  public override doRefresh(): void{
    super.doRefresh()
    this.lastRefreshTime = new Date().getTime()
  }

  constructor(subject : BehaviorSubject<T>
    ) {
      super()
      this.current = subject
      this.lastRefreshTime = new Date().getTime()
   }

}
