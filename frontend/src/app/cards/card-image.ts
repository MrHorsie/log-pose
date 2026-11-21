import { Component, computed, input, linkedSignal } from '@angular/core';

const COLOR_VARS: Record<string, string> = {
  red: 'var(--op-red)',
  green: 'var(--op-green)',
  blue: 'var(--op-blue)',
  purple: 'var(--op-purple)',
  black: 'var(--text)',
  yellow: 'var(--op-yellow)',
};

/**
 * Card image with a text fallback when the image is missing or fails to load.
 */
@Component({
  selector: 'app-card-image',
  template: `
    @if (src() && !failed()) {
      <img
        [src]="src()"
        [alt]="name()"
        [attr.loading]="eager() ? 'eager' : 'lazy'"
        (error)="failed.set(true)"
      />
    } @else {
      <div class="fallback" [style.--stripe]="stripe()">
        <span class="fallback-name">{{ name() }}</span>
        @if (details()) {
          <span class="fallback-details">{{ details() }}</span>
        }
        <span class="fallback-number">{{ cardNumber() }}</span>
      </div>
    }
  `,
  styles: `
    :host {
      display: block;
      aspect-ratio: 600 / 838;
      border-radius: 4.5% / 3.2%;
      overflow: hidden;
      background: var(--surface);
    }
    img {
      display: block;
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    .fallback {
      height: 100%;
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      gap: 0.5rem;
      padding: 1rem 0.75rem;
      text-align: center;
      color: var(--text);
      border: 1px solid var(--border);
      border-radius: inherit;
      background: var(--stripe) top / 100% 6px no-repeat, var(--surface);
    }
    .fallback-name {
      font-weight: 600;
      line-height: 1.3;
    }
    .fallback-details {
      color: var(--text-muted);
      font-size: 0.85rem;
      line-height: 1.4;
    }
    .fallback-number {
      color: var(--text-muted);
      font-family: var(--mono);
      font-size: 0.8rem;
    }
  `,
})
export class CardImage {
  readonly src = input<string | null>(null);
  readonly name = input.required<string>();
  readonly cardNumber = input<string>('');
  readonly colors = input<string[]>([]);
  readonly details = input<string>('');
  readonly eager = input(false);

  // Resets whenever the image source changes, e.g. when switching printings
  protected readonly failed = linkedSignal({ source: this.src, computation: () => false });

  // A stripe in the card's colors along the top of the fallback tile
  protected readonly stripe = computed(() => {
    const colors = this.colors().map((c) => COLOR_VARS[c.toLowerCase()] ?? 'var(--border)');
    if (colors.length === 0) {
      return 'linear-gradient(var(--border), var(--border))';
    }
    if (colors.length === 1) {
      return `linear-gradient(${colors[0]}, ${colors[0]})`;
    }
    return `linear-gradient(90deg, ${colors.join(', ')})`;
  });
}
