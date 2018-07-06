package entity;

import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Item {
	private String itemId;  
	private String name;
	private double rating;
	private String address;
	private Set<String> categories;
	private String imageUrl;
	private String url;
	private double distance;          //需要从response中筛选出的8个数据  存在Item中
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((itemId == null) ? 0 : itemId.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {    //hashCode和equals是为了推荐算法中的比较
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Item other = (Item) obj;
		if (itemId == null) {
			if (other.itemId != null)
				return false;
		} else if (!itemId.equals(other.itemId))
			return false;
		return true;
	}
	public String getItemId() {       //8个数据的getter(用Source中的 generate getter and setter)
		return itemId;
	}
	public String getName() {
		return name;
	}
	public double getRating() {
		return rating;
	}
	public String getAddress() {
		return address;
	}
	public Set<String> getCategories() {
		return categories;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public String getUrl() {
		return url;
	}
	public double getDistance() {
		return distance;
	}
	
	public JSONObject toJSONObject() {    //生成JSON传给前端
		JSONObject obj = new JSONObject();
		try {
			obj.put("item_id", itemId);
			obj.put("name", name);
			obj.put("rating", rating);
			obj.put("address", address);
			obj.put("categories", new JSONArray(categories));
			obj.put("image_url", imageUrl);
			obj.put("url", url);
			obj.put("distance", distance);
		}catch(JSONException e) {
			e.printStackTrace();
		}
		return obj;
	}
	/*inner class 这是为了避免写很多种constructor  否则每几种组合就要写一个constructor
	 * 这里必须是Static的  如果没有这个static ItemBuilder就需要创建instance 才能使用
	 * 但要创建instance又需要最上面的Item 然后Item又需要ItemBuilder来提供支持 就死循环了
	 */
	
	public static class ItemBuilder{  	                              
		private String itemId;  
		private String name;
		private double rating;
		private String address;
		private Set<String> categories;
		private String imageUrl;
		private String url;
		private double distance;
		
		/* 
		 * setter
		 * 这里setter的返回值必须是 ItemBuilder 因为如果返回void  无法级联操作
		 * 就是不能ItemBuilder().setItemId().setName().set....build()
		 */
		public ItemBuilder setItemId(String itemId) {   
		
			this.itemId = itemId;
			return this;
		}
		public ItemBuilder setName(String name) {
			this.name = name;
			return this;
		}
		public ItemBuilder setRating(double rating) {
			this.rating = rating;
			return this;
		}
		public ItemBuilder setAddress(String address) {
			this.address = address;
			return this;
		}
		public ItemBuilder setCategories(Set<String> categories) {
			this.categories = categories;
			return this;
		}
		public ItemBuilder setImageUrl(String imageUrl) {
			this.imageUrl = imageUrl;
			return this;
		}
		public ItemBuilder setUrl(String url) {
			this.url = url;
			return this;
		}
		public ItemBuilder setDistance(double distance) {
			this.distance = distance;
			return this;
		}
		
		public Item build() {       //创建Item这个class
			return new Item(this);   
		}
	}
	
	/*
	 * This is a builder pattern in Java.
	 * 这个Item的constuctor是private的 只能通过static的ItemBuilder来创建这个类 这是为了封装性
	 * 且不能变成public 因为如果是public 那用constructor生成Item这个类时就要把八个都传进去 很麻烦 
	 */

	private Item(ItemBuilder builder) { 
		this.itemId = builder.itemId;
		this.name = builder.name;
		this.rating = builder.rating;
		this.address = builder.address;
		this.categories = builder.categories;
		this.imageUrl = builder.imageUrl;
		this.url = builder.url;
		this.distance = builder.distance;
	}
	
	/*整体步骤是这样的  新建时 Item item = new ItemBuilder().setItemId().setName().set....build();
	 *  就是说先把set到inner class ItemBuilder里 
	 *  然后outer class Item的每一项都等于inner class里的对应项 因为this.itemId = builder.itemId
	 *  然后get和生成JSONObject都是从outer class里生成. 
	 *  
	 *  一个class  要有get set 和constructor. 这里set在inner class Item里
	 *  这样的好处是 如果没有inner class 在生成时就要 
	 *  Item item = new Item(把那八个每一个都写一遍 即使有的是null) 而且更改时也需要所有参数 不灵活
	 *  而现在就可以用inner class ItemBuilder来先设置这八个参数的默认值 当需要修改的时候 再用set重新修改就可以了
	 */
	
	/*
	 * 这种用一个inner class的方法 叫做builder pattern.  builder patter是一种design pattern
	 * builder pattern的出现主要是为了解决java语言中不支持默认参数的问题
	 */

}
