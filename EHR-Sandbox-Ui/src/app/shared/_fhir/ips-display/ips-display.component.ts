import {Component, Inject, Optional} from '@angular/core';
import {MatDialogRef, MAT_DIALOG_DATA} from '@angular/material/dialog';
import {Observable, of} from 'rxjs';
import {EhrPatient, VaccinationEvent} from 'src/app/core/_model/rest';
import {PatientService} from 'src/app/core/_services/patient.service';
import {VaccinationService} from 'src/app/core/_services/vaccination.service';
import {ReceivedHistoryDTO} from 'src/app/core/_model/dtos';

@Component({
  selector: 'app-ips-display',
  templateUrl: './ips-display.component.html',
  styleUrls: ['./ips-display.component.css']
})
export class IpsDisplayComponent {
  public loading: boolean = false


  public selectedPatient?: EhrPatient
  public selectedPatientIndex?: number

  public remoteSelectedPatient?: EhrPatient

  public selectedVaccination: VaccinationEvent | null = null;

  public remoteVaccinations: VaccinationEvent[] = [];
  public localVaccinations: VaccinationEvent[] = [];

  public remotePatients: EhrPatient[] = [];
  public localPatients: EhrPatient[] = [];


  constructor(
    public vaccinationService: VaccinationService,
    public patientService: PatientService,
    @Optional() public _dialogRef: MatDialogRef<IpsDisplayComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: {
      receivedHistory?: ReceivedHistoryDTO
    }) {
    this.patientService.quickReadPatients().subscribe((patients) => {
      this.localPatients = patients
    })
    if (data) {
      if (data?.receivedHistory) {
        this.remoteVaccinations = data.receivedHistory.vaccinationEvents
        if (data.receivedHistory.patient) {
          this.remotePatients = [data.receivedHistory.patient]
          this.selectedPatient = data.receivedHistory.patient
        } else {
          this.remotePatients = []
        }
      }
    }
  }


  public patientIndexSelected(value: number | undefined) {
    this.selectedPatientIndex = value
    if (value != undefined && this.localPatients) {
      this.patientSelected(this.localPatients[value] ?? undefined)
    } else {
      this.patientSelected(undefined)
    }
  }

  public patientSelected(value?: EhrPatient) {
    this.patientService.setCurrent(value ?? {id: -1, names: []})
    this.selectedPatient = value
    this.vaccinationService.quickReadVaccinations().subscribe((res) => {
      this.localVaccinations = res
    })
  }

  remotePatientSelected(value?: EhrPatient) {
    this.remoteSelectedPatient = value
  }

  public selectedVaccinationIndex?: number

  selectVaccinationIndex(value: number | undefined) {
    this.selectedVaccinationIndex = value
  }

  selectVaccination(value: VaccinationEvent | null | undefined) {
    this.selectedVaccination = value ?? null
  }

  refreshLocalHistoryObservable(): Observable<boolean> {
    return of(true, true)
  }

}
