package algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;

public class GeoRecommendation {
	public List<Item> recommendedItems(String userId, double lat, double lon){
		List<Item> recommendedItems = new ArrayList<>();         //创建返回值
		DBConnection conn = DBConnectionFactory.getConnection();  //建立连接
		
	    // Step 1 Get all favorited items
		Set<String> favoriteItemIds = conn.getFavoriteItemIds(userId);  //通过UserId获得已经收藏的Item的ID
		
		
		// Step 2 Get all categories for favorited items, sort by count
		Map<String, Integer> allCategories = new HashMap<>();     //建立一个Map结构来存储种类 同时进行技术 所以用map来实现 因为要一一对应
		for (String itemId : favoriteItemIds) {
			Set<String> categories = conn.getCategories(itemId);   //通过上面获得的ItemId获得categories
			for (String category : categories) {
				if (allCategories.containsKey(category)) {          //判断allCategories这个hashmap里有没有这个category
					allCategories.put(category, allCategories.get(category) + 1);   //如果有 count加1
				}else {
					allCategories.put(category, 1);                 //如果没有 放进去 count设置为1
				}
				//上面这两个if else还可以合并为一个  allCategories.put(category, allCategories.getOrDefault(category, 0) + 1);
				//这个get到了 就返回一个integer 然后count加1  没get到 设置为0
			}
		}
		
		List<Entry<String, Integer>> categoryList = 
				new ArrayList<Entry<String, Integer>>(allCategories.entrySet()); 
		//因为hashmap没法排序 所以我们先提取出一个entry entry就是每一个map里的 key value pair 然后把Entry存入一个List
		Collections.sort(categoryList, new Comparator<Entry<String, Integer>>() {   //Comparator是一个类  进行比较 然后排序
			@Override
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {  
				return Integer.compare(o2.getValue(), o1.getValue());     
			}
		});      //根据count按照降序进行排序   把count大的 放在前面 先访问 count小的放在后面
		
		// Step 3 do search based on category, filter out favorited events, sort by distance
		Set<Item> visitedItems = new HashSet<>();   //存之搜索中已经访问过的项目
		
		for (Entry<String, Integer> category : categoryList) {  //遍历一下已经排序好的项目种类
			List<Item> items = conn.searchItems(lat, lon, category.getKey()); //按照category的count顺序依次到TicketMaster上进行搜索
			List<Item> filteredItems = new ArrayList<>();       //过滤后的items
			for (Item item : items) {  //遍历获得的items
				 if (!favoriteItemIds.contains(item.getItemId()) && !visitedItems.contains(item)) {
					 filteredItems.add(item);     //如果这个item没有被收藏 也没有被访问过 则放入过滤过的items中
				 }
			}
			Collections.sort(filteredItems, new Comparator<Item>(){
				@Override
				public int compare(Item item1, Item item2) {
					return Double.compare(item1.getDistance(), item2.getDistance());
				}
			});   //根据distance按照升序进行排序  把distance小的 就是比较近的放在前面 远的放在后面
			
			visitedItems.addAll(items);                 
			//将已经访问过的items记录一下 为了一下个category的搜索 这是因为一个item可能同时有多个种类 怕for loop搜索下一个种类时又推荐了一遍
			recommendedItems.addAll(filteredItems);     
			//将已经过滤过且排好序的items放入推荐items中  最后推荐的顺序是收藏次数最多的种类和距离最近项目在最前面
		}	
		return recommendedItems;
	}
}
