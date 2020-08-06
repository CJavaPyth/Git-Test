import { Injectable } from '@angular/core';
import { Leader } from '../menu/shared/leader';
import { LEADERS } from '../menu/shared/leaders';

@Injectable({
  providedIn: 'root'
})
export class LeaderService {

  constructor() { }
  getLeaders(): Promise<Leader[]> {
  	return Promise.resolve(LEADERS);
  }
  getLeader(id: string): Promise<Leader> {
  	return Promise.resolve(LEADERS.filter((leader) => (leader.id===id))[0]);
  }

  getFeaturedLeader(): Promise<Leader>  {
  	return Promise.resolve(LEADERS.filter((leader) => leader.featured)[0]);
}
