package org.onetwo.common.fish.event.oracle;

import java.util.List;

import org.onetwo.common.fish.event.JFishInsertEvent;
import org.onetwo.common.fish.exception.JFishOrmException;
import org.onetwo.common.fish.orm.JFishMappedEntry;
import org.onetwo.common.fish.orm.JdbcStatementContext;
import org.onetwo.common.profiling.TimeCounter;
import org.onetwo.common.utils.Assert;
import org.onetwo.common.utils.LangUtils;

public class JFishOracleBatchInsertEventListener extends JFishOracleInsertEventListener {
	
	protected void beforeDoInsert(JFishInsertEvent event, JFishMappedEntry entry){
		Object entity = event.getObject();

		List<Object> list = LangUtils.asList(entity);
		
		TimeCounter counter = new TimeCounter("select batch seq...");
		counter.start();
		String seq_sql = entry.getStaticSeqSql() + " connect by rownum <= ?";
		List<Long> seqs = event.getEventSource().getJFishJdbcTemplate().queryForList(seq_sql, Long.class, list.size());
		Assert.isTrue(list.size()==seqs.size(), "the size of seq is not equals to data, seq size:"+seqs.size()+", data size:"+list.size());
		counter.stop();
		
		int i = 0;
		for(Object en : list){
			entry.setId(en, seqs.get(i++));
		}
		
	}
	
	/********
	 * 不可以根据更新数量的数目来确定是否成功
	 * oracle如果使用了不符合jdbc3.0规范的本地批量更新机制（BatchPerformanceWorkaround=true），
	 * 每次插入的返回值都是{@linkplain java.sql.Statement#SUCCESS_NO_INFO -2}
	 */
	@Override
	protected void doInsert(JFishInsertEvent event, JFishMappedEntry entry) {
		if(!LangUtils.isMultiple(event.getObject())){
			throw new JFishOrmException("batch insert's args must be a Collection or Array!");
		}
		
		this.beforeDoInsert(event, entry);

		JdbcStatementContext<List<Object[]>> insert = entry.makeInsert(event.getObject());
		TimeCounter counter = new TimeCounter("batch insert "+insert.getValue().size()+"...");
		counter.start();
		int count = this.executeJdbcUpdate(true, insert.getSql(), insert.getValue(), event.getEventSource());
		if(count<0){
			count = Math.abs(count)/2;
		}
		counter.stop();
		
		event.setUpdateCount(count);
	}
}
