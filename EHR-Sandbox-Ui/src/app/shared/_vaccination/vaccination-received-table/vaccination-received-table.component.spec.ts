import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VaccinationReceivedTableComponent } from './vaccination-received-table.component';

describe('VaccinationReceivedTableComponent', () => {
  let component: VaccinationReceivedTableComponent;
  let fixture: ComponentFixture<VaccinationReceivedTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ VaccinationReceivedTableComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VaccinationReceivedTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
