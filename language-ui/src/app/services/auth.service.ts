// src/app/services/auth.service.ts
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';

@Injectable({ providedIn: 'root' })
export class AuthService {

  constructor(private router: Router) {}

  logout() {
    localStorage.removeItem('token');   // 🔴 key step
    this.router.navigate(['/login']);   // or '/'
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }
}
