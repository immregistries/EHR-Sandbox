import { AfterViewInit, Component, ViewChild } from '@angular/core';
import { MatStepper } from '@angular/material/stepper';
import { Router } from '@angular/router';
import { Tenant, Facility } from 'src/app/core/_model/rest';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { ImmunizationRegistryService } from 'src/app/core/_services/immunization-registry.service';
import { PatientService } from 'src/app/core/_services/patient.service';
import { TenantService } from 'src/app/core/_services/tenant.service';
import { FhirResourceService } from '../../_services/_fhir/fhir-resource.service';

@Component({
  selector: 'app-group-steps',
  templateUrl: './group-steps.component.html',
  styleUrls: ['./group-steps.component.css']
})
export class GroupStepsComponent implements AfterViewInit {
  // @ViewChild('tenantStepper', { static: false }) tenantStepper!: MatStepper;
  // @ViewChild('facilityStepper', { static: false }) facilityStepper!: MatStepper;
  @ViewChild('stepper', { static: false }) stepper!: MatStepper;

  public bundleResource: string = ""

  constructor(private tenantService: TenantService,
    public facilityService: FacilityService,
    public patientService: PatientService,
    public registryService: ImmunizationRegistryService,
    public fhirResourceService: FhirResourceService,

    private router: Router) {

  }

  ngAfterViewInit(): void {
    this.stepper.selectionChange.subscribe((value) => {
      this.onStepChange(value)
    })
  }

  nextTenant(tenant: Tenant) {
    this.tenantService.setCurrent(tenant)
    this.stepper.next()
  }
  nextFacility(facility: Facility) {
    this.facilityService.setCurrent(facility)
    this.stepper.next()
  }

  end() {
    this.router.navigate(['/dashboard'])
  }

  onStepChange(event: any): void {
    //use event.selectedIndex to know which step your user in.
    if (event.selectedIndex != 4) {
    } else {
      this.fhirResourceService.getFacilityExportBundle(this.facilityService.getCurrentId()).subscribe((res) => {
        this.bundleResource = res
      })
    }
  }


}
