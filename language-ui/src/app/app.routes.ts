import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'listen',
    loadComponent: () =>
      import('./pages/guest-word-list/guest-word-list-component')
        .then(m => m.GuestWordListComponent)
  },
  {
    path: 'learner',
    loadComponent: () =>
      import('./pages/practice-word-list/practice-word-list')
        .then(m => m.PracticeWordListComponent)
  },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', loadComponent: () =>
      import('./pages/login/login').then(m => m.Login) },
  {
    path: 'register',
    loadComponent: () => import('./pages/register/register').then(m => m.Register)
  },
  {
    path: 'demo-record',
    loadComponent: () =>
      import('./pages/demo-record/demo-record').then(m => m.DemoRecordComponent)
  },

  { path: 'record', loadComponent: () =>
      import('./pages/record/record').then(m => m.Record) }
];
