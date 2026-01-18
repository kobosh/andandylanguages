import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  ///standalone: true,                      // ⭐ REQUIRED ⭐
  imports: [RouterOutlet],               // router-outlet works now
  templateUrl: './app.html',
  styleUrls: ['./app.css']               // ⭐ FIXED ⭐
})
export class App {
  title = signal('language-ui');
}
