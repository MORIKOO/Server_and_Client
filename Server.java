import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	// メインメソッド
	public static void main(String[] args) {
		Server se = new Server();
		se.serverCommand();
	}

	public void serverCommand() {
		try {
			byte buf[] = new byte[256];
			System.out.println("Port No.: ");
			int len = System.in.read(buf); 
			String temp = new String(buf, 0, len);
			int port = Integer.parseInt(temp.trim()); 
			ServerSocket aServer = new ServerSocket(port); 
			aServer.setSoTimeout(0); 
			System.out.println("File Sever started.");

			while(true) {
				Socket aClient = aServer.accept();
				System.out.println("Client is connected.");
				InputStream inStream = aClient.getInputStream(); 
				OutputStream outStream = aClient.getOutputStream(); 
				PrintStream prtStream = new PrintStream(outStream);
				String serverCurrentDirectory = ".";
				String serverTempDirectory = ".";

				while(true) {
					len = inStream.read(buf); 
					String command = new String(buf, 0, len); 
					String[] splCommand = command.split(" ");
					if (splCommand[0].trim().equals("quit")) {
						System.out.println("Client is disconnected.");
						aClient.close();
						break;
					}
					else if (splCommand[0].trim().equals("ls")) {
						prtStream.println("ls begin");
						test(serverCurrentDirectory, outStream);
						prtStream.println("ls end");
					}
					else if (splCommand[0].trim().equals("cat")) {
						prtStream.println("cat begin");
						String readst = readFile(splCommand[1].trim());
						prtStream.println(readst);
						prtStream.println("cat end");
					}
					else if (splCommand[0].trim().equals("cd")) {
						serverTempDirectory = cd(splCommand[1].trim(), serverCurrentDirectory.trim());
						File tempDir = new File(serverTempDirectory.trim());
						if (!tempDir.isDirectory()) {
							prtStream.println("ng");
						} else {
							prtStream.println("ok");
							serverCurrentDirectory = serverTempDirectory;
						}
					}
					else if (splCommand[0].trim().equals("get")) {
						String getreact = fileCheck(splCommand[1].trim());
						if (getreact.trim().equals("get begin")){
							prtStream.println("get begin");
							readandsendFile(splCommand[1].trim(), outStream);
							prtStream.println("get end");
						} else if(getreact.trim().equals("ng")) {
							prtStream.println("ng");
						}
					}
					else if (splCommand[0].trim().equals("put")) {

						receiveandwriteFile(splCommand[1].trim(), inStream); 
					}
				}

			}
		} catch (IOException ioErr) {
			System.out.println(ioErr.getLocalizedMessage());
		} catch (SecurityException secErr) {
			System.out.println(secErr.getLocalizedMessage());
		}
	}

	public void test(String directoryName, OutputStream os) {
		File aDir = new File(directoryName.trim());

		if (!aDir.isDirectory()) {
			System.out.println("It is not a direcotory.");
			System.exit(0); 
		}

		listup(aDir, os);
	}

	void listup(File aDir, OutputStream os) {
		PrintStream prtStream = new PrintStream(os); 
		String contents[] = aDir.list();
		int index; 
		int numOfDirs = 0; 
		int numOfFiles = 0; 
		for (index = 0; index < contents.length; index++) {

			File aFileOrDir = new File(aDir, contents[index]);
			if (aFileOrDir.isDirectory()) {
				prtStream.println(aFileOrDir + "/"); 
				numOfDirs++; 
			} else {
				prtStream.println(aFileOrDir);
				numOfFiles++; 
			}
		}
		prtStream.println("File: " + String.valueOf(numOfFiles));
		prtStream.println("Directory: " + String.valueOf(numOfDirs));
	}

	String readFile(String filename) {
		String st = ""; 
		try {
			int size = 20; 
			byte buf[] = new byte[size]; 
			int rsize; 
			FileInputStream fis = new FileInputStream(filename);
			do {
				rsize = fis.read(buf, 0, size); 
				if (rsize <= 0 ) break; 
				st = st + new String(buf, 0, rsize); 
			} while (rsize == size);
			fis.close();
		} catch (IOException e) {
			System.out.println("Read error:" + e.getLocalizedMessage());
			st = "ng";
			return st;
		}
		return st;
	}

	String cd(String toDirectoryName, String fromDirectoryName) {
		toDirectoryName = fromDirectoryName + "/" + toDirectoryName;
		String dirPath = toDirectoryName;
		String absolutePath = "";
		String absolutePathNew = "";
		File aDir = null;
		while (true) {
			aDir = new File(dirPath);
			try {
				absolutePathNew = aDir.getCanonicalPath();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (absolutePathNew.equals(absolutePath)) {
				break;
			}
			absolutePath = absolutePathNew;
			break;
		}
		return absolutePathNew;
	}
	String fileCheck(String filename) {

		File aFile = new File(filename.trim());

		if (!aFile.isFile()) {
			return("ng");
		} else {
			return("get begin");
		}

	}
	void readandsendFile(String filename, OutputStream os) {
		try {
			int size = 20; 
			byte buf[] = new byte[size]; 
			int rsize; 
			FileInputStream fis = new FileInputStream(filename.trim());
			do {
				rsize = fis.read(buf, 0, size); 
				if (rsize <= 0) break; 
				os.write(buf, 0, rsize); 
			} while (rsize == size); 
			os.flush();  
			fis.close();
		} catch (IOException e) {
			System.out.println("Error:" + e.getLocalizedMessage());
		}
	}

	void receiveandwriteFile(String filename, InputStream is) {
		try {
			int size = 20; 
			byte buf[] = new byte[size]; 
			int rsize;
			String stput;
			FileOutputStream fos = new FileOutputStream(filename.trim() +".bak"); 
			do {
				rsize = is.read(buf, 0, size); 
				if (rsize <= 0  || is.available() == 0  ) break; 
				fos.write(buf, 0, rsize);
				stput = new String(buf, 0, rsize);
			} while (rsize == size || stput.trim().indexOf("put end") < 0);
			fos.flush();
			fos.close(); 
		} catch (IOException e) {
			System.out.println("Error:" + e.getLocalizedMessage());
		}
	}
}