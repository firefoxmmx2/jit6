package importer;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.springframework.context.ApplicationContext;

import com.aisino2.jdy.domain.Ljjbxx;

public class ImporterSlayer {
	ApplicationContext context;
	HSSFWorkbook wb;
	Map<String, Object> globals = new HashMap<String, Object>();
	Document document;

	public ImporterSlayer(String xlsPath, String xmlModelPath,
			ApplicationContext context) throws JDOMException, IOException {
		SAXBuilder sb = new SAXBuilder();
		InputStream xmlin = null;
		InputStream xlsin = null;
		this.context = context;
		try {
			xmlin = ImporterSlayer.class.getResourceAsStream(xmlModelPath);
			xlsin = ImporterSlayer.class.getResourceAsStream(xlsPath);
			document = sb.build(xmlin);
			wb = new HSSFWorkbook(xlsin);
			// 默认的外部变量表
			globals.put("variable", new HashMap<String, Object>());
			// 初始化实体类型表
			globals.put("entityClass", new HashMap<String, Object>());
		} finally {
			xmlin.close();
			xlsin.close();
		}
	}

	/**
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws InvocationTargetException
	 * @throws ParseException 
	 * 
	 */
	public void initData() throws InstantiationException,
			IllegalAccessException, ClassNotFoundException,
			InvocationTargetException, ParseException {
		Element rootElement = document.getRootElement();
		Element dataElement = rootElement.getChild("Data");
		List<Object> datalist = new ArrayList<>();
		// 存放datalist到全局变量表
		globals.put("data", datalist);
		// 解析Data数据部分
		for (Element dataChildEle : (List<Element>) dataElement
				.getChildren("Entities")) {
			buildEntities(dataChildEle, null, true);
		}
	}

	public void initService() {
		Element rootElement = document.getRootElement();
	}

	/**
	 * 字符串空值验证，不为空返回true
	 * 
	 * @param str
	 * @return true 不为空
	 */
	private boolean isNotEmpty(String str) {
		if (str != null && str.trim().length() > 0)
			return true;
		return false;
	}

	/**
	 * 解析成员
	 * 
	 * @param el
	 * @return
	 */
	private Map<String, Object> getProperty(Element el) {
		Map<String, Object> attrs = new HashMap<>();
		for (Attribute attr : (List<Attribute>) el.getAttributes()) {
			if (isNotEmpty(attr.getValue())) {
				attrs.put(attr.getName(), attr.getValue());
			}
		}
		return attrs;
	}

	/**
	 * 获取反射方法
	 * 
	 * @param className
	 * @param methodName
	 * @param type
	 * @return
	 * @throws ClassNotFoundException
	 */
	private FastMethod getMethod(String className, String methodName,
			String type) throws ClassNotFoundException {
		Map<String, Object> entityClass = (Map<String, Object>) globals
				.get("entityClass");
		FastClass cls = (FastClass) entityClass.get(className + "Class");
		if (cls == null) {
			cls = FastClass.create(Class.forName(className));
			entityClass.put(className + "Class", cls);
		}
		Map<String, FastMethod> methods = (Map<String, FastMethod>) entityClass
				.get(className + "Methods");
		if (methods == null) {
			methods = new HashMap<>();
		}
		FastMethod targetMethod = methods.get(methodName);
		if (targetMethod == null) {
			Class[] paraType = null;
			if (type != null) {
				String[] types = type.split(",");
				paraType = new Class[types.length];
				for (int i = 0; i < types.length; i++)
					paraType[i] = Class.forName(types[i]);

			}
			targetMethod = cls.getMethod(methodName, paraType);
			methods.put(methodName, targetMethod);
		}

		return targetMethod;
	}

