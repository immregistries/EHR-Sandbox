import { Component } from '@angular/core';
import { AbstractDataTableComponent } from '../../_components/abstract-data-table/abstract-data-table.component';
import { AckSortedResults } from 'src/app/core/_model/form-structure';
import { Feedback } from 'src/app/core/_model/rest';

@Component({
  selector: 'app-ack-table',
  templateUrl: './ack-table.component.html',
  styleUrls: ['./ack-table.component.css']
})
export class AckTableComponent extends AbstractDataTableComponent<AckSortedResults<Feedback>> {

  columns = [

  ]

  rowClass(element: AckSortedResults<Feedback>): string {
    switch (element.msa_2) {
      case "E":
        return 'error'
      case "W":
        return 'warning'
      case "N":
        return 'notice'
      case "I":
        return 'info'
      default:
        return ""
    }
  }

}
