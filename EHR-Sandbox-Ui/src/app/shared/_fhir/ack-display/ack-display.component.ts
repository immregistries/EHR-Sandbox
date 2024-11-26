import { Component, Input } from '@angular/core';
import { Message } from '@rethinkhealth/hl7v2/global';
import { EhrPatient, VaccinationEvent } from 'src/app/core/_model/rest';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';

@Component({
  selector: 'app-ack-display',
  templateUrl: './ack-display.component.html',
  styleUrls: ['./ack-display.component.css']
})
export class AckDisplayComponent {

  constructor(public snackBarService: SnackBarService,) {

  }


  private _ack: string = "";
  public get ack(): string {
    return this._ack;
  }
  @Input()
  public set ack(value: string) {
    this._ack = value;
    for (const segment of value.split("\n")) {
      const values = segment.split("|")
      if (values[0] === "MSA") {
        this.msa_2 = values[1];
      }
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
  @Input()
  isError: boolean = false;



  errors: string[] = []
  warnings: string[] = []
  notices: string[] = []
  infos: string[] = []
  msa_2: string = ""

  errSegments: { errors: string[], warnings: string[], notices: string[], infos: string[] } = {
    errors: this.errors,
    warnings: this.warnings,
    notices: this.notices,
    infos: this.infos
  }

  resultClass(): string {
    if (this.ack === "") {
      return "w3-left w3-padding"
    }
    if (this.isError) {
      return 'w3-red w3-left w3-padding'
    }
    if (this.msa_2 === "AE") {
      return 'w3-deep-orange w3-left w3-padding'
    } else if (this.msa_2 === "AW") {
      return 'w3-orange w3-left w3-padding'
    } else if (this.msa_2 === "AN") {
      return 'w3-yellow w3-left w3-padding'
    } if (this.msa_2 === "AA") {
      return 'w3-light-green w3-left w3-padding'
    }
    return 'w3-light-green w3-left w3-padding'
  }


}
