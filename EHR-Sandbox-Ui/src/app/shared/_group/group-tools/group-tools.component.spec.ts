import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GroupToolsComponent } from './group-tools.component';

describe('GroupToolsComponent', () => {
  let component: GroupToolsComponent;
  let fixture: ComponentFixture<GroupToolsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GroupToolsComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GroupToolsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
