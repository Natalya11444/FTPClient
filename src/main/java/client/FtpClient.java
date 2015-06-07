package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nat.UserData;

public class FtpClient {
	public static final boolean DEBUG_MODE_ON = true;
	private Socket commandSocket;
	private PrintWriter commandWriter;
	private BufferedReader commandReader;
	private String lastLine;
	private static final String CHANGE_DIR_CMD = "CWD";
	private static final String USER_CMD = "USER";
	private static final String PASW_CMD = "PASS";
	private static final String LIST_CMD = "LIST";
	private static final String PASSIVE_MODE_CMD = "PASV";
	private static final String PARENT_DIR_CMD = "CDUP";
	private static final String SPACE = " ";

	/**
	 * Connect to the server
	 *
	 * @param host
	 *            Host
	 * @param port
	 *            Port number
	 * @param user
	 *            Login name
	 * @param password
	 *            Password
	 * @return True if connected successful
	 */
	public boolean connect(String host, int port, String user, String password)
			throws Exception {
		commandSocket = new Socket(host, port);
		commandWriter = new PrintWriter(commandSocket.getOutputStream(), true);
		commandReader = new BufferedReader(new InputStreamReader(
				commandSocket.getInputStream()));

		nextResponse();

		send(USER_CMD + SPACE + user);
		nextResponse();

		send(PASW_CMD + SPACE + password);
		String response = nextResponse();
		if (!lastLine.startsWith("230 ")) {
			if (DEBUG_MODE_ON) {
				System.out.println("Login was not successful. Response: "
						+ response);
			}
			return false;
		}
		return true;
	}

	private void changeDir(String dir) throws Exception {
		send(CHANGE_DIR_CMD + SPACE + dir);
		nextResponse();
	}

	private void list() throws Exception {
		System.out.println("List:\n" + getResponsePasv(LIST_CMD));
	}

	private String getResponsePasv(String cmd) throws Exception {
		Socket socket = getNewSocket(cmd);
		InputStreamReader reader = new InputStreamReader(
				socket.getInputStream());
		StringBuilder response = new StringBuilder();
		int c;
		while ((c = reader.read()) != -1) {
			response.append((char) c);
		}
		socket.close();

		// ------------------------------
		// for LIST and NLST
		// FTP: 150 Here comes the directory listing.
		// FTP: 226 Directory send OK.
		// ---
		// for RETR
		// FTP: 150 Opening BINARY mode data connection for README (178 bytes).
		// FTP: 226 Transfer complete.
		nextResponse();
		nextResponse();
		// ------------------------------

		return response.toString();
	}

	public Socket getNewSocket(String cmd) throws Exception {
		send(PASSIVE_MODE_CMD);
		String response = nextResponse();
		if (DEBUG_MODE_ON) {
			System.out.println("response in getNewSocket: " + response);
		}
		// FTP: 227 Entered Passive Mode (63,245,215,46,199,194)
		// get ip and port for new opened socket
		Pattern pattern = Pattern
				.compile("\\((\\d+),(\\d+),(\\d+),(\\d+),(\\d+),(\\d+)\\)");
		Matcher m = pattern.matcher(response);
		m.find();
		String dataIp = String.format("%s.%s.%s.%s", m.group(1), m.group(2),
				m.group(3), m.group(4));
		int dataPort = Integer.parseInt(m.group(5)) * 256
				+ Integer.parseInt(m.group(6));

		send(cmd);

		Socket socket = new Socket(dataIp, dataPort);
		return socket;
	}

	private void send(String command) throws Exception {
		if (DEBUG_MODE_ON) {
			System.out.println("Send now: " + command);
		}
		commandWriter.write(command + "\r\n");
		commandWriter.flush();
	}

	public String nextLine() throws Exception {
		String line = commandReader.readLine();
		lastLine = line;
		if (DEBUG_MODE_ON) {
			System.out.println("FTP: " + line);
		}
		return line;
	}

