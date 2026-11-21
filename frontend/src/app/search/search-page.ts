import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { map, switchMap } from 'rxjs';
import { CardApi, PAGE_SIZE } from '../cards/card-api';
import { CardImage } from '../cards/card-image';
import { CardSummary, SearchParams, SortDirection, SortOrder } from '../cards/card.models';
import { EXAMPLE_QUERIES } from './example-queries';

const SORT_ORDERS: SortOrder[] = ['name', 'number', 'cost', 'power', 'counter'];

@Component({
  selector: 'app-search-page',
  imports: [RouterLink, CardImage],
  templateUrl: './search-page.html',
  styleUrl: './search-page.scss',
})
export class SearchPage {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(CardApi);

  protected readonly sortOrders = SORT_ORDERS;
  protected readonly examples = EXAMPLE_QUERIES;

  // The URL is the source of truth, so searches can be bookmarked, shared, and navigated with Back
  private readonly params$ = this.route.queryParamMap.pipe(
    map((p): SearchParams | null => {
      const q = p.get('q');
      if (q === null || q.trim() === '') {
        return null;
      }
      const order = p.get('order') as SortOrder;
      return {
        q,
        page: Math.max(0, Number(p.get('page') ?? 0) || 0),
        order: SORT_ORDERS.includes(order) ? order : 'name',
        dir: p.get('dir') === 'desc' ? 'desc' : 'asc',
      };
    }),
  );

  protected readonly params = toSignal(this.params$, { initialValue: null });

  protected readonly results = toSignal(
    this.params$.pipe(switchMap((params) => (params ? this.api.search(params) : [null]))),
    { initialValue: null },
  );

  protected readonly page = computed(() => {
    const r = this.results();
    return r?.status === 'ok' ? r.value : null;
  });

  protected readonly error = computed(() => {
    const r = this.results();
    return r?.status === 'error' ? r.message : null;
  });

  protected readonly rangeLabel = computed(() => {
    const page = this.page();
    if (!page || page.totalItems === 0) {
      return '';
    }
    const start = page.page * PAGE_SIZE + 1;
    const end = start + page.items.length - 1;
    return `${start}–${end} of ${page.totalItems.toLocaleString()} cards`;
  });

  /** e.g. "Character · Cost 3 · 4000 power · +1000 counter" */
  protected describe(card: CardSummary): string {
    const parts = [card.category];
    if (card.cost !== null) {
      parts.push(`${card.category === 'Leader' ? 'Life' : 'Cost'} ${card.cost}`);
    }
    if (card.power !== null) {
      parts.push(`${card.power} power`);
    }
    if (card.counter !== null) {
      parts.push(`+${card.counter} counter`);
    }
    return parts.join(' · ');
  }

  protected changeSort(order: string): void {
    this.updateParams({ order, page: null });
  }

  protected changeDirection(dir: SortDirection): void {
    this.updateParams({ dir, page: null });
  }

  protected goToPage(page: number): void {
    this.updateParams({ page: page > 0 ? page : null });
    window.scrollTo({ top: 0 });
  }

  private updateParams(changes: Record<string, string | number | null>): void {
    this.router.navigate([], { queryParams: changes, queryParamsHandling: 'merge' });
  }
}
