package org.onetwo.common.db.parser.interceptors;

import java.util.List;

import org.onetwo.common.db.ExtQuery.K.IfNull;
import org.onetwo.common.db.parser.QueryContext;
import org.onetwo.common.db.parser.SqlCondition;
import org.onetwo.common.db.parser.SqlObject;

public interface DynamicQueryInterceptor {
	
	public void onParse(SqlObject sqlObject, List<SqlCondition> conditions);
	
	public void onCompile(SqlObject sqlObject, SqlCondition cond, IfNull ifNull, StringBuilder segment, QueryContext qcontext);
	
	public String translateSql(String sql, QueryContext qcontext);
	
	public String translateCountSql(String countSql, QueryContext qcontext);

}
