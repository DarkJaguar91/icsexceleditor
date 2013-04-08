package ICSJournal;

public class data implements Comparable<data>{
	public String commodity;
	public valqty[] data;

	public data(String com) {
		commodity = com;
		data = new valqty[53];
		
		for (int i = 0; i < 53; ++i)
			data[i] = new valqty();
	}

	public void add(double price, double qty, int week) {
		data[week].qty += qty;
		data[week].value += price * qty;
	}

	@Override
	public boolean equals(Object other){
		if (other instanceof data)
			return ((data)other).commodity.equals(commodity);
		return false;
	}
	
	@Override
	public int compareTo(data o) {
		return this.commodity.compareTo(o.commodity);
	}
}
