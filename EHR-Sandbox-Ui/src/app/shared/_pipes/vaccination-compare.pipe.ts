import { Pipe, PipeTransform } from '@angular/core';
import { VaccinationEvent } from 'src/app/core/_model/rest';
import { ComparisonResult } from 'src/app/core/_model/structure';

@Pipe({
  name: 'vaccinationCompare'
})
export class VaccinationComparePipe implements PipeTransform {
  private fields_to_ignore: string[] = [
    'vaccinationEvents',
    'actionCode',
    'updatedDate' // TODO change the way updated date is mapped ?
  ]

  transform(value: VaccinationEvent, ...args: (VaccinationEvent | null)[]): {} {
    const differences = this.recursiveComparison(value, args[0]);
    if (!differences) {
      return "MATCH"
    } else {
      return differences;
    }
  }

  recursiveComparison(a: any, b: any): ComparisonResult | any | null {
    if (a === b) return null;
    if (a instanceof Date && b instanceof Date && (a.getTime() === b.getTime())) return null;
    if ((a === null || a === undefined) && (b === null || b === undefined)) return null;
    if (a === null || a === undefined || b === null || b === undefined) {
      return a
    }
    // if (a.prototype !== b.prototype) return false;
    if ((typeof a === 'string' && a === "" && !b) || (typeof b === 'string' && b === "" && !a)) {
      return null
    }
    if (typeof a === 'string' || typeof b === 'string') {
      return b
    }
    let result: ComparisonResult = {};
    for (const key in b) {
      if (Object.prototype.hasOwnProperty.call(b, key) && !Object.prototype.hasOwnProperty.call(a, key) && !this.fields_to_ignore.includes(key)) {
        result[key] = b
      }
    }
    for (const key in a) {
      if (Object.prototype.hasOwnProperty.call(a, key) && !this.fields_to_ignore.includes(key)) {
        if (Object.prototype.hasOwnProperty.call(b, key)) {
          let next: ComparisonResult | any | null = this.recursiveComparison(a[key], b[key]);
          if (next != null) {
            result[key] = next;
          }
        } else {
          result[key] = a
        }
      }
    }
    /**
     * if result is populated
     */
    if (Object.keys(result).length > 0) {
      return result;
    }
    return null;
  }
}
