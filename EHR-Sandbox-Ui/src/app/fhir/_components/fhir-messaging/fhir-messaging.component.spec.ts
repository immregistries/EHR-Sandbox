import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FhirMessagingComponent } from './fhir-messaging.component';

describe('FhirMessagingComponent', () => {
  let component: FhirMessagingComponent;
  let fixture: ComponentFixture<FhirMessagingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FhirMessagingComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FhirMessagingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
