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

  private _facility: Facility = { id: -1 };
  public get facility(): Facility {
    return this._facility;
  }
  public set facility(value: Facility) {
    this._facility = value;
    if (this._facility.parentFacility) {
      if (typeof this._facility.parentFacility === "number" || "string") {
        this.parentId = +this._facility.parentFacility
      } else {
        this.parentId = this._facility.parentFacility.id
      }
    } else {
      this.parentId = undefined
    }
  }

  add_patients: boolean = false;
  facilityList!: Facility[];
  editionMode: boolean = false;

  private _parentId?: number | undefined;
  public get parentId(): number | undefined {
    return this._parentId;
  }
  public set parentId(value: number | undefined) {
    this._parentId = value;
    this._facility.parentFacility = value? {id: value} : undefined
    // if (this._parentId) { // TODO change the whole mechanism
    //    this.facilityService.readFacility(this.tenantService.getCurrentId(), this._parentId).subscribe((res) => {
    //     this._facility.parentFacility = res
    //   })
    // } else {
    //   this._facility.parentFacility = undefined
    // }
  }

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
    if (this.editionMode) {
      this.facilityService.putFacility(this.tenantService.getCurrentId(), this.facility).subscribe({
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

    } else {
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


  }

  random() {
    this.facilityService.getRandom(this.tenantService.getCurrentId()).subscribe((res) => {
      this.facility = res
    })
  }

}
