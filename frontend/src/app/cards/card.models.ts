// Shapes returned by the Spring Boot API (see backend/src/main/java/com/logpose/api/dto)

export interface CardSummary {
  cardNumber: string;
  name: string;
  category: string;
  colors: string[];
  cost: number | null;
  power: number | null;
  counter: number | null;
  rarity: string | null;
  setCode: string | null;
  imageUrl: string | null;
  printingCount: number;
}

export interface Printing {
  sourceId: string;
  setCode: string;
  setName: string | null;
  rarity: string | null;
  imageUrl: string | null;
  blockNumber: number | null;
}

export interface CardDetail {
  cardNumber: string;
  name: string;
  category: string;
  colors: string[];
  attributes: string[];
  traits: string[];
  cost: number | null;
  power: number | null;
  counter: number | null;
  effectText: string | null;
  triggerText: string | null;
  printings: Printing[];
}

export interface PageResponse<T> {
  items: T[];
  page: number;
  size: number;
  totalItems: number;
  totalPages: number;
}

export type SortOrder = 'name' | 'number' | 'cost' | 'power' | 'counter';
export type SortDirection = 'asc' | 'desc';

export interface SearchParams {
  q: string;
  page: number;
  order: SortOrder;
  dir: SortDirection;
}

/** Async request state for templates: loading, loaded value, or an error message. */
export type Loadable<T> =
  | { status: 'loading' }
  | { status: 'ok'; value: T }
  | { status: 'error'; message: string };
