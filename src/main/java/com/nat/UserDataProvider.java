package com.nat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class UserDataProvider {

	private static final String EMPTY_SPACE = "";

	private static UserData userData = UserData.getUserData();

	public static String getUserAnswerForQuestion(String question) {
		System.out.println(question);
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			return br.readLine().trim();
		} catch (IOException e) {
			return EMPTY_SPACE;
		}
	}

	public static String getCommandFromUser() {
		String fileCmd = getUserAnswerForQuestion("Enter a file name to download or folder name to open, \"..\" for "
				+ "returning to the parent folder, \"-1\" to exit");
		if (fileCmd.equals(EMPTY_SPACE)) {
			System.out.println("You didn't enter any command");
			return EMPTY_SPACE;
		}
		return fileCmd;
	}

	public static UserData getDataForConnection() {
		String hostCmd = UserDataProvider
				.getUserAnswerForQuestion("Please enter a host name (default "
						+ userData.getHost() + ")");
		if (!hostCmd.equals(EMPTY_SPACE)) {
			userData.setHost(hostCmd);
		}
		String loginCmd = UserDataProvider
				.getUserAnswerForQuestion("Please enter a login (default "
						+ userData.getLogin() + ")");
		if (!loginCmd.equals(EMPTY_SPACE)) {
			userData.setLogin(loginCmd);
		}
		String passwordCmd = UserDataProvider
				.getUserAnswerForQuestion("Please enter a password (default "
						+ userData.getPassword() + ")");
		if (!passwordCmd.equals(EMPTY_SPACE)) {
			userData.setPassword(passwordCmd);
		}
		String portCmd = UserDataProvider
				.getUserAnswerForQuestion("Please enter a port (default "
						+ userData.getPort() + ")");
		if (!portCmd.equals(EMPTY_SPACE)) {
			try {
				userData.setPort(Integer.parseInt(portCmd));
			} catch (NumberFormatException e) {
			}
		}
		return userData;
	}
}
