import { HttpResponse } from '@angular/common/http';
import { Component, EventEmitter, Inject, OnInit, Optional, Output } from '@angular/core';
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

  @Output()
  success: EventEmitter<Tenant> = new EventEmitter<Tenant>()

  constructor(private tenantService: TenantService,
    private snackBarService: SnackBarService,
    @Optional() public _dialogRef?: MatDialogRef<TenantCreationComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data?: { tenant?: Tenant }) {
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
        if (res.body) {
          if (this._dialogRef && this._dialogRef.id) {
            this._dialogRef.close(res.body)
          } else {
            this.success.emit(res.body)
          }
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


