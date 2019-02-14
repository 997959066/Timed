package cn.xiaoyu.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

	public class SessionInterceptor extends HandlerInterceptorAdapter {
		private static String[] IGNORE_URI = {"/login.jsp", "/login/","/login","/loginIndex", "/error"};
		private static Logger log = LoggerFactory.getLogger(SessionInterceptor.class);  
		
		@Override
	    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
			boolean flag = false;
	     
			String url = request.getRequestURL().toString();
	        
			for (String s : IGNORE_URI) {
	            if (url.contains(s)) {
	                flag = true;
	                break;
	            }
	        }
	        if (!flag) {
	        	Object obj = request.getSession().getAttribute("user");  
	            if (null == obj) {//未登录
	            	String servletPath = request.getServletPath();
	            	log.error("session失效，当前url：" + url+";module Paht:"+servletPath);
	            	if (request.getHeader("x-requested-with") != null && 
	            					request.getHeader("x-requested-with").equalsIgnoreCase("XMLHttpRequest")){
	            		
	            		response.setHeader("sessionstatus", "timeout");//在响应头设置session状态  
	            		response.setCharacterEncoding("text/html;charset=utf-8");
	    				response.setContentType("text/html;charset=utf-8");
	    				response.getWriter().print("error");
	    	        } else {
	    	        	response.sendRedirect(request.getContextPath()+"/login.html");
	    	        }
	            	return false;
	            } else {
	            	/*User user = (User)obj;
	            	if(!RightUtil.hasRight(currentURLTemp, request)){
	            		if(!"iisAdminTmp".equals(user.getName()) && !"/index".equals(targetURL)){
	            			//response.sendRedirect(request.getContextPath()+"/login/login");//应该返回到没有权限的页面
	            			//request.getRequestDispatcher("/login/login").forward(request, response);
	            			return false;
	            		}
	            	}*/
	            }
	        }
	        return super.preHandle(request, response, handler);
	    }
		 
	    @Override
	    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
	        super.postHandle(request, response, handler, modelAndView);
	    }

}