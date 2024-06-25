import { Component, Injectable, Input } from '@angular/core';

@Injectable()
export abstract class JsonFormComponent<T extends Object> {

  @Input()
  model!: string

  answer: string = ""
  error: boolean = false
  resultLoading: boolean = false

  resultClass(): string {
    if (this.answer === "") {
      return "w3-left w3-padding"
    }
    return this.error ? 'w3-red w3-left w3-padding' : 'w3-green w3-left w3-padding'
  }

  abstract send(): void;
}
