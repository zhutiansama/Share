package com.atguigu.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atguigu.utils.VerifyCodeConfig;

import redis.clients.jedis.Jedis;

//验证手机验证码
public class CodeVerifyServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public CodeVerifyServlet() {

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// 获取传入的手机号和验证码
		// 接受电话号
		String phone_no = request.getParameter("phone_no");
		String code_send = request.getParameter("verify_code");

		if ("".equals(phone_no) || phone_no == null || "".equals(code_send) || code_send == null) {

			System.out.println("数据非法！");

			return;
		}

		// 获取Jedis，查询验证码
		Jedis jedis = new Jedis(VerifyCodeConfig.HOST, VerifyCodeConfig.PORT);

		// 生成Key
		String phone_key = VerifyCodeConfig.PHONE_PREFIX + phone_no + VerifyCodeConfig.PHONE_SUFFIX;

		String code_query = jedis.get(phone_key);

		if (code_send.equals(code_query)) {

			System.out.println("验证成功！");

			jedis.close();

			response.getWriter().print(true);
		} else {

			System.out.println("验证失败！");

			jedis.close();

			response.getWriter().print(false);
		}

	}

}
