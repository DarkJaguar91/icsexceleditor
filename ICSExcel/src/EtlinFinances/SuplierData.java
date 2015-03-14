package EtlinFinances;

import java.util.ArrayList;

public class SuplierData implements Comparable<SuplierData> {
    String name;

    public ArrayList<values> vals;

    values totalforSuplier;

    public SuplierData(String name) {
        this.name = name;
        vals = new ArrayList<>();
        totalforSuplier = new values(null);
    }

    public void addVals(weekInYear week, double unconf, double conf) {
        if (!vals.contains(week))
            vals.add(new values(week));

        vals.get(vals.indexOf(new values(week))).add(unconf, conf);

        totalforSuplier.add(unconf, conf);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SuplierData)
            return ((SuplierData) obj).name.equals(name);

        return false;
    }

    @Override
    public int compareTo(SuplierData o) {
        return this.name.compareTo(o.name);
    }
}
