package com.jit.common;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class CommonUtil {
	Map<String,Object> variable = new HashMap<String, Object>();
	public void buildConstant() throws Exception{
		
		
		class StringObject extends SimpleJavaFileObject{
			private String content;
			
			public StringObject (String classname,String content) throws Exception{
				super(URI.create("string:///"+classname.replace(".", "/")+Kind.SOURCE.extension),Kind.SOURCE);
				this.content=content;
			}

			@Override
			public CharSequence getCharContent(boolean ignoreEncodingErrors)
					throws IOException {
				
				return this.content;
			}
			
			
		}
		
		String basePackageName="com.jit.common";
		String constantClassName="Constant";
		StringBuffer contentbuffer=new StringBuffer();
		
		//package pack
		contentbuffer.append("package ");
		contentbuffer.append(basePackageName);
		contentbuffer.append(";\n");
		//class pack
		contentbuffer.append("class ");
		contentbuffer.append(constantClassName);
		contentbuffer.append(" {\n");
		
//		contentbuffer.append("\t/** 测试标题 */\n");
//		contentbuffer.append("\tpublic static final String SUPER_TEST=\"hello , 这是一个测试\";\n");
//		
//		contentbuffer.append("\t/** 状态进行中 */\n");
//		contentbuffer.append("\tpublic static final String ST_STATUS_GOING=\"going\";\n");
		contentbuffer.append("\tpublic void doInvoke(){");
		contentbuffer.append("\t\tvariable.put(\"hello\",\"world\");\n");
		contentbuffer.append("\t}");
		
		contentbuffer.append("}\n");
		
		System.out.println(contentbuffer);
		JavaCompiler compiler=ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager filemanager=compiler.getStandardFileManager(null, null, null);
		
		JavaFileObject javaObj=new StringObject(constantClassName, contentbuffer.toString());
		Iterable<JavaFileObject> fileIter = Arrays.asList(javaObj);
//		File classFile=new File(CommonUtil.class.getResource("").getPath()+"/"+constantClassName+".class");
//		if(!classFile.exists()){
//			classFile.createNewFile();
//		}
//		FileWriter out=new FileWriter(classFile);
		Iterable<String> options=Arrays.asList("-d",CommonUtil.class.getResource("/").getPath());
		JavaCompiler.CompilationTask task = compiler.getTask(null, filemanager, null, options, null, fileIter);
		Boolean result = task.call();
//		out.close();
		
		System.out.println("result="+result);
//        URLClassLoader classLoader = new URLClassLoader(  
//                new URL[] { new File(System.getProperty("java.io.tmpdir")).toURI().toURL() });  
//    clazz = classLoader.loadClass("ETEvaler");  
//    Method method = clazz.getDeclaredMethod("eval");  
//    Object value = method.invoke(clazz.newInstance() );
	}
	
	public String buildConstantItemByDict(String prefix,String dictcode){
		StringBuffer constantItem=new StringBuffer();
		
		return constantItem.toString();
	}
	
	public static void main(String[] args) throws Exception {
		CommonUtil util = new CommonUtil();
		util.buildConstant();
	}
}
