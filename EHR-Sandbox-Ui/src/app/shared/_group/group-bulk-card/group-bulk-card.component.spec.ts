import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GroupBulkCardComponent } from './group-bulk-card.component';

describe('GroupBulkCardComponent', () => {
  let component: GroupBulkCardComponent;
  let fixture: ComponentFixture<GroupBulkCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GroupBulkCardComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GroupBulkCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
