import { Component, Input } from '@angular/core';
import { AbstractDataTableComponent } from '../../_components/abstract-data-table/abstract-data-table.component';
import { AcknowledgementObject } from 'src/app/core/_model/form-structure';
import { EhrPatient, Feedback, VaccinationEvent } from 'src/app/core/_model/rest';
import { FeedbackService } from 'src/app/core/_services/feedback.service';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';
import { trigger, state, style, transition, animate } from '@angular/animations';
import { Sort } from '@angular/material/sort';
import { PatientResumePipe } from '../../_pipes/patient-resume.pipe';
import { RegistryNamePipe } from '../../_pipes/registry-name.pipe';

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
    private patientResumePipe: PatientResumePipe,
    private registryNamePipe: RegistryNamePipe
  ) {
    super();
  }

  @Input()
  patient?: EhrPatient
  @Input()
  vaccination?: VaccinationEvent;
  @Input()
  registryId?: number

  @Input()
  public set singleAck(value: string) {
    // if (!this.dataSource.data) {
    //   this.dataArray = []
    // }
    // console.log(this.dataSource.data)
    this.feedbackService.convertAck(value).subscribe(result => {
      this.dataArray = [result]
      // this.dataSource.data.push(result);
      // console.log(this.dataSource.data)
      // this.dataArray = []

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

  override ngAfterViewInit(): void {
    super.ngAfterViewInit();
    // this.dataArray = []
    this.dataSource.sortingDataAccessor = (data: AcknowledgementObject<Feedback>, sortHeaderId: string) => {
      if (sortHeaderId === "names") {
        return this.patientResumePipe.transform(data.patient, ["name"])
      }
      if (sortHeaderId === "mrn") {
        return this.patientResumePipe.transform(data.patient, ["mrn"])
      }
      if (sortHeaderId === "iis") {
        return this.registryNamePipe.transform(+(data.iis ?? 0))
      }
      //@ts-ignore
      return data[sortHeaderId]
    }
  }
}
