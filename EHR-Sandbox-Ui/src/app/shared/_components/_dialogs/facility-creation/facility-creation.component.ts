import { HttpResponse } from '@angular/common/http';
import { Component, OnInit, Optional } from '@angular/core';
import { UntypedFormControl } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { Facility } from 'src/app/core/_model/rest';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';
import { TenantService } from 'src/app/core/_services/tenant.service';

@Component({
  selector: 'app-facility-creation',
  templateUrl: './facility-creation.component.html',
  styleUrls: ['./facility-creation.component.css']
})
export class FacilityCreationComponent implements OnInit {

  constructor(
    private facilityService: FacilityService,
    private tenantService: TenantService,
    private snackBarService: SnackBarService,
    @Optional() public _dialogRef: MatDialogRef<FacilityCreationComponent>) { }

  ngOnInit(): void {
  }

  newFacilityForm: UntypedFormControl = new UntypedFormControl("")
  newFacility?: Facility;
  create() {
    this.newFacility = {id: -1, nameDisplay: this.newFacilityForm.value}
    this.facilityService.postFacility(this.tenantService.getTenantId(), this.newFacility).subscribe({
      next: (res: HttpResponse<Facility>) => {
        if (res.body) {
          // this._snackBar.open("OK", 'close')
          this.facilityService.setFacility(res.body);
        }
        this.facilityService.doRefresh()
        this._dialogRef?.close(true)
      },
      error: (err) => {
        console.log(err.error)
        this.snackBarService.errorMessage(err.error.error)
      }
    });
  }

}
