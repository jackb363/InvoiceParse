package connector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class CSV_Conversion {
	//converts a string array to CSV File
	public static void arrayToCSV(String fileName, String dir, String[] parsedData) throws IOException {
		File file = new File(dir, fileName + ".csv");
		//checks to see if car CSV file exists if not then creates one 
		if (file.exists() == false && fileName == "CarHire_Invoices") {
			System.out.println("File doesnt exist...Creating CSV File");
			String[] dataColumns = { "Agency Account Number", "Car Provider", "Invoice Number", "Invoice Date",
					"Travel Date", "Reservation Number", "Passenger Name", "Gross Amount", "VAT %", "Comm",
					"Net Amount", "Currency", "Payment Type", "Destination" };
			FileWriter fw = new FileWriter(file, false);
			BufferedWriter bw = new BufferedWriter(fw);
			for (String dc : dataColumns) {
				bw.append(dc + ",");
			}
			//goes to next line after appending data
			bw.newLine();
			bw.close();
			fw.close();
		//checks to see if train CSV file exists if not then creates one 
		} else if (file.exists() == false && fileName.contains("TL")) {
			System.out.println("File doesnt exist...Creating CSV File");
			String[] dataColumns = {"Corporate Reference","Booking.Refund Date","Branch Code","Cost Centre","Transaction ID","Customer Name","Departure Date","Ticket Type","Destination Station","Rail or Refund Cost","Delivery Charge","POS Delivery Charge","Diff Delivery Charge","Commission","Total Charge"};
			FileWriter fw = new FileWriter(file, false);
			BufferedWriter bw = new BufferedWriter(fw);
			for (String dc : dataColumns) {
				bw.append(dc + ",");
			}
			bw.newLine();
			bw.close();
			fw.close();
		}
		//appends invoice data to respective CSV file
		FileWriter fw = new FileWriter(file, true);
		BufferedWriter bw = new BufferedWriter(fw);
		for (String pd : parsedData) {
			bw.append(pd + ",");
		}
		bw.newLine();
		bw.close();
		fw.close();
	}
}
