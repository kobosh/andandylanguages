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
    //console.log('IdleService TOKEN:', token);

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
/*@Injectable({ providedIn: 'root' })
export class IdleService {
  private timeoutId: any;

  constructor(private router: Router, private zone: NgZone) {}

  startWatching() {
    this.resetTimer();

    const events = ['mousemove', 'keydown', 'click', 'scroll', 'touchstart', 'pointerdown'];
    events.forEach(event =>
      document.addEventListener(event, () => this.resetTimer(), true)
    );
  }

  private parseJwt(token: string): any {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    return JSON.parse(atob(base64));
  }

  private getRemainingTokenTime(): number {
    const token = localStorage.getItem('token');
    if (!token) return 0;

    const payload = this.parseJwt(token);
    const expMs = payload.exp * 1000;
    const remaining = expMs - Date.now();

    console.log(`time remaining: ${remaining} ms`);
    return remaining;
  }

  private resetTimer() {
    clearTimeout(this.timeoutId);

    const remaining = this.getRemainingTokenTime();

    if (remaining <= 0) {
      this.zone.run(() => this.handleIdleTimeout());
      return;
    }

    this.timeoutId = setTimeout(() => {
      this.zone.run(() => this.handleIdleTimeout());
    }, remaining);
  }

  private handleIdleTimeout() {
    alert('Session ended (token expired). Please log in again.');
    localStorage.removeItem('token');
    this.router.navigate(['/login']);
  }
}*/

/*@Injectable({ providedIn: 'root' })
export class IdleService {
  private idleTimeoutMs: number=0;



  constructor(
    private router: Router,
    private zone: NgZone
  ) {}
  //private idleTimeoutMs =this.getRemainingMsFromToken();//  600000; // 3 seconds
  private timeoutId: any;
  startWatching() {
    this.resetTimer();

    const events = ['mousemove', 'keydown', 'click', 'scroll'];

    events.forEach(event =>
      document.addEventListener(event, () => this.resetTimer())
    );
  }

  private resetTimer() {
    clearTimeout(this.timeoutId);
  this.idleTimeoutMs =this.getRemainingMsFromToken();
    this.timeoutId = setTimeout(() => {
      this.zone.run(() => {
        this.handleIdleTimeout();
      });
    }, this.idleTimeoutMs);
  }

  private handleIdleTimeout() {
    alert(Date.now()+ 'Session ended due to inactivity. log in again if you want to record.' +
      'If you want to practice recording do not log');
    localStorage.removeItem('token');
    this.router.navigate(['/login']);
  }
  private getRemainingMsFromToken(): number {
    const token = localStorage.getItem('token');
    console.log("TOKEN !!!",token);

    if (!token) {   console.log("TOKEN   is NULL!!!");
      return 3000;   }//hard coded because token is null

    const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    const payload = JSON.parse(atob(base64));

    const expMs = payload.exp * 1000;
    return expMs - Date.now();
  }
}*/
