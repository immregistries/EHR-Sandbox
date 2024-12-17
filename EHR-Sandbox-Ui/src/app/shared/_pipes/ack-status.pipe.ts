import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'ackStatus',
  pure: true
})
export class AckStatusPipe implements PipeTransform {

  transform(value: string): any {
    switch (value.toUpperCase()) {
      case "AE":
      case "E":
      case "ERRORS":
        return 'error'
      case "AW":
      case "W":
      case "WARNINGS":
        return 'warning'
      case "AN":
      case "N":
      case "NOTICES":
        return 'notice'
      case "AI":
      case "I":
      case "INFOS":
        return 'info'
      default:
        return ""
    }
  }

}

// @Pipe({
//   name: 'keys'
// })
// export class ObjectKeysPipe implements PipeTransform {
//   transform(value: object, args: string[]): any {
//     return Object.keys(value);
//   }
// }

