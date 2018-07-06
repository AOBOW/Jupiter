package rpc;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;

/*
 * Servlet implementation class SearchItem
 */
@WebServlet("/search")   //这里是url输入时mapping到这个endpoint的输入
public class SearchItem extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SearchItem() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONArray array = new JSONArray();
		try {
			String userId = request.getParameter("user_id");
			double lat = Double.parseDouble(request.getParameter("lat"));  //TicketMaster API 中search method的传参是double
			double lon = Double.parseDouble(request.getParameter("lon"));
			String keyword = request.getParameter("term");
			
			DBConnection connection = DBConnectionFactory.getConnection();
			List<Item> items = connection.searchItems(lat, lon, keyword);
			
			Set<String> favorite = connection.getFavoriteItemIds(userId);
			connection.close();   //现在使用DBConnection这个interface来实现（具体是在MySQLConnection这个class)  现在在取数据的同时还可以向数据库中存数据
			
			//TicketMasterAPI tmAPI = new TicketMasterAPI();
			//List<Item> items = tmAPI.search(lat, lon, keyword);  //search函数不是static的 所以要新建一个instance 然后用instance调用

			for(int i = 0; i < items.size(); i++) {
				Item item = items.get(i);
				JSONObject obj = item.toJSONObject();
				
				// Check if this is a favorite one.
				// This field is required by frontend to correctly display favorite items.
				obj.put("favorite", favorite.contains(item.getItemId()));

				array.put(obj);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		RpcHelper.writeJsonArray(response, array);  //使用辅助函数 且这个函数是static的 所以可以直接用泪调用
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
