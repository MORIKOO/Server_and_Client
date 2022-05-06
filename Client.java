import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;

public class Client {
	// メインメソッド
	public static void main(String[] args) {
		Client cl = new Client();
		cl.clientCommand();
	}

	public void clientCommand() {
		try {
			int len;
			byte buf[] = new byte[256];
			while(true) {
				System.out.println("Server Host: ");
				len = System.in.read(buf); 
				String host = new String(buf, 0, len); 
				InetAddress anAddress = InetAddress.getByName(host.trim()); 
				System.out.println("Port No.: ");
				len = System.in.read(buf); 
				String temp = new String(buf, 0, len); 
				int port = Integer.parseInt(temp.trim());
				Socket aSocket = new Socket(anAddress, port);
				OutputStream outStream = aSocket.getOutputStream(); 
				InputStream inStream = aSocket.getInputStream(); 
				String currentDirectory = ".";
				String tempDirectory = ".";

				while(true) {
					System.out.print("Command> ");
					len = System.in.read(buf);
					String commandName = new String(buf, 0, len);
					String[] splCommandName = commandName.split(" ");

					if (aSocket.isConnected()) { 
						if (splCommandName[0].trim().equals("quit")) {
							outStream.write(buf, 0, len);
							aSocket.close();
							System.exit(0);
						}
						else if(splCommandName[0].trim().equals("lls")){
							test(currentDirectory.trim());
						}
						else if(splCommandName[0].trim().equals("ls")) {
							outStream.write(buf, 0, len);
							String st;
							do{
								len = inStream.read(buf); 
								st = new String(buf, 0, len); 
								System.out.print(st); 
							} while(st.trim().indexOf("ls end") < 0);
						}
						else if(splCommandName[0].trim().equals("lcat")) {
							readFile(splCommandName[1].trim());
						}
						else if(splCommandName[0].trim().equals("cat")) {
							outStream.write(buf, 0, len);
							String stcat;
							do{
								len = inStream.read(buf); 
								stcat = new String(buf, 0, len); 
								System.out.print(stcat);
							} while(stcat.trim().indexOf("cat end") < 0);
						}
						else if(splCommandName[0].trim().equals("lcd")) {
							tempDirectory = lcd(splCommandName[1].trim(), currentDirectory.trim());
							File tempDir = new File(tempDirectory.trim());
							if (tempDir.isDirectory()) {
								System.out.println("ok");
								currentDirectory = tempDirectory;
							} else {
								System.out.println("ng");
							}
						}
						else if(splCommandName[0].trim().equals("cd")) {
							outStream.write(buf, 0, len);
							len = inStream.read(buf);
							String cdreact = new String(buf, 0, len);
							System.out.print(cdreact);
						}
						else if(splCommandName[0].trim().equals("get")) {
							outStream.write(buf, 0, len);
							len = inStream.read(buf);
							String getreact = new String(buf, 0, len);
							if(getreact.trim().equals("get begin")) {
								System.out.print(getreact);
								receiveandwriteFile(splCommandName[1].trim(), inStream);
							} else if(getreact.trim().equals("ng")) {
								System.out.println(getreact);
							}
						}
						else if(splCommandName[0].trim().equals("put")) {
							String putreact = fileCheck(splCommandName[1].trim());
							PrintStream prtStream = new PrintStream(outStream);
							if(putreact.trim().equals("put begin")) {
								System.out.println(putreact);
								outStream.write(buf, 0, len);
								readandsendFile(splCommandName[1].trim(), outStream);
								prtStream.println("put end");
								System.out.println("put end");
							} else if(putreact.trim().equals("ng")){
								System.out.print(putreact);
							}
						}
						else {
							System.out.println("undifined command");
						}
					}

				}
			}
		} catch (IOException ioErr) {
			System.out.println(ioErr.getLocalizedMessage());
		} catch (SecurityException secErr) {
			System.out.println(secErr.getLocalizedMessage());
		}
	}

	public void test(String directoryName) {
		File aDir = new File(directoryName.trim()); 
		if (!aDir.isDirectory()) {
			System.out.println("It is not a direcotory.");
			System.exit(0); 
		}
		listup(aDir);
	}

	void listup(File aDir) {
		String contents[] = aDir.list(); 
		int index; 
		int numOfDirs = 0; 
		int numOfFiles = 0; 
		for (index = 0; index < contents.length; index++) {
			File aFileOrDir = new File(aDir, contents[index]);
			if (aFileOrDir.isDirectory()) {
				System.out.println(aFileOrDir + "/"); 
				numOfDirs++; 
			} else {
				System.out.println(aFileOrDir);
				numOfFiles++;
			}
		}
		System.out.println("File: " + String.valueOf(numOfFiles));
		System.out.println("Directory: " + String.valueOf(numOfDirs));
	}

	void readFile(String filename) {
		String st = ""; 
		try {
			int size = 20; 
			byte buf[] = new byte[size];
			int rsize; 
			FileInputStream fis = new FileInputStream(filename); 
			do {
				rsize = fis.read(buf, 0, size);
				if (rsize <= 0) break; 
				st = st + new String(buf, 0, rsize); 
			} while (rsize == size);
			System.out.println(st); 
			fis.close(); 
		} catch (IOException e) {
			System.out.println("Read error:" + e.getLocalizedMessage());
		}
	}

	String lcd(String toDirectoryName, String fromDirectoryName) {
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
		return absolutePath;
	}

	void receiveandwriteFile(String filename, InputStream is) {
		try {
			int size = 20;
			byte buf[] = new byte[size]; 
			int rsize; 
			String stget;
			FileOutputStream fos = new FileOutputStream(filename.trim() +".bak"); 
			do {
				rsize = is.read(buf, 0, size);
				if (rsize <= 0  || is.available() == 0 ) break; 
				fos.write(buf, 0, rsize);
				stget = new String(buf, 0, rsize); 
			} while (rsize == size || stget.trim().indexOf("get end") < 0);
			System.out.println("get end");
			fos.flush(); 
			fos.close(); 
		} catch (IOException e) {
			System.out.println("Error:" + e.getLocalizedMessage());
		}
	}

	String fileCheck(String filename) {

		File aFile = new File(filename.trim());

		if (!aFile.isFile()) {
			return("ng");
		} else {
			return("put begin");
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
}