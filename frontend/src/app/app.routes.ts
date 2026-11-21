import { Routes } from '@angular/router';
import { CardPage } from './card/card-page';
import { SearchPage } from './search/search-page';
import { SyntaxPage } from './syntax/syntax-page';

export const routes: Routes = [
  { path: '', component: SearchPage, title: 'Log Pose · One Piece Card Search' },
  { path: 'card/:cardNumber', component: CardPage, title: 'Card · Log Pose' },
  { path: 'syntax', component: SyntaxPage, title: 'Search syntax · Log Pose' },
  { path: '**', redirectTo: '' },
];
