import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TabWithMenuComponent } from './tab-with-menu.component';

describe('TabWithMenuComponent', () => {
  let component: TabWithMenuComponent;
  let fixture: ComponentFixture<TabWithMenuComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TabWithMenuComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TabWithMenuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
