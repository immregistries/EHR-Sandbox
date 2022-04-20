import { KeyValuePipe } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormControl } from '@angular/forms';
import { BehaviorSubject, map, Observable, of, startWith } from 'rxjs';
import { Patient, VaccinationEvent } from 'src/app/_model/rest';
import { Code, CodeMap, Form, Reference, ReferenceLink } from 'src/app/_model/structure';
import { CodeMapsService } from 'src/app/_services/code-maps.service';

@Component({
  selector: 'app-select-codebase',
  templateUrl: './select-codebase.component.html',
  styleUrls: ['./select-codebase.component.css']
})
export class SelectCodebaseComponent implements OnInit {
	@Input() referenceFilter!: BehaviorSubject<{[key:string]: {reference: Reference, value: string}}>;
  @Output() referenceEmitter = new EventEmitter<{reference: Reference, value: string}>();

  @Input() form!: Form;

  @Input() model!: string;
  @Output() modelChange = new EventEmitter<any>();


  codeMap!: CodeMap;
  filteredOptions!: Code[];
  warning: boolean = false;

  constructor(public codeMapsService: CodeMapsService) { }

  ngOnInit(): void {
    this.codeMapsService.getObservableCodeBaseMap().subscribe((codeBaseMap) => {
      if ( this.form.codeMapLabel && codeBaseMap[this.form.codeMapLabel]) {
        this.codeMap = codeBaseMap[this.form.codeMapLabel]
        // this.filterChange('')
      }
      this.referenceFilter.subscribe((ref) => {
        // console.log(this.form.codeMapLabel + " test")
        this.filterChange(this.model)
        if (this.filteredOptions && this.filteredOptions.length == 0 ) {
          this.warning = true
        }
        // if (this.filteredOptions && this.filteredOptions.length == 1 && ( this.model == '' || !this.model )) {
        //   this.model = this.filteredOptions[0].value
        //   this.valueChanged()
        // }
      })
    })
  }

  filterChange(event: string){
    let filterValue = ''
    if (event) {
      filterValue = event.toLowerCase();
    }
    if(this.codeMap){
      this.filteredOptions = Object.values(this.codeMap).filter(
        option => {
          if (!JSON.stringify(option).toLowerCase().includes(filterValue)) {
            return false
          }
          return this.filterWithReference(option)
        }
      )

    }
  }

  filterWithReference(option: Code): boolean {
    let scanned = false
    let included = false
    for (const codeMapType in this.referenceFilter.getValue()) {
      // Checking if optionned is referred to in the other codes selected
      scanned = false
      included = false
      this.referenceFilter.getValue()[codeMapType].reference.linkTo.forEach((ref) => {
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
       // Scanning the options own references
       scanned = false
       included = false
       if ( option && option.reference) {
         option.reference.linkTo.forEach((ref) => {
           if (ref.codeset == codeMapType) {
             scanned = true
             if (this.referenceFilter.getValue()[codeMapType].value == ref.value) {
               included = true
             }
           }
         }
       )}
       if (scanned && !included) {
         return false
       }
    }
    return true
  }

  displayCode(codeKey: string): string{
    if (codeKey && this.codeMap[codeKey]) {
      let code: Code = this.codeMap[codeKey]
      return code.label + ' | ' + code.value
    } else {
      return codeKey
    }
  }

  valueChanged(){
    if (this.model) {
      this.warning = !this.filterWithReference(this.codeMap[this.model])
      if (this.filteredOptions && this.form.codeMapLabel &&
          this.filteredOptions.length < 1 && this.model && this.referenceFilter.getValue()) {
        this.warning = true
      } else {
        this.warning = false
      }
      this.modelChange.emit(this.model)
      if (this.codeMap[this.model] && this.codeMap[this.model].reference) {
        this.referenceEmitter.emit({reference: (this.codeMap[this.model].reference ?? {linkTo:[]}), value: this.model})
      } else {
        this.referenceEmitter.emit({reference: {linkTo:[]}, value: this.model})
      }
    } else {
      this.warning = false
      this.modelChange.emit('')
      this.referenceEmitter.emit(undefined)
    }
  }
}
