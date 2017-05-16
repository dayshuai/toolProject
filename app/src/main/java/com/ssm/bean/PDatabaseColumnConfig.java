package com.ssm.bean;

public class PDatabaseColumnConfig {

	private static final long serialVersionUID = 1L;
	

	/** 序号 **/
	private Long id;

	/** 业务表表名称 **/
	private String tableName;

	/** 字段名称 **/
	private String columnName;

	/** 表推送顺序 **/
	private Long tableOrder;

	/** 字段类型（mysql数据库中字段类型） **/
	private String columnType;

	/** 字段备注（对字段的说明） **/
	private String columnComment;

	/** 字段宽度（字符宽度） **/
	private Integer columnWidth;

	/** 文件名 ${replace} 表示被替换部分 **/
	private String fileName="";
	
	public PDatabaseColumnConfig(){

	}

	public PDatabaseColumnConfig(String tableName,String columnName,Long tableOrder,String columnType,String columnComment,Integer columnWidth){
		this.tableName=tableName;
		this.columnName=columnName;
		this.tableOrder=tableOrder;
		this.columnType=columnType;
		this.columnComment=columnComment;
		this.columnWidth=columnWidth;
	}
	public void setId(Long id){
		this.id=id;
	}

	public Long getId(){
		return this.id;
	}
	
	public void setTableName(String tableName){
		this.tableName=tableName;
	}

	public String getTableName(){
		return this.tableName;
	}
	
	public void setColumnName(String columnName){
		this.columnName=columnName;
	}

	public String getColumnName(){
		return this.columnName;
	}
	
	public void setTableOrder(Long tableOrder){
		this.tableOrder=tableOrder;
	}

	public Long getTableOrder(){
		return this.tableOrder;
	}
	
	public void setColumnType(String columnType){
		this.columnType=columnType;
	}

	public String getColumnType(){
		return this.columnType;
	}
	
	public void setColumnComment(String columnComment){
		this.columnComment=columnComment;
	}

	public String getColumnComment(){
		return this.columnComment;
	}
	
	public void setColumnWidth(Integer columnWidth){
		this.columnWidth=columnWidth;
	}

	public Integer getColumnWidth(){
		return this.columnWidth;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
}