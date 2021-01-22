package com.dao.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.dao.model.PrintModelConfig;
import com.dao.model.PrintModelConfigItem;
import com.dao.model.PrintTableConfig;

public interface QueryPrintMapper {
	//获取通知书批单模板
	public List<PrintModelConfig> queryPrintModel(@Param("printType")String printType);
    //获取通知书包含要素信息
	public List<PrintModelConfigItem> queryPrintModelItem(@Param("printType")String printType);
	//获取计算要素依赖表清单 
	public List<PrintTableConfig> queryTableRelaList(@Param("tList") List<String> tList);
	//获取抄单表计算要素返回结果
	 public List<Map<String,String>> getPropertyValue(@Param("propertySql")String propertySql,@Param("connectSql")String connectSql,@Param("conditionSql")String conditionSql);

}
