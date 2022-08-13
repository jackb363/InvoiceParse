package functions;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Base64;

public class Serialisation {
		public static void ser(ArrayList<String> serialObjs, String fileName) {
			String user = System.getProperty("user.name");
			try {
				//Part 1 Serialization 
				FileOutputStream fileOut = new FileOutputStream("C:/Users/"+user+"/Documents/"+fileName);
				ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
				for(String obj : serialObjs) {
					objectOut.writeObject(obj);
				}
				objectOut.close();
			}
			catch(FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		public static String encrypt(String encryptStr){
			String encrypted = Base64.getEncoder().encodeToString(encryptStr.getBytes());
			return encrypted;
		}
	}
