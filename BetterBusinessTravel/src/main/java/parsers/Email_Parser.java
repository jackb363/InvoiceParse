package parsers;

import connector.CSV_Conversion;
import functions.Func;
import org.jsoup.Jsoup;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;

public class Email_Parser {

	public static void parseTrainLine(String emailContent) throws IOException, ParseException{
		ArrayList<String> data = new ArrayList<>();
		String[] parseLine = emailContent.split(System.lineSeparator());

		for(int i = 0; i < parseLine.length; i++) {//destination station
			if(parseLine[i].contains("Reservations")) {
				String destination = parseLine[i+4].split("- ")[1].trim();
				data.add(destination.toUpperCase());
			}
		}

		String[] parsed = emailContent.split("\\s+");
		for (int i = 0; i < parsed.length; i++) {
			//System.out.println(parsed[i]);
			if (parsed[i].contains("Sent:")) {
				if (Character.isDigit(parsed[i + 1].charAt(0))) {// for Booking Date
					int monthNo = Func.getMonthNum(parsed[i+2]);
					data.add(parsed[i+1]+"."+monthNo+"."+parsed[i+3]);
				}
			} else if (parsed[i].contains("Fare") || parsed[i].contains("fee:") || parsed[i].contains("Booking")&&parsed[i+1].contains("Confirmation")) {// for ticket amount, reservation number and fee charge
				if (Character.isDigit(parsed[i + 2].charAt(0))) {
					data.add(parsed[i + 2]);
				}
			} else if (parsed[i].contains("Dear")) {// for passenger name
				if (parsed[i + 1].contains("Mr") || parsed[i + 1].contains("Ms")) {
					data.add(parsed[i+1]+" "+parsed[i + 2] + " " + parsed[i + 3].replace(",", ""));
				}
			} else if (parsed[i].contains("Travel") && parsed[i + 1].contains("on")) {// for date of travel
				if (Character.isDigit(parsed[i + 3].charAt(0))) {
					int monthNo = Func.getMonthNum(parsed[i+4]);
					data.add(parsed[i + 3] + "." + monthNo + "." + parsed[i + 5]);
				}
			}
		}
		String agency = "TRAINLINE";
		String branchCode  = "BBTTrainline";
		String costCentre  = Func.getCostCentre().toUpperCase();
		data = Func.removeDuplicates(data);

		DecimalFormat df = new DecimalFormat("0.00");
		double commission = Double.parseDouble(data.get(5))*0.025;

		double delivCharge = Double.parseDouble(data.get(6));
		double diffDelivCharge;
		if(delivCharge!=0.00){
			diffDelivCharge = 0.09;
		}
		else{
			diffDelivCharge = 0.00;
		}
		double netCharge = Double.sum(delivCharge, diffDelivCharge);
		double netDeduct = netCharge-commission;
		double netTotal = Double.parseDouble(data.get(5))+netDeduct;

		String ticketType = "STANDARD";
		String user = System.getProperty("user.name");
		String fileName = costCentre;
		String dir = "C:/Users/"+user+"/Documents/CSV_Files";
		String[] sortedArr = { agency, data.get(1), branchCode, costCentre, data.get(2), data.get(3), data.get(4), ticketType, data.get(0), data.get(5),Double.toString(netCharge), data.get(6), Double.toString(diffDelivCharge),"-"+df.format(commission),"-"+df.format(netTotal)};
		CSV_Conversion.arrayToCSV(fileName, dir, sortedArr);
	}

	public static String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
		String result = "";
		int count = mimeMultipart.getCount();
		for (int i = 0; i < count; i++) {
			BodyPart bodyPart = mimeMultipart.getBodyPart(i);
			if (bodyPart.isMimeType("text/plain")) {
				result = result + "\n" + bodyPart.getContent();
				break; // without break same text appears twice in my tests
			} else if (bodyPart.isMimeType("text/html")) {
				String html = (String) bodyPart.getContent();
				result = result + "\n" + Jsoup.parse(html).text();
			} else if (bodyPart.getContent() instanceof MimeMultipart) {
				result = result + getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
			}
		}
		return result;
	}
}
