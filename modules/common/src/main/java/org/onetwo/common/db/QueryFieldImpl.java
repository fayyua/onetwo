package org.onetwo.common.db;

import org.onetwo.common.db.ExtQuery.K;
import org.onetwo.common.db.sqlext.SQLSymbolManager;
import org.onetwo.common.exception.ServiceException;
import org.onetwo.common.utils.StringUtils;

public class QueryFieldImpl implements QueryField {
	
	public static QueryFieldImpl create(String exp){
		return new QueryFieldImpl(exp);
	}
	
	private ExtQueryImpl extQuery;
	private String fieldExpr;
	private Object value;
	
	private String fieldName;
	private String operator;
	

	QueryFieldImpl(String fieldExpr) {
		super();
		this.fieldExpr = fieldExpr;
	}
	
	public void init(ExtQuery extQuery, Object value){
		this.extQuery = (ExtQueryImpl)extQuery;
		this.value = value;
		
		String[] sp = StringUtils.split(fieldExpr, SQLSymbolManager.SPLIT_SYMBOL);
		this.fieldName = sp[0];
		if(sp.length==2)
			this.operator = sp[1];
		else
			this.operator = "=";
	}

	public String getActualFieldName() {
		String newf = this.fieldName;
		if(newf.startsWith(K.FUNC)){
			newf = processFunction(newf);
		}else{
			newf = extQuery.getFieldName(newf);
		}
		return newf;
	}

	public String processFunction(String f) {
		String result = f;
		
		int leftParentheses = f.indexOf('(');
		int rightParentheses = f.indexOf(')');
		if(leftParentheses==-1 || rightParentheses==-1)
			throw new ServiceException("the function must with parentheses : " + f);
		
		String fname = f.substring(K.FUNC.length(), leftParentheses);
		String paramString = f.substring(leftParentheses+1, rightParentheses);
		if(StringUtils.isBlank(paramString))
			throw new ServiceException("the function's parameter can not be emtpy : " + paramString);
		
		String[] args = StringUtils.split(paramString, ",");
		args[0] = extQuery.getFieldName(args[0]);
		result = extQuery.getSqlFunctionManager().exec(fname, (Object[])args);
		
		return result;
	}

	public String getOperator() {
		return operator;
	}

	public String getFieldExpr() {
		return fieldExpr;
	}

	public ExtQuery getExtQuery() {
		return extQuery;
	}

	public Object getValue() {
		return value;
	}

	public String getFieldName() {
		return fieldName;
	}
}
