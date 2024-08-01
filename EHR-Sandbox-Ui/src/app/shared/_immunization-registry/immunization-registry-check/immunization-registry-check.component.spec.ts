import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ImmunizationRegistryCheckComponent } from './immunization-registry-check.component';

describe('ImmunizationRegistryCheckComponent', () => {
  let component: ImmunizationRegistryCheckComponent;
  let fixture: ComponentFixture<ImmunizationRegistryCheckComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ImmunizationRegistryCheckComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ImmunizationRegistryCheckComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
