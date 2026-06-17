import { CommonModule } from '@angular/common';
import {
  Component,
  EventEmitter,
  Output,
  Input,
  OnChanges,
  SimpleChanges
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule, HttpParams } from '@angular/common/http';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';

interface ContentItem {
  id: number;
  englishWord: string;
  englishSentence: string;
}

@Component({
  selector: 'app-content-range',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './content-range.html',
  styleUrl: './content-range.css'
})
export class ContentRangeComponent implements OnChanges {

  @Output() contentLoaded = new EventEmitter<ContentItem[]>();
  @Input() numberOfRecordings = 0;

  dialect = 'MAHASSI';
  itemType = 'WORD';
  start =0;
  end = 10;
  recordingsCount=0;
  items: ContentItem[] = [];
  loading = false;
  errorMessage = '';

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router
  ) {}

 /* ngOnChanges(changes: SimpleChanges): void {
    console.log('NGONCHANGES FIRED', this.numberOfRecordings);

    if (changes['numberOfRecordings'] && this.numberOfRecordings > 0) {

      this.getNumberOfContributorRecordings(); //make this wait here until function finishes
      console.log("this.start ",this.start);
      this.end = this.start + this.numberOfRecordings;
      console.log("END ",this.end);

    }
  }*/
ngOnChanges(changes: SimpleChanges): void {
  console.log("onchanges this.start ",this.start," end ",this.end);
  if (changes['numberOfRecordings'] && this.numberOfRecordings > 0) {
    this.loadNextRange();
  }
}
 /*getNumberOfContributorRecordings() {
   const token = localStorage.getItem('token');

   if (!token) {
     return;
   }

   const payload = JSON.parse(atob(token.split('.')[1]));
   const contributorId = Number(payload.sub);

   this.http.get<number>(
     `${environment.contentUrl}/api/content/numberofrecordings`,
     {
       params: {
         contributorId: contributorId
       }
     }
   ).subscribe({
     next: (count: number) => {
       this.start = count+1;
       console.log('START = ', this.start);

     },
     error: (err: any) => {
       console.error('Failed to get number of recordings', err);
     }
   });

 }*/
 loadNextRange(): void {
   const token = localStorage.getItem('token');

   if (!token) {
     this.errorMessage = 'No token found';
     return;
   }

   const payload = JSON.parse(atob(token.split('.')[1]));
   const contributorId = Number(payload.sub);

   this.http.get<number>(
     `${environment.contentUrl}/api/content/numberofrecordings`,
     {
       params: {
         contributorId: contributorId
       }
     }
   ).subscribe({
     next: (recordingsAlreadyDone: number) => {
       this.start = recordingsAlreadyDone/2 + 1;
       this.end = this.start + this.numberOfRecordings - 1;

       console.log('Already done = ', recordingsAlreadyDone);
       console.log('User wants = ', this.numberOfRecordings);
       console.log('START = ', this.start);
       console.log('END = ', this.end);

       this.loadRange();
     },
     error: (err: any) => {
       console.error('Failed to get contributor progress', err);
       this.errorMessage = 'Failed to get contributor progress';
     }
   });
 }
  loadRange(): void {
    this.loading = true;
    this.errorMessage = '';

    //const end = this.start + this.numberOfRecordings;
    console.log('load range START = ', this.start);
           console.log('load range END = ', this.end);
    const params = new HttpParams()

      .set('start', this.start.toString())
      .set('end', this.end.toString());

    this.http.get<ContentItem[]>(
      `${environment.contentUrl}/api/content/words/range`,
      { params }
    ).subscribe({
      next: (data) => {
        this.items = data;
       // this.end = end;

        localStorage.setItem('contentItems', JSON.stringify(data));

        console.log('contentRange ', this.items);

        this.contentLoaded.emit(data);
        console.log('emitted contentLoaded');

        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.errorMessage = 'Failed to load content range.';
        this.loading = false;
      }
    });
  }
}
