import { describe, expect, it } from 'vitest';
import { encodePlantUml, getPlantUmlServerUrl } from './plantuml';

describe('PlantUML utilities', () => {
  it('encodes ASCII input with the server hex format', () => {
    expect(encodePlantUml('@startuml')).toBe('~h407374617274756d6c');
  });

  it('encodes UTF-8 input without losing non-ASCII characters', () => {
    expect(encodePlantUml('тест')).toBe('~hd182d0b5d181d182');
  });

  it('uses a local proxy by default', () => {
    expect(getPlantUmlServerUrl()).toBe('/plantuml');
  });
});
