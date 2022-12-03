import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'textShorten'
})
export class TextShortenPipe implements PipeTransform {

  transform(content: string, ...args: number[]): string {
    if (content) {
      return content.length > args[0] + 2? content.substring(0, args[0]) + ".." : content
    }
    return ''
  }


}
