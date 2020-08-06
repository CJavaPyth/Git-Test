import { Injectable } from '@angular/core';
import { Leader } from '../menu/shared/leader';
import { LEADERS } from '../menu/shared/leaders';

@Injectable({
  providedIn: 'root'
})
export class LeaderService {

  constructor() { }
  getLeaders(): Promise<Leader[]> {
  	return new Promise(resolve => {
  		// simulate server latency with 2 second delay
  		setTimeout(() => resolve(LEADERS), 2000);
  	});
  }
  getLeader(id: string): Promise<Leader> {
  	return new Promise(resolve => {
  		// simulate server latency with 2 second delay
  		setTimeout(() => resolve(LEADERS.filter((leader) => (leader.id===id))[0]), 2000);
  	});
  }

  getFeaturedLeader(): Promise<Leader>  {
  	return new Promise(resolve => {
  		// simulate server latency with 2 second delay
  		setTimeout(() => resolve(LEADERS.filter((leader) => leader.featured)[0]), 2000);
  		});
	}
}
