import { Observable } from "rxjs"
import { SnackBarService } from "../snack-bar.service"
import { CurrentSelectedWithIdService } from "./current-selected-with-id.service"

export class IdUrlVerifyingService {

  observables_parent_ids_valid(message?: string, ..._services: CurrentSelectedWithIdService<any>[]): Observable<boolean> {
    return new Observable((subscriber) => {
      let valid = true
      for (const service of _services) {
        if (service.getCurrentId() < 0) {
          valid = false
          break;
        }
      }
      subscriber.next(valid)
    })
  }

  idsNotValid(...ids: number[]): boolean {
    for (const id of ids) {
      if (id < 0) {
        this.snackBarService.errorMessage("Select tenant or facility")
        return true
      }
    }
    return false
  }

  constructor(public snackBarService: SnackBarService
  ) {
  }

}
