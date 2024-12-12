import { Component, Input } from '@angular/core';
import { AbstractDataTableComponent } from '../../_components/abstract-data-table/abstract-data-table.component';
import { AcknowledgementObject } from 'src/app/core/_model/form-structure';
import { Feedback } from 'src/app/core/_model/rest';
import { FeedbackService } from 'src/app/core/_services/feedback.service';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';
import { trigger, state, style, transition, animate } from '@angular/animations';

@Component({
  selector: 'app-ack-table',
  templateUrl: './ack-table.component.html',
  styleUrls: ['./ack-table.component.css'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({ height: '0px', minHeight: '0' })),
      state('expanded', style({ height: '*' })),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ],
})
export class AckTableComponent extends AbstractDataTableComponent<AcknowledgementObject<Feedback>> {

  constructor(
    public snackBarService: SnackBarService,
    public feedbackService: FeedbackService,
  ) {
    super();
  }

  @Input()
  public set singleAck(value: string) {
    this.feedbackService.convertAck(value).subscribe(result => {
      this.dataArray = [result];
    })
  }

  columns = [
    "messageId",
    "patient",
    "msa_2",
    "timestamp",
    "iis",
    "senderId",
    "receiverId",
  ]

  rowClass(element: AcknowledgementObject<Feedback>): string {
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
