package code.model

import net.liftweb.mapper._

class Problem extends LongKeyedMapper[Problem] with IdPK {
  def getSingleton = Problem
  object title extends MappedString(this, 256)
  object statement extends MappedText(this)
  object dirname extends MappedString(this, 256)
  object published extends MappedBoolean(this)
}

object Problem extends Problem with LongKeyedMetaMapper[Problem]
