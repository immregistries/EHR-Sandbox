import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'keys'
})
export class KeysPipe implements PipeTransform {

  transform(value: object): any {
    return Object.keys(value);
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

