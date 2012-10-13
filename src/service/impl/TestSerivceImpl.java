package service.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.springframework.stereotype.Component;

import service.ITestService;

import com.aisino2.jdy.domain.Ljjbxx;

@Component
public class TestSerivceImpl implements ITestService {

	@Override
	public void printTestInfo(Object obj) {
		
		Method[] md = obj.getClass().getDeclaredMethods();
		Method.setAccessible(md, true);
		for(Method m : md){
			if(m.getName().indexOf("get")==0){
				try {
					System.out.println(m.invoke(obj));
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
