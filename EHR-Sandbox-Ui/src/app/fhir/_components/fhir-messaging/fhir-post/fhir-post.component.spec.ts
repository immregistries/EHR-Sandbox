import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FhirPostComponent } from './fhir-post.component';

describe('FhirPostComponent', () => {
  let component: FhirPostComponent;
  let fixture: ComponentFixture<FhirPostComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FhirPostComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FhirPostComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
