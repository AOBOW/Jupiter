package db.mysql;

//帮助生成URL  连接数据库
public class MySQLDBUtil {
	private static final String HOSTNAME = "localhost";
	private static final String PORT_NUM = "3306"; // change it to your mysql port number
	public static final String DB_NAME = "laiproject";
	private static final String USERNAME = "root";
	private static final String PASSWORD = "root";    //这里的用户名和密码root是访问数据库的默认用户名 密码
	public static final String URL = "jdbc:mysql://"
			+ HOSTNAME + ":" + PORT_NUM + "/" + DB_NAME
			+ "?user=" + USERNAME + "&password=" + PASSWORD
			+ "&autoReconnect=true&serverTimezone=UTC";  
	//jdbc:mysql://localhost:8889/laiproject?user=root&password=root&autoReconnect=true&serverTimezone=UTC"  这是用于注册MySQL driver的信息
}
