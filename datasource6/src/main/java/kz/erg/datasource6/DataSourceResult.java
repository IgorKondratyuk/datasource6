package kz.erg.datasource6;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class DataSourceResult
	implements Serializable{
    
	private long total;
    
    private List<?> data;
    
    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<?> getData() {
        return data;
    }

    public void setData(List<?> data) {
        this.data = data;
    }

}
