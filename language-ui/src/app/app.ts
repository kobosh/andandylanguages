import { Component, signal ,OnInit} from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {IdleService} from './services/IdleService';
import {Landing} from './pages/landing/landing'
@Component({
  selector: 'app-root',
  imports: [RouterOutlet,Landing],

  templateUrl: './app.html',
  styleUrls: ['./app.css']               // ⭐ FIXED ⭐
})
export class App implements OnInit{

  constructor(private idleService: IdleService) {}
  title = signal('language-ui');
  ngOnInit() {

  }
}
