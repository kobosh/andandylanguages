import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';

export interface UploadWordResponse {
  id: number;
  word: string;
  meaning: string;
  userId: number;
  wordObjectKey: string;
  author: string;
  dialect: string;
}

@Injectable({providedIn: 'root'})
export class UploadService {
  constructor(private http: HttpClient) {
  }

  uploadWord(
    word: string, meaning: string, blob: Blob, filename: string, author: string, dialect: string | null):
    Observable<UploadWordResponse> {

    const formData = new FormData();
    formData.append('word', word);
    formData.append('meaning', meaning);
    formData.append('file', blob, filename);
    if (author)
      formData.append('authorname', author);
      if (dialect)
        formData.append('dialect', dialect.toUpperCase())


    const token = localStorage.getItem('token');

    if (!token
    )
      throw new

      Error(
        'Missing JWT token'
      );

    return this.http.post<UploadWordResponse>(
      `${environment.audioUrl}/api/recordings/upload-word`
      ,
      formData
      , {
        headers: {Authorization: `Bearer ${token}`}
      }
    )
      ;
  }

  uploadSentence(
    recordId: number, sentence: string, sentenceMeaning: string, blob: Blob, filename: string
  ):
    Observable<object> {

    const formData = new FormData();

    // 🔥 MUST match backend param names exactly
   // formData.append('dialect', dialect)
    formData.append("recordingId", recordId.toString());
    formData.append("sentence", sentence);
    formData.append("sentenceMeaning", sentenceMeaning);
    formData.append("file", blob, filename);

    const token = localStorage.getItem('token');
    if (!
      token
    )
      throw new Error('Missing JWT token');

    return this.http.post(
      `${environment.audioUrl}/api/recordings/upload-sentence`,
      formData,
      {
        headers: {
          Authorization: `Bearer ${token}`
        }
      }
    );
  }
  assess(recordingId: number, mode: 'WORD' | 'SENTENCE', blob: Blob) {
    const formData = new FormData();
    formData.append('recordingId', recordingId.toString());
    formData.append('mode', mode);
    formData.append('file', blob, 'learner.webm');

    const token = localStorage.getItem('token');
    return this.http.post(`${environment.audioUrl}/api/recordings/assess`, formData, {
      headers: { Authorization: `Bearer ${token}` }
    });
  }
}





