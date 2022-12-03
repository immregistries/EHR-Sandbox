import { TextShortenPipe } from './text-shorten.pipe';

describe('TextShortenPipe', () => {
  it('create an instance', () => {
    const pipe = new TextShortenPipe();
    expect(pipe).toBeTruthy();
  });
});
