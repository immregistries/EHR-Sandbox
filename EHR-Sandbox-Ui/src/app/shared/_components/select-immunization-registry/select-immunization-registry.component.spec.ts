import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SelectImmunizationRegistryComponent } from './select-immunization-registry.component';

describe('SelectImmunizationRegistryComponent', () => {
  let component: SelectImmunizationRegistryComponent;
  let fixture: ComponentFixture<SelectImmunizationRegistryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SelectImmunizationRegistryComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SelectImmunizationRegistryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
