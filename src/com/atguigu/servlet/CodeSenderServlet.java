package com.atguigu.servlet;

import java.io.IOException;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atguigu.utils.VerifyCodeConfig;

import redis.clients.jedis.Jedis;


/*
 * 	①输入手机号，点击发送后随机生成6位数字码，2分钟有效
 * 		 前台页面提供的数据：  phone_no : 电话号
 *      后台：
 *      		key:  phone_no
 *      		value :  String类型
 *      			setex(key,value,120)
 * 					生成之后，响应true即可！
 * 
	②输入验证码，点击验证，返回成功或失败
	
	③每个手机号每天只能输入3次
			
			额外的变量记录用户每天生成验证码的次数！
				key: phone_no
				value: string类型
				   setex(key,value,一天)
				   incr(key);
				   
			判断次数的逻辑，位于生成验证码之前，如果已经超过次数就不用生成了！
			
	核心：  根据业务需要，使用合适的value类型，调用相应的api!
				   
	

 */
public class CodeSenderServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
   
    public CodeSenderServlet() {
        
    }

    
    
	@SuppressWarnings("resource")
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// 接受电话号
		String phone_no = request.getParameter("phone_no");
		
		//验证数据合法性
		if ("".equals(phone_no) || phone_no==null) {
			
			System.out.println("数据非法！");
			
			return;
		}
		
		Jedis jedis = new Jedis(VerifyCodeConfig.HOST, VerifyCodeConfig.PORT);
		
		// 生成计数的key
		String count_key=phone_no+VerifyCodeConfig.COUNT_SUFFIX;
		
		String count_value = jedis.get(count_key);
		
		// 对查出的计数进行判断  ①null（第一次发送请求）  ② >=3 超过次数  ③1< xx <3 可以生成
		if (count_value==null) {
			
			jedis.setex(count_key, VerifyCodeConfig.SECONDS_PER_DAY, "1");
			
		}else {
			
			int count = Integer.parseInt(count_value);
			
			if (count >= 3) {
				
				System.out.println("当前用户："+phone_no+"今日已经超过3次！请明天再来！");
				
				jedis.close();
				
				response.getWriter().print("limit");
				
				return;
			}else {
				
				jedis.incr(count_key);
				
			}
			
		}
		
		
		//=========================生成验证码=============================
		
		// 生成Key
		String phone_key=VerifyCodeConfig.PHONE_PREFIX+phone_no+VerifyCodeConfig.PHONE_SUFFIX;
		
		// 生成value
		String code = genCode(VerifyCodeConfig.CODE_LEN);
		
		jedis.setex(phone_key, VerifyCodeConfig.CODE_TIMEOUT, code);
		
		// 默认发送短信给用户
		System.out.println("尊敬的："+phone_no+"用户，您好，您的6位验证码是： "+code+"请妥善保管，不要告诉任何人！" );
		
		jedis.close();
		
		//  不要使用println()
		response.getWriter().print(true);
		
	} 
	
	
	//生成6位验证码
	private  String genCode(int len){
		 String code="";
		 for (int i = 0; i < len; i++) {
		     int rand=  new Random().nextInt(10);
		     code+=rand;
		 }
		 
		return code;
	}
	
	
 
}
