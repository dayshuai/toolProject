package com.ssm.app;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.ssm.bean.PDatabaseColumnConfig;

public class SupScnyTool {
	/** 日志管理 */
	/*private static final Logger logger = LoggerFactory.getLogger(SupScnyTool.class);
	private static ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
			"applicationContext-dao.xml");

	private static PDatabaseColumnConfigDAO pDatabaseColumnConfigDAO = (PDatabaseColumnConfigDAO) applicationContext
			.getBean("PDatabaseColumnConfigDAO");

	private static DatabasesPushDAO databasesPushDAO = (DatabasesPushDAO) applicationContext
			.getBean("databasesPushDAO");
	private static List<String> commonTableList = new ArrayList<String>();
	static {
		getCommonTableList();
	}*/
	public static void main(String[] args) {
		String str = "ss VARCHAR2(3) 22(3)";
		Pattern pattern = Pattern.compile("VARCHAR2\\((\\d+)\\)");
		Matcher matcher = pattern.matcher(str);
		if (matcher.find()) {
			String tbName = matcher.group(1);
			String bb = str.replaceAll("VARCHAR2\\((\\d+)\\)", "VARCHAR2(" + 2 * Integer.valueOf(tbName) + ")");
			
			System.out.println(tbName + "|" + bb);
		}
		
		/*if(str.contains("VARCHAR2")){
			int index = str.indexOf("VARCHAR2(");
			System.out.println(index);
		}*/
		
		
		/*for(String table : commonTableList){
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("tableName", "sup_p_database_column_config");
			paramMap.put("configtableName", getSupTableName(table));
			List<PDatabaseColumnConfig> supColumnCofigList = pDatabaseColumnConfigDAO.queryColumnListForSup(paramMap);
			paramMap.put("tableName", "p_database_column_config");
			paramMap.put("configtableName", table);
			List<PDatabaseColumnConfig> columnCofigList = pDatabaseColumnConfigDAO.queryColumnListForSup(paramMap);
			System.out.println(table);
			compareDifferent(supColumnCofigList, columnCofigList);
		}*/
			
		
	}
	
	public static void compareDifferent (List<PDatabaseColumnConfig> supColumnCofigList, List<PDatabaseColumnConfig> columnCofigList) {
		for (PDatabaseColumnConfig columnConfig : columnCofigList) {
			for (PDatabaseColumnConfig supColumnCofig : supColumnCofigList) {
				
			}
		}
	}
	
	public static String getSupTableName (String tableName) {
		return "t_" + tableName;
	}
	
	/*public static void getCommonTableList () {
		Map<String, Object>paramMap = new HashMap<String, Object>();
		paramMap.put("tableName", "p_database_column_config");
		List<String> databaseConfigList = pDatabaseColumnConfigDAO.queryTableListForSup(paramMap);
		paramMap.put("tableName", "sup_p_database_column_config");
		List<String> supDatabaseConfigList = pDatabaseColumnConfigDAO.queryTableListForSup(paramMap);
			for (String supTable : supDatabaseConfigList) {
				String tableName = supTable.replace("t_", "");
				if (databaseConfigList.contains(tableName)) {
					commonTableList.add(tableName);
				}
			}
	}*/
}
