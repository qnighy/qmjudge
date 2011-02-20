package code.model

import net.liftweb.mapper._

class TestDatum extends LongKeyedMapper[TestDatum] {
  def getSingleton = TestDatum
  def primaryKeyField = id
  object id extends MappedLongIndex(this)
  object problem extends LongMappedMapper(this, Problem)
  object input extends MappedText(this)
  object output extends MappedText(this)
}

object TestDatum extends TestDatum with LongKeyedMetaMapper[TestDatum]

class Problem extends LongKeyedMapper[Problem] with OneToMany[Long, Problem] {
  def getSingleton = Problem
  def primaryKeyField = id
  object id extends MappedLongIndex(this)
  object public extends MappedBoolean(this)
  object title extends MappedString(this, 256)
  object problemStatement extends MappedText(this)
  object referenceImplementation extends MappedText(this)
  object testData extends MappedOneToMany(TestDatum, TestDatum.problem, OrderBy(TestDatum.id, Ascending))
}

object Problem extends Problem with LongKeyedMetaMapper[Problem]
