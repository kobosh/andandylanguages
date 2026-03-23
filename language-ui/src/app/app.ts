import { Component, signal ,OnInit} from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {IdleService} from './services/IdleService';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],

  templateUrl: './app.html',
  styleUrls: ['./app.css']               // ⭐ FIXED ⭐
})
export class App implements OnInit{

  constructor(private idleService: IdleService) {}
  title = signal('language-ui');
  ngOnInit() {

  }
}
