package code.model

import net.liftweb.mapper._
import net.liftweb.common._
import code.lib._

class CaseResult extends LongKeyedMapper[CaseResult] with IdPK {
  def getSingleton = CaseResult

  object submission extends LongMappedMapper(this, Submission)
  object caseid extends MappedInt(this)
  object description extends MappedString(this, 256) {
    override def defaultValue = "Waiting"
  }
  object time extends MappedInt(this)
  object mem extends MappedInt(this)

  def description_tm:String =
    JudgeManager.description_tm(description.is, time.is, mem.is)
}

object CaseResult extends CaseResult with LongKeyedMetaMapper[CaseResult]
