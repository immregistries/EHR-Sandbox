import {ChangeDetectionStrategy, Component, computed, input} from '@angular/core';
import {FormArray, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {TooltipModule} from 'primeng/tooltip';
import {FormCard, FormField, FormType, hl7LocationLabel} from './form-config';
import {FormWarnings} from './form-warnings';
import {FormFieldComponent} from './form-field.component';
import {FormListComponent} from './form-list.component';

interface FieldGroup {
  fields: FormField<never>[];
  /** Present when this group is a single repeating field. */
  list?: FormField<never>;
}

/** One section of an entity form. */
@Component({
  selector: 'app-form-card',
  standalone: true,
  imports: [ReactiveFormsModule, TooltipModule, FormFieldComponent, FormListComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="card">
      <header class="card-header">
        <h3>{{ card().title }}</h3>
        @if (hl7Label(); as label) {
          <span class="hl7" [pTooltip]="'HL7 segment ' + label" tooltipPosition="top">{{ label }}</span>
        }
      </header>

      <div class="card-body">
        <!-- Scalar fields share a responsive grid; each list gets a row of
             its own, so its horizontal track is never boxed into a column. -->
        @for (group of fieldGroups(); track $index) {
          @if (group.list) {
            <section class="list-row">
              <h4 class="list-row-title">{{ group.list.title }}</h4>
              <app-form-list
                [field]="group.list"
                [array]="arrayFor(group.list.attributeName)"
                [comparison]="listComparison(group.list.attributeName)"
                [warnings]="listWarnings(group.list.attributeName)"
              />
            </section>
          } @else {
            <div class="field-row">
              @for (field of group.fields; track field.attributeName) {
                <div [class.span-2]="field.span === 2" [class.span-3]="field.span === 3">
                  <app-form-field
                    [field]="field"
                    [group]="form()"
                    [comparison]="comparison()?.[field.attributeName]"
                    [warnings]="fieldWarnings(field.attributeName)"
                  />
                </div>
              }
            </div>
          }
        }
      </div>

      @if (card().note) {
        <footer class="card-note">{{ card().note }}</footer>
      }
    </section>
  `,
  styleUrl: './form-card.component.scss',
})
export class FormCardComponent {
  readonly card = input.required<FormCard<never>>();
  readonly form = input.required<FormGroup>();
  readonly comparison = input<Record<string, unknown> | undefined>(undefined);
  readonly warnings = input<FormWarnings>({});

  readonly listType = FormType.list;

  /**
   * Splits a card's fields into runs of scalar fields and standalone lists,
   * preserving the configured order. Each list then occupies a full row.
   */
  readonly fieldGroups = computed<FieldGroup[]>(() => {
    const groups: FieldGroup[] = [];
    let run: FormField<never>[] = [];

    for (const field of this.card().fields as FormField<never>[]) {
      if (field.type === FormType.list) {
        if (run.length) {
          groups.push({fields: run});
          run = [];
        }
        groups.push({fields: [], list: field});
        continue;
      }
      run.push(field);
    }
    if (run.length) {
      groups.push({fields: run});
    }
    return groups;
  });

  hl7Label(): string {
    return hl7LocationLabel(this.card().hl7Location);
  }

  fieldWarnings(attribute: string): string[] {
    const entry = this.warnings()[attribute];
    return Array.isArray(entry) ? entry : [];
  }

  arrayFor(attribute: string): FormArray {
    return this.form().get(attribute) as FormArray;
  }

  listComparison(attribute: string): Record<number, Record<string, unknown>> | undefined {
    return this.comparison()?.[attribute] as Record<number, Record<string, unknown>> | undefined;
  }

  listWarnings(attribute: string): Record<number, Record<string, string[]>> {
    const entry = this.warnings()[attribute];
    return Array.isArray(entry) ? {} : (entry ?? {});
  }
}
