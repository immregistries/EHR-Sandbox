import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FhirGetComponent } from './fhir-get.component';

describe('FhirGetComponent', () => {
  let component: FhirGetComponent;
  let fixture: ComponentFixture<FhirGetComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FhirGetComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FhirGetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
