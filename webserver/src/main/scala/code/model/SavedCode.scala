package code.model

import net.liftweb.mapper._
import net.liftweb.common._

class SavedCode extends LongKeyedMapper[SavedCode] with IdPK with OneToMany[Long, SavedCode] {
  def getSingleton = SavedCode
  object problem extends LongMappedMapper(this, Problem)
  object user extends LongMappedMapper(this, User)
  object savetime extends MappedDateTime(this) {
    override def defaultValue = new java.util.Date
  }
  object lang extends MappedString(this, 256)

  def files:List[String] = problem.obj.get.files(lang.is)
  def findDefault(f:String):String =
        problem.obj.get.fileDefaultValue(lang.is, f) match {
          case Full(code) => code
          case _ => ""
        }
  def findfile(f:String):SavedFile =
    SavedFile.find(By(SavedFile.savedcode, this), By(SavedFile.name, f)) match {
      case Full(sf) => sf
      case _ => {
        val sf = SavedFile.create.savedcode(this).name(f).code(findDefault(f))
        sf.save
        sf
      }
    }
}

object SavedCode extends SavedCode with LongKeyedMetaMapper[SavedCode]

class SavedFile extends LongKeyedMapper[SavedFile] with IdPK {
  def getSingleton = SavedFile
  object savedcode extends LongMappedMapper(this, SavedCode)
  object name extends MappedString(this, 256)
  object code extends MappedText(this)
}

object SavedFile extends SavedFile with LongKeyedMetaMapper[SavedFile]

