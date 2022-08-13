package parsers;

import connector.CSV_Conversion;
import functions.Func;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class WordDoc_Parser {
	private static String fileName;
	private static String dir;

	public static void parseWord(String fileName) throws IOException {
		File file = new File(fileName);
		FileInputStream fis = new FileInputStream(file);
		XWPFDocument docx = new XWPFDocument(fis);
		// checks for invoice type and uses appropriate parser
		for (XWPFParagraph paragraph : docx.getParagraphs()) {
			if (paragraph.getText().contains("EUROPCAR")) {
				parseEuropCar(docx);
				break;
			} else if (paragraph.getText().contains("AVIS")) {
				parseAvis(docx);
				break;
			} else if (paragraph.getText().contains("HERTZ")) {
				parseHertz(docx);
				break;
			} else if (paragraph.getText().contains("ENTERPRISE")) {
				break;
			}
		}
		docx.close();
	}

	public static void parseEuropCar(XWPFDocument europDoc) throws IOException {
		String toParse = "";
		ArrayList<String> data = new ArrayList<>();
		for (XWPFParagraph paragraph : europDoc.getParagraphs()) {// parse paragraphs first
			toParse += paragraph.getText();
		}

		String[] parsed = toParse.split("\\R");
		for (int i = 0; i < parsed.length; i++) {
			if (parsed[i].contains("Invoice") || parsed[i].contains("date") && parsed[i - 2].contains("Invoice")
					|| parsed[i].contains("Charged") || parsed[i].equals(":") || parsed[i].contains("Account:")) {// for invoice date/num,travel date,account num and reservation num
				if (Character.isDigit(parsed[i + 1].charAt(0))) {
					data.add(parsed[i + 1]);
				}
			} else if (parsed[i].contains("Driver") && parsed[i + 2].contains("ID")
					|| parsed[i].equals("Registration")) {// for driver name and destination
				data.add(parsed[i + 3]);
			}
		}

		toParse = "";
		for (XWPFTable table : europDoc.getTables()) {// parse tables
			toParse += table.getText();
		}

		parsed = toParse.split("\\s+");
		for (int i = 0; i < parsed.length; i++) {
			if (parsed[i].contains("Tax")) {
				if (Character.isDigit(parsed[i + 1].charAt(0))) {// for gross amount,VAT% and currency type
					data.add(parsed[i + 1].replace("%", ""));
					data.add(parsed[i + 2]);
					data.add(parsed[i + 3]);
				}
			} else if (parsed[i].contains("excise.")) {// for net amount and commission
				data.add(parsed[i + 1]);
				data.add(parsed[i + 3]);
			}
		}
		String provider = "EUROPCAR";
		String payType = "AGENCY";
		String user = System.getProperty("user.name");
		
		data = Func.removeDuplicates(data);
		fileName = "CarHire_Invoices";
		dir = "C:/Users/"+user+"/Documents/CSV_Files";
		String[] sortedArr = { data.get(6), provider, data.get(3), data.get(4), data.get(2), data.get(5), data.get(0),
				data.get(8), data.get(7), data.get(10), data.get(11), data.get(9), payType, data.get(1) };
		CSV_Conversion.arrayToCSV(fileName, dir, sortedArr);
	}

	public static void parseAvis(XWPFDocument avisDoc) throws IOException {
		String toParse = "";
		ArrayList<String> data = new ArrayList<>();
		for (XWPFParagraph paragraph : avisDoc.getParagraphs()) {// parse paragraphs
			toParse += paragraph.getText();
		}

		for (XWPFTable table : avisDoc.getTables()) {// parse tables
			toParse += table.getText();
		}
		String[] parsed = toParse.split("(\\s+)|(Commission)");
		for (int i = 0; i < parsed.length; i++) {
			if (parsed[i].contains("VAT") && parsed[i + 1].contains("@")) {// for commission amount (has to be
																			// calculated)
				Double grossCom = Double.parseDouble(parsed[i - 2]);
				Double vat = Double.parseDouble(parsed[i + 2]);
				Double vatCom = grossCom * vat / 100;
				data.add(Double.toString(grossCom + vatCom.shortValue()));
			} else if (parsed[i].contains("Taxable") && parsed[i - 1].contains("on")) {// for VAT% and gross amount
				data.add(parsed[i + 1]);
				data.add(parsed[i + 3].replace("%", ""));
			} else if (parsed[i].contains("No") || parsed[i].contains("Date") || parsed[i].contains("by")
					|| parsed[i].contains("date")) {// for account num,invoice num/date,travel date,reserve num,passenger name
				if (parsed[i - 1].contains("Invoice") || parsed[i - 1].contains("Rented")
						|| parsed[i - 1].contains("Account") || parsed[i - 1].contains("Reservation")
						|| parsed[i - 1].contains("out")) {
					data.add(parsed[i + 2].replaceAll("(,)|(:)", " "));
					data.remove(" ");
				}
			} else if (parsed[i].contains("location") && parsed[i - 1].contains("Return")) {// for destination
				String destination = "";
				for (int j = i + 2; !parsed[j].contains("Check-"); j++) {// loop to get full destination name
					destination += parsed[j].replace(",", "") + " ";
				}
				data.add(destination);
			} else if (parsed[i].contains("due") && parsed[i - 1].contains("Amount")) {// for net amount and GBP
				data.add(parsed[i + 2]);
				data.add(parsed[i + 3]);
			}
		}
		String provider = "AVIS";
		String payType = "AGENCY";
		String user = System.getProperty("user.name");
		data = Func.removeDuplicates(data);

		fileName = "CarHire_Invoices";
		dir = "C:/Users/"+user+"/Documents/CSV_Files";
		String[] sortedArr = { data.get(6), provider, data.get(2), data.get(0).replaceAll("/", "."),
				data.get(8).replaceAll("/", "."), data.get(5), data.get(7), data.get(10), data.get(11), data.get(1),
				data.get(4), data.get(3), payType, data.get(9) };
		CSV_Conversion.arrayToCSV(fileName, dir, sortedArr);
	}

	public static void parseHertz(XWPFDocument hertzDoc) throws IOException {
		String toParse = "";
		ArrayList<String> data = new ArrayList<>();
		for (XWPFParagraph paragraph : hertzDoc.getParagraphs()) {// parse paragraphs
			toParse += paragraph.getText();
		}
		for (XWPFTable table : hertzDoc.getTables()) {// parse tables
			toParse += table.getText();
		}
		String[] parsed = toParse.split("\\s+");
		for(int i=0; i<parsed.length;i++) {
			if(parsed[i].contains("On:")) {//for travel date,reservation num and destination
				if(parsed[i-1].contains("Rented")) {
					data.add(parsed[i-2]);
					data.add(parsed[i+1].replaceAll("/", "."));
				} else if(parsed[i-1].contains("Returned")) {
					data.add(parsed[i+4]+" "+parsed[i+5].replace(",", ""));
				}
			} else if(parsed[i].contains("AMOUNT")||parsed[i].contains("COMMISSION")) {//for gross/commission amount and currency type
				data.add(parsed[i+3]);
				data.add(parsed[i+4]);
			} else if(parsed[i].contains("No")||parsed[i].contains("Date:")||parsed[i].contains("Pay:")) {//for invoice num/date,net amount,account num,passenger name
				if(parsed[i-1].contains("Invoice")&&Character.isDigit(parsed[i+1].charAt(0))||parsed[i-1].contains("Please")&&Character.isDigit(parsed[i+1].charAt(0))) {
					data.add(parsed[i+1].replaceAll("/", "."));
				} else if(parsed[i-1].contains("Account")&&Character.isDigit(parsed[i+3].charAt(0))) {
					data.add(parsed[i+1]+" "+parsed[i+2]);
					data.add(parsed[i+3]);
				}
			}
		}
		String vat = "0";
		String provider = "HERTZ";
		String payType = "AGENCY";
		String user = System.getProperty("user.name");
		data = Func.removeDuplicates(data);
		
		fileName = "CarHire_Invoices";
		dir = "C:/Users/"+user+"/Documents/CSV_Files";
		String[] sortedArr = { data.get(6), provider, data.get(3), data.get(4),data.get(1), data.get(0), data.get(5), data.get(8),
				vat, data.get(10),data.get(7), data.get(9), payType, data.get(2) };
		CSV_Conversion.arrayToCSV(fileName, dir, sortedArr);
		
	}
}