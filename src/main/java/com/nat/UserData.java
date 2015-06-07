package com.nat;

import client.FtpClient;

public class UserData {

	private String host = "ftp.mozilla.org";
	private String login = "anonymous";
	private String password = "anonymous";
	private int port = 21;
	private String folder = "C:\\Users\\Public\\Documents";
	private static UserData userData;

	private UserData() {
	}

	public static UserData getUserData() {
		if (null == userData) {
			userData = new UserData();
		}
		return userData;
	}

	public String getFolder() {
		return folder;
	}

	public String getHost() {
		return host;
	}

	public String getLogin() {
		return login;
	}

	public String getPassword() {
		return password;
	}

	public int getPort() {
		return port;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
