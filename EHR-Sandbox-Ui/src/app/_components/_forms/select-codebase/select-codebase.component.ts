import { KeyValuePipe } from '@angular/common';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormControl } from '@angular/forms';
import { map, Observable, of, startWith } from 'rxjs';
import { Code, CodeMap, Form, Reference, ReferenceLink } from 'src/app/_model/structure';
import { CodeMapsService } from 'src/app/_services/code-maps.service';

@Component({
  selector: 'app-select-codebase',
  templateUrl: './select-codebase.component.html',
  styleUrls: ['./select-codebase.component.css']
})
export class SelectCodebaseComponent implements OnInit {

  @Input() form!: Form;
  @Input() referenceFilter!: {[key:string]: Reference} ;
  @Output() referenceEmitter = new EventEmitter<Reference>();

  @Input() model!: any;
  @Output() modelChange = new EventEmitter();


  codeMap!: CodeMap;
  filteredOptions!: Code[];

  constructor(public codeMapsService: CodeMapsService) { }

  ngOnInit(): void {
    this.codeMapsService.getObservableCodeBaseMap().subscribe((codeBaseMap) => {
      if ( this.form.codeMapLabel && codeBaseMap[this.form.codeMapLabel]) {
        this.codeMap = codeBaseMap[this.form.codeMapLabel]

        if (this.referenceFilter) {
          this.filteredOptions = Object.values(this.codeMap)
          // .filter(option => this.referenceFilter.includes(option.value))
        } else {
          this.filteredOptions = Object.values(this.codeMap)
        }
      }
    })
  }

  filterChange(event: string){
    let filterValue = ''
    if (event) {
      filterValue = event.toLowerCase();
    }
    this.filteredOptions = Object.values(this.codeMap).filter(
      option => {
        if (!JSON.stringify(option).toLowerCase().includes(filterValue)) {
          return false
        }
        for (const codeMapType in this.referenceFilter) {
          let included = false
          let scanned = false
          this.referenceFilter[codeMapType].linkTo.forEach((ref) => {
            if (ref.codeset == this.form.codeMapLabel) {
              scanned = true
              if (option.value == ref.value) {
                included = true
              }
            }
          })
          if (scanned && !included) {
            return false
          }
        }
        return true
      })
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
    this.referenceEmitter.emit(this.codeMap[this.model].reference)
  }
}
