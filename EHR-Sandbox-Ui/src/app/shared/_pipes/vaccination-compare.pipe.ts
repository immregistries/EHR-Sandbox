import { Pipe, PipeTransform } from '@angular/core';
import { VaccinationEvent } from 'src/app/core/_model/rest';
import { AbstractComparisonPipe } from './abstract-comparison.pipe';

@Pipe({
  name: 'vaccinationCompare'
})
export class VaccinationComparePipe extends AbstractComparisonPipe implements PipeTransform {
  readonly fields_to_ignore: string[] = [
    'vaccinationEvents',
    'actionCode',
    'createdDate',
    'updatedDate' // TODO change the way updated date is mapped ?
  ]

  public transform(value: VaccinationEvent, ...args: (VaccinationEvent | null)[]): {} {
    const differences = this.packageComp(value, args[0]);
    if (!differences || differences.length === 0) {
      return "MATCH"
    } else {
      return differences;
    }
  }

}
