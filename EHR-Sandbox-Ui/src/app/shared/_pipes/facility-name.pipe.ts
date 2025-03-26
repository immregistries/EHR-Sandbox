import { Pipe, type PipeTransform } from '@angular/core';
import { Facility } from 'src/app/core/_model/rest';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { FacilityCachePipe } from './facility-cache.pipe';

@Pipe({
  name: 'facilityName',
  // standalone: true,
})
export class FacilityNamePipe implements PipeTransform {


  constructor(private facilityCachePipe: FacilityCachePipe) {
  }

  transform(facility: number | Facility, list?: Facility[]): string {
    if (!facility) {
      return "";
    }

    let obj: Facility | undefined = (this.facilityCachePipe.transform([facility], list) ?? [undefined])[0]
    return obj?.nameDisplay ?? '' + facility
  }
}
