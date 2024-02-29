import { HttpResponse } from '@angular/common/http';
import { Component, Inject, OnInit, Optional } from '@angular/core';
import { UntypedFormControl } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Tenant } from 'src/app/core/_model/rest';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';
import { TenantService } from 'src/app/core/_services/tenant.service';

@Component({
  selector: 'app-tenant-creation',
  templateUrl: './tenant-creation.component.html',
  styleUrls: ['./tenant-creation.component.css']
})
export class TenantCreationComponent implements OnInit {

  public tenant: Tenant = {id: -1}
  editionMode: boolean = false;

  constructor(private tenantService: TenantService,
    private snackBarService: SnackBarService,
    @Optional() public _dialogRef: MatDialogRef<TenantCreationComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: { tenant?: Tenant }) {
      if (data && data.tenant) {
        this.tenant = data.tenant
        this.editionMode = true
      } else {
        this.tenant = { id: -1 }
      }
    }

  ngOnInit(): void {
  }

  save() {
    this.tenantService.postTenant( this.tenant).subscribe({
      next: (res: HttpResponse<Tenant>) => {
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
    this.tenantService.getRandom().subscribe((res) => {
      this.tenant = res
    })
  }
}


