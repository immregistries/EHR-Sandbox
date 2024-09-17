import { Pipe, type PipeTransform } from '@angular/core';
import { Facility } from 'src/app/core/_model/rest';
import { FacilityService } from 'src/app/core/_services/facility.service';

@Pipe({
  name: 'facilityName',
  // standalone: true,
})
export class FacilityNamePipe implements PipeTransform {


  constructor(private facilityService: FacilityService) {
  }

  transform(facility: number | Facility, list?: Facility[]): string {
    if (!facility) {
      return "";
    }
    if (typeof facility === "object") {
      return facility.nameDisplay ?? ""
    }
    if (list) {
      return list?.find((fac) => facility == fac.id)?.nameDisplay ?? '' + facility
    }

    return this.facilityService.facilitiesCached.find(f => (facility == f.id))?.nameDisplay ?? '' + facility;
  }
}
