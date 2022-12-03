import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SelectImmregistryComponent } from './select-immregistry.component';

describe('SelectImmregistryComponent', () => {
  let component: SelectImmregistryComponent;
  let fixture: ComponentFixture<SelectImmregistryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SelectImmregistryComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SelectImmregistryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
