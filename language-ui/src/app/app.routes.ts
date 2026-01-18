import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', loadComponent: () =>
      import('./pages/login/login').then(m => m.Login) },
  {
    path: 'register',
    loadComponent: () => import('./pages/register/register').then(m => m.Register)
  },

  { path: 'record', loadComponent: () => import('./pages/record/record').then(m => m.Record) }
];
