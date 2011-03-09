package code.model

import net.liftweb.mapper._

class Problem extends LongKeyedMapper[Problem] with IdPK {
  def getSingleton = Problem
  object title extends MappedString(this, 256)
  object statement extends MappedText(this)
  object dirname extends MappedString(this, 256)
  object published extends MappedBoolean(this)

  lazy val files:Map[String,List[String]] = Map(
    "cpp" -> List("Main.cpp").sorted,
    "java" -> List("Main.java").sorted
  )
  lazy val langs:List[String] = files.keys.toList.sorted
}

object Problem extends Problem with LongKeyedMetaMapper[Problem]
