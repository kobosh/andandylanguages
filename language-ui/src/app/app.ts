import { Component, signal ,OnInit} from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {Header} from './layout/header/header';
import {IdleService} from './services/IdleService';

@Component({
  selector: 'app-root',

  ///standalone: true,                      // ⭐ REQUIRED ⭐
  imports: [RouterOutlet,Header],               // router-outlet works now
  templateUrl: './app.html',
  styleUrls: ['./app.css']               // ⭐ FIXED ⭐
})
export class App implements OnInit{
  constructor(private idleService: IdleService) {}
  title = signal('language-ui');
  ngOnInit() {
    this.idleService.startWatching();
  }
}
