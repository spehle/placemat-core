import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { SlicePipe} from '@angular/common';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, SlicePipe],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {

  result = '';

  constructor(private http: HttpClient) {}

  callMe() {
    console.log('callMe clicked');
    this.result = 'Calling /api/auth/me...';
    this.http.get('/api/auth/me', {
      headers: {
        Authorization: `Bearer ${this.token}`
      }
    }).subscribe({
      next: res => this.result = JSON.stringify(res, null, 2),
      error: err => this.result = JSON.stringify(err, null, 2)
    });
  }

  token = '';

  login() {
    console.log('login clicked');
    this.result = 'Logging in...';
    this.http.post<any>('/api/auth/login', {
      username: 'admin',
      password: 'admin'
    }).subscribe({
      next: res => {
        this.token = res.token;
        this.result = 'LOGIN OK\n\n' + JSON.stringify(res, null, 2);
      },
      error: err => {
        this.result = JSON.stringify(err, null, 2);
      }
    });
  }

}
