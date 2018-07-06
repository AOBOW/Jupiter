package external;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import entity.Item;
import entity.Item.ItemBuilder;

public class TicketMasterAPI {
	private static final String URL = "https://app.ticketmaster.com/discovery/v2/events.json";
	private static final String DEFAULT_KEYWORD = ""; // no restriction
	private static final String API_KEY = "jfHiGNj3JO4SZF6s2JtkAcr5ZRGa1XoA";
	
//  {
	//    "name": "laioffer",
              //    "id": "12345",
              //    "url": "www.laioffer.com",
	//    ...
	//    "_embedded": {
	//	    "venues": [
	//	        {
	//		        "address": {
	//		           "line1": "101 First St,",
	//		           "line2": "Suite 101",
	//		           "line3": "...",
	//		        },
	//		        "city": {
	//		        	"name": "San Francisco"
	//		        }
	//		        ...
	//	        },
	//	        ...
	//	    ]
	//    }
	//    ...
	//  }   这个function是帮助找到address   address在response里藏得比较深  位置如上面的JSON格式所示
	
	//一个小tip  这些string很容易拼错 而且错了之后不好Debug 所以可以建一个class 然后把所有的String用String static存起来 之后直接调用那个class的对应值
	private String getAddress(JSONObject event) throws JSONException{
		if(!event.isNull("_embedded")) {  //events 找下面的_embedded
			JSONObject embedded = event.getJSONObject("_embedded");
			
			if(!embedded.isNull("venues")) {  //找_embedded下面的venues
				JSONArray venues = embedded.getJSONArray("venues");
				
				for(int i = 0; i < venues.length(); i++) {
					JSONObject venue = venues.getJSONObject(i);
					
					StringBuilder builder = new StringBuilder();
					
					if(!venue.isNull("address")) {    //找venues下面的address 每个address下面有三行 每一行是一个String
						JSONObject address = venue.getJSONObject("address");
						
						if(!address.isNull("line1")) {
							builder.append(address.getString("line1"));
						}
						if(!address.isNull("line2")) {
							builder.append(" ");
							builder.append(address.getString("line2"));
						}
						if(!address.isNull("line3")) {
							builder.append(" ");
							builder.append(address.getString("line3"));
						}
					}
					
					if(!venue.isNull("city")) {    //找venues下面的city 每个city下面有一个叫name的String.
						JSONObject city = venue.getJSONObject("city");
						
						if(!city.isNull("name")) {
							builder.append(" ");
							builder.append(city.getString("name"));
						}
					}
					
					if(!builder.toString().equals("")) {
						return builder.toString(); //取到array中的一项的address和city 就返回来  之所以用for loop是第一项为null
					}
				}
			}
		}
		return "";
	}
	
	//{"images": [{"url":"www.exple.com/my_image.jpg"},.......]}
	private String getImageUrl(JSONObject event) throws JSONException{
		if(!event.isNull("images")) {
			JSONArray images = event.getJSONArray("images");
			
			for(int i = 0; i < images.length(); i++) {
				JSONObject image = images.getJSONObject(i);
				
				if(!image.isNull("url")) {
					String imageUrl = image.getString("url");
					return imageUrl;      //取到array中的一项的imageUrl 就返回了  之所以用for loop是怕第一项为null
				}
			}
		}
		
		return "";
	}
	
	//{"classifications" : [{"segment":{"name":"music"}},......]}
	private Set<String> getCategories(JSONObject event) throws JSONException{
		Set<String> categories = new HashSet<>();
		if(!event.isNull("classifications")) {
			JSONArray classifications = event.getJSONArray("classifications");
			
			for(int i = 0; i < classifications.length(); i++) {
				JSONObject classification = classifications.getJSONObject(i);
				
				if(!classification.isNull("segment")) {
					JSONObject segment = classification.getJSONObject("segment");
					
					if(!segment.isNull("name")) {
						String name = segment.getString("name");
						categories.add(name);
					}
				}
			}
		}		
		return categories; //这个function里是把Array中的所有分类(categories)都取出来了  因为分类比较多元 都下来放到数据库方便以后的本地搜索
	}
	
	//Convert JSONArray to a list of item objects  从所有取得的数据中去取我们所需要的8个数据
	private List<Item> getItemList(JSONArray events) throws JSONException{
		List<Item> itemList = new ArrayList<>();  //一个Item的list  Item是entity下面那个class 用来存着八个数据
		
		for(int i = 0; i < events.length(); i++) {
			JSONObject event = events.getJSONObject(i);
			
			ItemBuilder builder = new ItemBuilder();  //这里不是Item.ItemBuilder 是因为import时Item和ItemBuilder两个都import进去了
			
			if(!event.isNull("name")) {    //event中存在name 就写入builder中
				builder.setName(event.getString("name"));
			}
			if(!event.isNull("id")) {
				builder.setItemId(event.getString("id"));
			}
			if(!event.isNull("url")) {
				builder.setUrl(event.getString("url"));
			}
			if(!event.isNull("rating")) {
				builder.setRating(event.getDouble("rating"));
			}
			if(!event.isNull("distance")) {
				builder.setDistance(event.getDouble("distance"));
			}
			/*
			 * 上面五个可以直接写 是因为这五个直接就在TicketMaster response的events的第一层 且直接就是String或Double格式
			 * 而下面的三个要继续往下几层才能找到 比较长 所以用helper function帮助
			 */
			builder.setCategories(getCategories(event));
			builder.setImageUrl(getImageUrl(event));
			builder.setAddress(getAddress(event));
			
			itemList.add(builder.build());
		}
		
		return itemList;
	}
	
