import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ImmunizationRegistryFormComponent } from './immunization-registry-form.component';

describe('ImmunizationRegistryFormComponent', () => {
  let component: ImmunizationRegistryFormComponent;
  let fixture: ComponentFixture<ImmunizationRegistryFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ImmunizationRegistryFormComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ImmunizationRegistryFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
