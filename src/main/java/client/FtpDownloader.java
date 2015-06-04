package client;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class FtpDownloader {
	
	public Boolean download(String file, String dest, FtpClient ftpClient) {
		try {
			if (FtpClient.DEBUG_MODE_ON) {
				System.out.println("stert download " + file + " file to dir: "
						+ dest);
			}

			String response;

			Socket sock = ftpClient.getNewSocket("RETR " + file);
			response = ftpClient.nextLine();
			if (FtpClient.DEBUG_MODE_ON) {
				System.out.println("resp while downl " + response);
			}
			if (!response.startsWith("150 ")) {
				throw new IOException(
						"FtpClient was not allowed to download the file: "
								+ response);
			}
			OutputStream os = new FileOutputStream(new File(dest + "/" + file));

			BufferedInputStream is = new BufferedInputStream(
					sock.getInputStream());
			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = is.read(buffer)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
			os.close();
			sock.close();
			if (FtpClient.DEBUG_MODE_ON) {
				System.out.println("RETR " + file + " : File received");
			}
			response = ftpClient.nextLine();
			return response.startsWith("226 ");
		} catch (Exception e) {
			System.out.println("Can't download the file " + file
					+ " to the directory " + dest);
			return false;
		}

	}

}
