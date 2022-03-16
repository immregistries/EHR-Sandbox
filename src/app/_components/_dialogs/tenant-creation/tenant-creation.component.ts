import { Component, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Tenant } from 'src/app/_model/rest';
import { TenantService } from 'src/app/_services/tenant.service';

@Component({
  selector: 'app-tenant-creation',
  templateUrl: './tenant-creation.component.html',
  styleUrls: ['./tenant-creation.component.css']
})
export class TenantCreationComponent implements OnInit {

  constructor(private tenantService: TenantService,
    private _snackBar: MatSnackBar,
    public _dialogRef: MatDialogRef<TenantCreationComponent>) { }

  ngOnInit(): void {
  }

  newTenantForm: FormControl = new FormControl("")
  newTenant?: Tenant;
  create() {
    this.newTenant = {id: -1, nameDisplay: this.newTenantForm.value}
    // this.tenantService.getTenant(1).subscribe((res) => {this._snackBar.open(`${res}`)})
    this.tenantService.postTenant(this.newTenant).subscribe({
      next: (res) => {
        this._snackBar.open(`${res}`, 'close')
        this._dialogRef.close(true)
      },
      error: (err) => {
        this._snackBar.open(`Error : ${err.error.error}`, 'close')
        console.error(err.error.error)
      }
    })
  }

}
