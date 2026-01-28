import { Injectable, NgZone } from '@angular/core';
import { Router } from '@angular/router';

@Injectable({ providedIn: 'root' })
export class IdleService {

  private idleTimeoutMs =  1200000; // 3 seconds
  private timeoutId: any;

  constructor(
    private router: Router,
    private zone: NgZone
  ) {}

  startWatching() {
    this.resetTimer();

    const events = ['mousemove', 'keydown', 'click', 'scroll'];

    events.forEach(event =>
      document.addEventListener(event, () => this.resetTimer())
    );
  }

  private resetTimer() {
    clearTimeout(this.timeoutId);

    this.timeoutId = setTimeout(() => {
      this.zone.run(() => {
        this.handleIdleTimeout();
      });
    }, this.idleTimeoutMs);
  }

  private handleIdleTimeout() {
    alert('Session ended due to inactivity.');
    localStorage.removeItem('token');
    this.router.navigate(['/login']);
  }
}
