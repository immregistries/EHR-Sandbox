import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RemoteGroupTableComponent } from './remote-group-table.component';

describe('RemoteGroupTableComponent', () => {
  let component: RemoteGroupTableComponent;
  let fixture: ComponentFixture<RemoteGroupTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RemoteGroupTableComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RemoteGroupTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
