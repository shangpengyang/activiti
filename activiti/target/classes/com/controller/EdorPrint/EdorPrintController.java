package com.controller.EdorPrint;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dao.mapper.QueryPrintMapper;
import com.dao.model.PrintModelConfig;
import com.dao.model.PrintModelConfigItem;
import com.dao.model.PrintTableConfig;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

@RestController
public class EdorPrintController {
	@Autowired
	public 	QueryPrintMapper queryPrintMapper;

	public Map<String, String> xmlMap = new HashMap();

	public static final XmlMapper xmlMapper;
	static {
		xmlMapper = new XmlMapper();
		// 字段为null不转换
		xmlMapper.setSerializationInclusion(Include.NON_NULL);

	}

	@RequestMapping("/Edor/Print")
	public String edorPrint() {
		Map<String, String> tMap = new HashMap<String, String>();
		tMap.put("contno", "'001'");
		// tMap.put("polno", "'002'");
		tMap.put("edoracceptno", "'003'");
		String printType = "POS001";
		// 获取通知书配置模板
		List<PrintModelConfig> tprintModel = queryPrintMapper.queryPrintModel(printType);
		// 获取模板依赖要素列表
		List<PrintModelConfigItem> tprintModelItem = queryPrintMapper.queryPrintModelItem(printType);
		// 获取根节点元素
		List<PrintModelConfigItem> tRootItem = tprintModelItem.stream().filter(a -> a.getParentnode().equals("Root"))
				.collect(Collectors.toList());
		// 递归解析节点元素并拼装
		String xmlList = analysisModelConfi(tRootItem, tprintModelItem, tMap);

		return xmlList;
	}

	// 递归解析节点元素
	public String analysisModelConfi(List<PrintModelConfigItem> tRootItem, List<PrintModelConfigItem> tprintModelItem,
			Map<String, String> tMap) {
		// 获取根节点元素中的列表元素
		List<PrintModelConfigItem> listItem = tRootItem.stream().filter(a -> a.getItemtype().equals("04"))
				.collect(Collectors.toList());

		if (listItem.size() > 0) {
			for (PrintModelConfigItem printConfigItem : listItem) {
				String nextListXML = analysisModelConfi(
						tprintModelItem.stream().filter(a1 -> a1.getParentnode().equals(printConfigItem.getItemcode()))
								.collect(Collectors.toList()),
						tprintModelItem, tMap);
				//记录每一层级拼装的xml列表
				xmlMap.put(printConfigItem.getParentnode(), nextListXML);

			}
		}

		// 获取节点中的非列表元素
		List<PrintModelConfigItem> baseItem = tRootItem.stream().filter(a -> !a.getItemtype().equals("04"))
				.collect(Collectors.toList());
		/*
		 * //获取节点中的非列表元素（抄单表元素旧） List<PrintModelConfigItem>
		 * oldCopyItem=tRootItem.stream().filter(a->
		 * a.getItemtype().equals("02")).collect(Collectors.toList());
		 * //获取节点中的非列表元素（非抄单表） List<PrintModelConfigItem>
		 * bpmItem=tRootItem.stream().filter(a->
		 * a.getItemtype().equals("03")).collect(Collectors.toList());
		 * //获取节点中的非列表元素（产品获取）
		 */
		// 拼接查询字段
		String propertySql = baseItem.stream().map(a -> String.join(".", a.getTablename(), a.getTableColumn()))
				.collect(Collectors.joining(","));
		// 获取计算要素依赖表清单
		List<String> tTableList = baseItem.stream()
				.map(a -> new StringJoiner("','", "('", "')").add(a.getTablename()).add(a.getItemtype()).toString())
				.collect(Collectors.toList());
		// 从计算要素表关系配置中获取表与表之间关系
		List<PrintTableConfig> tTableRelaList = queryPrintMapper.queryTableRelaList(tTableList);
		// 获取主表对象
		PrintTableConfig mainTable = tTableRelaList.stream().filter(a -> a.getMaintable().equals(a.getRelatable()))
				.findAny().get();
		// 获取关联表集合
		List<PrintTableConfig> tRelaTable = tTableRelaList.stream()
				.filter(a -> !a.getMaintable().equals(a.getRelatable())).collect(Collectors.toList());
		// 拼接关联关系
		String connectSql = getConnectSql(mainTable, tRelaTable, tMap);
		// 拼接where查询条件
		String conditionSql = getconditionSql(mainTable, tMap);
		// 获取计算要素赋值结果
		List<Map<String, String>> calPropertyMap = queryPrintMapper.getPropertyValue(propertySql, connectSql,
				conditionSql);
		System.out.println(calPropertyMap.getClass().getSimpleName());
		String xml = null;
		try {

			xml = xmlMapper.writeValueAsString(calPropertyMap);
			//根节点列表拼装
			if (baseItem.get(1).getParentnode().equals("Root")) {
				xml = StringUtils.replace(xml, "item", baseItem.get(1).getListname());
				xml = StringUtils.replace(xml, "<ArrayList>", "");
				xml = StringUtils.replace(xml, "</ArrayList>", "");
				// 嵌套下一层级数据，根节点特殊处理，适配根节点名称不相同报文
				return StringUtils.replace(xml, "</" + baseItem.get(1).getListname() + ">",
						xmlMap.get(baseItem.get(1).getParentnode()) + "</" + baseItem.get(1).getListname() + ">");
			} else {
			//非根节点列表拼装
				xml = StringUtils.replace(xml, "item", baseItem.get(1).getParentnode());
				xml = StringUtils.replace(xml, "ArrayList", baseItem.get(1).getListname());

			}
			if (xmlMap.get(baseItem.get(1).getParentnode()) != null) {
				// 嵌套下一层级数据
				return StringUtils.replace(xml, "</" + baseItem.get(1).getParentnode() + ">",
						xmlMap.get(baseItem.get(1).getParentnode()) + "</" + baseItem.get(1).getParentnode() + ">");
			}
			;
			System.out.println(xml);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return xml;

	}

	// 拼接关联关系
	public String getConnectSql(PrintTableConfig mainTable, List<PrintTableConfig> tList, Map<String, String> tMap) {
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
	public String getconditionSql(PrintTableConfig mainTable, Map<String, String> tMap) {
		String conditionSql = Arrays.stream(mainTable.getQuerycontidion().split(",")).filter(a -> tMap.get(a) != null)
				.collect(Collectors.toList()).stream()
				.map(a1 -> String.join("=", String.join(".", mainTable.getMaintable(), a1), tMap.get(a1)))
				.collect(Collectors.joining(" and "));

		return conditionSql;

	}
}
