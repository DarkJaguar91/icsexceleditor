package ICSJournal;

import java.util.Date;
import java.util.GregorianCalendar;

public class ICSDate implements Comparable<ICSDate> {

	public Date date;
	public int week;
	public GregorianCalendar gc;
	
	public ICSDate(Date date){
		this.date = date;
		
		gc = new GregorianCalendar(date.getYear(), date.getMonth(), date.getDay());
		
		week = gc.get(GregorianCalendar.WEEK_OF_YEAR);
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof ICSDate){
			ICSDate o = (ICSDate)other;
			if (this.date.getYear() == o.date.getYear()){
				if (this.week == o.week)
					return true;
			}
		}
		
		return false;
	}

	@Override
	public int compareTo(ICSDate o) {
		int yearcomp = this.date.getYear() > o.date.getYear() ? 1 : this.date.getYear() < o.date.getYear() ? -1 : 0;
		
		// if same year compare week
		if (yearcomp == 0){
			return week < o.week ? -1 : week > o.week ? 1 : 0;
		}
			
		// if year less or more return that
		return yearcomp;
	}
}