	private String nextResponse() throws Exception {
		StringBuilder response = new StringBuilder();
		while (true) {
			String line = nextLine();
			if (null == line) {
				if (DEBUG_MODE_ON) {
					System.out.println("line is null  " + line);
				}
				return null;
			}

			response.append(line);
			// end of the response
			if (line.matches("^\\d{3} .*")) {
				break;
			}
			response.append("\r\n");
		}

		return response.toString();
	}

	public void parentDir() throws Exception {
		send(PARENT_DIR_CMD);
		if (DEBUG_MODE_ON) {
			System.out.println("go to parent dir");
		}
		nextResponse();

	}

	public void disconnect() throws Exception {
		send("QUIT");
		if (DEBUG_MODE_ON) {
			System.out.println("disconnect");
		}
		nextResponse();
	}

	public boolean isFile(String file) throws Exception {

		/*
		 * LIST:
		 * 
		 * -rw-r--r-- 1 ftp ftp 178 Apr 25 2014 README
		 * 
		 * -rw-r--r-- 1 ftp ftp 384 Apr 25 2014 index.html
		 * 
		 * drwxr-xr-x 41 ftp ftp 4096 May 21 16:20 pub
		 */

		if (DEBUG_MODE_ON) {
			System.out.println("check is a file");
		}
		String response = getResponsePasv(LIST_CMD);
		if (!response.contains(file)) {
			throw new Exception("Folder doesn't contain file  " + file);
		}
		String[] lines = response.split("\n");
		boolean isFile = false;
		for (String line : lines) {
			line = line.trim();
			if (line.endsWith(file)) {
				if (line.startsWith("-")) {
					isFile = true;
				}

				break;
			}
		}
		if (DEBUG_MODE_ON) {
			System.out.println("check is a file response:\n" + response);
		}

		return isFile;
	}

	public boolean goToDirAndShowFiles(String fileCmd) {
		try {
			// go to the directory
			changeDir(fileCmd);
		} catch (Exception e1) {
			System.out.println("Can't go to the directory " + fileCmd);
			return false;
		}
		if (DEBUG_MODE_ON) {
			System.out.println("changed dir:");
		}
		try {
			// show files in entered directory
			list();
		} catch (Exception e) {
			System.out.println("Can't show all files.");
			return false;
		}
		return true;
	}

	public boolean goToParentDirAndShowFiles() {
		try {
			parentDir();
		} catch (Exception e1) {
			System.out.println("Can't go to the parent directory");
			return false;
		}
		if (DEBUG_MODE_ON) {
			System.out.println("parent dir:");
		}
		try {
			// show all files in parent directory
			list();
		} catch (Exception e1) {
			System.out.println("Can't show all files.");
			return false;
		}
		return true;
	}

	public void disconnectAndExit() {
		try {
			disconnect();
		} catch (Exception e1) {
			System.out.println("Some error while disconnecting");
		}
		System.exit(0);
	}

	public boolean connectAndShowFiles(String host, int port, String login,
			String password) {
		try {
			if (!connect(host, port, login, password)) {
				System.out
						.println("Connecting was not successful. Try again please.");

			}
		} catch (Exception e) {
			System.out
					.println("Connecting was not successful. Try again please.");
			e.printStackTrace();
			return false;
		}

		if (DEBUG_MODE_ON) {
			System.out.println("Dirs and files:");
		}
		try {
			// show all files and directories
			list();
		} catch (Exception e) {
			System.out.println("Can't show all files. Try again please.");
			return false;
		}
		return true;
	}

	public boolean performCommand(String fileCmd) {
		// check the command
		switch (fileCmd) {
		case "-1":
			disconnectAndExit();
			break;
		case "..":
			goToParentDirAndShowFiles();
			break;
		default:
			// check is entered name a file or a directory
			boolean isFile;
			try {
				isFile = isFile(fileCmd);
			} catch (Exception e) {
				System.out
						.println("The name is not correct. Try again please ");
				return false;
			}
			if (isFile) {
				new FtpDownloader().download(fileCmd, UserData.getUserData()
						.getFolder(), this);
			} else {
				if (DEBUG_MODE_ON) {
					System.out.println(fileCmd + " is not a file ");
				}
				goToDirAndShowFiles(fileCmd);
			}
			break;
		}
		return true;
	}
}
