import { Component, Input } from '@angular/core';
import { Message } from '@rethinkhealth/hl7v2/global';
import { EhrPatient, VaccinationEvent } from 'src/app/core/_model/rest';

@Component({
  selector: 'app-ack-display',
  templateUrl: './ack-display.component.html',
  styleUrls: ['./ack-display.component.css']
})
export class AckDisplayComponent {


  private _ack: string = "";
  public get ack(): string {
    return this._ack;
  }
  @Input()
  public set ack(value: string) {
    this._ack = value;
    for (const segment of value.split("\n")) {
      const values = segment.split("|")
      if (values[0] === "ERR") {
        switch (values[4]) {
          case "E": {
            this.errors.push(segment);
            break;
          }
          case "W": {
            this.warnings.push(segment);
            break;
          }
          case "N": {
            this.notices.push(segment);
            break;
          }
          case "I": {
            this.infos.push(segment);
            break;
          }
        }
      }
    }
    this.errSegments = {
      errors: this.errors,
      warnings: this.warnings,
      notices: this.notices,
      infos: this.infos
    }
  }

  @Input()
  patient?: EhrPatient;
  @Input()
  vaccination?: VaccinationEvent;


  @Input()
  loading: Boolean = false;



  errors: string[] = []
  warnings: string[] = []
  notices: string[] = []
  infos: string[] = []

  errSegments: { errors: string[], warnings: string[], notices: string[], infos: string[] } = {
    errors: this.errors,
    warnings: this.warnings,
    notices: this.notices,
    infos: this.infos
  }


}
