import { HttpResponse } from '@angular/common/http';
import { Component, OnInit, Optional } from '@angular/core';
import { UntypedFormControl } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { Tenant } from 'src/app/core/_model/rest';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';
import { TenantService } from 'src/app/core/_services/tenant.service';

@Component({
  selector: 'app-tenant-creation',
  templateUrl: './tenant-creation.component.html',
  styleUrls: ['./tenant-creation.component.css']
})
export class TenantCreationComponent implements OnInit {

  constructor(private tenantService: TenantService,
    private snackBarService: SnackBarService,
    @Optional() public _dialogRef: MatDialogRef<TenantCreationComponent>) { }

  ngOnInit(): void {
  }

  newTenantForm: UntypedFormControl = new UntypedFormControl("")
  newTenant?: Tenant;
  create() {
    this.newTenant = {id: -1, nameDisplay: this.newTenantForm.value}
    // this.tenantService.getTenant(1).subscribe((res) => {this._snackBar.open(`${res}`)})
    this.tenantService.postTenant(this.newTenant).subscribe({
      next: (res: HttpResponse<Tenant>) => {
        if (res.body) {
          // this._snackBar.open("Ok", 'close')
          this.tenantService.setTenant(res.body)
        }
        this.tenantService.doRefresh()
        this._dialogRef?.close(true)
      },
      error: (err) => {
        console.log(err)
        if (err.error.error) {
          this.snackBarService.errorMessage(err.error.error)
        } else {
          this.snackBarService.errorMessage(err.message)
        }
      }
    });
  }

}
