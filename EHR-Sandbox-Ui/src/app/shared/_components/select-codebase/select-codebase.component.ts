import { Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { FormControl, NgForm } from '@angular/forms';
import { BehaviorSubject, Subscription } from 'rxjs';
import { ComparisonResult, BaseForm, BaseFormOption } from 'src/app/core/_model/structure';
import { Code, CodeMap, CodeReference } from "src/app/core/_model/code-base-map";
import { CodeMapsService } from 'src/app/core/_services/code-maps.service';

@Component({
  selector: 'app-select-codebase',
  templateUrl: './select-codebase.component.html',
  styleUrls: ['./select-codebase.component.css']
})
export class SelectCodebaseComponent implements OnInit {
  formControl: FormControl = new FormControl();
  @ViewChild('selectForm', { static: true })
  selectForm!: NgForm;
  /** Receives */
  private _referenceFilter?: BehaviorSubject<{ [key: string]: { reference: CodeReference; value: string; }; }> | undefined;
  public get referenceFilter(): BehaviorSubject<{ [key: string]: { reference: CodeReference; value: string; }; }> | undefined {
    return this._referenceFilter;
  }
  @Input()
  public set referenceFilter(value: BehaviorSubject<{ [key: string]: { reference: CodeReference; value: string; }; }> | undefined) {
    this._referenceFilter = value;
    this.referenceFilter?.subscribe((ref) => {
      this.filterChange(this.model)
      if (this.filteredCodeMapsOptions?.length == 0) {
        this.warning = true
      }
      // if (this.filteredCodeMapsOptions && this.filteredCodeMapsOptions.length == 1 && !this.erasedOnLastChange && (this.model == '' || !this.model)) {
      //   this.model = this.filteredCodeMapsOptions[0].value
      //   this.valueChanged()
      // }
      this.erasedOnLastChange = false
      this.refreshWarning()
    })

  }
  @Output() referenceEmitter = new EventEmitter<{ reference: CodeReference, value: string }>();

  @Input() baseForm!: BaseForm;

  @Input() warningCheck!: EventListener;

  @Input() model!: string;
  @Output() modelChange = new EventEmitter<any>();

  @Input() toolTipDisabled: boolean = false;
  @Input() compareTo?: ComparisonResult | any | null;

  codeMap?: CodeMap;
  private _warning: boolean = false;
  public get warning(): boolean {
    return this._warning;
  }
  public set warning(value: boolean) {
    this._warning = value;
  }
  /**
   * Used to avoid an interblocking situation with autoselection on filter changes
   */
  erasedOnLastChange: boolean = false;

  constructor(public codeMapsService: CodeMapsService) { }

  private formChangesSubscription!: Subscription

  ngOnInit(): void {
    this.codeMapsService.getObservableCodeBaseMap().subscribe((codeBaseMap) => {
      if (this.baseForm.codeMapLabel && codeBaseMap[this.baseForm.codeMapLabel]) {
        this.codeMap = codeBaseMap[this.baseForm.codeMapLabel]
      }
      this.referenceFilter?.subscribe((ref) => {
        this.filterChange(this.model)
        if (this.filteredCodeMapsOptions && this.filteredCodeMapsOptions.length == 0) {
          this.warning = true
        }
        // if (this.filteredCodeMapsOptions && this.filteredCodeMapsOptions.length == 1 && !this.erasedOnLastChange && (this.model == '' || !this.model)) {
        //   this.model = this.filteredCodeMapsOptions[0].value
        //   this.valueChanged()
        // }
        this.erasedOnLastChange = false
      })
    })
    // this.formControl.valueChanges.subscribe((value) => {
    //   this.valueChanged()
    // })
    this.formChangesSubscription = this.selectForm.form.valueChanges.subscribe((value) => {
      this.valueChanged()
    })
  }

  ngOnDestroy() {
    this.formChangesSubscription.unsubscribe();
  }

  public filteredCodeMapsOptions!: Code[];
  public filteredFormOptions!: BaseFormOption[];
  /**
   * Buffer for ordering filtered options
   */
  private filteredCodeMapsOn: { byValue: Code[], byLabel: Code[], byDescription: Code[], byOther: Code[] } = { byValue: [], byLabel: [], byDescription: [], byOther: [] };
  filterChange(event: string) {
    let filterValue = ''
    if (event) {
      filterValue = event.toLowerCase();
    }
    if (this.codeMap) {
      this.filteredCodeMapsOn = { byValue: [], byLabel: [], byDescription: [], byOther: [] }
      Object.values(this.codeMap).forEach(
        option => {
          if (option.value.toLowerCase().includes(filterValue)) {
            if (this.filterWithReference(option)) {
              this.filteredCodeMapsOn.byValue.push(option)
            }
          } else if (option.label && option.label?.toLowerCase().includes(filterValue)) {
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
      // console.log(this.form.codeMapLabel, filterValue, this.filteredCodeMapsOptions)
    }
    if (this.baseForm.options) {
      this.filteredFormOptions = this.baseForm.options.filter((option) => {
        if (option.code === true && 'true'.includes(filterValue)) {
          return true
        } else if (option.code === false && 'false'.includes(filterValue)) {
          return true
          //@ts-ignore
        } else if (option.code.toLowerCase().includes(filterValue)) {
          return true
        } else if (option.display?.toLowerCase().includes(filterValue)) {
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
      /**
       * Checking if new value should trigger a warning i.e
       */
      this.refreshWarning()
      this.erasedOnLastChange = false
      /**
       * emitting the new code value
       */
      this.modelChange.emit(this.model)
      /**
       * emitting the references linked to the code
       */
      if (this.codeMap && this.codeMap[this.model]) {
        this.referenceEmitter.emit({ reference: (this.codeMap[this.model].reference ?? { linkTo: [] }), value: this.model })
      } else {
        this.referenceEmitter.emit({ reference: { linkTo: [] }, value: this.model })
      }
    } else {
      this.warning = false
      this.erasedOnLastChange = true
      this.filterChange('')
      this.modelChange.emit('')
      this.referenceEmitter.emit(undefined)
    }
  }

  clear() {
    this.model = '';
    this.erasedOnLastChange = true
    console.log('clear', this.erasedOnLastChange)
    this.valueChanged()
    console.log('clear value changed', this.erasedOnLastChange)
  }

  displayCode(codeKey: string): string {
    if (codeKey && this.codeMap && this.codeMap[codeKey]) {
      let code: Code = this.codeMap[codeKey]
      return code.label + ' (' + code.value + ')'
    } else if (this.baseForm.options) {
      let option: BaseFormOption | undefined = this.baseForm.options.find((opt) => opt.code == codeKey)
      if (option?.display) {
        return option.display + ' (' + codeKey + ')'
      }
    }
    return codeKey
  }

  @Input() overrideNoFieldsRequired: boolean = false
  @Input() overrideAllFieldsRequired: boolean = false
  isRequired(): 'true' | 'false' {
    if (this.overrideNoFieldsRequired) {
      return 'false'
    } else if (this.overrideAllFieldsRequired) {
      return 'true'
    } else if (this.baseForm.required) {
      return 'true'
    } else {
      return 'false'
    }
  }

  refreshWarning() {
    if (this.filteredCodeMapsOptions && this.baseForm.codeMapLabel &&
      this.filteredCodeMapsOptions.length < 1 && this.model && this.referenceFilter && this.referenceFilter.getValue()) {
      this.warning = true
    } else {
      this.warning = false
    }
  }
}
