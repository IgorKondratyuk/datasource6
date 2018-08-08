package kz.erg.datasource6;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.hibernate.Session;

import com.google.gson.Gson;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description="Данные запроса")
@SuppressWarnings("serial")
public class DataSourceRequest
	implements Serializable{

	private StringBuilder sql;
    
    @ApiModelProperty(notes = "номер страницы", allowEmptyValue = false, required=true, position = 0)
	private int page;
    
    @ApiModelProperty(notes = "количество элементов на странице", allowEmptyValue = false, required=true, position = 1)
    private int pageSize;
    
    @ApiModelProperty(notes = "сортировки", allowEmptyValue = true, position = 2)
    private List<SortDescriptor> sort;
    
    @ApiModelProperty(notes = "фильтр", allowEmptyValue = true, position = 4)
    private FilterDescriptor filter;
    
    @ApiModelProperty(notes = "алиасы", allowEmptyValue = true, position = 3)
    private List<AliasDescriptor> aliases;
	
    public int getPage() {
		return page;
	}
    
	public void setPage(int page) {
		this.page = page;
	}
	
	public int getPageSize() {
		return pageSize;
	}
	
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	
    public List<SortDescriptor> getSort() {
		return sort;
	}
    
	public void setSort(List<SortDescriptor> sort) {
		this.sort = sort;
	}
	
	public FilterDescriptor getFilter() {
		return filter;
	}
	
	public void setFilter(FilterDescriptor filter) {
		this.filter = filter;
	}
	
	public List<AliasDescriptor> getAliases() {
		return aliases;
	}

	public void setAliases(List<AliasDescriptor> aliases) {
		this.aliases = aliases;
	}

	@SuppressWarnings("incomplete-switch")
	private StringBuilder addFilter(StringBuilder sqlFilter, FilterDescriptor filter, Class<?> clazz, int iNumParam) {
		Operator operator = Operator.valueOf(filter.getOperator());
		String field = (filter.field.indexOf(".")==(-1)?"this."+filter.field:filter.field);
		String param = filter.field.replace(".", "");
		switch(operator) {
	    	case in:
	    		sqlFilter.append(" "+field+" IN :"+param+iNumParam);//((Object[])((Object) filter.field))+iNumParam);
	    		break;
	    	case notin:
	    		sqlFilter.append(" "+field+" NOT IN :"+param+iNumParam);//((Object[])((Object) filter.field))+iNumParam);
	    		break;
	    	case isnull:
	    		sqlFilter.append(" "+field+" IS NULL");
	    		break;
	    	case isnotnull:
	    		sqlFilter.append(" "+field+" IS NOT NULL");
	    		break;
	    	case eq:
	    		sqlFilter.append(" "+field+" = :"+param+iNumParam);
	            break;
	        case neq:
	        	sqlFilter.append(" "+field+" <> :"+param+iNumParam);
	        	break;
	        case gt:
	        	sqlFilter.append(" "+field+" > :"+param+iNumParam);
	            break;
	        case gte:
	        	sqlFilter.append(" "+field+" >= :"+param+iNumParam);
	            break;
	        case lt:
	        	sqlFilter.append(" "+field+" < :"+param+iNumParam);
	            break;
	        case lte:
	        	sqlFilter.append(" "+field+" <= :"+param+iNumParam);
	            break;
	        case startswith:
	        	sqlFilter.append(" lower("+field+") LIKE lower(:"+param+iNumParam+")");
	            break;
	        case endswith:
	        	sqlFilter.append(" lower("+field+") LIKE lower(:"+param+iNumParam+")");
	            break;
	        case contains:
	        	sqlFilter.append(" lower("+field+") LIKE lower(:"+param+iNumParam+")");
	            break;
	        case doesnotcontain:
	        	sqlFilter.append(" lower("+field+") LIKE lower(:"+param+iNumParam+")");
	        	break;
	    }
		return sqlFilter;
	}
	
	private StringBuilder addFilters(StringBuilder sqlFilter, FilterDescriptor filter, int sizeFilters, int iFilter, String logic, Class<?> clazz,  int iNumParam) {
		int iParam = iNumParam;
		if (filter != null) {
            List<FilterDescriptor> filters = filter.filters;
            	if(sqlFilter.length() == 0) {
            		sqlFilter.append(" WHERE");
            	}
            	if(iFilter > 1) {
        			sqlFilter.append(" "+logic);
        		}

            	if(iFilter == 1 && sizeFilters > 0) {
        			sqlFilter.append(" (");
        		}

            	if(filter.getField() != null) {
            		sqlFilter = addFilter(sqlFilter, filter, clazz, iParam);
            	}
            	int iFilters = 0;
                for(FilterDescriptor entry : filters) {
                	iFilters++;
                	if(entry.getField() != null) {
                		iParam = iParam+1;
                	}
                   	sqlFilter = this.addFilters(sqlFilter, entry, filters.size(), iFilters, filter.getLogic(), clazz, iParam);
                }
        		if(iFilter == sizeFilters ) {
        			sqlFilter.append(" )");
        		}
		}
        return sqlFilter;
	}
	
	@SuppressWarnings("incomplete-switch")
	private void addParametersFilter(Query query, FilterDescriptor filter, Class<?> clazz, int iNumParam) {
		int iParam = iNumParam;
		Operator operator = null;
        if (filter != null) {
        	if(filter.getField() != null) {
	        	operator = Operator.valueOf(filter.getOperator());
        	}
    		Object value = filter.getValue();
            try {
            	Gson gson = new Gson();
                Class<?> type = new PropertyDescriptor(filter.getField(), clazz).getPropertyType();
                if (type == double.class || type == Double.class) {
                	if(operator.equals(Operator.in)
                			|| operator.equals(Operator.notin)) {
                		value = Arrays.asList(gson.fromJson(filter.getValue().toString(), Double[].class));
                	}else {
                		if(value != null) {
                			value = Double.parseDouble(value.toString());
                		}
                	}
                } else if (type == float.class || type == Float.class) {
                	if(operator.equals(Operator.in)
                			|| operator.equals(Operator.notin)) {
                		value = Arrays.asList(gson.fromJson(filter.getValue().toString(), Float[].class));
                	}else {
                		if(value != null) {
                			value = Float.parseFloat(value.toString());
                		}
                	}            	
                } else if (type == long.class || type == Long.class) {
                	if(operator.equals(Operator.in)
                			|| operator.equals(Operator.notin)) {
                		value = Arrays.asList(gson.fromJson(filter.getValue().toString(), Long[].class));
                	}else {
                		if(value != null) {
                			value = Long.parseLong(value.toString());
                		}
                	}            	
                } else if (type == int.class || type == Integer.class) {
                	if(operator.equals(Operator.in)
                			|| operator.equals(Operator.notin)) {
                		value = Arrays.asList(gson.fromJson(filter.getValue().toString(), Integer[].class));
                	}else {
                		if(value != null) {
                			value = Integer.parseInt(value.toString());
                		}
                	}
                } else if (type == short.class || type == Short.class) {
                	if(operator.equals(Operator.in)
                			|| operator.equals(Operator.notin)) {
                		value = Arrays.asList(gson.fromJson(filter.getValue().toString(), Short[].class));
                	}else {
                		if(value != null) {
                			value = Short.parseShort(value.toString());
                		}
                	}
                } else if (type == boolean.class || type == Boolean.class) {
                	if(operator.equals(Operator.in)
                			|| operator.equals(Operator.notin)) {
                		value = Arrays.asList(gson.fromJson(filter.getValue().toString(), Boolean[].class));
                	}else {
                		if(value != null) {
                			value = Boolean.parseBoolean(value.toString());
                		}
                	}
                }
            }catch (IntrospectionException e) {
            }catch(NumberFormatException nfe) {
            }

        	List<FilterDescriptor> filters = filter.filters;
        	if(filter.getField() != null) {
        		switch(operator) {
    	        case startswith:
    	        	value = value+"%";
    	            break;
    	        case endswith:
    	        	value = "%"+value;
    	            break;
    	        case contains:
    	        	value = "%"+value+"%";
    	            break;
    	        case doesnotcontain:
    	        	value = "%"+value+"%";
    	        	break;
    	    }
        		query.setParameter(filter.getField().replace(".", "")+iParam, value);
        	}
            for(FilterDescriptor entry : filters) {
            	if(entry.getField() != null) {
            		iParam = iParam+1;
            	}
            	this.addParametersFilter(query, entry, clazz, iParam);
            }
        }		
	}
	
	private StringBuilder addSort(StringBuilder sql, List<SortDescriptor> sort) {
        if (sort != null && !sort.isEmpty()) {
        	sql.append(" ORDER BY");
        	StringBuilder orderBy = new StringBuilder();
            for (SortDescriptor entry : sort) {
            	if(orderBy.length() > 0) {
            		orderBy.append(",");	
            	}
            	orderBy.append(" "+entry.getField());
            	if(entry.getDir() != null && !entry.getDir().isEmpty()) {
            		orderBy.append(" "+entry.getDir());
            	} 
            }
            sql.append(orderBy);
        }
        return this.sql;
    }
    
	private StringBuilder addAliases() {
		StringBuilder sqlAliases = null;
		sqlAliases = new StringBuilder();
		for(AliasDescriptor entry : this.aliases) {
			sqlAliases.append(" "+entry.getJoinType()+" this."+entry.fieldAlias+" "+entry.getAlias());
		}
		return sqlAliases;
	}
	
	@SuppressWarnings("unchecked")
	private long toTotal(Session session, StringBuilder sql, Class<?> clazz) {
		String totalSql = "SELECT COUNT(this.id) " +this.sql;
    	TypedQuery<Long> totalQuery = session.createQuery(totalSql);
    	//добавляем параметры фильтра
    	this.addParametersFilter(totalQuery, this.filter, clazz, 0);

    	return totalQuery.getSingleResult();
	}
	
    public DataSourceResult toResult(Session session, Class<?> clazz) {
    	
    	this.sql = new StringBuilder("FROM "+clazz.getName()+" this");

    	//добавляем алиасы
    	if(this.aliases != null && !this.aliases.isEmpty()) {
	    	StringBuilder sqlAliases = new StringBuilder();
	    	sqlAliases = this.addAliases();
	    	this.sql.append(sqlAliases);
    	}
    	//добавляем фильтры
    	if(this.filter != null) {
    		StringBuilder sqlFilter = new StringBuilder();
    		sqlFilter = this.addFilters(sqlFilter, this.filter, 0, 1, filter.getLogic(), clazz, 0);
    		this.sql.append(sqlFilter);
    	}

    	//получаем количество записей всего
    	long total = this.toTotal(session, this.sql, clazz);
    	// добавляем сортировку
    	this.sql = this.addSort(this.sql, this.sort);

//    	if(this.aliases != null && !this.aliases.isEmpty()) {
    		this.sql.insert(0, "SELECT DISTINCT this ");
//    	}
    	//создаём запрос
    	Query query = session.createQuery(this.sql.toString(), clazz);
    	
    	//добавляем параметры фильтра
    	this.addParametersFilter(query, this.filter, clazz, 0);
    	
    	//определяем количество пропущенных записей
    	int skip = this.page*this.getPageSize()-this.getPageSize();
    	
    	//ограничиваем набор записей
    	query.setFirstResult(skip);
    	query.setMaxResults(this.pageSize);

    	DataSourceResult result = new DataSourceResult();
    	result.setData(query.getResultList());
    	result.setTotal(total);
    	return result;
    }
    
    @ApiModel(description="Сортировка")
    public static class SortDescriptor {
    	
    	@ApiModelProperty(notes = "наименование поля", allowEmptyValue = false, required=true, position = 0)
        private String field;
    	
    	@ApiModelProperty(notes = "режим сортировки", allowableValues = "asc, desc", allowEmptyValue = false, required=true, position = 1)
        private String dir;
        
        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getDir() {
            return dir;
        }

        public void setDir(String dir) {
            this.dir = dir;
        }
    }

    @ApiModel(description="Фильтр")
    public static class FilterDescriptor {
    	
    	@ApiModelProperty(notes = "логический оператор", allowableValues = "and, or", allowEmptyValue = true, position = 0)
        private String logic;
    	
    	@ApiModelProperty(notes = "фильтры", allowEmptyValue = true, position = 4)
        private List<FilterDescriptor> filters;
    	
    	@ApiModelProperty(notes = "наименование поля", allowEmptyValue = true, position = 1)
        private String field;
    	
    	@ApiModelProperty(notes = "значение", allowEmptyValue = true, position = 2, dataType="Object")
        private Object value;
    	
    	@ApiModelProperty(notes = "оператор сравнения", allowEmptyValue = true, position = 3)
        private String operator;
        
        public FilterDescriptor() {
            filters = new ArrayList<FilterDescriptor>();
        }
        
        public String getField() {
            return field;
        }
        
        public void setField(String field) {
            this.field = field;
        }
        
        public Object getValue() {
            return value;
        }
        
        public void setValue(Object value) {
            this.value = value;
        }
        
		public String getOperator() {
            return operator;
        }
		
        public void setOperator(String operator) {
            this.operator = operator;
        }
        
        public String getLogic() {
            return logic;
        }
        
        public void setLogic(String logic) {
            this.logic = logic;
        }

		public List<FilterDescriptor> getFilters() {
            return filters;
        }
    }

    @ApiModel(description="Алиас")
    public static class AliasDescriptor {
    	
    	@ApiModelProperty(notes = "наименование поля алиаса", allowEmptyValue = false, required=true, position = 0)
        private String fieldAlias;

    	@ApiModelProperty(notes = "наименование алиаса", allowEmptyValue = false, required=true, position = 1)
    	private String alias;

    	@ApiModelProperty(notes = "оператор операции соединения", allowableValues = "JOIN, LEFT\t JOIN, RIGHT\t JOIN", allowEmptyValue = true, position = 2)
    	private String joinType;
        
		public AliasDescriptor() {
			this.joinType = "JOIN"; 
		}

		public String getFieldAlias() {
			return fieldAlias;
		}
		
		public void setFieldAlias(String fieldAlias) {
			this.fieldAlias = fieldAlias;
		}
		
		public String getAlias() {
			return alias;
		}
		
		public void setAlias(String alias) {
			this.alias = alias;
		}
		
		public String getJoinType() {
			return joinType;
		}
		
		public void setJoinType(String joinType) {
			this.joinType = joinType;
		}
    	
    }

}
