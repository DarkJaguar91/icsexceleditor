package EtlinFinances;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

public class dataHolder {
	public ArrayList<weekInYear> weeks;
	public ArrayList<SuplierData> supliers;
	
	public dataHolder(){
		weeks = new ArrayList<>();
		supliers = new ArrayList<>();
	}
	
	public void adddata(String name, Date date, double unconf, double conf){
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(date);
		
		weekInYear week = new weekInYear(c.get(GregorianCalendar.WEEK_OF_YEAR), c.get(GregorianCalendar.YEAR));
		
		if (!weeks.contains(week))
			weeks.add(week);
		
		weeks.get(weeks.indexOf(week)).addData(unconf, conf);
				
		if (!supliers.contains(new SuplierData(name))){
			supliers.add(new SuplierData(name));
		}
		
		supliers.get(supliers.indexOf(new SuplierData(name))).addVals(week, unconf, conf);
	}
}
