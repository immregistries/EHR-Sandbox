import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GroupBulkCompareComponent } from './group-bulk-compare.component';

describe('GroupBulkCompareComponent', () => {
  let component: GroupBulkCompareComponent;
  let fixture: ComponentFixture<GroupBulkCompareComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GroupBulkCompareComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GroupBulkCompareComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
