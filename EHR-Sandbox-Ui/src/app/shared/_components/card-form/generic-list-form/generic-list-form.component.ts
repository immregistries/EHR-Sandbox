import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { BaseForm, GenericForm } from 'src/app/core/_model/form-structure';
import { AbstractBaseFormComponent } from '../abstract-base-form/abstract-base-form.component';

@Component({
  // selector: 'app-generic-list-form',
  templateUrl: './generic-list-form.component.html',
  styleUrls: ['./generic-list-form.component.css']
})
export class GenericListFormComponent<T> extends AbstractBaseFormComponent implements OnInit {

  private _compareTo?: string
  @Input()
  public set compareTo(value: string | undefined) {
    this._compareTo = value;
  }
  public get compareTo(): string | undefined {
    return this._compareTo
  }

  getCompareTo(i: number, attributeName: string): any {
    //@ts-ignore
    return this.compareTo && this.compareTo[i] ? this.compareTo[i][attributeName] : undefined
  }


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
  public modelChange: EventEmitter<(T)[]> = new EventEmitter<(T)[]>()

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

  @Input()
  public single_mode: boolean = false

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
