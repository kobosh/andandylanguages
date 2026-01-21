import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {Header} from './layout/header/header';

@Component({
  selector: 'app-root',

  ///standalone: true,                      // ⭐ REQUIRED ⭐
  imports: [RouterOutlet,Header],               // router-outlet works now
  templateUrl: './app.html',
  styleUrls: ['./app.css']               // ⭐ FIXED ⭐
})
export class App {
  title = signal('language-ui');
}
