import { Pipe, PipeTransform } from '@angular/core';
import { Code, CodeSet } from "src/app/core/_model/code-base-map";
import { CodeMapsService } from 'src/app/core/_services/code-maps.service';

@Pipe({
  name: 'codeMaps'
})
export class CodeMapsPipe implements PipeTransform {
  constructor(private service: CodeMapsService) { }

  transform(value: string, ...args: string[]): Code {
    let codeSet = this.service.getCodeSet(args[0]);
    if (value && codeSet) {
      return codeSet[value] ?? { value: value, label: undefined };
    } else {
      return { value: value, label: '' };
    }
  }

}
