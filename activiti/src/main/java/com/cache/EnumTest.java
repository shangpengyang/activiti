package com.cache;

public enum EnumTest implements EnumInfo<String>{
	already_Prt("01","已打印"),	already_Prt1("02","没有打印");
	

	private final String value;
	private final String valueName;
	
	public String getValue() {
		return value;
	}
	public String getValueName() {
		return this.valueName;
	}


	private EnumTest(String value,String valueName){
		this.value=value;
		this.valueName=valueName;
	}
    public boolean match(String value){
    	return null!=value&&value.equals(this.value);
    }
    public boolean oneOf(String value,EnumTest...enumTests){
    	if(value==null||enumTests==null){
    		return false;
    	}
    	for(EnumTest tEnumTest : enumTests){
    		if(value.equals(tEnumTest.getValue())){
    			return true;
    		}
    	}
    	return false;
    }
}
