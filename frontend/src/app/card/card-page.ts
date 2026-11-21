import { Component, computed, effect, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { map, switchMap } from 'rxjs';
import { CardApi } from '../cards/card-api';
import { CardImage } from '../cards/card-image';
import { splitEffect } from '../cards/effect-text';

@Component({
  selector: 'app-card-page',
  imports: [RouterLink, CardImage],
  templateUrl: './card-page.html',
  styleUrl: './card-page.scss',
})
export class CardPage {
  private readonly route = inject(ActivatedRoute);
  private readonly api = inject(CardApi);
  private readonly title = inject(Title);

  private readonly result = toSignal(
    this.route.paramMap.pipe(switchMap((p) => this.api.getCard(p.get('cardNumber') ?? ''))),
  );

  // Which printing is shown; kept in the URL (?print=OP01-001_p1) so it can be linked to
  private readonly printParam = toSignal(this.route.queryParamMap.pipe(map((p) => p.get('print'))));

  protected readonly card = computed(() => {
    const r = this.result();
    return r?.status === 'ok' ? r.value : null;
  });

  protected readonly error = computed(() => {
    const r = this.result();
    return r?.status === 'error' ? r.message : null;
  });

  protected readonly selectedPrinting = computed(() => {
    const card = this.card();
    if (!card) {
      return null;
    }
    return (
      card.printings.find((p) => p.sourceId === this.printParam()) ??
      card.printings.find((p) => p.sourceId === card.cardNumber) ??
      card.printings[0] ??
      null
    );
  });

  protected readonly effectParts = computed(() => splitEffect(this.card()?.effectText ?? null));
  protected readonly triggerParts = computed(() => splitEffect(this.card()?.triggerText ?? null));

  // Leaders have Life instead of a cost; the data source stores it in the cost field
  protected readonly costLabel = computed(() => (this.card()?.category === 'Leader' ? 'Life' : 'Cost'));

  constructor() {
    effect(() => {
      const card = this.card();
      if (card) {
        this.title.setTitle(`${card.name} (${card.cardNumber}) · Log Pose`);
      }
    });
  }

  protected quoted(value: string): string {
    return /\s/.test(value) ? `"${value}"` : value;
  }
}
