import {Component} from '@angular/core';
import {renderComponent} from '../../../testing';
import {FormFooterComponent} from './form-footer.component';

/** Host that projects save actions, as the real forms do. */
@Component({
  standalone: true,
  imports: [FormFooterComponent],
  template: `
    <app-form-footer [allowLift]="lift" [allowRandom]="random">
      <ng-container actions>
        <button type="button">Save</button>
      </ng-container>
    </app-form-footer>
  `,
})
class HostComponent {
  lift = true;
  random = true;
}

describe('FormFooterComponent', () => {
  it('offers the form-wide options beside the projected actions', async () => {
    const h = await renderComponent(HostComponent);

    expect(h.query('p-toggleswitch')).not.toBeNull();
    expect(h.queryAll<HTMLElement>('button').some((b) => b.textContent?.includes('Fill random')))
      .toBeTrue();
    expect(h.queryAll<HTMLElement>('button').some((b) => b.textContent?.includes('Save')))
      .toBeTrue();
  });

  it('hides the lift toggle when the form opts out', async () => {
    const h = await renderComponent(HostComponent, {inputs: {}});
    h.component.lift = false;
    h.detectChanges();

    expect(h.query('p-toggleswitch')).toBeNull();
  });

  it('hides fill random when the form opts out', async () => {
    const h = await renderComponent(HostComponent);
    h.component.random = false;
    h.detectChanges();

    expect(h.queryAll<HTMLElement>('button').some((b) => b.textContent?.includes('Fill random')))
      .toBeFalse();
  });

  it('keeps the projected actions when both options are hidden', async () => {
    const h = await renderComponent(HostComponent);
    h.component.lift = false;
    h.component.random = false;
    h.detectChanges();

    expect(h.queryAll<HTMLElement>('button').some((b) => b.textContent?.includes('Save')))
      .toBeTrue();
  });
});

describe('FormFooterComponent stickiness', () => {
  it('is positioned sticky so it stays visible while the form scrolls', async () => {
    const h = await renderComponent(HostComponent);

    const footer = h.query<HTMLElement>('.form-footer');
    expect(getComputedStyle(footer!).position).toBe('sticky');
    // Opaque, so scrolling content does not show through.
    expect(getComputedStyle(footer!).backgroundColor).not.toBe('rgba(0, 0, 0, 0)');
  });
});
