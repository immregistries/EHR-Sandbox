import { Component, Input } from '@angular/core';
import { AcknowledgementObject } from 'src/app/core/_model/form-structure';
import { Feedback } from 'src/app/core/_model/rest';
import { FeedbackService } from 'src/app/core/_services/feedback.service';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';

@Component({
  selector: 'app-ack-display',
  templateUrl: './ack-display.component.html',
  styleUrls: ['./ack-display.component.css']
})
export class AckDisplayComponent {

  private _acknowledgementObject: AcknowledgementObject<Feedback> = { sortedResult: { errors: [], warnings: [], notices: [], infos: [] } };
  public get acknowledgementObject(): AcknowledgementObject<Feedback> {
    return this._acknowledgementObject;
  }
  @Input()
  public set acknowledgementObject(value: AcknowledgementObject<Feedback>) {
    this._acknowledgementObject = value;
    this._rawAck = value.rawAck ?? ""
    this.msa_2 = value.msa_2 ?? ""
    this.plain = this.plainText(value)
  }


  constructor(
    public snackBarService: SnackBarService,
    public feedbackService: FeedbackService,
  ) { }

  private _rawAck: string = "";
  public get rawAck(): string {
    return this._rawAck;
  }
  @Input()
  public set rawAck(value: string) {
    this.plain = ""
    this._rawAck = value;
    for (const segment of value.split("\n")) {
      const values = segment.split("|")
      if (values[0] === "MSA") {
        this.msa_2 = values[1];
      }
      //   if (values[0] === "ERR") {
      //     switch (values[4]) {
      //       case "E": {
      //         this.errSegments.errors.push(segment);
      //         break;
      //       }
      //       case "W": {
      //         this.errSegments.warnings.push(segment);
      //         break;
      //       }
      //       case "N": {
      //         this.errSegments.notices.push(segment);
      //         break;
      //       }
      //       case "I": {
      //         this.errSegments.infos.push(segment);
      //         break;
      //       }
      //     }
      //   }
    }
    this.feedbackService.convertAck(value, this.registryId, this.patientId, this.vaccinationId).subscribe(result => {
      this.acknowledgementObject = result
      this.plain = this.plainText(result)
    })
    // this.errSegments = { errors: [], warnings: [], notices: [], infos: [] }


  }

  @Input()
  registryId!: number;
  @Input()
  patientId?: number;
  @Input()
  vaccinationId?: number;
  @Input()
  loading: Boolean = false;
  @Input()
  isError: boolean = false;

  msa_2: string = ""

  // errSegments: AckSortedResults<string> = { errors: [], warnings: [], notices: [], infos: [] }

  resultClass(): string {
    if (this.rawAck === "") {
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

  plain: string = ""

  plainText(ack: AcknowledgementObject<Feedback>): string {
    let actionRequired: string = ""
    let messageStatus: string = ""
    switch (ack.msa_2) {
      case "AA":
      case "AI": {
        actionRequired = ""
        messageStatus = "Accepted"
        break;
      }
      case "AE": {
        actionRequired = "Correct problems and resubmit mandatory"
        messageStatus = "Error"
        break;
      }
      case "AW": {
        actionRequired = "Correct problems and resubmit"
        messageStatus = "Warning"
        break;
      }
      case "AN": {
        actionRequired = "Correct problems"
        messageStatus = "Notices"
        break;
      }
    }
    let txt =
      `Message Id: ${ack.messageId}
Origin: ${ack.sender} ${ack.senderSoftware}
Destination: ${ack.destination} ${ack.destinationSoftware}
Status: ${ack.msa_2}-${messageStatus}
Actions Required: ${actionRequired}
----------------- Result List ------------------
Number of Errors: ${ack.sortedResult.errors.length}
Number of Warnings: ${ack.sortedResult.warnings.length}
Number of Notices: ${ack.sortedResult.notices.length}
Number of Infos: ${ack.sortedResult.infos.length}
------ Errors ------ ${this.feedbackPlain(ack.sortedResult.errors, "E")}
------ Warnings ------ ${this.feedbackPlain(ack.sortedResult.warnings, "W")}
------ Notices ----- ${this.feedbackPlain(ack.sortedResult.notices, "N")}
------ Infos ----- ${this.feedbackPlain(ack.sortedResult.infos, "I")}

     `
    return txt
  }

  feedbackPlain(feedbackArray: Feedback[], prefix?: string): string {
    let txt = ""
    let index = 1
    feedbackArray.forEach(element => {
      txt += "\n" + (prefix ?? "") + "-" + index++ + ": " + element.code + "\n" + element.content + "\n"
    });
    if (txt === "") {
      txt = "N/A"
    }
    return txt
  }

  readonly COLOR_THEME_CLASS = {
    "errors": 'error-mode',
    "warnings": 'warning-mode',
    "notices": 'notice-mode',
    "infos": 'info-mode'
  }

  colorThemeClass(elementKey: string): string {
    switch (elementKey) {
      case "errors":
        return 'error-mode'
      case "warnings":
        return 'warning-mode'
      case "notices":
        return 'notice-mode'
      case "infos":
        return 'info-mode'
      default:
        return ""
    }
  }


}