	private void buildEntities(Element entityEl, Map<String, Object> parentEnv,
			boolean isRootEl) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException,
			InvocationTargetException, ParseException {
		boolean hasParentEnv = parentEnv == null ? false : true;
		Map<String, Object> locals = new HashMap<String, Object>();

		Map<String, Object> attrs = getProperty(entityEl);
		locals.put("attrs", attrs);
		if (hasParentEnv) {
			commonExtendEnv(locals, parentEnv);
		}
		// type
		String type = (String) attrs.get("type");
		if (!isNotEmpty(type))
			throw new RuntimeException("type为必需的参数");
		Object instance = Class.forName(type).newInstance();
		// id
		String id = (String) attrs.get("id");
		// worksheet 工作单设置
		String worksheet = (String) attrs.get("worksheet");
		// column 数据所在列数。
		Integer column = getInteger((String) attrs.get("column"));
		//cast
		String castType = (String)attrs.get("cast");
		// name
		String name = (String) attrs.get("name");
		// ///////////////////////处理部分///////////////////////////
		locals.put("instance", instance);
		// 按照ID保存到全局变量字典里面
		if (isNotEmpty(id)) {
			globals.put(id, instance);
		}
		// 想全局datalist里面放入顶层Entities实体。
		if (isRootEl) {
			((List<Object>) globals.get("data")).add(instance);
			locals.put("rootEntityId", id);
		}
		HSSFSheet sheet = hasParentEnv ? (HSSFSheet) parentEnv
				.get("currentSheet") : null;
		if (isNotEmpty(worksheet)) {
			sheet = wb.getSheet(worksheet);
			if (sheet == null)
				sheet = wb.getSheetAt(Integer.parseInt(worksheet));
			if (sheet == null)
				throw new RuntimeException("当前sheet为空");
			locals.put("currentSheet", sheet);
		}

		if (isNotEmpty(name) && hasParentEnv) { // 如果名称和父级的环境存在的话，
			// 从父级环境里面取出subClass
			Map<String, Object> parentAttrs = (Map<String, Object>) parentEnv
					.get("attrs");
			String parentSubclass = (String) parentAttrs.get("class");
			FastMethod method = getMethod(parentSubclass, "set"
					+ name.substring(0, 1).toUpperCase() + name.substring(1),
					isNotEmpty(castType)? castType : type);

			method.invoke(parentAttrs.get("instance"),
					new Object[] { instance });
		}

		for (Element propertyEl : (List<Element>) entityEl.getChildren()) {
			if (propertyEl.getName().equals("Entity")) {
				buildDataEntity(propertyEl, locals);
			}
		}

		// ///////////////////////处理结束///////////////////////////
	}

	private void buildDataEntity(Element entityEl, Map<String, Object> parentEnv)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, InvocationTargetException, ParseException {
		// 解析子实体
		Map<String, Object> locals = new HashMap<>();
		Map<String, Object> attrs = getProperty(entityEl);
		locals.put("attrs", attrs);
		commonExtendEnv(locals, parentEnv);
		HSSFSheet sheet = (HSSFSheet) parentEnv.get("currentSheet");
		// xls数据起始行数
		Integer startrow = getInteger((String) attrs.get("startrow"));
		// xls数据结束行数
		Integer endrow = getInteger((String) attrs.get("endrow"));

		// class 类
		String className = (String) attrs.get("class");
		// id 是作为全局变量字典里面的索引值，存在ID的元素一般都会放到全局变量字典里面。
		String id = (String) attrs.get("id");
		// 名字 (名字通常是作为该实体的父级实体的成员属性，一般具有name的实体或者字典那么就作为该实体的一个属性)
		String name = (String) attrs.get("name");
		// propertySize（属性个数）和column和name连用，表示从根据列的长度来截取属性，再
		// 根据截取后的属性值和属性个数，赋值到实体里面

		Integer propertySize = getInteger((String) attrs.get("propertySize"));
		// xls数据所在列
		Integer column = getInteger((String) attrs.get("column"));

		// //////////////////处理部分/////////////////////////
		if (startrow != null && endrow == null)
			endrow = sheet.getLastRowNum();
		if (!isNotEmpty(className))
			throw new RuntimeException("Entity的class属性为必填");
		Object instance = Class.forName(className).newInstance();
		locals.put("instance", instance);
		if (isNotEmpty(id))
			globals.put(id, instance);
		if (isNotEmpty(name)) {
			Map<String, Object> parentAttrs = (Map<String, Object>) parentEnv
					.get("attrs");
			FastMethod method = getMethod(
					(String) parentAttrs.get("class"),
					"set" + name.substring(0, 1).toUpperCase()
							+ name.substring(1), className);
			method.invoke(parentEnv.get("instance"), new Object[] { instance });
		}
		
		if (startrow != null) {
			for (int i = startrow; i <= endrow; i++) {
				locals.put("currentRow", i);
				// 当父级实体是集合的时候添加当前实体到集合里面
				collectionAdd(parentEnv.get("instance"), instance);
				// 实体属性
				for (Element propertyEl : (List<Element>) entityEl
						.getChildren()) {

					if (propertyEl.getName().equals("Entities"))
						buildEntities(propertyEl, locals, false);
					// 解析属性 ，并且封装实体
					else if (propertyEl.getName().equals("property")) {
						buildDataProperty(propertyEl, locals);
					}
					// 实体
					else if (propertyEl.getName().equals("Entity")) {
						buildDataEntity(propertyEl, locals);
					}

				}
			}
		} else if (propertySize != null && column != null) {
			for (int i = 0; i < propertySize; i++) {
				locals.put("propertyNum", i);
				// 当父级实体是集合的时候添加当前实体到集合里面
				collectionAdd(parentEnv.get("instance"), instance);
				// 实体属性
				for (Element propertyEl : (List<Element>) entityEl
						.getChildren()) {
					if (propertyEl.getName().equals("Entities"))
						buildEntities(propertyEl, locals, false);
					// 解析属性 ，并且封装实体
					else if (propertyEl.getName().equals("property")) {
						buildDataProperty(propertyEl, locals);
					}
					// 实体
					else if (propertyEl.getName().equals("Entity")) {
						buildDataEntity(propertyEl, locals);
					}

				}
			}
		} else {
			// 当父级实体是集合的时候添加当前实体到集合里面
			collectionAdd(parentEnv.get("instance"), instance);
			// 实体属性
			for (Element propertyEl : (List<Element>) entityEl.getChildren()) {
				if (propertyEl.getName().equals("Entities"))
					buildEntities(propertyEl, locals, false);
				// 解析属性 ，并且封装实体
				else if (propertyEl.getName().equals("property")) {
					buildDataProperty(propertyEl, locals);
				}
				// 实体
				else if (propertyEl.getName().equals("Entity")) {
					buildDataEntity(propertyEl, locals);
				}

			}
		}

		// //////////////////处理结束/////////////////////////
	}

