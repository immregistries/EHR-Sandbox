import { Pipe, type PipeTransform } from '@angular/core';
import { firstValueFrom, Observable, of, tap } from 'rxjs';
import { Facility } from 'src/app/core/_model/rest';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { TenantService } from 'src/app/core/_services/tenant.service';

@Pipe({
  name: 'facilityCache',
  // standalone: true,
})
export class FacilityCachePipe implements PipeTransform {

  constructor(private facilityService: FacilityService, private tenantService: TenantService) {
  }

  _updated_cached_patients: Observable<Facility[]> = new Observable((subscriber) => {
    if (this.tenantService.getCurrentId() > -1 && !this.facilityService.facilitiesCached) {
      this.facilityService.readAllFacilities().subscribe((res) => {
        subscriber.next(res)
      })
    } else {
      subscriber.next(this.facilityService.facilitiesCached)
    }
  })

  transform(facilities: (number | Facility)[] | undefined, list?: Facility[]): Facility[] | undefined {
    if (this.tenantService.getCurrentId() > -1 && !this.facilityService.facilitiesCached) {
      this.facilityService.readAllFacilities().subscribe();
    }
    if (!facilities) {
      return undefined
    }
    if (facilities.length < 1) {
      return []
    }
    // Ignore type to still include id if nothing is found
    //@ts-ignore
    return facilities.map((value) => {
      if (typeof value === "object") {
        return value
      }
      if (list) {
        return list.find((pat) => value == pat.id) ?? value
      } else {
        return this.facilityService.facilitiesCached?.find(p => (value == p.id)) ?? value;
      }
    })
  }
}
