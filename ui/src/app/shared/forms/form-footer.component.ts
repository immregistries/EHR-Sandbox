import {ChangeDetectionStrategy, Component, input, model, output} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {ButtonModule} from 'primeng/button';
import {ToggleSwitchModule} from 'primeng/toggleswitch';
import {TooltipModule} from 'primeng/tooltip';

/**
 * Sticky action bar for a form dialog. Holds the form-wide options (lifting
 * required rules, generating a record) on the left and the host's save
 * actions, projected into `[actions]`, on the right — so the primary button
 * stays reachable however long the form is.
 */
@Component({
  selector: 'app-form-footer',
  standalone: true,
  imports: [FormsModule, ButtonModule, ToggleSwitchModule, TooltipModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <footer class="form-footer">
      @if (allowLift()) {
        <div class="option">
          <p-toggleswitch
            inputId="lift-required"
            [ngModel]="liftRequired()"
            (ngModelChange)="liftRequired.set($event)"
            [ngModelOptions]="{standalone: true}"
          />
          <label for="lift-required" class="option-label">
            Lift required-field rules
            <small>Save incomplete data to test how the registry responds</small>
          </label>
        </div>
      }

      @if (allowRandom()) {
        <p-button
          icon="pi pi-sparkles"
          label="Fill random"
          size="small"
          [outlined]="true"
          severity="secondary"
          pTooltip="Replace the form with a generated record"
          tooltipPosition="top"
          (onClick)="fillRandom.emit()"
        />
      }

      <span class="footer-spacer"></span>

      <ng-content select="[actions]" />
    </footer>
  `,
  styleUrl: './form-footer.component.scss',
})
export class FormFooterComponent {
  readonly allowLift = input(true);
  readonly allowRandom = input(true);

  readonly liftRequired = model(false);
  readonly fillRandom = output<void>();
}