	private void buildDataProperty(Element property,
			Map<String, Object> parentEnv) throws ClassNotFoundException,
			InvocationTargetException, ParseException {
		Map<String, Object> attrs = getProperty(property);
		String name = (String) attrs.get("name");
		String type = (String) attrs.get("type");
		Integer column = getInteger((String) attrs.get("column"));

		// /////////////////////处理部分/////////////////////
		HSSFSheet sheet = (HSSFSheet) parentEnv.get("currentSheet");
		Integer currentRow = (Integer) parentEnv.get("currentRow");
		if (column == null)
			column = (Integer) parentEnv.get("column")
					+ (Integer) parentEnv.get("propertyNum");
		FastMethod method = getMethod(
				((Map<String, String>) parentEnv.get("attrs")).get("class"),
				"set" + name.substring(0, 1).toUpperCase() + name.substring(1),
				type);
		Object value = getCellValue(sheet.getRow(currentRow).getCell(column),
				type);

		method.invoke(parentEnv.get("instance"), new Object[] { value });

		// /////////////////////处理结束/////////////////////

	}

	public Object getCellValue(HSSFCell cell, String type)
			throws ClassNotFoundException, ParseException {
		Object result = null;
		if (cell != null) {
			switch (cell.getCellType()) {
			case HSSFCell.CELL_TYPE_STRING:
				if (type.contains("Date")){
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					result = sdf.parse(cell.getRichStringCellValue().getString());
				}
				else
					result = cell.getRichStringCellValue().getString();
				break;
			case HSSFCell.CELL_TYPE_BOOLEAN:
				result = cell.getBooleanCellValue();
				break;
			case HSSFCell.CELL_TYPE_ERROR:
				result = cell.getErrorCellValue();
				break;
			case HSSFCell.CELL_TYPE_FORMULA:
				result = cell.getCellFormula();
				break;
			case HSSFCell.CELL_TYPE_NUMERIC:
				if (type.contains("Date"))
					result = cell.getDateCellValue();
				else if (type.contains("Integer") || type.equals("int"))
					result = (int) cell.getNumericCellValue();
				else if (type.contains("Long") || type.equals("long"))
					result = (long) cell.getNumericCellValue();
				else if (type.contains("Float") || type.equals("float"))
					result = (float) cell.getNumericCellValue();
				else if (type.contains("String") || type.equals("string")) {
					result = (long) cell.getNumericCellValue() == cell
							.getNumericCellValue() ? String.valueOf((long) cell
							.getNumericCellValue()) : cell
							.getNumericCellValue();

				}
				break;
			default:
				result = null;
				break;
			}
		}
		return result;
	}

	private void collectionAdd(Object collection, Object target) {
		if (collection.getClass().getName().contains("List"))
			((List<Object>) collection).add(target);
		else if (collection.getClass().getName().contains("Set"))
			((Set<Object>) collection).add(target);
	}

	private Integer getInteger(String target) {
		Integer result = null;
		try {
			result = Integer.parseInt(target);
		} catch (Exception e) {
		}
		return result;
	}

	private void commonExtendEnv(Map<String, Object> locals,
			Map<String, Object> parentEnv) {
		locals.put("currentRow", parentEnv.get("currentRow"));
		locals.put("propertyNum", parentEnv.get("propertyNum"));
		locals.put("currentSheet", parentEnv.get("currentSheet"));
	}

	public static void main(String[] args) throws Exception {
//		System.out.println();
		 ImporterSlayer is = new ImporterSlayer("/物流单揽件批量导入模板.xls",
		 "/LjxxImportPdf.xml", null);
		 is.initData();
		 List<Ljjbxx> ljxx = (List<Ljjbxx>)is.globals.get("ljxx");
		 System.out.println(ljxx.size());
		 System.out.println(ljxx.get(0).getJdp_list().size());
	}
}
