import { splitEffect } from './effect-text';

describe('splitEffect', () => {
  it('returns nothing for empty text', () => {
    expect(splitEffect(null)).toEqual([]);
    expect(splitEffect('')).toEqual([]);
  });

  it('marks bracketed keywords', () => {
    expect(splitEffect('[DON!! x1] [When Attacking] Draw 1 card.')).toEqual([
      { text: '[DON!! x1]', keyword: true },
      { text: ' ', keyword: false },
      { text: '[When Attacking]', keyword: true },
      { text: ' Draw 1 card.', keyword: false },
    ]);
  });

  it('leaves plain text alone', () => {
    expect(splitEffect('Draw 1 card.')).toEqual([{ text: 'Draw 1 card.', keyword: false }]);
  });
});
