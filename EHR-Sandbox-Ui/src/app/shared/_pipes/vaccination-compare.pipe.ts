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

  transform(value: VaccinationEvent, ...args: (VaccinationEvent | null)[]): {} {
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
  recursiveComparison(local: any, remote: any): ComparisonResult | any | null {
    if (local === remote) return null;
    // if (local instanceof Date && remote instanceof Date && (local.getTime() - remote.getDate())) return null;
    if (this.isIsoDate(local) && this.isIsoDate(remote)) {
      let localDate = new Date(local).setMilliseconds(0)
      let remoteDate = new Date(remote).setMilliseconds(0)
      if (localDate === remoteDate) return null;
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

  isIsoDate(date: any) {
    const dateParsed = new Date(Date.parse(date))
    return dateParsed.toUTCString() === new Date(date).toUTCString()
  }
}
