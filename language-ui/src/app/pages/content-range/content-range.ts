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
  start = 1;
  end = 10;

  items: ContentItem[] = [];
  loading = false;
  errorMessage = '';

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    console.log('NGONCHANGES FIRED', this.numberOfRecordings);

    if (changes['numberOfRecordings'] && this.numberOfRecordings > 0) {
      this.end = this.start + this.numberOfRecordings;
      this.loadRange();
    }
  }

  loadRange(): void {
    this.loading = true;
    this.errorMessage = '';

    const end = this.start + this.numberOfRecordings;

    const params = new HttpParams()
      .set('dialect', this.dialect)
      .set('type', this.itemType)
      .set('start', this.start.toString())
      .set('end', end.toString());

    this.http.get<ContentItem[]>(
      `${environment.contentUrl}/api/content/words`,
      { params }
    ).subscribe({
      next: (data) => {
        this.items = data;
        this.end = end;

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
