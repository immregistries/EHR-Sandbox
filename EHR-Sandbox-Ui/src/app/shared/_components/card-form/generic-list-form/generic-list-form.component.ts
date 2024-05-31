import { Component, EventEmitter, Input, Output } from '@angular/core';
import FormType, { BaseForm, GenericForm } from 'src/app/core/_model/structure';

@Component({
  selector: 'app-generic-list-form',
  templateUrl: './generic-list-form.component.html',
  styleUrls: ['./generic-list-form.component.css']
})
export class GenericListFormComponent<T> {

  @Input()
  form!: BaseForm;
  @Input()
  itemList?: (T)[]
  @Output()
  itemListChange: EventEmitter<(T)[]> = new EventEmitter<(T)[]>()

  addItem() {
    /**
     * Necessary to bypass type checking, as I did not find a way to specify that all fields are optional in generic type
     */
    let newItem = JSON.parse(JSON.stringify({}))
    if (!this.itemList || this.itemList.length == 0) {
      this.itemList = []
      this.itemList.push(newItem)
      this.itemListChange.emit(this.itemList)
    } else {
      let lastValue = JSON.stringify(this.itemList[this.itemList.length - 1])
      console.log(lastValue);
      if (lastValue.length > 3 && !(lastValue === this.EMPTY_VALUE)) {
        this.itemList.push(newItem)
        this.itemListChange.emit(this.itemList)
      }
    }
  }

  FORMS!: GenericForm<T>[];
  /** example of an empty object used to prevent adding new item when last is still empty */
  EMPTY_VALUE: string = '{}';



}
