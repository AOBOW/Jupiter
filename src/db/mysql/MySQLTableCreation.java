package db.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

//这个class是帮助恢复数据库的初始状态  方便debug  这个class是用来reset  并没有和其他class有联系  所以执行在main函数中
public class MySQLTableCreation {
	// Run this as Java application to reset db schema.
	
	public static void main(String[] args) {  //创建一个和数据库的连接
		try {
			// This is java.sql.Connection. Not com.mysql.jdbc.Connection.
			Connection conn = null;  //初始化 Connection是JDBC的一个interface 具体实现是由DriverManager获得的

			// Step 1 Connect to MySQL. 使用MySQLBUtil里创建的URL
			try {
				System.out.println("Connecting to " + MySQLDBUtil.URL);
				/*
				 * Class.forName("com.mysql.jdbc.Driver")这是一个反射 反射是通过运行期间出现的一些数值来创建Class
				 * 这里运行期出现的值就是com.mysql.jdbc.Driver这个String  这里以内mysql的jar包就在lib里 所以是从那里取了  但其实也可以把这个包在运行时传进来 然后运行过程中找这个String.
				 * 这里这句话就是获得一个叫com.mysql.jdbc.Driver这个名字的class 获得driver之后注册到drivermanagement里（注册的过程是在Driver这个class内部实现的）
				 * 下面再用下面的drivermanager把这个Class再load出来  再通过URL建立connection
				 * driver是一套数据库用来支持JDBC这套API所使用的程序
				 */
				Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance(); //反正记住这句话的目的就是创建一个driver然后把这个driver本身注册到drivermanagement里
				conn = DriverManager.getConnection(MySQLDBUtil.URL);  //用DriverManagement通过URL创建和MySQL的连接  获得Connection conn
				//之所以这么复杂是为了支持多种数据库  现在这里是支持mysql（看forname找的那个driver名）有一天我换数据库了 只要把这个数据库的.jar文件load进来 然后换寻找的name就行 因为JDBC是不变的  所有数据库都要基于JDBC
			} catch (SQLException e) {
				e.printStackTrace();
			}

			if (conn == null) {   //没建立成功  直接return结束
				return;      
			}
			
			//step 2 Drop tables in case they exist  和数据库连接上之后清空里面有的数据
			Statement stmt = conn.createStatement();
			String sql = "DROP TABLE IF EXISTS categories";
			stmt.executeUpdate(sql);  //executeUpdate的返回值是int  表示这个table里有多少数据被更新了
			sql = "DROP TABLE IF EXISTS history";
			stmt.executeUpdate(sql);
			sql = "DROP TABLE IF EXISTS items";
			stmt.executeUpdate(sql);
			sql = "DROP TABLE IF EXISTS users";
			stmt.executeUpdate(sql);
			//categories history items users是我们这个project数据库的4个table
			
	
			//steps 2 Create new tables 清空之后创建新的表
			sql = "CREATE TABLE items ("
					+ "item_id VARCHAR(255) NOT NULL,"  //item_id是primary key 不能重复 不能为NULL 是每一个entity的标志
					+ "name VARCHAR(255),"                 //VARCHAR是变长数组 大小可变 最大255  类型是String的都用这个
					+ "rating FLOAT,"
					+ "address VARCHAR(255),"    //对这种String debug时一定要注意 问题可能出在,的上面  用了汉语的,
					+ "image_url VARCHAR(255),"
					+ "url VARCHAR(255),"
					+ "distance FLOAT,"       //Item里的数据  除了categories  这个单独一个table
					+ "PRIMARY KEY (item_id))";  //将item_id创建为primary key
			stmt.executeUpdate(sql);
			sql = "CREATE TABLE categories ("
					+ "item_id VARCHAR(255) NOT NULL,"
					+ "category VARCHAR(255) NOT NULL,"
					+ "PRIMARY KEY (item_id, category)," //categories这个table中primary key是 item_id与category的组合  即不能出现二者同时一样的entity 但这两个分别可以重复
					+ "FOREIGN KEY (item_id) REFERENCES items(item_id))";   //foreign key是指从其他table写入的参数  categories这个table中item_id是从items table写入的 即只有items表里有这个值才能出现在categories表里
			stmt.executeUpdate(sql);
			//这里之所以把categories单独拿出来做个table是因为categories可能有多个值 写在一个表里很麻烦 在另一个表里用这种多对多的关系  取出时就方便的多
			sql = "CREATE TABLE users ("
					+ "user_id VARCHAR(255) NOT NULL,"
					+ "password VARCHAR(255) NOT NULL,"  //password 不能为空
					+ "first_name VARCHAR(255),"
					+ "last_name VARCHAR(255),"
					+ "PRIMARY KEY (user_id))";     //user id 是primary key 不能重复 不能为空
			stmt.executeUpdate(sql);
			sql= "CREATE TABLE history ("
					+ "user_id VARCHAR(255) NOT NULL,"
					+ "item_id VARCHAR(255) NOT NULL,"
					+ "last_favor_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"  //这是访问的时间  这里的default默认值是MySQL支持的一个数据库的扩展 因为这里不能为NULL  有一个默认值如果没有写时间这一项 会自动按插入时的时间填上
					+ "PRIMARY KEY (user_id, item_id),"  //连个参数是联合primary key
					+ "FOREIGN KEY (item_id) REFERENCES items(item_id),"
					+ "FOREIGN KEY (user_id) REFERENCES users(user_id))";
			stmt.executeUpdate(sql);
			/*
			 * 除了 primary key foreign key 还有一个种是unique key  代表值可以为null但不能重复 
			 * primary key 是值不能为null 不能重复  
			 * foreign key是从别的table导入的 一般是别的table的primary key 即不能为null 不能重复
			 */
			
			//step 4 insert data
			//create a fake user
			
			sql = "INSERT INTO users VALUES ("
					+ "'1111', '3229c1097c00d497a0fd282d586be050', 'Aobo', 'Wang')";  //这个密码是个MD5值  我们不在数据库的密码里存明文  存哈希值 避免数据库泄露  哈希值很难逆向回密码  用户登录时  将输入的密码取哈希值 与存的值对比
			System.out.println("Executing query: " + sql);  //输出一下 作为debug
			stmt.executeUpdate(sql);
			
			sql = "INSERT INTO users VALUES ("
					+ "'1234', '3229c1097c00d497a0fd282d586be050', 'Aobo', 'Wang')";  //这个密码是个MD5值  我们不在数据库的密码里存明文  存哈希值 避免数据库泄露  哈希值很难逆向回密码  用户登录时  将输入的密码取哈希值 与存的值对比
			System.out.println("Executing query: " + sql);  //输出一下 作为debug
			stmt.executeUpdate(sql);
			

			System.out.println("Import is done successfully.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}