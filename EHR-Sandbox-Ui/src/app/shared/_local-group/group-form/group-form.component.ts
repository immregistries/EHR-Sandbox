import { Component } from '@angular/core';
import { EhrGroup } from 'src/app/core/_model/rest';
import FormType, { FormCard, FormCardGeneric } from 'src/app/core/_model/structure';

@Component({
  selector: 'app-group-form',
  templateUrl: './group-form.component.html',
  styleUrls: ['./group-form.component.css']
})
export class GroupFormComponent {


  formCards: FormCardGeneric<EhrGroup>[] =  [
    {title: 'Name',  cols: 3, rows: 1, forms: [
      {type: FormType.text, title: 'name', attribute: 'name'},
      {type: FormType.text, title: 'description', attribute: 'description'},
    ]}
  ]

}
