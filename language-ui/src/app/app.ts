import { Component } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule],
  templateUrl: './app.html'
})
export class App {

  constructor(private router: Router) {}

  isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }

  logout(): void {
   // localStorage.clear();
    this.router.navigate(['']);
  }
}
/*import { Component, signal ,OnInit} from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {IdleService} from './services/IdleService';
//import {Landing} from './pages/landing/landing'
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
}*/
