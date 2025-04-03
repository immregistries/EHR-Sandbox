import { Pipe, PipeTransform } from '@angular/core';
import { VaccinationEvent } from 'src/app/core/_model/rest';
import { ComparisonResult } from 'src/app/core/_model/form-structure';

@Pipe({
  name: 'vaccinationCompare'
})
export class VaccinationComparePipe implements PipeTransform {
  private fields_to_ignore: string[] = [
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

  /**
   * Recursive comparision of elements,
   * @param local
   * @param remote
   * @returns returns Comparison Result or null if no differences
   */
  private recursiveComparison(local: any, remote: any): ComparisonResult | any | null {
    if (local === remote) return null;
    console.info('WSH', this.isIsoDate(new Date().toISOString()), new Date().toISOString())
    console.info('Local', this.isIsoDate(local), local)
    console.info('Remote', this.isIsoDate(remote), remote)
    // if (local instanceof Date && remote instanceof Date && (local.getTime() - remote.getDate())) return null;
    if (this.isIsoDate(local) && this.isIsoDate(remote)) {
      // let localDate: Date = new Date(local)
      // localDate.setMilliseconds(0)
      // localDate.setSeconds(0)
      // localDate.setMinutes(0)
      // localDate.setHours(0)
      // let remoteDate = new Date(remote)
      // remoteDate.setMilliseconds(0)
      // remoteDate.setSeconds(0)
      // remoteDate.setMinutes(0)
      // remoteDate.setHours(0)
      // console.info(localDate, remoteDate)
      if (this.areSameDatesIgnoringTime(local, remote)) return null;
    }
    if ((local === null || local === undefined) && (remote === null || remote === undefined)) return null;
    if (local === null || local === undefined || remote === null || remote === undefined) {
      return local
    }
    // if (a.prototype !== b.prototype) return false;
    if ((typeof local === 'string' && local === "" && !remote) || (typeof remote === 'string' && remote === "" && !local)) {
      return null
    }
    if (typeof local === 'string' || typeof remote === 'string') {
      return remote
    }
    let result: ComparisonResult = {};
    for (const key in remote) {
      if (Object.prototype.hasOwnProperty.call(remote, key) && !Object.prototype.hasOwnProperty.call(local, key) && !this.fields_to_ignore.includes(key)) {
        result[key] = remote
      }
    }
    for (const key in local) {
      if (Object.prototype.hasOwnProperty.call(local, key) && !this.fields_to_ignore.includes(key)) {
        if (Object.prototype.hasOwnProperty.call(remote, key)) {
          let next: ComparisonResult | any | null = this.recursiveComparison(local[key], remote[key]);
          if (next != null) {
            result[key] = next;
          }
        } else {
          result[key] = local
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

  private isIsoDate(dateString: any) {
    if (!dateString) {
      return false; // Handle null or empty strings
    }

    const isoRegex = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2}(?:\.\d*)?)Z?([+-]\d{2}:?\d{2})?$/;

    if (!isoRegex.test(dateString)) {
      return false; // Basic ISO 8601 format check failed
    }

    const date = new Date(dateString);

    return !isNaN(date.getTime());
    // const dateParsed = new Date(Date.parse(dateString))
    // return dateParsed.toISOString() === new Date(dateString).toUTCString()
  }

  private areSameDatesIgnoringTime(dateString1: string, dateString2: string): boolean {
    if (!dateString1 || !dateString2) {
      return false; // Handle null or empty strings
    }

    const date1 = new Date(dateString1);
    const date2 = new Date(dateString2);

    if (isNaN(date1.getTime()) || isNaN(date2.getTime())) {
      return false; // Invalid date strings
    }

    const year1 = date1.getFullYear();
    const month1 = date1.getMonth(); // Months are 0-indexed
    const day1 = date1.getDate();

    const year2 = date2.getFullYear();
    const month2 = date2.getMonth();
    const day2 = date2.getDate();

    return year1 === year2 && month1 === month2 && day1 === day2;
  }

}
