package com.urbanshef.urbanshefapp;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.urbanshef.urbanshefapp.objects.Basket;

import java.util.List;

@Dao
public interface BasketDAO {
    @Query("SELECT * FROM basket")
    List<Basket> getAll();

    @Insert
    void insertAll(Basket... baskets);

    @Query("DELETE FROM basket")
    void deleteAll();


    @Delete
    void delete(Basket basket);

    @Query("SELECT * FROM basket WHERE meal_id = :mealId")
    Basket getBasket(String mealId);

    @Query("UPDATE basket SET meal_quantity = meal_quantity + :mealQty WHERE id = :basketId")
    void updateBasket(int basketId, int mealQty);
}
