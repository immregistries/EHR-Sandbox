import { Pipe, PipeTransform } from '@angular/core';
import { VaccinationEvent } from 'src/app/core/_model/rest';
import { ComparisonResult } from 'src/app/core/_model/form-structure';
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
    const differences = this.recursiveComparison(value, args[0]);
    if (!differences) {
      return "MATCH"
    } else {
      return differences;
    }
  }

}
