import { HttpResponse } from '@angular/common/http';
import { Component, Inject, OnInit, Optional } from '@angular/core';
import { UntypedFormControl } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
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

  facility: Facility = { id: -1 };
  add_patients: boolean = false;
  facilityList!: Facility[];
  editionMode: boolean = false;

  parentId?: number

  constructor(
    public facilityService: FacilityService,
    private tenantService: TenantService,
    private snackBarService: SnackBarService,
    @Optional() public _dialogRef: MatDialogRef<FacilityCreationComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: { facility?: Facility }) {
    if (data && data.facility) {
      this.facility = data.facility
      this.editionMode = true
    } else {
      this.facility = { id: -1 }
    }
  }

  ngOnInit(): void {
    this.facilityService.readFacilities(this.tenantService.getCurrentId()).subscribe(res => {
      this.facilityList = res
    })
  }

  save() {
    this.facilityService.postFacility(this.tenantService.getCurrentId(), this.facility).subscribe({
      next: (res: HttpResponse<Facility>) => {
        if (this._dialogRef && res.body) {
          this._dialogRef.close(res.body)
        }
      },
      error: (err) => {
        console.log(err.error)
        this.snackBarService.errorMessage(err.error.error)
      }
    });
  }

  random() {
    this.facilityService.getRandom(this.tenantService.getCurrentId()).subscribe((res) => {
      this.facility = res
    })
  }

}
