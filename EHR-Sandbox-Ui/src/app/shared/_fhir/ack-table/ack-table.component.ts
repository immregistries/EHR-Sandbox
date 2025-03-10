import { Component, Input } from '@angular/core';
import { AbstractDataTableComponent } from '../../_components/abstract-data-table/abstract-data-table.component';
import { AcknowledgementObject } from 'src/app/core/_model/form-structure';
import { EhrPatient, Feedback, VaccinationEvent } from 'src/app/core/_model/rest';
import { FeedbackService } from 'src/app/core/_services/feedback.service';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';
import { PatientResumePipe } from '../../_pipes/patient-resume.pipe';
import { RegistryNamePipe } from '../../_pipes/registry-name.pipe';
import Chart from 'chart.js/auto';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { AckDisplayComponent } from '../ack-display/ack-display.component';
import { MatDialog } from '@angular/material/dialog';
import { merge, tap } from 'rxjs';
import { PatientDashboardComponent } from '../../_patient/patient-dashboard/patient-dashboard.component';
import { VaccinationDashboardComponent } from '../../_vaccination/vaccination-dashboard/vaccination-dashboard.component';
import { VaccinationResumePipe } from '../../_pipes/vaccination-resume.pipe';


@Component({
  selector: 'app-ack-table',
  templateUrl: './ack-table.component.html',
  styleUrls: ['./ack-table.component.scss'],
})
export class AckTableComponent extends AbstractDataTableComponent<AcknowledgementObject<Feedback>> {
  columns = [
    "messageId",
    "msa_2",
    "patient",
    "vaccination",
    "timestamp",
    // "iis",
    "sender",
    "destination",
  ]
  constructor(
    public snackBarService: SnackBarService,
    public feedbackService: FeedbackService,
    private patientResumePipe: PatientResumePipe,
    private vaccinationResumePipe: VaccinationResumePipe,
    private registryNamePipe: RegistryNamePipe,
    private facilityService: FacilityService,
    private dialog: MatDialog,
  ) {
    super();
    if (!this.observableRefresh) {
      this.observableRefresh = merge(this.facilityService.getCurrentObservable(), this.feedbackService.getRefresh())
      // this.observableRefresh?.subscribe(() => this.updateChart())
    }
    if (!this.observableSource) {
      this.observableSource = this.feedbackService.readAcks()
        .pipe(tap((values) => this.updateChart(values)))
    }

  }

  @Input()
  charts: boolean = false

  override ngAfterViewInit(): void {
    super.ngAfterViewInit();
    this.dataSource.filterPredicate = this.ackFilterPredicate
    // this.observableRefresh?.subscribe(() => this.updateChart())
    this.dataSource.sortingDataAccessor = (data: AcknowledgementObject<Feedback>, sortHeaderId: string) => {
      if (sortHeaderId === "names") {
        return this.patientResumePipe.transform(data.patient, ["name"])
      }
      if (sortHeaderId === "mrn") {
        return this.patientResumePipe.transform(data.patient, ["mrn"])
      }
      if (sortHeaderId === "iis") {
        return this.registryNamePipe.transform(data.iis)
      }
      if (sortHeaderId === "vaccination") {
        return this.vaccinationResumePipe.transform(data.vaccination, ['cvx', 'administeredDate'])
      }
      //@ts-ignore
      return data[sortHeaderId]
    }
  }

  ackFilterPredicate = (data: AcknowledgementObject<Feedback>, filter: string) => {
    if (JSON.stringify(data).trim().toLowerCase().indexOf(filter) !== -1) {
      return true
    }
    if (JSON.stringify([
      this.patientResumePipe.transform(data.patient, ["mrn"]),
      this.registryNamePipe.transform(data.iis),
      this.vaccinationResumePipe.transform(data.vaccination, ['cvx', 'administeredDate'])
    ]).toLowerCase().indexOf(filter) !== -1) {
      return true
    }
    return false
  }

  public messagesChart: any;
  public errChart: any;

  updateChart(values?: AcknowledgementObject<Feedback>[]) {
    if (this.charts) {
      let messageData = [0, 0, 0, 0]
      let errData = [0, 0, 0, 0];
      (values ?? this.dataSource.data ?? []).forEach(element => {
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

      Chart.getChart("MessagesStatusChart")?.destroy(); // <canvas> id
      this.messagesChart = new Chart("MessagesStatusChart", {
        type: 'pie', //this denotes tha type of chart
        data: {// values on X-Axis
          labels: ['Rejected', 'Rejected with Warnings', 'Accepted with Notices', 'Accepted'],
          datasets: [{
            // label: 'Status',
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

      Chart.getChart("ErrChart")?.destroy(); // <canvas> id
      this.errChart = new Chart("ErrChart", {
        type: 'pie', //this denotes tha type of chart

        data: {// values on X-Axis
          labels: ['Error', 'Warnings', 'Notices', 'Informational'],
          datasets: [{
            // label: 'Severity',
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

  openDisplay(element: AcknowledgementObject<Feedback>) {
    const dialogRef = this.dialog.open(AckDisplayComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: { ack: element },
    });
  }

  openPatient(patient: EhrPatient | number) {
    const dialogRef = this.dialog.open(PatientDashboardComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: { patient: patient },
    });
  }

  openVaccination(vaccination: VaccinationEvent | number) {
    const dialogRef = this.dialog.open(VaccinationDashboardComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: { vaccination: vaccination },
    });
  }


}
