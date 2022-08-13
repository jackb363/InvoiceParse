package functions;

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Func {
	public static <T> ArrayList<T> removeDuplicates(ArrayList<T> list)
    {
		Set<T> set = new LinkedHashSet<>(); 
        // Add the elements to set
        set.addAll(list);
        // Clear the list
        list.clear();
        // add the elements of set
        // with no duplicates to the list
        list.addAll(set);
        // return the list
        return list;
    }
    public static int getMonthNum(String monthName) throws ParseException {
        Date date = new SimpleDateFormat("MMMM", Locale.ENGLISH).parse(monthName);
        int monthNum = date.getMonth()+1;
        return monthNum;
    }

    public static String getCostCentre() {
        String costCentre = "TL";
        Format monthFormat = new SimpleDateFormat("MMM");
        String strMonth = monthFormat.format(new Date());

        Format yearFormat = new SimpleDateFormat("yy");
        String strYear = yearFormat.format(new Date());
        costCentre += strMonth + strYear;
        return costCentre;
    }
}
