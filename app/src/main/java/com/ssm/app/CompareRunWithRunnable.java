package com.ssm.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.filechooser.FileSystemView;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import com.ssm.bean.PDatabaseColumnConfig;
import com.ssm.mapper.DatabasesPushDAO;
import com.ssm.mapper.PDatabaseColumnConfigDAO;

public class CompareRunWithRunnable implements Runnable {
	/** 日志管理 */
	private static final Logger logger = LoggerFactory.getLogger(CompareRunWithRunnable.class);
	private static ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
			"applicationContext-dao.xml");

	private static PDatabaseColumnConfigDAO pDatabaseColumnConfigDAO = (PDatabaseColumnConfigDAO) applicationContext
			.getBean("PDatabaseColumnConfigDAO");

	private static DatabasesPushDAO databasesPushDAO = (DatabasesPushDAO) applicationContext
			.getBean("databasesPushDAO");

	/** 文件内容编码格式 */
	private static final String CHARSET_NAME = "GB18030";
	/** 数据库中日期时刻类型的数据要进行格式化。 */
	private static String SQL_FMT_DATETIME = "date_format({0}, \"%Y-%m-%d %H:%i:%s\") as {0}";
	/**
	 * 文件名对应表配置
	 */
	private static Map<String, String> fileNameAndTableName = new HashMap<String, String>();
	/**
	 * 主键配置map
	 */
	private static Map<String, String> primaryKeyConfigMap = new HashMap<String, String>();
	/**
	 * 生成的文件
	 */
	private static File produceFile;
	/**
	 * 生成文件文件夹
	 */
	private static String produceDirPath;
	/**
	 * db.properties
	 */
	private static Properties props;
	/**
	 * 文件根路径
	 */
	private static String fileBasePath;
	/**
	 * 文件数
	 */
	private static int fileNo;
	/**
	 * 文件列表
	 */
	private static List<String> fileList = new LinkedList<String>();
	/**
	 * 是否监管系统
	 */
	private static String isSup = "false";
	private static int threadCount;// 初始化线程数
	private static int perCount = 3;// 每个线程处理的数据量
	private static int flag = 1;// 这是第几个线程
	private static int count;// 待处理数据总数量
	private static int havedCount = 0;// 已经处理的数据量
	private static List<String> tempFileList = new LinkedList<String>();
	static {
		Resource resource = new ClassPathResource("/db.properties");
		try {
			props = PropertiesLoaderUtils.loadProperties(resource);
			fileBasePath = props.getProperty("fileBasePath");
			isSup = props.getProperty("isSup");
		} catch (IOException e) {
			e.printStackTrace();
		}
		initFiles();
		getFileNameAndTableName();
		getPrimaryKeyConfigMap();
		getProduceFile();
	}

	public static void getProduceFile() {
		File desktopDir = FileSystemView.getFileSystemView().getHomeDirectory();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		produceDirPath = desktopDir.getAbsolutePath() + File.separator + sdf.format(new Date());
		// produceFile = new File(produceFilePath);
		CCRDFile.createDir(produceDirPath);
		String produceFilePath = produceDirPath + File.separator + "对比结果" + sdf.format(new Date()) + ".txt";
		CCRDFile.createFile(produceFilePath);
		produceFile = new File(produceFilePath);
	}

	public static void getFileNameAndTableName() {
		List<PDatabaseColumnConfig> tableList = pDatabaseColumnConfigDAO.querySyncTableList();
		for (PDatabaseColumnConfig config : tableList) {
			if (StringUtils.equals(isSup, "true")) {
				fileNameAndTableName.put(config.getTableName().replace("t_", "").replace("_", ""),
						config.getTableName());
			} else {
				fileNameAndTableName.put(config.getTableName().replace("_", ""), config.getTableName());
			}
		}
	}

	public static void getPrimaryKeyConfigMap() {
		String primaryKeyProp = props.get("primaryKeyConfigMap").toString();
		String[] primaryKeyPropArr = primaryKeyProp.split("@");
		for (String str : primaryKeyPropArr) {
			primaryKeyConfigMap.put(str.split(":")[0], str.split(":")[1]);
		}

	}

	/**
	 * 通过业务推送表取得的推送信息，编辑推送对象字符串。
	 * 
	 * @param colConfigList
	 *            业务推送表取得的推送信息。
	 * @return 推送对象字符串及主键等相关内容。
	 */
	public static Map<String, Object> pickupTableCols(List<PDatabaseColumnConfig> colConfigList) {
		Map<String, Object> mapResult = new HashMap<String, Object>();
		StringBuilder tableCols = new StringBuilder();
		for (int i = 0; i < colConfigList.size(); i++) {
			// 获取该对象表中的每一个字段等相关信息
			PDatabaseColumnConfig colConfig = colConfigList.get(i);
			String columnName = colConfig.getColumnName().trim();
			// datetime格式的数据，在执行查询时要统一格式化
			if ("datetime".equals(colConfig.getColumnType().trim().toLowerCase())) {
				// 根据定义格式重新设定datetime类型的列名
				columnName = MessageFormat.format(SQL_FMT_DATETIME, columnName);
			}
			// 拼接上每个字段名
			tableCols.append(columnName);
			// 每个字段结尾拼接上", "
			if (i != colConfigList.size() - 1) {
				tableCols.append(", ");
			}
			// 如果默认以第一个字段为主键
			if (i == 0) {
				mapResult.put("primaryKey", columnName);
				mapResult.put("primaryKeyType", colConfig.getColumnType());
			}
		}
		// 设定所有字段的组合字符串
		mapResult.put("tableCols", tableCols.toString());
		return mapResult;
	}

	public static void initFiles() {
		File baseFileArr = new File(fileBasePath);
		String[] baseFile = baseFileArr.list();
		if (baseFile == null || baseFile.length == 0) {
			logger.error("文件根目录下找不到文件，确保文件目录格式如下:f:\\\\aa\\\\bb\\\\");
			return;
		}
		for (String fileName : baseFile) {
			String suffix = fileName.substring(fileName.lastIndexOf("."), fileName.length());
			if (StringUtils.equals(suffix, ".txt")) {
				String filePath = fileBasePath + "\\" + fileName;
				try {
					fileList.add(filePath);
				} catch (Exception e) {
					logger.error(fileName + "对比出现异常");
				}
			}
		}
		// 总文件数
		count = fileList.size();
		// 线程数 最多5个线程
		threadCount = (count % perCount == 0 ? count / perCount : count / perCount + 1) > 5 ? 5
				: (count % perCount == 0 ? count / perCount : count / perCount + 1);
	}

	public static void loadTxtCompareToDatabase(String filePath, File tempFile) {
		File txtFile = new File(filePath);
		if (!txtFile.exists()) {
			System.out.println("同步到本地的txt文件不存在,不同步filepath：" + filePath);
			return;
		}
		List<PDatabaseColumnConfig> tableList = pDatabaseColumnConfigDAO.querySyncTableList();
		try {
			for (PDatabaseColumnConfig table : tableList) {
				Pattern pattern = Pattern.compile("[\\d]+([^\\d]+)[\\d]+");
				Matcher matcher = pattern.matcher(txtFile.getName());
				if (matcher.find()) {
					String tbName = matcher.group(1);
					if (StringUtils.equals(fileNameAndTableName.get(tbName), table.getTableName())) {
						compareTxtDatasWithDatabase(txtFile, table, tempFile);
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// loadFiles();
		CompareRunWithRunnable l = new CompareRunWithRunnable();
		for (int i = 0; i < threadCount; i++) {
			Thread t = new Thread(l);
			t.start();
		}
	}

	/**
	 * 设置行数据，转化为固定txt存储格式
	 * 
	 * @param config
	 * @param data
	 * @return
	 */
	private static String setColumnValues(List<PDatabaseColumnConfig> config, Map<String, Object> data) {

		// b_project表的字段个数
		String bStr = "";
		for (int i = 0; i < config.size(); i++) {
			PDatabaseColumnConfig col = config.get(i);
			// 获取字段名称
			String columnName = col.getColumnName();
			// 获取字段宽度
			String columnWidth = col.getColumnWidth() + "";
			// 判断业务表中是否遗漏字段
			/*
			 * if (!data.containsKey(columnName)) {
			 * log.warn(tableName+"业务表中"+columnName+"值为空！"); }
			 */
			// 获取业务表数据值,与推送表中记录的数据进行拼接
			String value = "";
			if (data.get(columnName) != null) {
				value = data.get(columnName).toString();
				value = value.replace("\r", "");
				value = value.replace("\n", "");
			}
			String aimString = getstandardColumnWidth(value, Integer.valueOf(columnWidth));
			aimString = aimString + "|";
			bStr += aimString;
		}
		// 最后一项不加|所以要去掉最后的
		bStr = bStr.substring(0, bStr.length() - 1);
		return bStr;
	}

	/**
	 * 根据GB18030编码，转化目标字符串为columnWidth字节的字符串
	 * 
	 * @param content
	 *            转化对象
	 * @param columnName
	 *            转化长度
	 * @return 返回转化后的字符串
	 */
	public static String getstandardColumnWidth(String content, int columnWidth) {

		if (columnWidth <= 0)
			return "";
		try {
			if (content == null)
				content = "";
			byte[] contentBytes = content.getBytes("GB18030");
			while (contentBytes.length > columnWidth) {
				content = content.substring(0, content.length() - 1);
				contentBytes = content.getBytes("GB18030");
			}
			byte[] container = new byte[columnWidth];
			for (int i = 0; i < columnWidth; i++) {
				container[i] = 0x20;
			}
			System.arraycopy(contentBytes, 0, container, 0, contentBytes.length);

			return new String(container, "GB18030");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return String.format("%-" + columnWidth + "s", "");
		}
	}

	/**
	 * 对比含有'|'的数据
	 * 
	 * @param columnConfigList
	 * @param values
	 * @param table
	 */
	public static void compareSpecialData(List<PDatabaseColumnConfig> columnConfigList, String[] values,
			PDatabaseColumnConfig table, String lineStr, File tempFile) {
		List<String> valueList = new LinkedList<String>();
		CollectionUtils.addAll(valueList, values);
		Map<String, Object> dataMap = queryDataByPrimaryValue(columnConfigList, table.getTableName(), valueList,
				primaryKeyConfigMap.get(table.getTableName()));
		if (dataMap == null) {
			String logStr = getPrimaryKeyAndValueByConfigMap(columnConfigList, valueList) + "在txt中存在，在数据库中不存在";
			writeText(tempFile, logStr);
			logger.info(logStr);
		} else {
			// 拼接成有'|'的格式进行对比
			String columnValues = replaceBlank(setColumnValues(columnConfigList, dataMap));
			// txt中一行数据
			String txtLineStr = replaceBlank(lineStr);
			if (!StringUtils.equals(columnValues, txtLineStr)) {
				String logStr = "\n" + getPrimaryKeyAndValueByConfigMap(columnConfigList, valueList) + "\t" + "有差异..."
						+ "\ndatabaseValue:" + columnValues + "\ntxtValue:" + txtLineStr + "\n";
				writeText(tempFile, logStr);
				logger.info(logStr);
			}
		}
	}

	public static void compareTxtDatasWithDatabase(File txtFile, PDatabaseColumnConfig table, File tempFile)
			throws IOException {
		List<PDatabaseColumnConfig> columnConfigList = pDatabaseColumnConfigDAO.queryColumnList(table.getTableName());
		logger.info("----------" + table.getTableName() + "比较差异开始----------");
		writeText(tempFile, "\n----------" + table.getTableName() + "比较差异开始----------\n");
		String str = null;
		FileInputStream fis = new FileInputStream(txtFile);
		BufferedReader br = new BufferedReader(new InputStreamReader(fis, CHARSET_NAME));
		int txtAlllineNo = 1;
		int finishLine = 1;
		while ((str = br.readLine()) != null) {
			logger.info("比较" + txtFile.getName() + "中第" + txtAlllineNo + "行...");
			txtAlllineNo++;
			if (StringUtils.isNotBlank(str)) {
				try {
					// 表示首次截取下标
					int firstIndex = 0;
					List<String> valueList = new LinkedList<String>();
					String[] values = str.split("\\|");
					// 如果分割的长度小于长度的话直接跳过本条数据
					if (values.length < columnConfigList.size()) {
						logger.error("本条数据分割的长度不符合规则,本条数据不处理 value=" + replaceBlank(str));
						writeText(tempFile, "\n本条数据分割的长度不符合规则,本条数据不处理 value=" + replaceBlank(str) + "\n");
						continue;
					}
					/*if (values.length > columnConfigList.size()) {
						try {
							// 拼接成‘|’进行对比
							compareSpecialData(columnConfigList, values, table, str, tempFile);
							finishLine++;
						} catch (Exception e) {
							logger.error("本条数据分割的长度不符合规则,含有'|',且对比时发生异常" + replaceBlank(str));
							writeText(tempFile, "\n本条数据分割的长度不符合规则,含有|,且对比时发生异常" + replaceBlank(str) + "\n");
						}
						continue;
					}*/
					for (int i = 0; i < columnConfigList.size(); i++) {
						PDatabaseColumnConfig databaseColumnConfig = columnConfigList.get(i);
						// 如果分割长度等于 默认按照分割取值
						if (values.length == columnConfigList.size()) {
							valueList.add(handlerValue(databaseColumnConfig, values[i]));
						} else {
							byte[] bytes = str.getBytes(CHARSET_NAME);
							byte[] valueBytes = new byte[databaseColumnConfig.getColumnWidth()];
							int valueIndex = 0;
							for (int j = firstIndex; j < firstIndex + databaseColumnConfig.getColumnWidth(); j++) {
								valueBytes[valueIndex] = bytes[j];
								valueIndex++;
							}
							// 得到一列的值
							String value = new String(valueBytes, CHARSET_NAME);
							valueList.add(handlerValue(databaseColumnConfig, value));
							// 设置文本行下一次截取的第一个位置下标
							firstIndex = firstIndex + databaseColumnConfig.getColumnWidth() + 1;
						}
					}
					Map<String, Object> dataMap = queryDataByPrimaryValue(columnConfigList, table.getTableName(),
							valueList, primaryKeyConfigMap.get(table.getTableName()));
					if (dataMap == null) {
						String logStr = getPrimaryKeyAndValueByConfigMap(columnConfigList, valueList)
								+ "在txt中存在，在数据库中不存在";
						writeText(tempFile, logStr);
						logger.info(logStr);
					} else {
						// 正常的对比
						compareDataMapWithValueString(columnConfigList, valueList, dataMap, tempFile);
					}
					finishLine++;
				} catch (Exception e) {
					logger.error(e.toString());
				}
			}
		}
		Map<String, String> countSearch = new HashMap<String, String>();
		countSearch.put("tableName", table.getTableName());
		// 数据库总条数
		String databaseCount = databasesPushDAO.selectCountTableDatas(countSearch);
		logger.info("\ntxt共" + Integer.valueOf(txtAlllineNo - 1) + "行,对比了" + Integer.valueOf(finishLine - 1)
				+ "行 database共" + databaseCount + "行数据\n" + "\n----------" + table.getTableName() + "比较差异结束----------");
		writeText(tempFile, "\ntxt共" + Integer.valueOf(txtAlllineNo - 1) + "行,对比了" + Integer.valueOf(finishLine - 1)
				+ "行 database共" + databaseCount + "行数据\n" + "\n----------" + table.getTableName() + "比较差异结束----------");
	}

	/**
	 * 数据库的值和txt读取的值进行对比
	 * 
	 * @param columnConfigList
	 * @param insertValue
	 * @param dataMap
	 */
	public static void compareDataMapWithValueString(List<PDatabaseColumnConfig> columnConfigList,
			List<String> valueList, Map<String, Object> dataMap, File tempFile) {
		int index = 0;
		for (PDatabaseColumnConfig config : columnConfigList) {
			String txtValue = replaceBlank(valueList.get(index));
			String databaseValue = replaceBlank(
					dataMap.get(config.getColumnName()) == null ? "" : dataMap.get(config.getColumnName()).toString());
			if (!StringUtils.equals(txtValue, databaseValue)
					&& !(StringUtils.equals(txtValue, "null") && StringUtils.isBlank(databaseValue))) {
				String logStr = "\n" + getPrimaryKeyAndValueByConfigMap(columnConfigList, valueList) + "\t"
						+ config.getColumnName() + "有差异..." + "\ndatabaseValue:" + databaseValue + "\ntxtValue:"
						+ txtValue + "\n";
				writeText(tempFile, logStr);
				logger.info(logStr);
			}
			index++;
		}

	}

	/**
	 * 去空格回车
	 * 
	 * @param str
	 * @return
	 */
	public static String replaceBlank(String str) {
		String dest = "";
		if (str != null) {
			Pattern p = Pattern.compile("\\s*|\t|\r|\n");
			Matcher m = p.matcher(str);
			dest = m.replaceAll("");
		}
		return dest;
	}

	/**
	 * 获得主键名和值
	 * 
	 * @param columnConfigList
	 * @param dataMap
	 * @return
	 */
	public static String getPrimaryKeyAndValueByConfigMap(List<PDatabaseColumnConfig> columnConfigList,
			List<String> valueList) {
		String keyAndValue = "";
		int index = 0;
		// 字段名+字段值
		keyAndValue = columnConfigList.get(index).getColumnName() + ":" + valueList.get(index) + "\t";
		if (primaryKeyConfigMap.containsKey(columnConfigList.get(0).getTableName())) {
			index = Integer.valueOf(primaryKeyConfigMap.get(columnConfigList.get(0).getTableName()).split(",")[1]);
			keyAndValue += columnConfigList.get(index).getColumnName() + ":" + valueList.get(index) + "\t";
		}
		return keyAndValue;
	}

	/**
	 * 通过主键查询数据库的数据
	 * 
	 * @param columnConfigList
	 * @param tableName
	 * @param insertValue
	 * @param primaryIndex
	 * @return
	 */
	public static Map<String, Object> queryDataByPrimaryValue(List<PDatabaseColumnConfig> columnConfigList,
			String tableName, List<String> valueList, String primaryIndex) {
		Map<String, Object> params = new HashMap<String, Object>();
		Map<String, Object> resultcols = pickupTableCols(columnConfigList);
		params.putAll(resultcols);
		params.put("tableName", tableName);
		params.put("primaryValue", valueList.get(0));
		if (StringUtils.isNotBlank(primaryIndex)) {
			// 当没有主键时
			params.put("anotherPrimaryKey",
					columnConfigList.get(Integer.valueOf(primaryIndex.split(",")[1])).getColumnName());
			params.put("anotherPrimaryValue", valueList.get(Integer.valueOf(primaryIndex.split(",")[1])));
			params.put("anotherPrimaryKeyType",
					columnConfigList.get(Integer.valueOf(primaryIndex.split(",")[1])).getColumnType());
		}
		appendSql(params);
		Map<String, Object> map = databasesPushDAO.findTableDataByPrimaryValue(params);
		return map;
	}

	/**
	 * 拼接sql
	 * 
	 * @param params
	 */
	public static void appendSql(Map<String, Object> params) {
		StringBuilder sb = new StringBuilder("and ");
		String primaryValue = params.get("primaryValue").toString();
		String primaryKeyType = params.get("primaryKeyType").toString();
		sb.append(params.get("primaryKey")).append(" = ").append(handValueByType(primaryValue, primaryKeyType));
		if (params.get("anotherPrimaryKey") != null) {
			String anotherPrimaryValue = params.get("anotherPrimaryValue").toString();
			String anotherPrimaryKeyType = params.get("anotherPrimaryKeyType").toString();
			sb.append(" and " + params.get("anotherPrimaryKey") + " = "
					+ handValueByType(anotherPrimaryValue, anotherPrimaryKeyType));
		}
		params.put("sql", sb);
	}

	public static String handValueByType(String primaryValue, String primaryKeyType) {
		if (StringUtils.equals(primaryKeyType, "varchar")) {
			primaryValue = "\'" + primaryValue + "\'";
		}
		return primaryValue;
	}

	/**
	 * 处理字段对应的值做处理
	 * 
	 * @param databaseColumnConfig
	 * @param value
	 * @return
	 */
	private static String handlerValue(PDatabaseColumnConfig databaseColumnConfig, String value) {
		// 如果为空,则默认赋值空串避免后面报错
		if (value == null) {
			value = "";
		} else {
			if (StringUtils.equals("varchar", databaseColumnConfig.getColumnType())) {
				// 找到最后一个补位的下标
				int subIndex = 0;
				for (int j = value.length() - 1; j >= 0; j--) {
					if (!String.valueOf(value.charAt(j)).equals(" ")) {
						subIndex = j + 1;
						break;
					}
				}
				// 截取掉补位部分
				value = value.substring(0, subIndex);
			} else {
				value = value.trim();
			}
		}
		if (StringUtils.equals("", value) || StringUtils.isBlank(value)) {
			value = "null";
		}
		return value;
	}

	public static void writeText(File file, String content) {
		try {
			// 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "GB18030"));
			if (content.contains("\n")) {
				String[] contents = content.split("\\n");
				for (String str : contents) {
					bw.write(str);
					bw.write("\r\n");
				}
			} else {
				bw.write(content);
				bw.write("\r\n");
			}
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	public static void copyTempFileToProduceFile() throws Exception {
		logger.info("------------------对比完成，开始复制临时文件到produceFile----------------------");
		for (String tempFilePath : tempFileList) {
			CCRDFile.copyFile(produceDirPath + File.separator + tempFilePath,
					produceDirPath + File.separator + produceFile.getName());
		}
	}

	public void run() {
		List<String> sublist;
		while (count - havedCount > 0) {// 线程会循环执行，直到所有数据都处理完
			synchronized (this) {// 在分包时需要线程同步，避免线程间处理重复的数据
				sublist = fileList.subList(perCount * (flag - 1),
						count - havedCount > perCount ? perCount * flag : perCount * (flag - 1) + (count - havedCount));
				flag = flag + 1;
				havedCount += sublist.size();
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			String tempFilePath = produceDirPath + File.separator + Thread.currentThread().getName() + "_" + flag + "_"
					+ sdf.format(new Date()) + ".txt";
			File tempFile = new File(tempFilePath);
			// CCRDFile.createFile(tempFilePath);
			for (int j = 0; j < sublist.size(); j++) {
				logger.info("加载文件:" + sublist.get(j));
				try {
					loadTxtCompareToDatabase(sublist.get(j), tempFile);
					synchronized (this) {
						fileNo++;
					}
				} catch (Exception e) {
					logger.error(sublist.get(j) + "对比出现异常");
				}
			}
			if (tempFile.exists()) {
				tempFileList.add(tempFile.getName());
			}
			if (count - fileNo == 0) {
				String tempStr = "\n共对比完成:" + fileNo + "个txt文件";
				logger.info(tempStr);
				writeText(tempFile, tempStr);
				try {
					copyTempFileToProduceFile();
					logger.info("复制成功");
					deleteTempFiles();
					logger.info("删除临时文件完成，对比结束");
				} catch (Exception e) {
					logger.error("复制临时文件到produceFile异常或删除临时文件异常");
				}
			}

		}
	}

	public static void deleteTempFiles() {
		logger.info("------------------开始删除临时文件----------------------");
		for (String tempFilePath : tempFileList) {
			CCRDFile.deleteFile(produceDirPath + File.separator + tempFilePath);
		}
	}
}
