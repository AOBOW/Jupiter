package db;

import db.mysql.MySQLConnection;

/*
 * 具体实现创建DBConnection的工厂
 */
public class DBConnectionFactory {
	// This should change based on the pipeline.
	private static final String DEFAULT_DB = "mysql";  //默认后台用的是mysql
	
	public static DBConnection getConnection() {   //无输入时调用默认选项 用MYSQL
		return getConnection(DEFAULT_DB);
	}
	
	/*
	 * 具体实现的接口是在DBConnection  
	 * 然后用这个getConnection来选择用哪个数据库来实现接口
	 * 用户factory实现的好处就是可以再切换数据集时方便
	 */
	public static DBConnection getConnection(String db) { //返回的是DBConnection那个类的object
		switch(db) {
		case "mysql":
			return new MySQLConnection();
		case "mongodb":
			//return new MongoDBConnection; //如果是MONGODB 就调用MONGODB的连接
			return null;
		default:
			throw new IllegalArgumentException("Invalid db: "+ db ); //两个都不是 抛出异常
		}
	}
}
