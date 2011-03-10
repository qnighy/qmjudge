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
  object compile_result extends MappedText(this)
  object judge_result extends MappedText(this)
  object score extends MappedDouble(this)

  def langname:String = JudgeManager.langDescription(lang.is)
  def files:List[String] = problem.obj.get.files(lang.is)
  def findfile(f:String) =
    SourceFile.find(By(SourceFile.submission, this), By(SourceFile.name, f)) match {
      case Full(sf) => sf
      case _ => {
        val sf = SourceFile.create.submission(this).name(f).code("")
        sf.save
        sf
      }
    }
}

object Submission extends Submission with LongKeyedMetaMapper[Submission]

class SourceFile extends LongKeyedMapper[SourceFile] with IdPK {
  def getSingleton = SourceFile
  object submission extends LongMappedMapper(this, Submission)
  object name extends MappedString(this, 100)
  object code extends MappedText(this)
}

object SourceFile extends SourceFile with LongKeyedMetaMapper[SourceFile]
