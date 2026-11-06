import {ChangeDetectionStrategy, Component, computed, DestroyRef, effect, inject, input, signal,} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {FormArray, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {ButtonModule} from 'primeng/button';
import {TooltipModule} from 'primeng/tooltip';
import {FormField} from './form-config';
import {FormEngineService} from './form-engine.service';
import {FormFieldComponent} from './form-field.component';

/**
 * A repeating field (names, identifiers, addresses...), rendered as one
 * nested card per entry with its own header and remove button.
 */
@Component({
  selector: 'app-form-list',
  standalone: true,
  imports: [ReactiveFormsModule, ButtonModule, TooltipModule, FormFieldComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="list">
      @if (!singleEntry()) {
        <header class="list-header">
          <h4 class="list-title">{{ field().title }}</h4>
          @if (entries().length) {
            <span class="list-count">{{ entries().length }}</span>
          }
          <span class="list-spacer"></span>
          <p-button
            icon="pi pi-eraser"
            label="Clear"
            [text]="true"
            size="small"
            severity="secondary"
            [disabled]="!entries().length"
            [pTooltip]="'Remove every ' + field().title.toLowerCase()"
            tooltipPosition="left"
            [ariaLabel]="'Clear ' + field().title"
            (onClick)="clear()"
          />
        </header>
      }

      @if (singleEntry()) {
        <!-- One fixed entry: the value stays an array, but the add/remove
             controls and the per-entry header are not offered. -->
        <div class="entry-grid entry-grid--single">
          @for (sub of field().itemFields ?? []; track sub.attributeName) {
            <app-form-field
              [field]="sub"
              [group]="soleEntry()"
              [idPrefix]="field().attributeName + '-0-'"
              [comparison]="comparisonFor(0, sub.attributeName)"
              [warnings]="warningsFor(0, sub.attributeName)"
            />
          }
        </div>
      } @else {
        <!-- Entries run horizontally in a scrolling track, so a list with
             many entries does not push the rest of the form down. -->
        <div class="track" role="group" [attr.aria-label]="field().title + ' entries'">
          @for (entry of entries(); track entry; let i = $index) {
            <section class="entry">
              <header class="entry-header">
                <span class="entry-title">{{ field().title }} {{ i + 1 }}</span>
                @if (entryLabel(i); as label) {
                  <span class="entry-badge">{{ label }}</span>
                }
                <span class="entry-spacer"></span>
                <p-button
                  icon="pi pi-times"
                  [text]="true"
                  size="small"
                  severity="secondary"
                  [pTooltip]="'Remove ' + field().title + ' ' + (i + 1)"
                  tooltipPosition="left"
                  [ariaLabel]="'Remove ' + field().title + ' ' + (i + 1)"
                  (onClick)="remove(i)"
                />
              </header>

              <div class="entry-grid">
                @for (sub of field().itemFields ?? []; track sub.attributeName) {
                  <app-form-field
                    [field]="sub"
                    [group]="entry"
                    [idPrefix]="field().attributeName + '-' + i + '-'"
                    [comparison]="comparisonFor(i, sub.attributeName)"
                    [warnings]="warningsFor(i, sub.attributeName)"
                  />
                }
              </div>
            </section>
          } @empty {
            <p class="empty">No {{ field().title.toLowerCase() }} yet.</p>
          }

          <!-- Stays beside the track rather than scrolling away with it. -->
          <div class="track-actions">
            <p-button
              icon="pi pi-plus"
              [label]="'Add ' + field().title.toLowerCase()"
              size="small"
              [outlined]="true"
              severity="secondary"
              (onClick)="add()"
            />
          </div>
        </div>
      }
    </div>
  `,
  styleUrl: './form-list.component.scss',
})
export class FormListComponent {
  private readonly engine = inject(FormEngineService);

  private readonly destroyRef = inject(DestroyRef);

  readonly field = input.required<FormField<never>>();
  readonly array = input.required<FormArray>();
  /** Per-entry previous values, when comparing against another record. */
  readonly comparison = input<Record<number, Record<string, unknown>> | undefined>(undefined);
  /** Per-entry warnings, keyed by entry index then attribute. */
  readonly warnings = input<Record<number, Record<string, string[]>>>({});

  /**
   * `array().controls` is a plain array, so a computed over it would never
   * recompute when the controls are replaced — which is what "Fill random",
   * opening the edit dialog, and add/remove all do. Mirror the array's
   * events into a signal and depend on that.
   */
  private readonly arrayVersion = signal(0);

  constructor() {
    // Re-subscribe whenever the bound array changes.
    effect((onCleanup) => {
      const array = this.array();
      this.arrayVersion.update((version) => version + 1);
      const subscription = array.events
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe(() => this.bumpVersion());
      onCleanup(() => subscription.unsubscribe());
    });
  }

  readonly entries = computed(
    () => {
      this.arrayVersion();
      return [...(this.array().controls as FormGroup[])];
    },
    // The controls array is mutated in place, so the default identity
    // comparison would treat every version as unchanged.
    {equal: () => false},
  );

  readonly singleEntry = computed(() => this.field().singleEntry === true);

  /**
   * The one group rendered in single-entry mode. The engine guarantees it
   * exists when building or resetting, so this never mutates during render.
   */
  readonly soleEntry = computed(() => this.entries()[0]);

  entryLabel(index: number): string | undefined {
    const value = this.entries()[index]?.getRawValue() as Record<string, unknown>;
    return this.field().itemLabel?.(value, index);
  }

  comparisonFor(index: number, attribute: string): unknown {
    return this.comparison()?.[index]?.[attribute];
  }

  warningsFor(index: number, attribute: string): string[] {
    return this.warnings()[index]?.[attribute] ?? [];
  }

  add(): void {
    this.engine.addListItem(this.array(), this.field());
    this.bumpVersion();
  }

  remove(index: number): void {
    this.array().removeAt(index);
    this.bumpVersion();
  }

  /** Empties the list; the per-entry button removes one at a time. */
  clear(): void {
    if (!this.array().length) {
      return;
    }
    this.array().clear();
    this.bumpVersion();
  }

  private bumpVersion(): void {
    this.arrayVersion.update((version) => version + 1);
  }
}