	/*
	 * 整个过程实际上是search发出url 然后得到response 从中取出JSON 然后传给getItemList
	 * getItemList从中提取出我们所需要的8个数据 生成List返回
	 * 然后在SearchItem class 的doGet函数中将所得到的List再转化成JSON 输出
	 * 之所以要这么麻烦的 JSON-->List-->JSON 是因为我们还要将所得的内容存入数据库中
	 * 所以需要将JSON转化为Java的数据类型  从而方便存入数据库中
	 */
	
	
	public List<Item> search(double lat, double lon, String keyword) {  //search不用static  是因为调用了类里的常量  如果用static 要把类里的那些常量也改为static的
		if(keyword == null) { //因为三个传参中  经纬度是必须的 keyword可以没有  所以当keyword没有输入时 赋一个DEFAULT_KEYWORD 即程序可以给一个默认使用的keyword
			keyword = DEFAULT_KEYWORD;
		}
		
		try { // encode一下keyword 防止输入的keyword有冲突字符 无法识别或破坏结果 如输入的keyword中有& 而原有url结构中用&作为分隔符 这种情况就会被破坏
			keyword = java.net.URLEncoder.encode(keyword, "UTF-8"); //encode keyword  不可读 但机器处理方便 用URL encode是因为请求是在url中发过去 所以encode后要让url可读  UTF8 是表示一个字符最少要用8位
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		String geoHash = GeoHash.encodeGeohash(lat, lon, 8); //将lat lon 转化为geoPoint. 这个函数在GeoHash Class 中
		
		String query = String.format("apikey=%s&geoPoint=%s&keyword=%s&radius=%s", API_KEY, geoHash, keyword, 50 ); //生成url中的query部分
		
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(URL + "?" + query).openConnection(); //把整体的URL拼接出来， 括号里的URL是上面的String 包括host和TicketMaster的endpoint 
			int responseCode = connection.getResponseCode(); //openConnection后 用getResponseCode来发送并获得这个请求的状态 返回一个值  这个值其实就是 200（OK） 404（not found) 这些值
			
			System.out.println("\nSending 'GET' request to URL: " + URL + "?" + query);//这两步是为了debug的需要  输出一下结果
			System.out.println("Response Code: " + responseCode);
			
			if(responseCode != 200) {
				throw new Exception("Connection error..."); //当返回的状态值不是200 就是出问题了 这里可以加一些解决办法 但现在先不写了  可以自己写一个异常  抛给前端
			}
			
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream())); //从返回中读取结果 BufferedReader是一次读一行 InputStreamReader是读connection的结果 就是其实信息都在inputstream里 先给InputStramReader 
			                                                                                            //然后再将这些信息用BufferedReader一行一行进行操作 且BufferedReader只是一个handle（相当于指针） 不能直接用 所以接下来要读出并放到一个个String里
			String inputline;
			StringBuilder response = new StringBuilder(); //StringBuilder是将多行结果的String放在一起  只用分配一次内存 直接拼接起来（其实一行一行String也可以  但是麻烦）
			while((inputline = in.readLine())!= null) { //将BufferedReader获得的数据（in）用readline这个method读出来 再放入inputline  再整合到response这个StringBuilder上
				response.append(inputline);
			}
			in.close(); // close BufferedReader
			connection.disconnect(); //close HttpURLConnection
			
			JSONObject obj = new JSONObject(response.toString()); //生产JSON格式
			if(obj.isNull("_embedded")) {   //TicketMaseter的response中所有有用的数据都存在_embedded里 检查是否收到了这个  
				return new ArrayList<>();     //如果不存在返回一个空的JSONArray 代表这个经纬度和keyword下没有event
			}
			
			JSONObject embedded = obj.getJSONObject("_embedded");
			JSONArray events = embedded.getJSONArray("events");   //TicketMaseter的response的格式  _embedded下面是events  这个就是我们想要的结果（一个一个活动）。
			return getItemList(events);  //调用getItemList这个函数来筛选出我们所需要的8个数据
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return new ArrayList<>(); //只有抛出异常throw exception 才会走到这一步
	}
	
	private void queryAPI(double lat, double lon) {   // 这个function是把search到的内容输出出来  在实际的与TicketMaster API 通信时并不会用到， 写出来是为了debug的 检测search获取的结果是否正确
		List<Item> events = search(lat, lon, null);
		try {
			for(int i = 0; i < events.size(); i++) {  //for(Item event: events) 还可以把这行加下一行简写成这这样
				Item event = events.get(i);
				System.out.println(event.toJSONObject()); //这个toJSONObject是Item这个class里的method 将数据生成JSONObject
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * Main entry for sample TicketMaster API requests.
	 */

	public static void main(String[] args) {
		TicketMasterAPI tmApi = new TicketMasterAPI();
		// Mountain View, CA
		// tmApi.queryAPI(37.38, -122.08);
		// London, UK
		// tmApi.queryAPI(51.503364, -0.12);
		// Houston, TX
		tmApi.queryAPI(29.682684, -95.295410);
	}


}
