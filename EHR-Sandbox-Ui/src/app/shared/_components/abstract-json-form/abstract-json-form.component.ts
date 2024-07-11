import { Component, EventEmitter, Injectable, Input } from '@angular/core';
import { ValidatorFn, AbstractControl, ValidationErrors, FormControl } from '@angular/forms';
import { EhrPatient } from 'src/app/core/_model/rest';

@Injectable()
export abstract class JsonFormComponent<T extends Object> {
  formControl: FormControl<string>;

  allow_emit: boolean = false


  abstract set model(value: string);
  abstract modelChange: EventEmitter<T>;


  constructor() {
    this.formControl = new FormControl()
    this.formControl.addValidators(this.jsonValidator())
    this.formControl.updateValueAndValidity()
    this.formControl.valueChanges.subscribe((value) => {
      // if (this.formControl.valid != valid) {
      //   this
      // }
      if (this.allow_emit && this.formControl.valid) {
        this.modelChange.emit(JSON.parse(value))
      } else {
        this.allow_emit = true
      }
    })
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
