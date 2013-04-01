package ICSJournal;

public class data implements Comparable<data>{
	public String commodity;
	public valqty[] data;

	public data(String com) {
		commodity = com;
		data = new valqty[53];
	}

	public void add(double val, double qty, int week) {
		data[week].qty += qty;
		data[week].value += val;
	}

	@Override
	public boolean equals(Object other){
		if (other instanceof data)
			return ((data)other).commodity.equals(commodity);
		return false;
	}
	
	@Override
	public int compareTo(data o) {
		this.commodity.compareTo(o.commodity);
		return 0;
	}
}
