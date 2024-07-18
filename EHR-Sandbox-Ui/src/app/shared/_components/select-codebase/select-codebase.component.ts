import { Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { AbstractControl, FormControl, NgForm, ValidationErrors, ValidatorFn } from '@angular/forms';
import { BehaviorSubject } from 'rxjs';
import { ComparisonResult, BaseForm, BaseFormOption } from 'src/app/core/_model/structure';
import { Code, CodeSet, CodeReference, CodeReferenceTable, CodeReferenceTableMember } from "src/app/core/_model/code-base-map";
import { CodeMapsService } from 'src/app/core/_services/code-maps.service';
import { MatAutocomplete } from '@angular/material/autocomplete';
import { AbstractBaseFormComponent } from '../card-form/abstract-base-form/abstract-base-form.component';

@Component({
  selector: 'app-select-codebase',
  templateUrl: './select-codebase.component.html',
  styleUrls: ['./select-codebase.component.css']
})
export class SelectCodebaseComponent extends AbstractBaseFormComponent implements OnInit {
  @ViewChild('selectCodebaseForm', { static: true })
  selectClinicianForm!: NgForm;
  @ViewChild('auto')
  private matAutoComplete!: MatAutocomplete;

  @Input()
  public baseForm!: BaseForm;
  @Input()
  public set model(value: string) {
    this.formControl.setValue(value);
    // this.valueChanged()
    // this._blockReferenceEmit = true
  }
  @Output()
  public modelChange = new EventEmitter<any>();
  @Input()
  public overrideNoFieldsRequired: boolean = false
  @Input()
  public overrideAllFieldsRequired: boolean = false

  @Input()
  public toolTipDisabled: boolean = false;
  @Input()
  public compareTo?: ComparisonResult | any | null;


  private codeSet?: CodeSet;
  public formControl: FormControl<string> = new FormControl();

  public filteredCodeMapsOptions!: Code[];
  public filteredFormOptions!: BaseFormOption[];
  /**
  * Buffer for ordering filtered options
  */
  private filteredCodeMapsOn: { byValue: Code[], byLabel: Code[], byDescription: Code[], byOther: Code[] } = { byValue: [], byLabel: [], byDescription: [], byOther: [] };


  private _blockReferenceEmit: boolean = false
  /** Receives */
  private _referenceFilter: BehaviorSubject<CodeReferenceTable> | undefined;
  public get referenceFilter(): BehaviorSubject<CodeReferenceTable> | undefined {
    return this._referenceFilter;
  }
  @Input()
  public set referenceFilter(value: BehaviorSubject<CodeReferenceTable> | undefined) {
    this._referenceFilter = value;
    this.referenceFilter?.subscribe((ref) => {
      this.filterChange(this.formControl.value)
      this._blockReferenceEmit = true
      // console.log(this.baseForm.attribute, " reference set validity")
      this.formControl.updateValueAndValidity()
      this._blockReferenceEmit = false
    })
  }
  @Output()
  referenceEmitter = new EventEmitter<CodeReferenceTableMember>();

  constructor(public codeMapsService: CodeMapsService) { super() }

  ngOnInit(): void {
    this.formControl.addValidators(this.codebaseReferenceValidator())
    this.codeSet = this.codeMapsService.getCodeSet(this.baseForm.codeMapLabel)
    this.filterChange(this.formControl.value)
    this.formControl.valueChanges.subscribe((value) => {
      this.filterChange(value)
      // console.log(value)

      // if (!this._blockReferenceEmit) {
      //   console.log(value)
      //   this.modelChange.emit(this.formControl.value ?? '')
      // } else {
      //   this._blockReferenceEmit = false

      // }
    })
  }


  filterChange(event: string) {
    let filterValue = event ? event.toLowerCase() : ''
    if (!(typeof event == 'string')) {
      filterValue = ''
    }
    if (this.codeSet) {
      this.filteredCodeMapsOn = { byValue: [], byLabel: [], byDescription: [], byOther: [] }
      Object.values(this.codeSet).forEach(
        option => {
          if (option.value.toLowerCase().includes(filterValue)) {
            if (this.filterWithReference(option)) {
              this.filteredCodeMapsOn.byValue.push(option)
            }
          } else if (this.displayCode(option.value).toLowerCase().includes(filterValue)) {
            if (this.filterWithReference(option)) {
              this.filteredCodeMapsOn.byLabel.push(option)
            }
          } else if (option.description?.toLowerCase().includes(filterValue)) {
            if (this.filterWithReference(option)) {
              this.filteredCodeMapsOn.byDescription.push(option)
            }
          }
        }
      )
      this.filteredCodeMapsOptions = this.filteredCodeMapsOn.byValue.concat(this.filteredCodeMapsOn.byLabel, this.filteredCodeMapsOn.byDescription, this.filteredCodeMapsOn.byOther)
    }
    if (this.baseForm.options) {
      this.filteredFormOptions = this.baseForm.options.filter((option) => {
        if (option.code === true && 'true'.includes(filterValue)) {
          return true
        } else if (option.code === false && 'false'.includes(filterValue)) {
          return true
        } else if (this.displayCode(option.code).toLowerCase().includes(filterValue)) {
          return true
        } else if (option.definition?.toLowerCase().includes(filterValue)) {
          return true
        } else {
          return false
        }
      })
    }
  }

  filterWithReference(option: Code): boolean {
    return !this.incompatibleReferences(option)
  }

  incompatibleReferences(option: Code): string | null {
    let scanned = false
    let included = false
    if (this.referenceFilter) {
      for (const codeMapType in this.referenceFilter.getValue()) {
        // Checking if option is referred to in the other codes selected
        scanned = false
        included = false
        this.referenceFilter.getValue()[codeMapType].reference.linkTo.forEach((ref) => {
          if (ref.codeset == this.baseForm.codeMapLabel) {
            scanned = true
            if (option.value == ref.value) {
              included = true
            }
          }
        })
        if (scanned && !included) {
          return 'Incompatible with ' + codeMapType + ' ' + this.referenceFilter.getValue()[codeMapType].value
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
          return 'Incompatible with ' + codeMapType
        }
      }
    }
    return null
  }

  valueChanged(log?: string) {
    console.log('log', log, this.formControl.value)
    this.modelChange.emit(this.formControl.value ?? '')
    this.emitReferences()
  }

  emitReferences() {
    if (!this._blockReferenceEmit) {
      if (!this.formControl.value || this.formControl.value == '') {
        this.referenceEmitter.emit(undefined)
      } else if (this.codeSet && this.codeSet[this.formControl.value]) {
        this.referenceEmitter.emit({ reference: (this.codeSet[this.formControl.value].reference ?? { linkTo: [] }), value: this.formControl.value })
      } else {
        this.referenceEmitter.emit({ reference: { linkTo: [] }, value: this.formControl.value })
      }
    } else {
      // this._blockReferenceEmit = false
    }
  }

  codebaseReferenceValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!this.codeSet) {
        return null
      }
      const valueCode: Code | undefined = Object.values(this.codeSet).find((code) => code.value === control.value)
      if (!valueCode) {
        return null
      }
      const message = this.incompatibleReferences(valueCode);
      return message ? { codeMapIssue: message } : null;
    };
  }

  clear() {
    this.formControl.setValue('');
    this.modelChange.emit('')
    this.emitReferences()
  }

  displayCode(codeKey: string | boolean): string {
    if (typeof codeKey != 'boolean' && codeKey && this.codeSet && this.codeSet[codeKey]) {
      let code: Code = this.codeSet[codeKey]
      return code.label + ' (' + code.value + ')'
    } else if (this.baseForm.options) {
      let option: BaseFormOption | undefined = this.baseForm.options.find((opt) => opt.code == codeKey)
      if (option?.display) {
        return option.display + ' (' + codeKey + ')'
      }
    }
    if (typeof codeKey == 'boolean') {
      return codeKey + ""
    } else {
      return codeKey
    }
  }


  /**
   * On press enter key select first visible option
   */
  submit() {
    if (this.matAutoComplete.options && this.matAutoComplete.options.first) {
      this.model = this.matAutoComplete.options.first?.value ?? this.matAutoComplete.options.first?.value.code ?? this.model
      this.valueChanged('enter')
    } else {
      this.valueChanged('enter')
    }
  }

}
