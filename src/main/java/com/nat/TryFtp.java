package com.nat;

import client.FtpClient;
import client.FtpDownloader;

public class TryFtp {

	private static UserData userData;

	private static String fileCmd;
	private static FtpClient ftp;

	public static void main(String[] args) {
		ftp = new FtpClient();
		System.getProperty("user.dir");
		while (true) {
			userData = UserDataProvider.getDataForConnection();

			ftp.connectAndShowFiles(userData.getHost(), userData.getPort(),
					userData.getLogin(), userData.getPassword());
			break;
		}
		// get the command from user
		while (true) {
			fileCmd = UserDataProvider.getCommandFromUser();
			if (null == fileCmd) {
				continue;
			}
			ftp.performCommand(fileCmd);
		}
	}

}
