import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VaccinationToolsComponent } from './vaccination-tools.component';

describe('VaccinationToolsComponent', () => {
  let component: VaccinationToolsComponent;
  let fixture: ComponentFixture<VaccinationToolsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ VaccinationToolsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VaccinationToolsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
