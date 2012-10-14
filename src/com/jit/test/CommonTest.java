package com.jit.test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import service.ITestService;

public class CommonTest {
//	
	public static void main(String[] args) throws Exception{
		ApplicationContext context = new ClassPathXmlApplicationContext(CommonTest.class.getResource("/applicationContext.xml").getPath());
		ITestService its = (ITestService) context.getBean("testSerivceImpl");
		SAXBuilder sb = new SAXBuilder();
		InputStream xmlin=null;
		InputStream xlsin=null;
		
		Map<String,Object> variable = new HashMap<String, Object>();
		List<Object> datalist = new ArrayList<Object>();
		try{
			xmlin =  CommonTest.class.getClassLoader().getResourceAsStream("LjxxImportPdf.xml");
			xlsin = CommonTest.class.getResourceAsStream("/物流单揽件批量导入模板.xls");
			Document docuemnt =  sb.build(xmlin);
			HSSFWorkbook wb = new HSSFWorkbook(xlsin);
			Element rootElement = docuemnt.getRootElement();
			Element dataElement = rootElement.getChild("Data");
			
			//解析Data数据部分
			for(Element dataChildEle : (List<Element>)dataElement.getChildren()){
				if(dataChildEle.getName().equals("Entities")){
					int startrow = 0;
					try{
						startrow = Integer.parseInt(dataChildEle.getAttributeValue("startrow"));
					}
					catch(Exception e){
						System.err.println("没有startrow属性或者值无法转换为数值");
						e.printStackTrace();
						
					}
					int endrow = 0;
					try{
						endrow = Integer.parseInt(dataChildEle.getAttributeValue("endrow"));
					}catch(Exception e){
						System.err.println("没有endrow属性或者值无法转换为数值");
						e.printStackTrace();
					}
					String type=null;
					try{
						type = dataChildEle.getAttributeValue("type");
					}catch(Exception e){
						e.printStackTrace();
					}
					
					if(!isNotEmpty(type)){
						throw new RuntimeException("type为必需的参数");
					}
					
					Object typeObject = Class.forName(type).newInstance();
					String id = null;
					try{
						id = dataChildEle.getAttributeValue("id");
					}catch(Exception e){
						e.printStackTrace();
					}
					//按照ID保存到全局变量字典里面
					if(isNotEmpty(id)){
						variable.put(id, typeObject);
					}
					
					//worksheet 工作单设置
					String worksheet = null;
					try{
						worksheet = dataChildEle.getAttributeValue("worksheet");
					}catch(Exception e){
						e.printStackTrace();
					}
					
					HSSFSheet sheet = null;
					if(isNotEmpty(worksheet)){
						try{
							sheet = wb.getSheet(worksheet);
						}catch(Exception e){
							e.printStackTrace();
							sheet = wb.getSheetAt(Integer.parseInt(worksheet));
						}
						
					}
					
					datalist.add(typeObject);
					//entity
					for(Element entityEl : (List<Element>)dataChildEle.getChildren("Entity")){
						Map<String, Object> locals = new HashMap<String, Object>();
						buildEntities(entityEl,locals,variable,id);
					}
					
				}
			}
			
			//解析服务部分
			
			System.out.println(((List)variable.get("ljxx")).size());
			
		}
		finally{
			xmlin.close();
			xlsin.close();
		}
		
	}
	
	public static boolean isNotEmpty(String str){
		if(str!=null && str.trim().length()>0)
			return true;
		return false;
	}
	
