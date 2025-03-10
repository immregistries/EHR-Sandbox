import { Component, Input, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { EhrPatient, Feedback, VaccinationEvent } from 'src/app/core/_model/rest';
import { FeedbackTableComponent } from '../feedback-table/feedback-table.component';

@Component({
  selector: 'app-feedback-icon',
  templateUrl: './feedback-icon.component.html',
  styleUrls: ['./feedback-icon.component.css']
})
export class FeedbackIconComponent implements OnInit {

  public feedbacksCount: number | undefined
  private _vaccination?: VaccinationEvent | undefined;
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
  @Input()
  public set patient(value: EhrPatient | undefined) {
    this._patient = value;
    if (value && !this.vaccination) {
      this.feedbacksCount = value.feedbacksCount
    } else if (!value) {
      this.feedbacksCount = undefined
    }
  }

  constructor(private dialog: MatDialog) { }

  ngOnInit(): void {
  }

  openFeedback() {
    const dialogRef = this.dialog.open(FeedbackTableComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: { patient: this._patient, vaccination: this._vaccination },
    });
  }



}
