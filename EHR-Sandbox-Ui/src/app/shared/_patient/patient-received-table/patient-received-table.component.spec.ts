import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PatientReceivedTableComponent } from './patient-received-table.component';

describe('PatientReceivedTableComponent', () => {
  let component: PatientReceivedTableComponent;
  let fixture: ComponentFixture<PatientReceivedTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PatientReceivedTableComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PatientReceivedTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