	public static void buildEntities(Element entityEl, Map<String,Object> locals,Map<String,Object> globals,String rootEntityId) throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		if(entityEl.getName().equals("Entities")){
			int startrow = 0;
			try{
				startrow = Integer.parseInt(entityEl.getAttributeValue("startrow"));
			}
			catch(Exception e){
				System.err.println("没有startrow属性或者值无法转换为数值");
				e.printStackTrace();
				
			}
			int endrow = 0;
			try{
				endrow = Integer.parseInt(entityEl.getAttributeValue("endrow"));
			}catch(Exception e){
				System.err.println("没有endrow属性或者值无法转换为数值");
				e.printStackTrace();
			}
			String type=null;
			try{
				type = entityEl.getAttributeValue("type");
			}catch(Exception e){
				e.printStackTrace();
			}
			
			if(!isNotEmpty(type)){
				throw new RuntimeException("type为必需的参数");
			}
			
			Object typeObject = Class.forName(type).newInstance();
			String id = null;
			try{
				id = entityEl.getAttributeValue("id");
			}catch(Exception e){
				e.printStackTrace();
			}
			//按照ID保存到全局变量字典里面
			if(isNotEmpty(id)){
				globals.put(id, typeObject);
			}
			
			//worksheet 工作单设置
			String worksheet = null;
			try{
				worksheet = entityEl.getAttributeValue("worksheet");
			}catch(Exception e){
				e.printStackTrace();
			}
			
			HSSFSheet sheet = null;
			if(isNotEmpty(worksheet)){
				HSSFWorkbook wb = (HSSFWorkbook) globals.get("wb");
				try{
					
					sheet = wb.getSheet(worksheet);
				}catch(Exception e){
					e.printStackTrace();
					sheet = wb.getSheetAt(Integer.parseInt(worksheet));
				}
				
				globals.put("currentSheet", sheet);
				
			}
			
			//entity
			for(Element subEntityEl : (List<Element>)entityEl.getChildren()){
				Map<String,Object> sublocals = new HashMap<String, Object>();
				buildEntities(entityEl, sublocals, globals,rootEntityId);
			}
			
		}
		else if(entityEl.getName().equals("Entity")){
			//解析子实体
			//
			// column 数据所在列数。
			Integer column = null;
			try {
				column = Integer.getInteger(entityEl.getAttributeValue("column"));
			} catch (Exception e) {
				e.printStackTrace();
			}
			// class 类
			String className = null;
			try {
				className = entityEl.getAttributeValue("class");
			} catch (Exception e) {
				e.printStackTrace();
			}
			// propertySize（属性个数）和multi连用，表示从根据列的长度来截取属性，再
			// 根据截取后的属性值和属性个数，赋值到实体里面
			Integer propertySize = null;
			try {
				propertySize = Integer.getInteger(entityEl.getAttributeValue("propertySize"));
			} catch (Exception e) {
				e.printStackTrace();
			}
			// id 是作为全局变量字典里面的索引值，存在ID的元素一般都会放到全局变量字典里面。
			String id = null;
			try {
				id = entityEl.getAttributeValue("id");
			} catch (Exception e) {
				e.printStackTrace();
			}
			//名字 (名字通常是作为该实体的父级实体的成员属性，一般具有name的实体或者字典那么就作为该实体的一个属性)
			String name = null;
			try {
				name = entityEl.getAttributeValue("name");
			} catch (Exception e) {
				e.printStackTrace();
			}
			//最后添加子实体到顶层集合
			boolean multi = false;
			try {
				multi = Boolean.getBoolean(entityEl.getAttributeValue("multi"));
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			//实体属性
			for(Element propertyEl : (List<Element>)entityEl.getChildren()){
				
				if(propertyEl.getName().equals("Entity") || propertyEl.getName().equals("Entities"))
					buildEntities(propertyEl, locals, globals, rootEntityId);
			}
			
			//当实体属性没有 multi 和 name的时候，才会添加到顶层集合中去。
			if(multi && !isNotEmpty(name)){
				String rootEntityType = globals.get(rootEntityId).getClass().getName();
				if(rootEntityType.indexOf("List") != -1){
					List rootEntities = (List)globals.get(rootEntityId);
				}
				else if(rootEntityType.indexOf("Set") != -1){
					Set rootEntities = (Set)globals.get(rootEntityId);
				}
			}
			
		}
	}
	
}
