import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { catchError, map, Observable, of, startWith } from 'rxjs';
import { CardDetail, CardSummary, Loadable, PageResponse, SearchParams } from './card.models';

export const PAGE_SIZE = 60;

@Injectable({ providedIn: 'root' })
export class CardApi {
  private readonly http = inject(HttpClient);

  search(params: SearchParams): Observable<Loadable<PageResponse<CardSummary>>> {
    const httpParams = new HttpParams()
      .set('q', params.q)
      .set('page', params.page)
      .set('size', PAGE_SIZE)
      .set('order', params.order)
      .set('dir', params.dir);
    return toLoadable(
      this.http.get<PageResponse<CardSummary>>('/api/cards', { params: httpParams }),
    );
  }

  getCard(cardNumber: string): Observable<Loadable<CardDetail>> {
    return toLoadable(this.http.get<CardDetail>(`/api/cards/${encodeURIComponent(cardNumber)}`));
  }
}

function toLoadable<T>(request: Observable<T>): Observable<Loadable<T>> {
  return request.pipe(
    map((value): Loadable<T> => ({ status: 'ok', value })),
    catchError((error: unknown) => of<Loadable<T>>({ status: 'error', message: errorMessage(error) })),
    startWith<Loadable<T>>({ status: 'loading' }),
  );
}

/** The API sends RFC 9457 problem details; show their "detail" when present. */
export function errorMessage(error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    const detail = (error.error as { detail?: unknown } | null)?.detail;
    if (typeof detail === 'string' && detail) {
      return detail;
    }
    if (error.status === 404) {
      return 'Not found.';
    }
    if (error.status === 0 || error.status >= 500) {
      return 'Could not reach the Log Pose API. Is the backend running?';
    }
  }
  return 'Something went wrong. Please try again.';
}
