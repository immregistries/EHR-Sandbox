import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { NextOfKinRelationship } from 'src/app/core/_model/rest';
import FormType, { BaseForm, GenericForm } from 'src/app/core/_model/structure';
import { NextOfKinRelationshipListFormComponent } from './next-of-kin-relationship-list-form/next-of-kin-relationship-list-form.component';
import { error } from 'console';
import { AbstractBaseFormComponent } from '../abstract-base-form/abstract-base-form.component';
import { flush } from '@angular/core/testing';

@Component({
  // selector: 'app-generic-list-form',
  templateUrl: './generic-list-form.component.html',
  styleUrls: ['./generic-list-form.component.css']
})
export class GenericListFormComponent<T> extends AbstractBaseFormComponent implements OnInit {

  private _baseForm!: BaseForm;
  public get baseForm(): BaseForm {
    return this._baseForm;
  }
  @Input()
  public set baseForm(value: BaseForm) {
    this._baseForm = value;
    this.addDefaultValue()
  }
  public _itemList?: (T)[] | undefined;
  public get model(): (T)[] | undefined {
    return this._itemList;
  }
  @Input()
  public set model(value: (T)[] | undefined) {
    this._itemList = value;
    this.addDefaultValue()
  }
  @Output()
  modelChange: EventEmitter<(T)[]> = new EventEmitter<(T)[]>()

  ngOnInit(): void {
    // if (!this.itemList || this.itemList.length < 1) {
    //   this.addItem()
    // }
  }

  addItem() {
    /**
     * Necessary to bypass type checking, as I did not find a way to specify that all fields are optional in generic type
     */
    const newItem = JSON.parse(this.EMPTY_VALUE)
    if (!this._itemList) {
      this._itemList = []
    }
    if (this._itemList.length == 0) {
      this._itemList.push(newItem)
      this.modelChange.emit(this.model)
    } else {
      const lastValue = JSON.stringify(this._itemList[this._itemList.length - 1])
      // console.log(lastValue);
      if (lastValue.length > 3 && !(lastValue === this.EMPTY_VALUE)) {
        this._itemList.push(newItem)
        this.modelChange.emit(this.model)
      }
    }
  }

  // removeItem(t: T) {
  //   this.itemList = this.itemList?.filter((item) => JSON.stringify(item) === JSON.stringify(t))
  // }

  removeItem(index: number) {
    if (this.model) {
      this.model.splice(index, 1)
      this.modelChange.emit(this.model)
    }
  }

  FORMS!: GenericForm<T>[];
  /** example of an empty object used to prevent adding new item when last is still empty */
  EMPTY_VALUE: string = '{}';

  @Input() overrideNoFieldsRequired: boolean = false
  @Input() overrideAllFieldsRequired: boolean = false

  addDefaultValue() {
    if ((!this.model || this.model.length < 1) && this.baseForm.defaultListEmptyValue) {
      this._itemList = []
      if (this.baseForm.defaultListEmptyValue.length < 2) {
        this._itemList.push(JSON.parse(this.EMPTY_VALUE))
      } else {
        this._itemList.push(JSON.parse(this.baseForm.defaultListEmptyValue))
      }
    }
  }

}
