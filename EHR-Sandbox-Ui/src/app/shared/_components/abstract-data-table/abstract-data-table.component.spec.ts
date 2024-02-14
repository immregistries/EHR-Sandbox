import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AbstractDataTableComponent } from './abstract-data-table.component';

describe('AbstractDataTableComponent', () => {
  let component: AbstractDataTableComponent;
  let fixture: ComponentFixture<AbstractDataTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AbstractDataTableComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AbstractDataTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
