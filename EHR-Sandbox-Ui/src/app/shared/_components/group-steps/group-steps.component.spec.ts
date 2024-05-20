import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GroupStepsComponent } from './group-steps.component';

describe('GroupStepsComponent', () => {
  let component: GroupStepsComponent;
  let fixture: ComponentFixture<GroupStepsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GroupStepsComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GroupStepsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
