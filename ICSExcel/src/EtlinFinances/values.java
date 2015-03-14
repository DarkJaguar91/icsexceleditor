package EtlinFinances;

public class values implements Comparable<values> {
    public weekInYear week;
    public double unconfirmed = 0;
    public double confirmed = 0;

    public values(weekInYear w) {
        week = w;
    }

    public void add(double unconf, double conf) {
        unconfirmed += unconf;
        confirmed += conf;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof values)
            if (((values) obj).week.equals(this.week))
                return true;
        return false;
    }

    @Override
    public int compareTo(values o) {
        return this.week.compareTo(o.week);
    }
}
