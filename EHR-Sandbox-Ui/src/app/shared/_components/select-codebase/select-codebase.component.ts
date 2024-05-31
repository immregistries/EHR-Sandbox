import { Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { NgForm } from '@angular/forms';
import { BehaviorSubject, Subscription } from 'rxjs';
import { Code, CodeMap, ComparisonResult, BaseForm, CodeReference } from 'src/app/core/_model/structure';
import { CodeMapsService } from 'src/app/core/_services/code-maps.service';

@Component({
  selector: 'app-select-codebase',
  templateUrl: './select-codebase.component.html',
  styleUrls: ['./select-codebase.component.css']
})
export class SelectCodebaseComponent implements OnInit {
  @Input() referenceFilter?: BehaviorSubject<{ [key: string]: { reference: CodeReference, value: string } }>;
  @Output() referenceEmitter = new EventEmitter<{ reference: CodeReference, value: string }>();

  @Input() form!: BaseForm;

  @Input() warningCheck!: EventListener;

  @Input() model!: string;
  @Output() modelChange = new EventEmitter<any>();

  @ViewChild('selectForm', { static: true }) selectForm!: NgForm;

  @Input() toolTipDisabled: boolean = false;
  @Input() compareTo?: ComparisonResult | any | null;

  codeMap?: CodeMap;
  filteredOptions!: Code[];
  warning: boolean = false;
  /**
   * Used to avoid an interblocking situation with autoselection on filter changes
   */
  erasedOnLastChange: boolean = false;


  constructor(public codeMapsService: CodeMapsService) { }

  private formChangesSubscription!: Subscription

  ngOnInit(): void {
    this.codeMapsService.getObservableCodeBaseMap().subscribe((codeBaseMap) => {
      if (this.form.codeMapLabel && codeBaseMap[this.form.codeMapLabel]) {
        this.codeMap = codeBaseMap[this.form.codeMapLabel]
      }
      this.referenceFilter?.subscribe((ref) => {
        this.filterChange(this.model)
        if (this.filteredOptions && this.filteredOptions.length == 0) {
          this.warning = true
        }
        // if (this.filteredOptions && this.filteredOptions.length == 1 && ( this.model == '' || !this.model )  && this.erasedOnLastChange == false) {
        if (this.filteredOptions && this.filteredOptions.length == 1 && !this.erasedOnLastChange && (this.model == '' || !this.model)) {
          this.model = this.filteredOptions[0].value
          this.valueChanged()
          // this.autofilled = true
        }
        this.erasedOnLastChange = false
      })
    })
    this.formChangesSubscription = this.selectForm.form.valueChanges.subscribe((value) => {
      this.valueChanged()
    })
  }

  ngOnDestroy() {
    this.formChangesSubscription.unsubscribe();
  }

  filterChange(event: string) {
    let filterValue = ''
    if (event) {
      filterValue = event.toLowerCase();
    }
    if (this.codeMap) {
      this.filteredOptions = Object.values(this.codeMap).filter(
        option => {
          if (!JSON.stringify(option).toLowerCase().includes(filterValue)) {
            return false
          } else {
            return this.filterWithReference(option)
          }
        }
      )
    }
  }

  filterWithReference(option: Code): boolean {
    let scanned = false
    let included = false
    if (this.referenceFilter) {
      for (const codeMapType in this.referenceFilter.getValue()) {
        // Checking if option is referred to in the other codes selected
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
        if (option && option.reference) {
          option.reference.linkTo.forEach((ref) => {
            if (ref.codeset == codeMapType) {
              scanned = true
              if (this.referenceFilter?.getValue()[codeMapType].value == ref.value) {
                included = true
              }
            }
          }
          )
        }
        if (scanned && !included) {
          return false
        }
      }
    }
    return true
  }

  valueChanged() {
    if (this.model && this.model != '') {
      // this.warning = !this.filterWithReference(this.codeMap[this.model])
      if (this.filteredOptions && this.form.codeMapLabel &&
        this.filteredOptions.length < 1 && this.model && this.referenceFilter && this.referenceFilter.getValue()) {
        this.warning = true
      } else {
        this.warning = false
      }
      this.erasedOnLastChange = false
      this.modelChange.emit(this.model)
      if (this.codeMap && this.codeMap[this.model]) {
        this.referenceEmitter.emit({ reference: (this.codeMap[this.model].reference ?? { linkTo: [] }), value: this.model })
      } else {
        this.referenceEmitter.emit({ reference: { linkTo: [] }, value: this.model })
      }
    } else {
      this.warning = false
      this.erasedOnLastChange = true
      this.modelChange.emit('')
      this.referenceEmitter.emit(undefined)
    }
  }

  displayCode(codeKey: string): string {
    if (codeKey && this.codeMap && this.codeMap[codeKey]) {
      let code: Code = this.codeMap[codeKey]
      return code.label + ' (' + code.value + ')'
    } else {
      return codeKey
    }
  }

  clear() {
    this.model = '';
    this.erasedOnLastChange = true
    this.valueChanged()
  }

  @Input() overrideNoFieldsRequired: boolean = false
  isRequired(): 'true' | 'false' {
    if (this.overrideNoFieldsRequired) {
      return 'false'
    } else if (this.form.required) {
      return 'true'
    } else {
      return 'false'
    }
  }
}
