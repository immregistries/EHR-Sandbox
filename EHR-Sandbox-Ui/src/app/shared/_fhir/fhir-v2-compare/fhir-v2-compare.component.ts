import { Component, Input } from '@angular/core';
import { FhirV2Service } from 'src/app/core/_services/_fhir/fhir-v2.service';
import { Hl7Service } from 'src/app/core/_services/_fhir/hl7.service';
import { FhirResourceService } from 'src/app/core/_services/_fhir/fhir-resource.service';

@Component({
  selector: 'app-fhir-v2-compare',
  templateUrl: './fhir-v2-compare.component.html',
  styleUrls: ['./fhir-v2-compare.component.css']
})
export class FhirV2CompareComponent {
  @Input() public vaccinationId!: number;
  @Input() public patientId: number = -1;


  public hl7V2Message: string = ""
  public fhirV2Message: string = "";

  public transaction: string = ""
  public transactionLoading: boolean = false

  @Input() public hl7V2Loading: boolean = false
  @Input() public fhirV2MessageLoading: boolean = false

  constructor(
    private hl7Service: Hl7Service,
    private fhirV2Service: FhirV2Service,
    private fhirResourceService: FhirResourceService,
  ) {
    // this.getHl7Message()
  }

  ngOnInit() {
    this.getHl7Message()
  }


  getHl7Message() {
    if (this.vaccinationId > 0) {
      this.hl7V2Loading = true
      this.hl7Service.getVXU(this.patientId, this.vaccinationId).subscribe((res) => {
        this.hl7V2Message = res
        this.hl7V2Loading = false
      })
      this.fhirV2MessageLoading = true
      this.fhirV2Service.getVXU(this.patientId, this.vaccinationId).subscribe((res) => {
        this.fhirV2Message = res
        this.fhirV2MessageLoading = false
      })
      this.transactionLoading = true
      this.fhirResourceService.getVaccinationExportBundle(this.patientId, this.vaccinationId).subscribe((resource) => {
        this.transaction = resource
        this.transactionLoading = false
      })
    } else if (this.patientId > 0) {
      this.hl7V2Loading = true
      this.hl7Service.getQBP(this.patientId).subscribe((res) => {
        this.hl7V2Message = res
        this.hl7V2Loading = false
      })
      this.fhirV2MessageLoading = true
      this.fhirV2Service.getQBP(this.patientId).subscribe((res) => {
        this.fhirV2Message = res
        this.fhirV2MessageLoading = false
      })
      this.fhirResourceService.getPatientExportBundle(this.patientId).subscribe((resource) => {
        this.transaction = resource
      })
    }
  }
}
