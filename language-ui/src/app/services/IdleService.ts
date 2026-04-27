import { Injectable, NgZone } from '@angular/core';
@Injectable({ providedIn: 'root' })
export class IdleService {
  private timeoutId: any;

  constructor(private router: Router, private zone: NgZone) {}

  startWatching() {
    // run immediately after login (after token is stored)
    this.resetTimer();

    const events = ['mousemove', 'keydown', 'click', 'scroll'];
    events.forEach(evt => {
      document.addEventListener(evt, this.onActivity, { passive: true });
    });
  }

  // optional: call this on logout / destroy pages
  stopWatching() {
    clearTimeout(this.timeoutId);
    const events = ['mousemove', 'keydown', 'click', 'scroll'];
    events.forEach(evt => document.removeEventListener(evt, this.onActivity));
  }

  private onActivity = () => this.resetTimer();

  private resetTimer() {
    clearTimeout(this.timeoutId);

    const remainingMs = this.getRemainingMsFromToken();

    // If token missing or already expired, end session now
    if (remainingMs <= 0) {
      this.zone.run(() => this.handleIdleTimeout());
      return;
    }

    this.timeoutId = setTimeout(() => {
      this.zone.run(() => this.handleIdleTimeout());
    }, remainingMs);
  }

  private handleIdleTimeout() {
    alert('Session ended. Please log in again.');
    localStorage.removeItem('token');
    localStorage.removeItem('expiresIn');
    this.router.navigate(['/login']);
  }

  private getRemainingMsFromToken(): number {
    const token = localStorage.getItem('token');


    if (!token) return 0;

    try {
      const payloadPart = token.split('.')[1];
      if (!payloadPart) return 0;

      const base64 = payloadPart.replace(/-/g, '+').replace(/_/g, '/');
      const payload = JSON.parse(atob(base64));

      const expMs = payload.exp * 1000; // exp is seconds
      const remaining = expMs - Date.now();

      // small safety buffer so you don't race exact expiration
      return remaining - 2000;
    } catch (e) {
      console.error('Failed to parse JWT', e);
      return 0;
    }
  }
}
import { Router } from '@angular/router';



