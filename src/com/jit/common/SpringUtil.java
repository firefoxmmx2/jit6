package com.jit.common;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringUtil {
	private static ApplicationContext context;
	
	public static ApplicationContext getContext(){
		if(context==null){
			context=new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
		}
		return context;
	}
	
}
