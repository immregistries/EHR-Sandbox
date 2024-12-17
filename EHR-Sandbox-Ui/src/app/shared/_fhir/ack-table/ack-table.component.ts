import { Component, Input } from '@angular/core';
import { AbstractDataTableComponent } from '../../_components/abstract-data-table/abstract-data-table.component';
import { AcknowledgementObject } from 'src/app/core/_model/form-structure';
import { EhrPatient, Feedback, VaccinationEvent } from 'src/app/core/_model/rest';
import { FeedbackService } from 'src/app/core/_services/feedback.service';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';
import { PatientResumePipe } from '../../_pipes/patient-resume.pipe';
import { RegistryNamePipe } from '../../_pipes/registry-name.pipe';
import Chart from 'chart.js/auto';


@Component({
  selector: 'app-ack-table',
  templateUrl: './ack-table.component.html',
  styleUrls: ['./ack-table.component.scss'],
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
  patientId?: number
  @Input()
  vaccinationId?: number;
  @Input()
  registryId?: number

  @Input()
  public set singleAck(value: string) {
    if (this.dataSource.data.length < 1) {
      this.dataArray = []
    }
    // console.log(this.dataSource.data)
    if (value && value.length > 1) {
      this.feedbackService.convertAck(value, this.registryId, this.patientId, this.vaccinationId).subscribe(result => {
        let array = JSON.parse(JSON.stringify(this.dataSource.data))
        result.id = array.push(result)
        // console.log(this.dataSource.data)
        this.dataArray = array
        // this.dataArray = [result]
        // console.log(this.dataSource.data)
        // this.dataArray = []
        this.updateChart()
      })
    }
  }



  rowClass(element: AcknowledgementObject<Feedback>): string {
    switch (element.msa_2) {
      case "AE":
        return 'errors'
      case "AW":
        return 'warnings'
      case "AN":
        return 'notices'
      case "AI":
        return 'infos'
      default:
        return ""
    }
  }

  actualRowClass(element: AcknowledgementObject<Feedback>) {
    return this.rowClass(element) + " element-row"
  }

  columns = [
    "messageId",
    "patient",
    "msa_2",
    "timestamp",
    // "iis",
    "sender",
    "destination",
  ]

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
      if (sortHeaderId === "iis") {
        return this.registryNamePipe.transform(+(data.iis ?? 0))
      }
      //@ts-ignore
      return data[sortHeaderId]
    }
  }

  public messagesChart: any;
  public errChart: any;

  updateChart() {
    let messageData = [0, 0, 0, 0]
    let errData = [0, 0, 0, 0]
    this.dataSource.data.forEach(element => {
      if (element.msa_2 === "AE") {
        messageData[0]++
      } else if (element.msa_2 === "AW") {
        messageData[1]++
      } else if (element.msa_2 === "AN") {
        messageData[2]++
      } else {
        messageData[3]++
      }
      errData[0] += element.sortedResult.errors.length
      errData[1] += element.sortedResult.warnings.length
      errData[2] += element.sortedResult.notices.length
      errData[3] += element.sortedResult.infos.length
    });

    this.messagesChart = new Chart("MessagesStatusChart", {
      type: 'pie', //this denotes tha type of chart
      data: {// values on X-Axis
        labels: ['Rejected', 'Rejected with Warnings', 'Accepted with Notices', 'Accepted'],
        datasets: [{
          label: 'Status',
          data: messageData,
          backgroundColor: [
            'red',
            'orange',
            'yellow',
            'green',
          ],
          hoverOffset: 4
        }],
      },
      options: {
        aspectRatio: 5,
        plugins: {
          title: {
            display: true,
            text: 'Status of processed acknowledgments'
          }
        }
      }

    });

    this.errChart = new Chart("ErrChart", {
      type: 'pie', //this denotes tha type of chart

      data: {// values on X-Axis
        labels: ['Error', 'Warnings', 'Notices', 'Informational'],
        datasets: [{
          label: 'Severity',
          data: errData,
          backgroundColor: [
            'red',
            'orange',
            'yellow',
            'grey',
          ],
          hoverOffset: 4
        }],
      },
      options: {
        aspectRatio: 5,
        plugins: {
          title: {
            display: true,
            text: 'Severity of messages'
          }
        }
      }

    });
  }


}
