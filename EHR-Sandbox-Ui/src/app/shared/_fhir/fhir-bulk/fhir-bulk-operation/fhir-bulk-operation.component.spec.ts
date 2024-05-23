import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FhirBulkOperationComponent } from './fhir-bulk-operation.component';

describe('FhirBulkOperationComponent', () => {
  let component: FhirBulkOperationComponent;
  let fixture: ComponentFixture<FhirBulkOperationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FhirBulkOperationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FhirBulkOperationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
