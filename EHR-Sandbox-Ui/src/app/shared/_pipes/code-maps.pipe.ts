import { Pipe, PipeTransform } from '@angular/core';
import { Code, CodeMap } from "src/app/core/_model/code-base-map";
import { CodeMapsService } from 'src/app/core/_services/code-maps.service';

@Pipe({
  name: 'codeMaps'
})
export class CodeMapsPipe implements PipeTransform {
  constructor(private service: CodeMapsService) { }

  transform(value: string, ...args: string[]): Code {
    if (!value) {
      return { value: value, label: '' }
    }
    return this.service.getCodeMap(args[0])[value] ?? { value: value, label: '' };
  }

}
