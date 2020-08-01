import { Injectable } from '@angular/core';
import { Dish } from '../menu/shared/dish';
import { DISHES } from '../menu/shared/dishes';

@Injectable({
  providedIn: 'root'
})
export class DishService {

  constructor() { }

  getDishes(): Dish[] {
  	return DISHES;
  }

  getDish(id: string): Dish {
  	return DISHES.filter((dish) => (dish.id===id))[0];
  }

  getFeaturedDish(): Dish {
  	return DISHES.filter((dish) => dish.featured)[0];
  }
}
