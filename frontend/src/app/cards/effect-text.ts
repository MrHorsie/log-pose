export interface EffectPart {
  text: string;
  keyword: boolean;
}

/**
 * Splits effect text so bracketed keywords like "[On Play]" or "[DON!! x1]" can be styled,
 * without rendering the text as HTML.
 */
export function splitEffect(text: string | null): EffectPart[] {
  if (!text) {
    return [];
  }
  return text
    .split(/(\[[^\]]+\])/)
    .filter((part) => part.length > 0)
    .map((part) => ({ text: part, keyword: part.startsWith('[') && part.endsWith(']') }));
}
