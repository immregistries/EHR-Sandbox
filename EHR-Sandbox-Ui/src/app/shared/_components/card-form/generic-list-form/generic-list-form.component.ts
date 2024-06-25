import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { NextOfKinRelationship } from 'src/app/core/_model/rest';
import FormType, { BaseForm, GenericForm } from 'src/app/core/_model/structure';
import { NextOfKinRelationshipListFormComponent } from './next-of-kin-relationship-list-form/next-of-kin-relationship-list-form.component';
import { error } from 'console';

@Component({
  selector: 'app-generic-list-form',
  templateUrl: './generic-list-form.component.html',
  styleUrls: ['./generic-list-form.component.css']
})
export class GenericListFormComponent<T> implements OnInit {

  private _form!: BaseForm;
  public get form(): BaseForm {
    return this._form;
  }
  @Input()
  public set form(value: BaseForm) {
    this._form = value;
    this.addDefaultValue()
  }
  public _itemList?: (T)[] | undefined;
  public get itemList(): (T)[] | undefined {
    return this._itemList;
  }
  @Input()
  public set itemList(value: (T)[] | undefined) {
    this._itemList = value;
    this.addDefaultValue()
  }
  @Output()
  itemListChange: EventEmitter<(T)[]> = new EventEmitter<(T)[]>()

  ngOnInit(): void {
    // if (!this.itemList || this.itemList.length < 1) {
    //   this.addItem()
    // }
  }

  addItem() {
    /**
     * Necessary to bypass type checking, as I did not find a way to specify that all fields are optional in generic type
     */
    let newItem = JSON.parse(this.EMPTY_VALUE)
    if (!this.itemList || this.itemList.length == 0) {
      this.itemList = []
      this.itemList.push(newItem)
      this.itemListChange.emit(this.itemList)
    } else {
      let lastValue = JSON.stringify(this.itemList[this.itemList.length - 1])
      // console.log(lastValue);
      if (lastValue.length > 3 && !(lastValue === this.EMPTY_VALUE)) {
        this.itemList.push(newItem)
      }
    }
  }

  // removeItem(t: T) {
  //   this.itemList = this.itemList?.filter((item) => JSON.stringify(item) === JSON.stringify(t))
  // }

  removeItem(index: number) {
    if (this.itemList) {
      this.itemList = this.itemList.splice(index, 1)
      this.itemListChange.emit(this.itemList)
    }
  }

  FORMS!: GenericForm<T>[];
  /** example of an empty object used to prevent adding new item when last is still empty */
  EMPTY_VALUE: string = '{}';

  @Input() overrideNoFieldsRequired: boolean = false
  @Input() overrideAllFieldsRequired: boolean = false

  addDefaultValue() {
    if ((!this.itemList || this.itemList.length < 1) && this.form.defaultListEmptyValue) {
      this._itemList = []
      if (this.form.defaultListEmptyValue.length < 2) {
        this._itemList.push(JSON.parse(this.EMPTY_VALUE))
      } else {
        this._itemList.push(JSON.parse(this.form.defaultListEmptyValue))
      }
    }
  }

}
