package com.ssm.app;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.ssm.bean.PDatabaseColumnConfig;
import com.ssm.mapper.DatabasesPushDAO;
import com.ssm.mapper.PDatabaseColumnConfigDAO;

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
		String str = "啊|";
		int firstIndex = 0;
		
		byte[] bytes;
		try {
			bytes = str.getBytes("GB18030");
			byte[] valueBytes = new byte[2];
			int valueIndex = 0;
			for (int j = firstIndex; j < firstIndex + 2; j++) {
				valueBytes[valueIndex] = bytes[j];
				valueIndex++;
			}
			// 得到一列的值
			String value = new String(valueBytes, "GB18030");
			System.out.println(value);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		
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
