package com.dao.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.dao.model.CalElementConfig;
import com.dao.model.TableConfi;

public interface QueryElementMapper {
 //获取计算要素配置集合
 public	List<CalElementConfig> queryElementList(@Param("tList")List tList);
 //获取表关系配置集合
 public List<TableConfi> queryTableRelaList(@Param("tList")List tList);
 //获取抄单表计算要素返回结果
 public Map<String,String> getPropertyValue(@Param("propertySql")String propertySql,@Param("connectSql")String connectSql,@Param("conditionSql")String conditionSql);

// public Map<String,String> getPropertyValue(@Param("propertySql")String getSql);

}
