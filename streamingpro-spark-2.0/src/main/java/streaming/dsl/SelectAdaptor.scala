package streaming.dsl

import org.antlr.v4.runtime.misc.Interval
import streaming.dsl.parser.DSLSQLLexer
import streaming.dsl.parser.DSLSQLParser.SqlContext
import streaming.dsl.template.TemplateMerge


/**
  * Created by allwefantasy on 27/8/2017.
  */
class SelectAdaptor(scriptSQLExecListener: ScriptSQLExecListener) extends DslAdaptor {
  override def parse(ctx: SqlContext): Unit = {
    val input = ctx.start.getTokenSource().asInstanceOf[DSLSQLLexer]._input

    val start = ctx.start.getStartIndex()
    val stop = ctx.stop.getStopIndex()
    val interval = new Interval(start, stop)
    val originalText = input.getText(interval)
    val chunks = originalText.split("\\s+")
    val tableName = chunks.last.replace(";", "")
    var sql = originalText.replaceAll(s"as[\\s|\\n]+${tableName}", "")
    sql = TemplateMerge.merge(sql, scriptSQLExecListener.env().toMap)
    scriptSQLExecListener.sparkSession.sql(sql).createOrReplaceTempView(tableName)
  }
}
