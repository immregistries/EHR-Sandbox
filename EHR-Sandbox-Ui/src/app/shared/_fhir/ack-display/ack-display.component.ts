import { Component, Input } from '@angular/core';
import { AckSortedResults } from 'src/app/core/_model/form-structure';
import { EhrPatient, Feedback, VaccinationEvent } from 'src/app/core/_model/rest';
import { FeedbackService } from 'src/app/core/_services/feedback.service';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';

@Component({
  selector: 'app-ack-display',
  templateUrl: './ack-display.component.html',
  styleUrls: ['./ack-display.component.css']
})
export class AckDisplayComponent {

  constructor(
    public snackBarService: SnackBarService,
    public feedbackService: FeedbackService,
  ) { }

  private _ack: string = "";
  public get ack(): string {
    return this._ack;
  }
  @Input()
  public set ack(value: string) {
    this.plain = ""
    this._ack = value;
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
      this.errFeedbacks = result
      this.plain = this.plainText(value, result)
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
  errFeedbacks: AckSortedResults<Feedback> = { sortedResults: { errors: [], warnings: [], notices: [], infos: [] } }

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

  plain: string = ""

  plainText(msg: string, feedbacks: AckSortedResults<Feedback>): string {
    let actionRequired: string = ""
    let messageStatus: string = ""
    switch (this.msa_2) {
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
      `Message Origin: ${this.msa_2}
Message Status ${messageStatus}
Actions Required: ${actionRequired}
Number of Errors: ${feedbacks.sortedResults.errors.length}
Number of Warnings: ${feedbacks.sortedResults.warnings.length}
Number of Notices: ${feedbacks.sortedResults.notices.length}
Number of Infos: ${feedbacks.sortedResults.infos.length}
------
Errors: ${this.feedbackPlain(feedbacks.sortedResults.errors)}
------
Warnings: ${this.feedbackPlain(feedbacks.sortedResults.warnings)}
------
Notices: ${this.feedbackPlain(feedbacks.sortedResults.notices)}
------
Infos: ${this.feedbackPlain(feedbacks.sortedResults.infos)}
------

     `
    return txt
  }

  feedbackPlain(feedbackArray: Feedback[]): string {
    let txt = ""
    feedbackArray.forEach(element => {
      txt += "\n" + element.code + " " + element.content
    });
    if (txt === "") {
      txt = "N/A"
    }
    return txt
  }


}
