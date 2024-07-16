import { EventEmitter, Injectable, ViewChild } from '@angular/core';
import { ValidatorFn, AbstractControl, ValidationErrors, FormControl } from '@angular/forms';
import { MatInput } from '@angular/material/input';

@Injectable()
export abstract class JsonFormComponent<T extends Object> {

  abstract txtArea: HTMLTextAreaElement;
  abstract matInput: MatInput;
  formControl: FormControl<string>;

  allow_emit: boolean = false

  set model(value: string) {
    this.allow_emit = false
    this.formControl.setValue(value);
    // this.txtArea.selectionEnd = 10

    // this.txtArea.setSelectionRange(1, 10)
    // if (this.txtArea.selection)
    if (this.matInput) {
      console.log(this.matInput.focused, this.txtArea.selectionStart)
      this.txtArea.selectionStart = 3
      console.log(this.txtArea.selectionStart)
    }
    // if ()
    // this.txtArea.focus()
  }
  abstract modelChange: EventEmitter<T>;


  constructor() {
    this.formControl = new FormControl()
    this.formControl.addValidators(this.jsonValidator())
    this.formControl.updateValueAndValidity()
  }

  ngAfterViewInit() {
    this.formControl.valueChanges.subscribe((value) => {
      // let selection = this.txtArea.selectionEnd

      if (this.allow_emit && this.formControl.valid) {
        console.log("emit")
        let selection = this.txtArea.selectionEnd
        this.modelChange.emit(JSON.parse(value))
        // this.txtArea.
        // this.txtArea.selectionEnd = 1
        // this.txtArea.selectionStart = 1
        // this.txtArea.selectionEnd = selection + 20
      } else {
        this.allow_emit = true
      }
    })
    // this.matInput.
  }

  answer: string = ""
  error: boolean = false
  resultLoading: boolean = false

  resultClass(): string {
    if (this.answer === "") {
      return "w3-left w3-padding"
    }
    return this.error ? 'w3-red w3-left w3-padding' : 'w3-green w3-left w3-padding'
  }


  jsonValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      try {
        this.checkType(JSON.parse(control.value))
        return null;
      } catch (error: any) {
        return { message: error }
      }
    };
  }

  abstract checkType(t: T): void

  // abstract send(): void;
}
