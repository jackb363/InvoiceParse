package parsers;

import connector.CSV_Conversion;
import functions.Func;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class PDF_Parser {
	private static String fileName;
	private static String dir;

	// for parsing the whole invoice document
	public static void parsePDF(String fileName) throws IOException {
		PDDocument document = null;
		String parsedPDFText = "";
		try {
			File f = new File(fileName);
			document = PDDocument.load(f);
			PDFTextStripper stripper = new PDFTextStripper();
			// TextStripper properties to preserve text formatting
			stripper.setParagraphStart("/t");
			stripper.setSortByPosition(true);
			// adds each line from doc to string
			for (String line : stripper.getText(document).split(stripper.getParagraphStart())) {
				parsedPDFText += line;
			}
			// checks for company name and puts through respective parser
			if (parsedPDFText.contains("AVIS")) {
				parseAvis(parsedPDFText);
			} else if (parsedPDFText.contains("HERTZ")) {
				parseHertz(parsedPDFText);
			} else if (parsedPDFText.contains("EUROPCAR")) {
				parseEuropCar(parsedPDFText);
			} else if (parsedPDFText.contains("ENTERPRISE")) {
				parseEnterprise(parsedPDFText);
			}
		} finally {
			if (document != null) {
				document.close();
			}
		}
	}

	public static void parseAvis(String toParse) throws IOException {
		ArrayList<String> data = new ArrayList<>();
		// Splits invoice into individual words and stores in array
		String[] parsed = toParse.split("\\s+");
		System.out.println("Found Avis");
		// iterates over word array and picks out data
		for (int i = 0; i < parsed.length; i++) {
			if (parsed[i].contains("No."))// for values after a ':'
			{
				if (parsed[i - 1].contains("Invoice") || parsed[i - 1].contains("Reservation")
						|| parsed[i - 1].contains("Account"))// for invoice, res and account num
				{
					data.add(parsed[i + 2]);
				}
			} else if (parsed[i].contains("Due") && parsed[i - 1].contains("Amount")) {// for net amount and currency type
				data.add(parsed[i + 1]);
				data.add(parsed[i + 2]);
			} else if (parsed[i].contains("VAT") && parsed[i - 1].contains("Commission")) {// for commission amount
				double amount = Double.parseDouble(parsed[i - 7]) + Double.parseDouble(parsed[i + 4]);
				data.add(Double.toString(amount));
			} else if (parsed[i].contains("location") && parsed[i - 1].contains("Return")) {// for Destination and																					// Travel Date
				data.add(parsed[i + 2]);
				data.add(parsed[i - 4].replaceAll("/", "."));
			} else if (parsed[i].contains("Taxable") && parsed[i - 2].contains("Charge")) {// for gross amount and VAT%
				data.add(parsed[i + 1]);
				data.add(parsed[i + 3].replace("%", ""));
			} else if (parsed[i].contains("Date") && parsed[i - 1].contains("Invoice")) {// for invoice date
				data.add(parsed[i + 2].replaceAll("/", "."));
			} else if (parsed[i].contains("by") && parsed[i - 1].contains("Rented")) {// for passenger name 
				data.add(parsed[i + 2].replace(",", " "));
			}
		}
		String provider = "AVIS";
		String payType = "AGENCY";
		String user = System.getProperty("user.name");
		// overwrites data array with duplicate values removed
		data = Func.removeDuplicates(data);

		fileName = "CarHire_Invoices";
		dir = "C:/Users/"+user+"/Documents/CSV_Files";
		// unsorted Array -> sorted Array -> CSV-Converter
		String[] sortedArr = { data.get(3), provider, data.get(0), data.get(2), data.get(6), data.get(1), data.get(4),
				data.get(10), data.get(11), data.get(7), data.get(9), data.get(8), payType, data.get(5) };
		
		CSV_Conversion.arrayToCSV(fileName, dir, sortedArr);
		
	}

	public static void parseHertz(String toParse) throws IOException {
		ArrayList<String> data = new ArrayList<>();
		String[] parsed = toParse.split("\\s+");
		System.out.println("Found Hertz");

		for (int i = 0; i < parsed.length; i++) {
			if (parsed[i].contains("Invoice")) {// for invoice date/num
				if (parsed[i + 1].contains("Date:") || parsed[i + 1].contains("No:")) {
					data.add(parsed[i + 2].replaceAll("/", "."));
				}
			} else if (parsed[i].contains("AMOUNT")) {// for currency type and gross amount
				data.add(parsed[i + 3]);
				data.add(parsed[i + 4]);
			} else if (parsed[i].contains("COMMISSION")) {// for commission amount
				data.add(parsed[i + 3]);
			}
			// for reservation num, net amount, account num
			else if (parsed[i].contains("No.:") && !parsed[i - 1].contains("CDP") || parsed[i].contains("Pay:")
					|| parsed[i].contains("ID:")) {
				if (Character.isDigit(parsed[i + 1].charAt(2))) {
					data.add(parsed[i + 1]);
				}
			} else if (parsed[i].contains("Renter:")) {// for driver name
				data.add(parsed[i + 1] + " " + parsed[i + 2]);
			} else if (parsed[i].contains("Rented") && parsed[i + 1].contains("On:")) {// for travel date
				data.add(parsed[i + 2].replaceAll("/", "."));
			} else if (parsed[i].contains("Voucher:")) {
				data.add(parsed[i + 2] + " " + parsed[i + 3].replace(",", " ") + parsed[i + 4]);
			}
		}
		String provider = "HERTZ";
		String payType = "AGENCY";
		String vat = "0";
		String user = System.getProperty("user.name");
		data = Func.removeDuplicates(data);

		fileName = "CarHire_Invoices";
		dir = "C:/Users/"+user+"/Documents/CSV_Files";
		String[] sortedArr = { data.get(3), provider, data.get(0), data.get(1), data.get(5), data.get(4), data.get(2),
				data.get(7), vat, data.get(9), data.get(10), data.get(8), payType, data.get(6) };
		CSV_Conversion.arrayToCSV(fileName, dir, sortedArr);
	}

	public static void parseEuropCar(String toParse) throws IOException {
		ArrayList<String> data = new ArrayList<>();
		String[] parsed = toParse.split("\\s+");
		System.out.println("Found Europcar");

		for (int i = 0; i < parsed.length; i++) {
			// for invoice date/num, account num, commission, gross amount, net amount,VAT%,
			// currency type
			if (parsed[i].contains("Invoice") || parsed[i].contains("Account") || parsed[i].contains("Commission")
					|| parsed[i].contains("Total") || parsed[i].contains("Tax")) {
				if (Character.isDigit(parsed[i + 1].charAt(1))) {
					data.add(parsed[i + 1].replace("%", ""));
				} else if (parsed[i + 1].contains("date")) {
					data.add(parsed[i + 2]);
				} else if (parsed[i + 1].contains("Charges:")) {
					data.add(parsed[i + 2]);
					data.add(parsed[i + 3]);
				}
			} else if (parsed[i].contains("Station")) {// for Destination and Travel date
				if (parsed[i - 1].contains("Check-out")) {
					data.add(parsed[i + 4]);
				} else if (parsed[i - 1].contains("Check-in")) {// for destination (1-3 words)
					if (!Character.isDigit(parsed[i + 2].charAt(0))) {
						data.add(parsed[i + 1] + " " + parsed[i + 2]);
					} else if (!Character.isDigit(parsed[i + 3].charAt(0))) {
						data.add(parsed[i + 1] + " " + parsed[i + 2] + " " + parsed[i + 3]);
					} else {
						data.add(parsed[i + 1]);
					}
				}
			} else if (parsed[i].contains("No") && parsed[i - 1].contains("Reservation")) {// for reservation number
				data.add(parsed[i + 2]);
			} else if (parsed[i].contains("Driver") && parsed[i - 1].contains("References")) {// for driver name
				data.add(parsed[i + 1] + " " + parsed[i + 2]);
			}
		}
		String provider = "EUROPCAR";
		String payType = "AGENCY";
		String user = System.getProperty("user.name");
		data = Func.removeDuplicates(data);

		fileName = "CarHire_Invoices";
		dir = "C:/Users/"+user+"/Documents/CSV_Files";

		String[] sortedArr = { data.get(3), provider, data.get(0), data.get(1), data.get(5), data.get(2), data.get(4),
				data.get(7), data.get(9), data.get(10), data.get(11), data.get(8), payType, data.get(6) };
		CSV_Conversion.arrayToCSV(fileName, dir, sortedArr);
	}

	// for parsing enterprise multiple invoices in single PDF
	public static void parseEnterprise(String toParse) throws IOException {
		ArrayList<String> concatted = new ArrayList<>();
		String[] parsed = toParse.split("(Page).*");// breaks invoice into individual pages and stores each as string in
		String user = System.getProperty("user.name");
		
		System.out.println("Found Enterprise");

		fileName = "CarHire_Invoices";
		dir = "C:/Users/"+user+"/Documents/CSV_Files";

		String provider = "ENTERPRISE";
		String payment = "AGENCY";
		// joins every two pages(to make up 1 invoice) together in array
		for (int i = 0; i < parsed.length - 1; i += 2) {
			concatted.add(parsed[i] + parsed[i + 1]);
		}
		for (String c : concatted) {
			ArrayList<String> data = new ArrayList<>();// for iterating over each invoice in array
			parsed = c.split("\\s+");
			for (int i = 0; i < parsed.length; i++) {
				if (parsed[i].contains("#:") && !parsed[i + 2].contains("BIC/SWIFT:")) {
					if (parsed[i - 1].contains("Invoice") || parsed[i - 1].contains("Account")
							|| parsed[i - 1].contains("Reservation")) {// for invoice,account and reservation number
						data.add(parsed[i + 1]);
					}
				} else if (parsed[i].contains("ADDED") && parsed[i + 1].contains("TAX")) {// for gross amount and VAT%
					data.add(parsed[i + 2]);
					data.add(parsed[i + 4]);
				} else if (parsed[i].contains("Date:") || parsed[i].contains("Out:")) {// for travel date
					data.add(parsed[i + 1].replaceAll("/", "."));
				} else if (parsed[i].contains("In:")) {// for destination
					String destination="";
					for(int j=i+4; !parsed[j].contains("Reserved"); j++) {
						destination += parsed[j]+" ";
					}
					data.add(destination);
				} else if (parsed[i].contains("VAT") && parsed[i - 1].contains("&")) {// for currency type and commission amount
					data.add(parsed[i + 2]);
					data.add(parsed[i + 1].replaceAll("\\(|\\)", ""));
				} else if (parsed[i].contains("Paid") && parsed[i - 1].contains("be")) {//for net amount
					data.add(parsed[i + 1]);
				} else if (parsed[i].contains("Driver:") && !parsed[i - 1].contains("Additional")) {// for driver name
					data.add(parsed[i + 2] + " " + parsed[i + 1].replace(",", ""));
				}
			}
			data = Func.removeDuplicates(data);
			System.out.println(data);
			String[] sortedArr = { data.get(2), provider, data.get(0), data.get(1), data.get(7), data.get(3),
					data.get(6), data.get(4), data.get(5), "-"+data.get(10), data.get(9), data.get(11), payment,
					data.get(8) };
			CSV_Conversion.arrayToCSV(fileName, dir, sortedArr);
		}
	}
}
