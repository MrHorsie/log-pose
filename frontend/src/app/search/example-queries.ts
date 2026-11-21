export interface ExampleQuery {
  query: string;
  description: string;
}

export const EXAMPLE_QUERIES: ExampleQuery[] = [
  { query: 'c:red t:character cost<=3 counter:2000', description: 'Cheap red characters with a 2000 counter' },
  { query: 't:leader trait:"straw hat"', description: 'Straw Hat Crew leaders' },
  { query: 'o:"[Blocker]" power>=6000', description: 'Big blockers' },
  { query: '(c:purple or c:black) t:event o:draw', description: 'Purple or black events that draw' },
  { query: 'set:op01 rarity:sec', description: 'Secret rares from Romance Dawn' },
  { query: 'has:trigger t:event block>=4', description: 'Newer events with a trigger' },
  { query: 'luffy -t:character', description: 'Luffy cards that are not characters' },
];
