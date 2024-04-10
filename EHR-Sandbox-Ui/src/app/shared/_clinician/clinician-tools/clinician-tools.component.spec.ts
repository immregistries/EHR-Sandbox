import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClinicianToolsComponent } from './clinician-tools.component';

describe('ClinicianToolsComponent', () => {
  let component: ClinicianToolsComponent;
  let fixture: ComponentFixture<ClinicianToolsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ClinicianToolsComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ClinicianToolsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
