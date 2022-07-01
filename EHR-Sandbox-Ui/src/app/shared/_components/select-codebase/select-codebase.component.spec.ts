import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SelectCodebaseComponent } from './select-codebase.component';

describe('SelectCodebaseComponent', () => {
  let component: SelectCodebaseComponent;
  let fixture: ComponentFixture<SelectCodebaseComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SelectCodebaseComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SelectCodebaseComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
