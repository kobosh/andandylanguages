import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class UploadService {
  constructor(private http: HttpClient) {
  }
  upload(
    word: string | null,
    meaning: string | null,
    blob: Blob,
    filename: string
  ): Observable<Object> {

    const formData = new FormData();
    formData.append('word', word ?? '');
    formData.append('meaning', meaning ?? '');
    formData.append('file', blob, filename);

    const token = localStorage.getItem('token');
    console.log('🔥 TOKEN AT REQUEST TIME (SERVICE):', token);


    return this.http.post(
      'http://localhost:8083/api/recordings/upload',
      formData,
      {
        headers: {
          Authorization: `Bearer ${token}`
        }
      }
    );
  }



 /* upload(
    word: string | null,
    meaning: string | null,
    blob: Blob,
    filename: string
  ): Observable<Object> {

    const token = localStorage.getItem('token');
    if (!token) {
      throw new Error('Not logged in');
    }

    const formData = new FormData();
    formData.append('word', word ?? '');
    formData.append('meaning', meaning ?? '');
    formData.append('file', blob, filename);

    return this.http.post(
      '/api/recordings/upload',
      formData,
      {
        headers: {
          Authorization: `Bearer ${token}`
        }
      }
    );
  }*/
}
