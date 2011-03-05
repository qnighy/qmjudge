package code.model

import net.liftweb.mapper._

class Problem extends LongKeyedMapper[Problem] with IdPK {
  def getSingleton = Problem
  object published extends MappedBoolean(this)
  object title extends MappedString(this, 256)
  object statement extends MappedText(this)
}

object Problem extends Problem with LongKeyedMetaMapper[Problem] {
  override def fieldOrder = List(id)
}

