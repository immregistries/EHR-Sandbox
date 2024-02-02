import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PatientGroupListComponent } from './patient-group-list.component';

describe('PatientGroupListComponent', () => {
  let component: PatientGroupListComponent;
  let fixture: ComponentFixture<PatientGroupListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PatientGroupListComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PatientGroupListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
