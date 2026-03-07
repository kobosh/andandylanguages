import { Component } from '@angular/core';
//import { AuthService } from 'layout/services/auth.service';
import {AuthService} from '../../services/auth.service';
import { NgIf} from '@angular/common';



@Component({
  selector: 'app-header',
  //standalone: true,
  imports: [NgIf],                 // ✅ REQUIRED for *ngIf
  templateUrl: './header.html',
  styleUrls: ['./header.css']
})
export class Header {

  constructor(public auth: AuthService) {}

  logout() {
    this.auth.logout();
  }
}
