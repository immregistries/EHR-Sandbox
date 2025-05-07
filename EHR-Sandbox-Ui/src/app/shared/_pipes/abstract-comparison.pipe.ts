import { Directive } from "@angular/core";
import { ComparisonResult } from "src/app/core/_model/form-structure";
import rdiff, { rdiffResult } from "recursive-diff"

@Directive()
export abstract class AbstractComparisonPipe {
  abstract fields_to_ignore: string[]


  protected packageComp(local: any, remote: any): rdiffResult[] {
    return rdiff.getDiff(local, remote, true);
  }

  /**
     * Recursive comparision of elements,
     * @param local
     * @param remote
     * @returns returns Comparison Result or null if no differences
     */
  protected recursiveComparison(local: any, remote: any): ComparisonResult | any | null {
    if (local === remote) return null;
    if (this.isIsoDate(local) && this.isIsoDate(remote)) {
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
    if (Object.values(result).length > 0) {
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
