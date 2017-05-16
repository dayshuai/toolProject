package com.ssm.mapper;

import java.util.List;
import java.util.Map;

public interface DatabasesPushDAO {
		
	/**
	 * 附件定时推送，查询附件路径
	 * @return
	 */
	public List<String> getAttachFilepaths();
	//p_file_manage表推送成功回写标识
	public void updateAnnexflgSucceed(Map<String, Object> params);
	//p_file_manage表推送成功失败标识
	public void updateAnnexflgFailed(Map<String, Object> params);
	
	/**
	 * 获取交易日时间
	 * @param params
	 * @return
	 */
	public List<String> getTradingDays(Map<String, Object> params);
	
	/**
	 * 更新推送表是否推送成功标识。
	 * @param params 推送对象表表名。
	 */
	public void updateFtpPushflg(Map<String, String> params);
	
	/**
	 * 获取推送对象表数据。
	 * @param params 推送对象表字段，表名，主键等。
	 * @return 推送对象表数据列表。
	 */
	public List<Map<String, Object>> findTableDatas(Map<String, String> params);
	
	public Map<String, Object> findTableDataByPrimaryValue (Map<String, Object> params);
	/**
	 * 分页获取推送对象表数据。
	 * @param params
	 * @param startIndex
	 * @param endIndex
	 * @return
	 */
	public List<Map<String, Object>> findTableDatasWithLimit(Map<String, Object> params);
	/**
	 * 获取总条数
	 * @param params
	 * @return
	 */
	public String selectCountTableDatas(Map<String, String> params);
	
}
