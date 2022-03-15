import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TenantCreationComponent } from './tenant-creation.component';

describe('TenantCreationComponent', () => {
  let component: TenantCreationComponent;
  let fixture: ComponentFixture<TenantCreationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TenantCreationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TenantCreationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
