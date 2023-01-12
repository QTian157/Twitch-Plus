package com.laioffer.jupiter.dao;

import com.laioffer.jupiter.entity.db.Item;
import com.laioffer.jupiter.entity.db.ItemType;
import com.laioffer.jupiter.entity.db.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
/**
 * 与DB的交互
 * 1. insert
 * 2. remover
 * 3. get(查询)
 *
 * */
@Repository
public class FavoriteDao {

    @Autowired
    private SessionFactory sessionFactory;

    // 1. Insert a favorite record to the database
    // 1.1: insert to user table
    // 1.2: insert to favorite record table
    public void setFavoriteItem(String userId, Item item) {
        Session session = null; //  创建session object --> 代表DB的一次操作

        try {
            session = sessionFactory.openSession();
            // Select * FROM users WHERE user_id = "123"
            User user = session.get(User.class, userId);
            // 写操作: add
            user.getItemSet().add(item);// 如果save user出错，就rollback()
            // 把数据放到db: INSERT xxx into favorite_record
            // Hibernate 帮我们具体操作
            session.beginTransaction();
            session.save(user);
            session.getTransaction().commit();

        }catch (Exception ex) {
            ex.printStackTrace();
            session.getTransaction().rollback();//  例子：银行付款不成 要把货品退回仓库
            // 如果save user出错，就rollback()

        }finally{
            if (session != null) session.close();
            //  如果不close，db上会有active connection
        }
    }
    // Remove a favorite record from the database
    public void unsetFavoriteItem(String userId, String itemId) {
        Session session = null;

        try {
            session = sessionFactory.openSession();
            // Select * FROM users WHERE user_id = "123"
            User user = session.get(User.class, userId);
            Item item = session.get(Item.class, itemId);
            // 写操作: remove
            user.getItemSet().remove(item);
            // 把数据放到db: INSERT xxx into favorite_record
            // Hibernite 帮我们具体操作
            session.beginTransaction();
            session.update(user);
            session.getTransaction().commit();

        }catch (Exception ex) {
            ex.printStackTrace();
            session.getTransaction().rollback();//  例子：银行付款不成 要把货品退回仓库
            // rollback to beginTransaction
            // 如果要存100个数据， 第50个出错了 就需要rollback

        }finally{
            if (session != null) session.close();
            //  如果不close，db上会有active connection
        }
    }

    public Set<Item> getFavoriteItems(String userId) {
        Session session = null;
        try{
            session = sessionFactory.openSession();
            return session.get(User.class, userId).getItemSet();

        }catch(Exception ex) {
            ex.printStackTrace();
        }finally {
            if (session != null) session.close();
        }
        return new HashSet<>();
    }


    // Get favorite item ids for the given user
//    public Set<String> getFavoriteItemIds(String userId) {
//        Set<String> itemIds = new HashSet<>();
//
//        try (Session session = sessionFactory.openSession()) {
//            Set<Item> items = session.get(User.class, userId).getItemSet();
//            // go to entity-db-User
//            for(Item item : items) {
//                itemIds.add(item.getId());
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return itemIds;
//    }

    // Get favorite items for the given user.
    // The returned map includes three entries like
    // {"Video": [item1, item2, item3], "Stream": [item4, item5, item6], "Clip": [item7, item8, ...]}

//    public Map<String, List<String>> getFavoriteGameIds(Set<String> favoriteItemIds) {
//        Map<String, List<String>> itemMap = new HashMap<>();
//        for (ItemType type : ItemType.values()) {
//            itemMap.put(type.toString(), new ArrayList<>());
//            // create 3 lists, their types are video,stream, clip
//        }
//
//        try (Session session = sessionFactory.openSession()) {
//            for(String itemId : favoriteItemIds) {
//                Item item = session.get(Item.class, itemId);
//                itemMap.get(item.getType().toString()).add(item.getGameId());
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return itemMap;
//    }

//    public Set<String> getFavoriteItemIds(String userId) {
//        Set<Item> items = getFavoriteItems(userId);
//
//        Set<String> ids = new HashSet<>();
//
//    }

    public Map<String, List<String>> getFavoriteGameIds(Set<Item> items) {
        Map<String, List<String>> itemMap = new HashMap<>();
        for (ItemType type : ItemType.values()) {
            itemMap.put(type.toString(), new ArrayList<>());
        }
        for (Item item : items) {
            itemMap.get(item.getType().toString()).add(item.getGameId());
        }
        return itemMap;
    }

}



