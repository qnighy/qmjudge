package code.model

import net.liftweb.mapper._
import net.liftweb.common._
import code.lib._

class Problem extends LongKeyedMapper[Problem] with IdPK {
  def getSingleton = Problem
  object title extends MappedString(this, 256)
  object statement extends MappedText(this)
  object dirname extends MappedString(this, 256) {
    override def defaultValue = ""
  }
  object published extends MappedBoolean(this)

  def files:Map[String,List[String]] = {
    val snip_dir = JudgeManager.problem_file(this, "snippet")
    if(snip_dir.isDirectory()) {
      snip_dir.list().toList.map({l =>
        val snip_dir_l = new java.io.File(snip_dir, l)
        l -> snip_dir_l.list().toList.sorted
      }).toMap
    } else {
      Map(
        "cpp" -> List("Main.cpp").sorted,
        "java" -> List("Main.java").sorted
      )
    }
  }
  def fileDefaultValue(lang:String, file:String):Box[String] = {
    val snip_dir = JudgeManager.problem_file(this, "snippet")
    val snip_dir_l = new java.io.File(snip_dir, lang)
    val snip_file = new java.io.File(snip_dir_l, file)
    if(snip_file.exists()) {
      Full(JudgeManager.readFileAll(snip_file))
    } else Empty
  }
  def timelimit:Int = {
    val f = JudgeManager.problem_file(this, "tl.txt")
    if(f.exists()) {
      JudgeManager.readFileAll(f) match {
        case Problem.intRegex(x) => x.toInt
        case _ => 0
      }
    } else 0
  }
  def memlimit:Int = {
    val f = JudgeManager.problem_file(this, "ml.txt")
    if(f.exists()) {
      JudgeManager.readFileAll(f) match {
        case Problem.intRegex(x) => x.toInt
        case _ => 0
      }
    } else 0
  }
  def langs:List[String] = files.keys.toList.sorted
}

object Problem extends Problem with LongKeyedMetaMapper[Problem] {
  val intRegex = "([0-9]+)\n".r
}
