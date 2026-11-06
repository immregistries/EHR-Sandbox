import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  input,
  model,
  output,
  signal
} from '@angular/core';
import {FormGroup, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {ButtonModule} from 'primeng/button';
import {MessageModule} from 'primeng/message';
import {TabsModule} from 'primeng/tabs';
import {ToggleSwitchModule} from 'primeng/toggleswitch';
import {TooltipModule} from 'primeng/tooltip';
import {FormCard, FormConfig, FormType} from './form-config';
import {FormCardComponent} from './form-card.component';
import {FormWarnings} from './form-warnings';
import {FormEngineService} from './form-engine.service';

/**
 * Reusable shell for any entity form: renders the configured cards, offers
 * the raw JSON view, and owns the "lift required rules" switch.
 *
 * The host supplies the config and the FormGroup, and handles saving.
 */
@Component({
  selector: 'app-entity-form',
  standalone: true,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    ButtonModule,
    MessageModule,
    TabsModule,
    ToggleSwitchModule,
    TooltipModule,
    FormCardComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="entity-form">
      <header class="form-toolbar">
        <div class="toolbar-group">
          <p-toggleswitch
            inputId="lift-required"
            [ngModel]="liftRequired()"
            (ngModelChange)="liftRequired.set($event)"
            [ngModelOptions]="{standalone: true}"
          />
          <label for="lift-required" class="toggle-label">
            Lift required-field rules
            <small>Save incomplete data to test how the registry responds</small>
          </label>
        </div>

        <span class="toolbar-spacer"></span>

        @if (allowRandom()) {
          <p-button
            icon="pi pi-sparkles"
            label="Fill random"
            size="small"
            [outlined]="true"
            severity="secondary"
            pTooltip="Replace the form with a generated record"
            tooltipPosition="bottom"
            (onClick)="fillRandom.emit()"
          />
        }
      </header>

      @if (liftRequired()) {
        <p-message severity="warn" styleClass="full-width">
          Required-field rules are lifted — this record may be rejected by the registry.
        </p-message>
      }

      <p-tabs value="form">
        <p-tablist>
          <p-tab value="form">Form</p-tab>
          <p-tab value="json">JSON</p-tab>
        </p-tablist>

        <p-tabpanels>
          <p-tabpanel value="form">
            <div class="cards">
              @for (card of config(); track card.title) {
                <app-form-card
                  [class.card--wide]="hasList(card)"
                  [card]="card"
                  [form]="form()"
                  [comparison]="comparison()"
                  [warnings]="warnings()"
                />
              }
            </div>
          </p-tabpanel>

          <p-tabpanel value="json">
            <p class="json-note">
              The raw model, useful for copying a record between users. Id fields are ignored
              on save.
            </p>
            <textarea
              class="json-editor"
              spellcheck="false"
              [value]="json()"
              (input)="onJsonInput($event)"
            ></textarea>
            @if (jsonError(); as error) {
              <p-message severity="error" [text]="error" styleClass="full-width" />
            }
          </p-tabpanel>
        </p-tabpanels>
      </p-tabs>
    </div>
  `,
  styleUrl: './entity-form.component.scss',
})
export class EntityFormComponent {
  private readonly engine = inject(FormEngineService);

  readonly config = input.required<FormConfig<never>>();
  readonly form = input.required<FormGroup>();
  readonly comparison = input<Record<string, unknown> | undefined>(undefined);
  readonly warnings = input<FormWarnings>({});
  readonly allowRandom = input(true);

  readonly liftRequired = model(false);
  readonly fillRandom = output<void>();
  /** Emitted when the JSON tab produces a valid, different model. */
  readonly jsonChange = output<Record<string, unknown>>();

  private readonly jsonDraft = signal<string | null>(null);
  readonly jsonError = signal<string | null>(null);

  readonly json = computed(
    () => this.jsonDraft() ?? JSON.stringify(this.form().getRawValue(), null, 2),
  );

  constructor() {
    // Re-apply validators in place, so values entered so far are preserved.
    effect(() => {
      const lift = this.liftRequired();
      const form = this.form();
      this.engine.applyRequiredRules(form, this.config(), lift);
      if (lift) {
        // Errors shown before lifting are no longer meaningful.
        form.markAsUntouched();
      }
    });
  }

  onJsonInput(event: Event): void {
    const text = (event.target as HTMLTextAreaElement).value;
    this.jsonDraft.set(text);
    try {
      const parsed = JSON.parse(text) as Record<string, unknown>;
      this.jsonError.set(null);
      this.jsonChange.emit(parsed);
    } catch (error) {
      this.jsonError.set(error instanceof Error ? error.message : 'Invalid JSON');
    }
  }

  /** A card with a repeating field needs the full width for its track. */
  hasList(card: FormCard<never>): boolean {
    return card.fields.some((field) => field.type === FormType.list);
  }

  /** Drops the JSON draft so the textarea follows the form again. */
  resetJsonDraft(): void {
    this.jsonDraft.set(null);
    this.jsonError.set(null);
  }
}
