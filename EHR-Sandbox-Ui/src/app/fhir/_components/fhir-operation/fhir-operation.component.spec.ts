import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FhirOperationComponent } from './fhir-operation.component';

describe('FhirOperationComponent', () => {
  let component: FhirOperationComponent;
  let fixture: ComponentFixture<FhirOperationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FhirOperationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FhirOperationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
