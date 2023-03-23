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

  feedback!: Feedback[]

  @Input()
  element!: EhrPatient | VaccinationEvent

  constructor(private dialog: MatDialog) { }

  ngOnInit(): void {
  }

  openFeedback(element: EhrPatient) {
    const dialogRef = this.dialog.open(FeedbackTableComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: {patient: element},
    });
  }



}
