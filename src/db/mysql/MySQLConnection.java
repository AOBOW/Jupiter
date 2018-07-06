package db.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import db.DBConnection;
import entity.Item;
import entity.Item.ItemBuilder;
import external.TicketMasterAPI;

public class MySQLConnection implements DBConnection {
	private Connection conn;
	
	public MySQLConnection() {  //constructor  创建时自动连接数据库
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();  //newInstance是为了解决有些某些java版本的bug 没有newinstance不会调用反射生成class的初始化
			conn = DriverManager.getConnection(MySQLDBUtil.URL);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	/*Override 是因为这里的方法都是用的DBConnection这个interface的方法
	 * 所以override一方面是文档的作用 标记这些method是重载的 一方面是检查的作用 检查是否与DBConnection中的方法名字 返回类型一致
	 */
	@Override 
	public void close() {    //手动关闭与MySQL的连接
		if(conn != null) {
			try {
				conn.close();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void setFavoriteItems(String userId, List<String> itemIds) {   //向history table中加入数据
		if(conn == null) {
			return;
		}
		
		try {
			String sql = "INSERT IGNORE history (user_id, item_id) VALUES(?,?)"; 
			//因为之前在创建history table时有自动插入时间  所以这里只输入两个就行 
			//但实际上这个table里有三个值 所以这里还要写出来要set的项目是什么(ueser_id, item_id)
			PreparedStatement stmt = conn.prepareStatement(sql);
			for(String itemId : itemIds) {   //输入的itemIds是一个list
				stmt.setString(1, userId);
				stmt.setString(2, itemId);
				stmt.execute();
			}
		}catch(SQLException e){
			e.printStackTrace();
		}

	}

	@Override
	public void unsetFavoriteItems(String userId, List<String> itemIds) {   //删除history中的项
		if(conn == null) {
			return;
		}
		
		try {
			String sql = "DELETE FROM history WHERE user_id = ? AND item_id = ?"; 
			PreparedStatement stmt = conn.prepareStatement(sql);
			for(String itemId : itemIds) {   
				stmt.setString(1, userId);
				stmt.setString(2, itemId);   //这里的set是set到上面sql这个语句的？处
				stmt.execute();
			}
		}catch(SQLException e){
			e.printStackTrace();
		}

	}

	@Override
	public Set<String> getFavoriteItemIds(String userId) {  //查history表通过userId获得itemId
		if(conn == null) {
			return new HashSet<>();
		}
		
		Set<String> favoriteItemIds = new HashSet<>();
		
		try {
			String sql = "SELECT item_id FROM history WHERE user_id = ?";  //用userid在history这个table中找itemid
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, userId);            //在？处放上userid
			ResultSet rs = stmt.executeQuery();   //在数据库中执行并返回一个resultSet
			while(rs.next()) {
				String itemId = rs.getString("item_id");
				favoriteItemIds.add(itemId);               //讲取出的值放入favoriteItemIds用来输出
			}		
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return favoriteItemIds;
	}

	@Override
	public Set<Item> getFavoriteItems(String userId) {   //根据上一个函数的itemid 查items table 获得的items
		if(conn == null) {
			return new HashSet<>();
		}
		
		Set<Item> favoriteItems = new HashSet<>();
		Set<String> itemIds = getFavoriteItemIds(userId);    //调用上一个函数获得itemid
		
		try {
			String sql = "SELECT * FROM items WHERE item_id = ?";  
			//这行sql语句是从items table中按照某一项（item_id）找到对应的item  其中的*代表提取这一项中所有column的数据
			PreparedStatement stmt = conn.prepareStatement(sql); //?和preparedStatement看saveItem这个method中的解释
			for(String itemId : itemIds) {
				stmt.setString(1, itemId);
				
				ResultSet rs = stmt.executeQuery();  
				//这里之所以要用resultset和executequery是因为select这个sql语句的返回值与create delete insert这些不同  
				//返回的是一个resultset类型的数据  这种类型要用Executequery获得
				
				ItemBuilder builder = new Item.ItemBuilder(); //将获得的数据放入Item这个Class中
				while (rs.next()) {   //用next检查rs下面是否还有值 
					builder.setItemId(rs.getString("item_id")); //将rs中获得的item_id放入Item中
					builder.setName(rs.getString("name"));
					builder.setAddress(rs.getString("address"));
					builder.setImageUrl(rs.getString("image_url"));
					builder.setUrl(rs.getString("url"));
					builder.setDistance(rs.getDouble("distance"));
					builder.setRating(rs.getDouble("rating"));
					builder.setCategories(getCategories(itemId)); //categories不在items table中 所以单独用getCategories这个函数来调用
					
					favoriteItems.add(builder.build());  //将存好的items放入favoriteItems中 作为输出
				}
			}
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return favoriteItems;
	}

	@Override
	public Set<String> getCategories(String itemId) {
		if(conn == null) {
			return new HashSet<>();
		}
		
		Set<String> categories = new HashSet<>();
		
		try {
			String sql = "SELECT category FROM categories WHERE item_id = ?";   //sql语句
			PreparedStatement stmt = conn.prepareStatement(sql);        
			stmt.setString(1, itemId);                                   //讲输入值itemId放入sql语句的？处
			ResultSet rs = stmt.executeQuery();                         //在数据库中执行并放回ResultSet格式的结果
			while(rs.next()) {
				String category = rs.getString("category");              //从返回值中提取出category的值
				categories.add(category);                                //加入新建的categories中并返回
			}
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return categories;
	}

	@Override
	public List<Item> searchItems(double lat, double lon, String term) {
		TicketMasterAPI tmAPI = new TicketMasterAPI();
		List<Item> items = tmAPI.search(lat, lon, term);  //用TickerMasterAPI class的search函数后的数据 然后存到数据库中
		for(Item item : items) {
			saveItem(item);
		}
		return items;
	}

	@Override
	public void saveItem(Item item) {  //将item作为参数传进来  然后save到数据库里
		if(conn == null) {
			return;
		}
		try {
			String sql = "INSERT IGNORE INTO items VALUES(?,?,?,?,?,?,?)"; 
			//IGNORE是为了检测是否有重复 如果数据库里已经有了  就不执行下面的sql语句(防止primary key重复）
			//这里之所以用preparestatement语句（前面用问好占位这么写）是为了防止sql injection攻击  另一种写法是mysqltablecreation中最后插入部分的写法 都写进括号里
			//现在setDouble setString等语句在输入时如果出现sql语句 0R "" = 等等时  就会抛出异常  不会执行  防止sql injection
			//这里preparestatement的另一个好处就是防止输入很多时（如传入的是list<Item>时） 重复创建很多  很慢  而现在只需要建立一次  然后分别执行语句就行  速度更快
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, item.getItemId()); //第一个参数是位置 第二个参数是所要set的值
			stmt.setString(2, item.getName());
			stmt.setDouble(3, item.getRating());
			stmt.setString(4, item.getAddress()); 
			stmt.setString(5, item.getImageUrl());
			stmt.setString(6, item.getUrl());
			stmt.setDouble(7, item.getDistance());
			stmt.execute();  
			/*
			 * 这里有三种execute   execute  executeupdate  executequery 这三种的区别主要在返回值 具体操作时根据返回值来做选择
			 * execute值返回一个boolean  就只显示操作成功没有
			 * executeUpdate返回值是int  显示的是更新成功了多少个   这里只更新一条  所以只需要excute
			 * executequery返回值是ResultSet  获得数据库返回的数据
			 * 而MySQLTableCreation中是输入sql  每次更新很多  所以用executeUpdate
			 */
			
			sql = "INSERT IGNORE INTO categories VALUES(?,?)";
			stmt = conn.prepareStatement(sql);                //将category放入数据库中categories的table中
			for(String category : item.getCategories()) {        
				stmt.setString(1, item.getItemId());          //categories的table有两个变量  id和种类 这是关系表
				stmt.setString(2, category);
				stmt.execute();
			}
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getFullname(String userId) {
		if (conn == null) {
			return null;
		}
		String name = "";
		try {
			String sql = "SELECT first_name, last_name from users WHERE user_id = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				name = String.join(" ", rs.getString("first_name"), rs.getString("last_name"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return name;
	}

	@Override
	public boolean verifyLogin(String userId, String password) {
		if (conn == null) {
			return false;
		}
		try {
			String sql = "SELECT user_id from users WHERE user_id = ? and password = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			statement.setString(2, password);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

}
