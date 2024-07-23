import { Component, Inject, Input, Optional } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { VaccinationEvent } from 'src/app/core/_model/rest';
import { FormCard } from 'src/app/core/_model/form-structure';

@Component({
  selector: 'app-vaccination-display',
  templateUrl: './vaccination-display.component.html',
  styleUrls: ['./vaccination-display.component.css']
})
export class VaccinationDisplayComponent {

  @Input()
  vaccinationEvent!: any;

  @Input()
  formCards!: FormCard[];

  isEditionMode: boolean = false;

  constructor(@Optional() public _dialogRef: MatDialogRef<VaccinationDisplayComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: { vaccinationEvent: VaccinationEvent }) {
    if (data && data.vaccinationEvent) {
      this.vaccinationEvent = data.vaccinationEvent;
    }
  }

}
