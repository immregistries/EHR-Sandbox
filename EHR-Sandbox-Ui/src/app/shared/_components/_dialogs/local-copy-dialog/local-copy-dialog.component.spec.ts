import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LocalCopyDialogComponent } from './local-copy-dialog.component';

describe('LocalCopyDialogComponent', () => {
  let component: LocalCopyDialogComponent;
  let fixture: ComponentFixture<LocalCopyDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ LocalCopyDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LocalCopyDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
