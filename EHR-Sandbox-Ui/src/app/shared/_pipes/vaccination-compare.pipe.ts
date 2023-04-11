import { Pipe, PipeTransform } from '@angular/core';
import { VaccinationEvent } from 'src/app/core/_model/rest';

@Pipe({
  name: 'vaccinationCompare'
})
export class VaccinationComparePipe implements PipeTransform {

  transform(value: VaccinationEvent, ...args: VaccinationEvent[]): string {
    return JSON.stringify(this.recursiveComparison(value, args[0]));
  }

  recursiveComparison(a: any, b: any) : ComparisonResult | string | null {
    if (a === b) return null;
    if (a instanceof Date && b instanceof Date && (a.getTime() === b.getTime())) return null;
    if ((a === null || a === undefined) && (b === null || b === undefined)) return null;
    // if (a.prototype !== b.prototype) return false;
    if (typeof a === 'string' || typeof b === 'string') {
      return a;
    }
    let result: ComparisonResult = {};
    for (const key in b) {
      if (Object.prototype.hasOwnProperty.call(b, key) && !Object.prototype.hasOwnProperty.call(a, key) ) {
        result[key] = ""
      }
    }
    for (const key in a) {
      if (Object.prototype.hasOwnProperty.call(a, key)) {
        if (Object.prototype.hasOwnProperty.call(b, key)) {
          let next : ComparisonResult | string | null = this.recursiveComparison(a[key], b[key]);
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

export interface ComparisonResult {[index:string]: ComparisonResult | string | null}
