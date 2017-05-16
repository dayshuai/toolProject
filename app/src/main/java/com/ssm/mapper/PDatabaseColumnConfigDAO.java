package com.ssm.mapper;

import java.util.List;
import java.util.Map;

import com.ssm.bean.PDatabaseColumnConfig;

/**
 * 表信息配置数据访问
 * 
 */
public interface PDatabaseColumnConfigDAO {
	
	/**
 	* 保存表信息配置
 	* 
 	*/
    int insert(PDatabaseColumnConfig pDatabaseColumnConfig);

	/**
 	* 表信息配置属性非空保存
 	* 
 	*/
    int insertSelective(PDatabaseColumnConfig pDatabaseColumnConfig);
	
	/**
 	* 根据Id查询表信息配置
 	* 
 	*/
    PDatabaseColumnConfig selectById(Long id);

	/**
 	* 修改表信息配置
 	* 
 	*/
    int updateById(PDatabaseColumnConfig pDatabaseColumnConfig);
    
    /**
 	* 表信息配置属性非空修改
 	* 
 	*/
    int updateIdKeySelective(PDatabaseColumnConfig pDatabaseColumnConfig);
    
    /**
 	* 根据id删除表信息配置
 	* 
 	*/	    	
	int deleteById(Long id);
    
	
	/**
	 * 查询需要同步的表 不重复
	 * 
	 * @return
	 */
	List<PDatabaseColumnConfig> querySyncTableList();
	
	Map<String, Object> findTableDataByPrimaryValue(Map<String, Object>param);

	/**
	 * 根据表名称查询 字段信息
	 * 
	 * @param tableName
	 * @return
	 */
	List<PDatabaseColumnConfig> queryColumnList(String tableName);
	
	List<String> queryTableListForSup(Map<String, Object>param);
	
	List<PDatabaseColumnConfig> queryColumnListForSup(Map<String, Object>param);
}
