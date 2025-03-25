import { Component, Input, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { EhrPatient, Facility, Feedback, VaccinationEvent } from 'src/app/core/_model/rest';
import { FeedbackTableComponent } from '../feedback-table/feedback-table.component';
import { FeedbackService } from 'src/app/core/_services/feedback.service';
import { of, switchMap } from 'rxjs';

@Component({
  selector: 'app-feedback-icon',
  templateUrl: './feedback-icon.component.html',
  styleUrls: ['./feedback-icon.component.css']
})
export class FeedbackIconComponent implements OnInit {

  public feedbacksCount: number | undefined

  private _vaccination?: VaccinationEvent | undefined;
  public get vaccination(): VaccinationEvent | undefined {
    return this._vaccination;
  }
  @Input()
  public set vaccination(value: VaccinationEvent | undefined) {
    this._vaccination = value;
    if (value) {
      this.feedbacksCount = value.feedbacksCount
    } else {
      this.feedbacksCount = undefined
    }
  }

  private _patient?: EhrPatient | undefined;
  public get patient(): EhrPatient | undefined {
    return this._patient;
  }
  @Input()
  public set patient(value: EhrPatient | undefined) {
    this._patient = value;
    if (value && !this.vaccination) {
      this.feedbacksCount = value.feedbacksCount
    } else if (!value) {
      this.feedbacksCount = undefined
    }
  }

  private _facility?: Facility | undefined;
  @Input()
  public set facility(value: Facility | undefined) {
    this._facility = value;
    if (value && !this.vaccination && !this.patient) {
      this.feedbacksCount = value.feedbacksCount
    } else if (!value) {
      this.feedbacksCount = undefined
    }
  }

  constructor(private dialog: MatDialog,
    private feedbackService: FeedbackService
  ) { }

  ngOnInit(): void {
  }

  openFeedback() {
    of([this._vaccination, this._patient]).pipe(
      switchMap(cond => {
        if (cond[0]) {
          return this.feedbackService.readVaccinationFeedback(this._vaccination ?? -1);
        } else if (cond[1]) {
          return this.feedbackService.readPatientFeedback(this._patient ?? -1);
        } else {
          return this.feedbackService.readFacilityFeedback(this._facility ?? -1)
        }
      })
    ).subscribe((res) => {
      const dialogRef = this.dialog.open(FeedbackTableComponent, {
        maxWidth: '95vw',
        maxHeight: '95vh',
        height: 'fit-content',
        width: '100%',
        panelClass: 'dialog-with-bar',
        data: { patient: this._patient, vaccination: this._vaccination, datatable: res },
      });
    });


  }



}
