import {ChangeDetectionStrategy, Component, input} from '@angular/core';
import {ProgressBarModule} from 'primeng/progressbar';

/** Framed dashboard tile with a header, optional count and action slot. */
@Component({
  selector: 'app-dashboard-panel',
  standalone: true,
  imports: [ProgressBarModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="panel">
      <header class="panel-header">
        <i [class]="icon()"></i>
        <h2>{{ title() }}</h2>
        @if (count() !== undefined) {
          <span class="panel-count">{{ count() }}</span>
        }
        <span class="panel-spacer"></span>
        <ng-content select="[actions]" />
      </header>

      <div class="panel-progress">
        @if (loading()) {
          <p-progressbar mode="indeterminate" [style]="{ height: '2px' }" />
        }
      </div>

      <div class="panel-body">
        <ng-content />
      </div>
    </section>
  `,
  styleUrl: './dashboard-panel.component.scss',
})
export class DashboardPanelComponent {
  readonly title = input.required<string>();
  readonly icon = input('pi pi-circle');
  readonly count = input<number | undefined>(undefined);
  readonly loading = input(false);
}
