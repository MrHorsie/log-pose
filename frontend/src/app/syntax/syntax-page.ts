import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

interface FieldHelp {
  keywords: string[];
  description: string;
  examples: string[];
}

@Component({
  selector: 'app-syntax-page',
  imports: [RouterLink],
  templateUrl: './syntax-page.html',
  styleUrl: './syntax-page.scss',
})
export class SyntaxPage {
  protected readonly fields: FieldHelp[] = [
    { keywords: ['name:', 'n:'], description: 'Card name contains the text. Use = for an exact name.', examples: ['name:zoro', 'name="monkey.d.luffy"'] },
    { keywords: ['color:', 'c:'], description: 'Card has this color: red, green, blue, purple, black, yellow.', examples: ['c:red', 'c:green c:blue'] },
    { keywords: ['type:', 't:'], description: 'Card category: leader, character, event, stage.', examples: ['t:leader', '-t:character'] },
    { keywords: ['cost:'], description: 'Cost (Life for leaders). Supports = != < <= > >=.', examples: ['cost<=3', 'cost>=8'] },
    { keywords: ['power:', 'pow:'], description: 'Power. Supports comparisons.', examples: ['power>=7000'] },
    { keywords: ['counter:'], description: 'Counter value. Cards without a counter count as 0.', examples: ['counter:2000', 'counter:0'] },
    { keywords: ['text:', 'o:'], description: 'Effect or trigger text contains the phrase.', examples: ['o:"[On Play]"', 'o:draw'] },
    { keywords: ['trigger:'], description: 'Trigger text contains the phrase.', examples: ['trigger:draw', 'trigger:"K.O."'] },
    { keywords: ['has:'], description: 'Card has the property: trigger, counter, effect, power.', examples: ['has:trigger', 't:character -has:counter'] },
    { keywords: ['trait:', 'tr:'], description: 'Has a trait (type line) containing the text.', examples: ['trait:"straw hat"', 'trait:navy'] },
    { keywords: ['attribute:', 'a:'], description: 'Attribute: slash, strike, ranged, special, wisdom.', examples: ['a:slash'] },
    { keywords: ['set:', 's:'], description: 'Has a printing in the set. Dashes are optional.', examples: ['set:op01', 'set:st-14'] },
    { keywords: ['rarity:', 'r:'], description: 'Has a printing of this rarity. Short forms: c, uc, r, sr, sec, l, sp, tr, p.', examples: ['r:sec', 'rarity:"super rare"'] },
    { keywords: ['block:', 'b:'], description: 'Has a printing with this block number. Supports comparisons.', examples: ['block:1', 'block>=4'] },
  ];
}
