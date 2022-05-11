import { HttpResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Facility } from 'src/app/core/_model/rest';
import { FacilityService } from 'src/app/core/_services/facility.service';
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
    private _snackBar: MatSnackBar,
    public _dialogRef: MatDialogRef<FacilityCreationComponent>) { }

  ngOnInit(): void {
  }

  newFacilityForm: FormControl = new FormControl("")
  newFacility?: Facility;
  create() {
    this.newFacility = {id: -1, nameDisplay: this.newFacilityForm.value}
    this.facilityService.postFacility(this.tenantService.getTenantId(), this.newFacility).subscribe({
      next: (res: HttpResponse<string>) => {
        if (res.body) {
          this._snackBar.open(res.body, 'close')
        }
        this.facilityService.doRefresh()
        this._dialogRef.close(true)
      },
      error: (err) => {
        console.log(err.error)
        this._snackBar.open(`Error : ${err.error.error}`, 'close')
      }
    });
  }

}
