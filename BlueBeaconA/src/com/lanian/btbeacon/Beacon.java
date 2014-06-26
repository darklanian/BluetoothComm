package com.lanian.btbeacon;

public class Beacon {
	private String address;
	private String name;
	
	public Beacon(String address) {
		this.address = address;
	}
	
	public Beacon(String address, String name) {
		this(address);
		setName(name);
	}
	
	public String getName() {
		if (name == null || name.isEmpty()) {
			return address;
		}

		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getAddress() { return address; }
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return getName();
	}

}
