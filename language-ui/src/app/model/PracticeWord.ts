export interface PracticeWord {
  id: number;
  word: string;
  meaning: string;
  wordAudioUrl: string;
  passed?: boolean;
  sentence: string;
  sentenceMeaning: string;
  sentenceAudioUrl: string;
  author: string;
}
