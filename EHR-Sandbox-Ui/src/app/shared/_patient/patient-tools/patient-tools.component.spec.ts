import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PatientToolsComponent } from './patient-tools.component';

describe('PatientToolsComponent', () => {
  let component: PatientToolsComponent;
  let fixture: ComponentFixture<PatientToolsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PatientToolsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PatientToolsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
