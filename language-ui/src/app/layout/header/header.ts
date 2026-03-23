import { Component } from '@angular/core';

import {AuthService} from '../../services/auth.service';



@Component({
  selector: 'app-header',

  templateUrl: './header.html',
  styleUrls: ['./header.css']
})
export class Header {

  constructor(public auth: AuthService) {}

  logout() {
    this.auth.logout();
  }
}
