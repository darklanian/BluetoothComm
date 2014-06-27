package com.lanian.btbeacon;

public class Beacon {
	private String address;
	private String alias;
	
	public Beacon(String address) {
		this.address = address;
	}
	
	public Beacon(String address, String alias) {
		this(address);
		setAlias(alias);
	}
	
	public String getDisplayName() {
		if (alias == null || alias.isEmpty()) {
			return address;
		}

		return alias;
	}
	
	public void setAlias(String alias) {
		this.alias = alias;
	}
	
	public String getAlias() { return alias; }
	
	public String getAddress() { return address; }
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return getDisplayName();
	}

}
