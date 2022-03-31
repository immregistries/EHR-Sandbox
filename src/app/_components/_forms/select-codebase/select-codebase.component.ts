import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormControl } from '@angular/forms';
import { map, Observable, of, startWith } from 'rxjs';
import { Code, Form } from 'src/app/_model/structure';
import { CodeMapsService } from 'src/app/_services/code-maps.service';

@Component({
  selector: 'app-select-codebase',
  templateUrl: './select-codebase.component.html',
  styleUrls: ['./select-codebase.component.css']
})
export class SelectCodebaseComponent implements OnInit {

  @Input() form!: Form;
  @Input() model!: any;
  @Output() modelChange = new EventEmitter();


  codeMap!: {[key:string]: Code};
  // filteredOptions!: Observable<Code[]>;
  filteredOptions!: Code[];

  constructor(public codeMapsService: CodeMapsService) { }

  ngOnInit(): void {
    this.codeMapsService.getObservableCodeBaseMap().subscribe((codeBaseMap) => {
      if ( this.form.codeMapLabel && codeBaseMap[this.form.codeMapLabel]) {
        this.codeMap = codeBaseMap[this.form.codeMapLabel]
        this.filteredOptions = Object.values(this.codeMap)
      }
    })
  }

  filterChange(event: string){
    const filterValue = event.toLowerCase();
    this.filteredOptions = Object.values(this.codeMap).filter(option =>  JSON.stringify(option).toLowerCase().includes(filterValue));
  }

  displayCode(codeKey: string): string{
    if (codeKey) {
      // let code: Code = this.codeMapsService.getCodeMap(this.form.codeLabel)[codeKey]
      let code: Code = this.codeMap[codeKey]
      return code.label + ' | ' + (this.form.codeLabel? this.form.codeLabel + ': ' : '' ) + code.value
    }
    return codeKey
  }

  valueChanged(){
    console.log(this.model)
  }
}
