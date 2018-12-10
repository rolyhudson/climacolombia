package ReadFilter;

import java.io.Serializable;
import java.util.Comparator;

public class YearComparator implements Comparator<Record>, Serializable {
    @Override
    public int compare(Record a, Record b) {
        return a.getDatetime().getYear() < b.getDatetime().getYear() ? -1 : a.getDatetime().getYear() == b.getDatetime().getYear() ? 0 : 1;
    }
}