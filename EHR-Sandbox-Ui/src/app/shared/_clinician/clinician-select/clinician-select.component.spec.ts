import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClinicianSelectComponent } from './clinician-select.component';

describe('ClinicianSelectComponent', () => {
  let component: ClinicianSelectComponent;
  let fixture: ComponentFixture<ClinicianSelectComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ClinicianSelectComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ClinicianSelectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
