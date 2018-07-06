package rpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;

/**
 * Servlet implementation class ItemHistory
 */
@WebServlet("/history")  // URL mapping
public class ItemHistory extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ItemHistory() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    //doget是根据用户的用户名来获得用户之前收藏的数据
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String userId = request.getParameter("user_id");  //从url中获得用户的userid
		//doget与dopost获得userid的方式不一样是因为请求的格式不一样  get请求中userID直接在url中  而post请求中userID在body中
		JSONArray array = new JSONArray();
		
		DBConnection conn = DBConnectionFactory.getConnection();    //建立MySQL的连接
		Set<Item> items = conn.getFavoriteItems(userId);            //输入userId获得返回的items  这个函数在MySQLConnection中
		
		for(Item item : items) {
			JSONObject obj = item.toJSONObject();       //将刚刚从数据库中取出的值转化为JSON结构
			
			try {
				obj.append("favorite", true);     //加这个是为了前端的代码   不是后端传来的数据
			}catch(JSONException e) {
				e.printStackTrace();
			}
			
			array.put(obj);  //将转化好的JSONObject 放入JSONArray中
			//向JSONArray中加JSONObject用put  向JSONObject中加key value pair用append
		}
		
		RpcHelper.writeJsonArray(response, array);  // 用helper function 进行返回
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)  
	 */
	//doPost是把favorite提交上来 加入数据库history table中  对应MySQLConnection class中的 setFavoriteItems method
	/*
	 * {
    		user_id = “1111”,
    		favorite = [
        		“abcd”,
        		“efgh”,
    		]
		}
		这是http请求传来的格式  这个格式是我们自己定义   abcd  efgh这些事item_id

	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			JSONObject input = RpcHelper.readJsonObject(request);
			String userId = input.getString("user_id");              //拿到user_id
			
			JSONArray array = input.getJSONArray("favorite");         //拿到item_id
			List<String> itemIds = new ArrayList<>();
			for(int i = 0; i < array.length(); i++) {
				itemIds.add(array.get(i).toString());  //获得array中第i个值  toString然后加到list里
			}
			
			DBConnection conn = DBConnectionFactory.getConnection();
			conn.setFavoriteItems(userId, itemIds);                   //调用MySQLConnection class中的 setFavoriteItems method
			conn.close();
			
			RpcHelper.writeJsonObject(response, new JSONObject().put("result", "SUCCESS"));  //给请求人返回一个状态  就是添加favorite成功
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	//doDelete是把history table中的数据删除  对应MySQLConnection class中的 unsetFavoriteItems method
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			JSONObject input = RpcHelper.readJsonObject(request);
			String userId = input.getString("user_id");              //拿到user_id
			
			JSONArray array = input.getJSONArray("favorite");         //拿到item_id
			List<String> itemIds = new ArrayList<>();
			for(int i = 0; i < array.length(); i++) {
				itemIds.add(array.get(i).toString());  //获得array中第i个值  toString然后加到list里
			}
			
			DBConnection conn = DBConnectionFactory.getConnection();
			conn.unsetFavoriteItems(userId, itemIds);                   //调用MySQLConnection class中的 unsetFavoriteItems method
			conn.close();
			
			RpcHelper.writeJsonObject(response, new JSONObject().put("result", "SUCCESS"));  //给请求人返回一个状态  就是删除favorite成功
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
