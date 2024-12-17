import { Component, Input } from '@angular/core';
import { AcknowledgementObject } from 'src/app/core/_model/form-structure';
import { EhrPatient, Feedback, VaccinationEvent } from 'src/app/core/_model/rest';
import { FeedbackService } from 'src/app/core/_services/feedback.service';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';
import { PatientCachePipe } from '../../_pipes/patient-cache.pipe';
import { PatientService } from 'src/app/core/_services/patient.service';
import { DatePipe } from '@angular/common';
import { PatientResumePipe } from '../../_pipes/patient-resume.pipe';
import { VaccinationComparePipe } from '../../_pipes/vaccination-compare.pipe';
import { VaccinationCachePipe } from '../../_pipes/vaccination-cache.pipe';
import { CodeMapsPipe } from '../../_pipes/code-maps.pipe';

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
    public patientCachePipe: PatientCachePipe,
    public vaccinationCachePipe: VaccinationCachePipe,
    public patientResumePipe: PatientResumePipe,
    public patientService: PatientService,
    public codeMapsPipe: CodeMapsPipe,
    public datePipe: DatePipe,
  ) { }

  private _rawAck: string = "";
  public get rawAck(): string {
    return this._rawAck;
  }
  @Input()
  public set rawAck(value: string) {
    this.plain = ""
    // this._rawAck = value;
    // for (const segment of value.split("\n")) {
    //   const values = segment.split("|")
    //   if (values[0] === "MSA") {
    //     this.msa_2 = values[1];
    //   }
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
    // }
    this.feedbackService.convertAck(value, this.registryId, this.patientId, this.vaccinationId).subscribe(result => {
      this.acknowledgementObject = result
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
  loading: boolean = false;
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

  plainTextPatient(ack: AcknowledgementObject<Feedback>): string {
    let ehrPatient: EhrPatient | undefined;
    let vaccinationEvent: VaccinationEvent | undefined;
    if (ack.patient) {
      ehrPatient = (this.patientCachePipe.transform([ack.patient]) ?? [undefined])[0]
    }
    let txt = ""
    if (ehrPatient) {
      txt += `----------------- PATIENT SUBMITTED -----------------
Name:\t${ehrPatient?.names[0].nameLast}, ${ehrPatient?.names[0].nameFirst ?? ""} ${ehrPatient?.names[0].nameMiddle ?? ""}
DOB:\t${this.patientResumePipe.transform(ehrPatient, ["birthDate"])}
MRN:\t${this.patientResumePipe.extractMrn(ehrPatient)}
`
      if (ack.vaccination) {
        vaccinationEvent = (this.vaccinationCachePipe.transform([ack.vaccination]) ?? [undefined])[0]
      }
      if (vaccinationEvent) {
        txt += `Imms:
\t${this.datePipe.transform(vaccinationEvent.vaccine.administeredDate, "shortDate")} ${this.codeMapsPipe.transform(vaccinationEvent.vaccine.vaccineCvxCode ?? "", "VACCINATION_CVX_CODE").label} (${vaccinationEvent.vaccine.vaccineCvxCode})\n\n`
      }
    }
    return txt

  }

  plainText(ack: AcknowledgementObject<Feedback>): string {
    let actionRequired: string = ""
    let messageStatus: string = ""
    switch (ack.msa_2) {
      case "AA":
      case "AI": {
        actionRequired = "Data was accepted by CHIRP"
        messageStatus = "ACCEPTED"
        break;
      }
      case "AE": {
        actionRequired = "Data was not accepted by CHIRP. You must correct and resubmit. "
        messageStatus = "NOT ACCEPTED"
        break;
      }
      case "AW": {
        actionRequired = "Data was not accepted by CHIRP. You should correct and resubmit."
        messageStatus = "Warning"
        break;
      }
      case "AN": {
        actionRequired = "Data was accepted by CHIRP. Issues detected, you should correct and resubmit."
        messageStatus = "Notices"
        break;
      }
    }
    let txt = this.plainTextPatient(ack);
    txt += `----------------- ${messageStatus} -------------------
Message Id:\t${ack.messageId}
Origin:\t${ack.sender}\t${ack.senderSoftware}
Destination:\t${ack.destination}\t${ack.destinationSoftware}
Submitted:\t${this.datePipe.transform(ack.timestamp, "short")}

${actionRequired}
Errors:\t${ack.sortedResult.errors.length}
Warnings:\t${ack.sortedResult.warnings.length}
Notices:\t${ack.sortedResult.notices.length}
Inform:\t${ack.sortedResult.infos.length}
${this.feedbackPlain("Errors", "The message could not be accepted because:", ack.sortedResult.errors, "E")}
${this.feedbackPlain("Warnings", "There are issues with your message or data quality that should be corrected:", ack.sortedResult.warnings, "W")}
${this.feedbackPlain("Notices", "", ack.sortedResult.notices, "N")}
${this.feedbackPlain("Infos", "", ack.sortedResult.infos, "I")}`
    return txt
  }

  feedbackPlain(title: string, message: string, feedbackArray: Feedback[], prefix?: string): string {
    if (feedbackArray.length < 1) {
      return ``
    }
    let txt = `
${title}: ${message}`
    let index = 1
    feedbackArray.forEach(element => {
      txt += `\n\t${(prefix ?? "")}-${index++}\t${element.content}`
      // txt += `\n\t-${(prefix ?? "")}-${index++}\t${element.code}\n${element.content}`
    });
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
