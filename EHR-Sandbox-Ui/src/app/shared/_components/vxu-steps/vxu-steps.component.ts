import { Component, ViewChild } from '@angular/core';
import { MatStepper } from '@angular/material/stepper';
import { Router } from '@angular/router';
import { Tenant, Facility, EhrPatient, VaccinationEvent } from 'src/app/core/_model/rest';
import { FhirResourceService } from 'src/app/core/_services/_fhir/fhir-resource.service';
import { Hl7Service } from 'src/app/core/_services/_fhir/hl7.service';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { ImmunizationRegistryService } from 'src/app/core/_services/immunization-registry.service';
import { PatientService } from 'src/app/core/_services/patient.service';
import { TenantService } from 'src/app/core/_services/tenant.service';
import { VaccinationService } from 'src/app/core/_services/vaccination.service';

@Component({
  selector: 'app-vxu-steps',
  templateUrl: './vxu-steps.component.html',
  styleUrls: ['./vxu-steps.component.css']
})
export class VxuStepsComponent {
  @ViewChild('stepper', { static: false }) stepper!: MatStepper;

  private vaccinationId?: number;
  public vxu: string = "";

  constructor(public tenantService: TenantService,
    public facilityService: FacilityService,
    public patientService: PatientService,
    public vaccinationService: VaccinationService,
    public registryService: ImmunizationRegistryService,
    public fhirResourceService: FhirResourceService,
    public hl7Service: Hl7Service,
    private router: Router) {

  }

  nextTenant(tenant: Tenant) {
    // console.log("NEXT", tenant)
    this.tenantService.setCurrent(tenant)
    this.stepper.next()
  }

  nextFacility(facility: Facility) {
    this.facilityService.setCurrent(facility)
    this.stepper.next()
  }

  nextPatient(patient: string | EhrPatient | number) {
    if (patient) {
      if (patient instanceof Object) {
        this.patientService.setCurrent(patient)
      } else {
        this.patientService.quickReadPatient(+patient).subscribe((res) => {
          this.patientService.setCurrent(res)
        })
      }
      this.stepper.next()
    }
  }

  nextVaccination(vaccinationEvent: string | VaccinationEvent | number) {
    if (vaccinationEvent) {
      if (vaccinationEvent instanceof Object) {
        this.vaccinationId = vaccinationEvent.id
      } else {
        this.vaccinationId = +vaccinationEvent
      }
      this.hl7Service.getVXU(this.patientService.getCurrentId(), this.vaccinationId ?? -1).subscribe((res) => {
        this.vxu = res
      })
      this.stepper.next()
    }
  }

  end() {
    this.router.navigate(['/dashboard'])
  }

}
