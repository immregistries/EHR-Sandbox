import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClinicianTableComponent } from './clinician-table.component';

describe('ClinicianTableComponent', () => {
  let component: ClinicianTableComponent;
  let fixture: ComponentFixture<ClinicianTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ClinicianTableComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ClinicianTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
