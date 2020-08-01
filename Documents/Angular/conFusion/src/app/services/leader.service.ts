import { Injectable } from '@angular/core';
import { Leader } from '../menu/shared/leader';
import { LEADERS } from '../menu/shared/leaders';

@Injectable({
  providedIn: 'root'
})
export class LeaderService {

  constructor() { }
  getLeaders(): Leader[] {
  	return LEADERS;
  }
  getLeader(id: string): Leader {
  	return LEADERS.filter((leader) => (leader.id===id))[0];
  }

  getFeaturedLeader(): Leader {
  	return LEADERS.filter((leader) => leader.featured)[0];
}
