package com.cache;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;


public class CacheExecute {
	private static final Map<String,Object> cache = new ConcurrentReferenceHashMap<>();
	public static void main(String...strings){
	
		cache.put("001", "002");
		String a=(String)cache.get("001");
		//System.out.println(EnumTest.already_Prt.getValue());
		//System.out.println(EnumTest.already_Prt.getValueName());
		
		//getCodeName(EnumTest.class,"01");
		System.out.println(getCodeName(EnumTest.class,"03"));
		/*EnumTest[] tEnum=EnumTest.values();
		for(EnumTest tEnumTest :tEnum){
			if(tEnumTest.getValue().equals("01")){
				System.out.println(tEnumTest.getValueName());
	
			}
		}*/
	}
	
	
	   public static String getCodeName(Class<? extends EnumInfo> clazz,String value){
		  for(EnumInfo enumInfo:clazz.getEnumConstants()){
			  if(enumInfo.getValue().equals(value)){
				  return enumInfo.getValueName();
			  }
		  }
		  return null;
	   }
}
