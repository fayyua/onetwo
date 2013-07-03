package org.onetwo.common.fish.event.oracle;

import java.util.List;

import org.onetwo.common.fish.event.JFishEventSource;
import org.onetwo.common.fish.event.JFishInsertEvent;
import org.onetwo.common.fish.event.JFishInsertEventListener;
import org.onetwo.common.fish.orm.JFishMappedEntry;
import org.onetwo.common.fish.orm.JdbcStatementContext;
import org.onetwo.common.utils.LangUtils;

public class JFishOracleInsertEventListener extends JFishInsertEventListener {

	protected void beforeDoInsert(JFishInsertEvent event, JFishMappedEntry entry){
		Object entity = event.getObject();

		if(entry.isEntity() && entry.getIdentifyField().isGeneratedValueFetchBeforeInsert()){
			Long id = null;
			if(LangUtils.isMultiple(entity)){
				List<Object> list = LangUtils.asList(entity);
				for(Object en : list){
					id = fetchIdentifyBeforeInsert(event, entry);
					entry.setId(en, id);
				}
			}else{
				id = fetchIdentifyBeforeInsert(event, entry);
				entry.setId(entity, id);
			}
		}
		
	}
	/********
	 * 不可以根据更新数量的数目来确定是否成功
	 * oracle如果使用了不符合jdbc3.0规范的本地批量更新机制（BatchPerformanceWorkaround=true），
	 * 每次插入的返回值都是{@linkplain java.sql.Statement#SUCCESS_NO_INFO -2}
	 */
	@Override
	protected void doInsert(JFishInsertEvent event, JFishMappedEntry entry) {
		JFishEventSource es = event.getEventSource();
		this.beforeDoInsert(event, entry);
		Object entity = event.getObject();
		JdbcStatementContext<List<Object[]>> insert = entry.makeInsert(entity);
		/*
		String sql = insert.getSql();
		List<Object[]> args = insert.getValue();
		
		int count = executeJdbcUpdate(event, sql, args, es);*/
		int count = this.executeJdbcUpdate(es, insert);
		
		event.setUpdateCount(count);
	}
	
	public Long fetchIdentifyBeforeInsert(JFishInsertEvent event, JFishMappedEntry entry){
		JFishEventSource es = event.getEventSource();
		Long id = es.getJFishJdbcTemplate().queryForLong(entry.getStaticSeqSql());
		return id;
	}

}
