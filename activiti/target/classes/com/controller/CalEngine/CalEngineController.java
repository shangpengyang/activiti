package com.controller.CalEngine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import javax.annotation.sql.DataSourceDefinition;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dao.mapper.CalElementConfigMapper;
import com.dao.mapper.QueryElementMapper;
import com.dao.model.CalElementConfig;
import com.dao.model.TableConfi;
@Aspect
@RestController
public class CalEngineController {
	
	@Autowired
	public CalElementConfigMapper calElementConfigMapper;
	@Autowired
	public QueryElementMapper queryElementMapper;

	@RequestMapping("/test")
	public Map<String, String> calEngine() {
		// 调用方传入Map
		Map<String, String> tMap = new HashMap();
		tMap.put("contno", "'001'");
		tMap.put("polno", "'002'");
		tMap.put("edoracceptno", "'003'");

		// 获取计算要素对应表数据
		List<String> tList = new ArrayList<>();
		tList.add("riskcode");
		tList.add("prem");
		tList.add("amnt");
		tList.add("addPrem");

		//读取计算要素配置表
		List<CalElementConfig> tCalList = queryElementMapper.queryElementList(tList);

		// tCalList.stream().forEach(a-> a.getCaltype().equals("01"));
		// 从计算要素配置表获取计算要素集合
		List<CalElementConfig> tCopyGetList = tCalList.stream().filter(a -> a.getCaltype().equals("01"))
				.collect(Collectors.toList());
		// 需从产品获取计算要素集合
		List<CalElementConfig> tProductGetList = tCalList.stream().filter(a -> a.getCaltype().equals("02"))
				.collect(Collectors.toList());
		//需要从前端获取的要素集合
		List<CalElementConfig> tInputGetList = tCalList.stream().filter(a -> a.getCaltype().equals("03"))
				.collect(Collectors.toList());

		// 获取计算要素依赖表清单
		List<String> tTableList = tCopyGetList.stream().map(a -> a.getTableSource()).collect(Collectors.toList());

		// 从计算要素表关系配置中获取表与表之间关系
		List<TableConfi> tTableRelaList = queryElementMapper.queryTableRelaList(tTableList);
		// 获取主表对象
		TableConfi mainTable = tTableRelaList.stream().filter(a -> a.getMaintable().equals(a.getRelatable())).findAny()
				.get();
		//获取关联表对象
		List<TableConfi> tRelaTable = tTableRelaList.stream().filter(a -> !a.getMaintable().equals(a.getRelatable()))
				.collect(Collectors.toList());

		// 拼接查询字段
		String propertySql = tCopyGetList.stream().map(a -> String.join(".", a.getTableSource(), a.getTableColumn()))
				.collect(Collectors.joining(","));
		// 拼接关联关系
		String connectSql = getConnectSql(mainTable, tRelaTable, tMap);

		// 拼接where查询条件
		String conditionSql = getconditionSql(mainTable, tMap);

		// 获取计算要素赋值结果
		Map<String, String> calPropertyMap = queryElementMapper.getPropertyValue(propertySql, connectSql, conditionSql);
		return calPropertyMap;
	}

	// 拼接关联关系
	public String getConnectSql(TableConfi mainTable, List<TableConfi> tList, Map<String, String> tMap) {
		String connectSql = new StringJoiner(" ")
				.add(mainTable.getMaintable()).add(
						tList.stream()
								.map(a -> String.join(" and ",
										String.join(" on ", String
												.join(" ", a.getConnecttype(),
														a.getRelatable()),
												a.getConnectcondition()),
										Arrays.stream(a.getQuerycontidion().split(","))
												.filter(a2 -> tMap.get(a2) != null).collect(Collectors.toList())
												.stream()
												.map(a3 -> String.join("=", String.join(".", a.getRelatable(), a3),
														tMap.get(a3)))
												.collect(Collectors.joining())))
								.collect(Collectors.joining())

				).toString();

		return connectSql;

	}

	// 拼接where查询条件
	public String getconditionSql(TableConfi mainTable, Map<String, String> tMap) {
		String conditionSql = Arrays.stream(mainTable.getQuerycontidion().split(",")).filter(a -> tMap.get(a) != null)
				.collect(Collectors.toList()).stream()
				.map(a1 -> String.join("=", String.join(".", mainTable.getMaintable(), a1), tMap.get(a1)))
				.collect(Collectors.joining(" and "));

		return conditionSql;

	}

}
