import { Pipe, type PipeTransform } from '@angular/core';

@Pipe({
  name: 'facilityName',
  standalone: true,
})
export class FacilityNamePipe implements PipeTransform {

  transform(value: unknown, ...args: unknown[]): unknown {
    return value;
  }

}
