import {ChangeDetectionStrategy, Component, inject, input, OnInit, signal} from '@angular/core';
import {ProgressSpinnerModule} from 'primeng/progressspinner';
import {TagModule} from 'primeng/tag';
import {ImmunizationRegistry} from '../../../core/models';
import {ImmunizationRegistryService} from '../../../core/services';

type CheckState = 'pending' | 'ok' | 'failed';

interface Check {
  key: string;
  label: string;
  state: CheckState;
  detail?: string;
}

/** Connectivity and authentication checks against a saved registry. */
@Component({
  selector: 'app-registry-check',
  standalone: true,
  imports: [ProgressSpinnerModule, TagModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <ul class="checks">
      @for (check of checks(); track check.key) {
        <li class="check">
          <span class="check-label">{{ check.label }}</span>
          @switch (check.state) {
            @case ('pending') {
              <p-progressspinner [style]="{width: '16px', height: '16px'}" strokeWidth="6" />
            }
            @case ('ok') {
              <p-tag value="OK" severity="success" [rounded]="true" icon="pi pi-check" />
            }
            @default {
              <p-tag value="Failed" severity="danger" [rounded]="true" icon="pi pi-times" />
            }
          }
        </li>
        @if (check.detail) {
          <li class="check-detail">{{ check.detail }}</li>
        }
      }
    </ul>
  `,
  styleUrl: './registry-check.component.scss',
})
export class RegistryCheckComponent implements OnInit {
  private readonly registryService = inject(ImmunizationRegistryService);

  readonly registry = input.required<ImmunizationRegistry>();

  readonly checks = signal<Check[]>([
    {key: 'hl7', label: 'HL7v2 SOAP connectivity', state: 'pending'},
    {key: 'hl7Auth', label: 'HL7v2 SOAP authentication', state: 'pending'},
    {key: 'fhir', label: 'FHIR server metadata', state: 'pending'},
  ]);

  ngOnInit(): void {
    const id = this.registry().id;
    if (!id) {
      return;
    }

    // Authentication implies connectivity, so a successful auth settles both.
    this.registryService.checkConnectivityAuth(id).subscribe({
      next: () => {
        this.set('hl7Auth', 'ok');
        this.set('hl7', 'ok');
      },
      error: (error) => {
        this.set('hl7Auth', 'failed', message(error));
        this.registryService.checkConnectivity(id).subscribe({
          next: () => this.set('hl7', 'ok'),
          error: (connectivityError) => this.set('hl7', 'failed', message(connectivityError)),
        });
      },
    });

    this.registryService.fhirMetadata(id).subscribe({
      next: () => this.set('fhir', 'ok'),
      error: (error) => this.set('fhir', 'failed', message(error)),
    });
  }

  private set(key: string, state: CheckState, detail?: string): void {
    this.checks.update((checks) =>
      checks.map((check) => (check.key === key ? {...check, state, detail} : check)),
    );
  }
}

function message(error: { status?: number; error?: unknown; message?: string }): string {
  if (typeof error?.error === 'string' && error.error.trim()) {
    return error.error.slice(0, 200);
  }
  return error?.status ? `HTTP ${error.status}` : (error?.message ?? 'Unreachable');
}
