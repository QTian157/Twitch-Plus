package com.laioffer.jupiter.entity.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.laioffer.jupiter.entity.db.Item;

/**
 * 前段发给后端的东西--> 写数据
 * Create FavoriteRequestBody under entity.request package to represents the payload that frontend sends to server
 * */
public class FavoriteRequestBody {
    @JsonProperty("favorite")
    private Item favoriteItem;

    public Item getFavoriteItem(){
        return favoriteItem;
    }

}
