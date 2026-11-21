import { Component, inject, linkedSignal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterOutlet } from '@angular/router';
import { filter, map } from 'rxjs';

@Component({
  imports: [RouterOutlet, RouterLink],
  selector: 'app-root',
  styleUrl: './app.scss',
  templateUrl: './app.html',
})
export class App {
  private readonly router = inject(Router);

  // The current ?q= from the URL, so the search box shows the active query on every page
  private readonly urlQuery = toSignal(
    this.router.events.pipe(
      filter((e) => e instanceof NavigationEnd),
      map(() => this.router.routerState.snapshot.root.queryParamMap.get('q') ?? ''),
    ),
    { initialValue: '' },
  );

  // What the user is typing; resets to the URL's query after each navigation
  protected readonly query = linkedSignal(() => this.urlQuery());

  protected search(event: Event): void {
    event.preventDefault();
    const q = this.query().trim();
    this.router.navigate(['/'], { queryParams: q ? { q } : {} });
  }
}
