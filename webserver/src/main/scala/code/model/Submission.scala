package code.model

import net.liftweb.mapper._

class Submission extends LongKeyedMapper[Submission] with IdPK with OneToMany[Long, Submission] {
  def getSingleton = Submission
  object problem extends LongMappedMapper(this, Problem)
  object user extends LongMappedMapper(this, User)
  object datetime extends MappedDateTime(this) {
    override def defaultValue = new java.util.Date
  }
  object lang extends MappedString(this, 10)
  object source_files extends MappedOneToMany(SourceFile, SourceFile.submission, OrderBy(SourceFile.name, Ascending))
  object state extends MappedString(this, 10)
  object compile_result extends MappedText(this)
  object judge_result extends MappedText(this)
  object score extends MappedDouble(this)
}

object Submission extends Submission with LongKeyedMetaMapper[Submission]

class SourceFile extends LongKeyedMapper[SourceFile] with IdPK {
  def getSingleton = SourceFile
  object submission extends LongMappedMapper(this, Submission)
  object name extends MappedString(this, 100)
  object code extends MappedText(this)
}

object SourceFile extends SourceFile with LongKeyedMetaMapper[SourceFile]
