import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ImmunizationRegistryMenuComponent } from './immunization-registry-menu.component';

describe('ImmunizationRegistryMenuComponent', () => {
  let component: ImmunizationRegistryMenuComponent;
  let fixture: ComponentFixture<ImmunizationRegistryMenuComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ImmunizationRegistryMenuComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ImmunizationRegistryMenuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
