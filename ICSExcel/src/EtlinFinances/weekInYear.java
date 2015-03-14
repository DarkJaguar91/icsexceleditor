package EtlinFinances;


public class weekInYear  implements Comparable<weekInYear>{
	public int week = 0;
	public int year = 1990;
	
	public double unconfTotal = 0;
	public double confTotal = 0;
	
	public weekInYear(int week, int year){
		this.week = week;
		this.year = year;
		unconfTotal = 0;
		confTotal = 0;
	}		
	
	public void addData(double ucon, double con){
		unconfTotal += ucon;
		confTotal += con;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof weekInYear)
			if (((weekInYear)obj).week == week && ((weekInYear)obj).year == year)
				return true;
		return false;
	}

	@Override
	public int compareTo(weekInYear o) {
		if (year < o.year)
			return -1;
		else if (year > o.year)
			return 1;
		else if (week < o.week)
			return -1;
		else if (week > o.week)
			return 1;
		else
			return 0;
	}
}
