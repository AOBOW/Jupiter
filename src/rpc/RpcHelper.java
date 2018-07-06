package rpc;

import java.io.BufferedReader;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

public class RpcHelper {
	
	// Writes a JSONObject to http response.
	public static void writeJsonObject(HttpServletResponse response, JSONObject obj) {
		try {
			response.setContentType("application/json");
			response.addHeader("Access-Control-Allow-Origin", "*");
			PrintWriter out = response.getWriter();
			out.print(obj);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

    // Writes a JSONArray to http response.
	public static void writeJsonArray(HttpServletResponse response, JSONArray array) {
		try {
			response.setContentType("application/json");  //告诉浏览器返回类型是json 否则postman需要手工改成json  有了这个  postman直接识别出来的就是json
			response.addHeader("Access-Control-Allow-Origin", "*");  //前端访问后端时 允许前端从哪里来(即哪个web site可以访问这个API)  现在是*（通配符） 就代表谁都可以来 谁都可以访问这个后端  
			PrintWriter out = response.getWriter();
			out.print(array);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//parses a JSONObject to http request 解析JSONObject请求   因为request传过来的是String  将String转换成JSONObject
	public static JSONObject readJsonObject(HttpServletRequest request) {
		StringBuilder builder = new StringBuilder();
		try {
			BufferedReader reader = request.getReader();   //用BufferedReader读request中的数据  每次读一行
			String line = null;
			while((line = reader.readLine()) != null) {
				builder.append(line);    //将BufferedReader中读出的数据一行一行的放入StringBuilder中
			}
			reader.close();     //关闭BufferedReader
			return new JSONObject(builder.toString());  //再将StringBuilder中的数据转化为JSONObject
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
