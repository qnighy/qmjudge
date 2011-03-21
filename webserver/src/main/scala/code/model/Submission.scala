package code.model

import net.liftweb.mapper._
import net.liftweb.common._
import code.lib._

class Submission extends LongKeyedMapper[Submission] with IdPK with OneToMany[Long, Submission] {
  def getSingleton = Submission
  object problem extends LongMappedMapper(this, Problem)
  object user extends LongMappedMapper(this, User)
  object compile_time extends MappedDateTime(this) {
    override def defaultValue = new java.util.Date
  }
  object lang extends MappedString(this, 256)
  object state extends MappedString(this, 256)
  object judge_server extends MappedString(this, 256)
  object compile_result extends MappedText(this)
  object score extends MappedDouble(this)

  def runnable:Boolean = state.is == "Compiled" || state.is == "Queueing" || state.is == "Judging" || state.is == "Judged"
  def judgeable:Boolean = state.is == "Compiled"

  def langname:String = JudgeManager.langDescription(lang.is)
  def files:List[String] = problem.obj.get.files(lang.is)

  def query_server:QueryServer = QueryServer.servers(judge_server)
  def case_results:Seq[CaseResult] =
    CaseResult.findAll(
      By(CaseResult.submission, this),
      OrderBy(CaseResult.caseid, Ascending))
  def case_result(i:Int):CaseResult = {
    CaseResult.find(
      By(CaseResult.submission, this),
      By(CaseResult.caseid, i)
    ) match {
      case Full(cr) => cr
      case _ => CaseResult.create.submission(this).caseid(i).saveMe()
    }
  }
}

object Submission extends Submission with LongKeyedMetaMapper[Submission]
