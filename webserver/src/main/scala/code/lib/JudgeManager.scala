package code.lib

import code.model._
import _root_.scala.collection.JavaConversions._
import _root_.java.io.File

object JudgeManager {
  val langDescription:Map[String,String] = Map("cpp"->"C++","java"->"Java")
  val qmjutil_file = new File("/home/qnighy/qmjudge/judgement/util/qmjutil")
  val session_base_dir = new File("/home/qnighy/qmjudge/judgement/session/")
  val problem_base_dir = new File("/home/qnighy/qmjudge/judgement/prob/")

  def run_qmjutil(s:Submission, args:String*):java.lang.Process = {
    val proc = new ProcessBuilder(
        qmjutil_file.getAbsolutePath() ::
        session_dir(s).getAbsolutePath() ::
        s.problem.obj.get.dirname.is ::
        s.lang.is ::
        args.toList).redirectErrorStream(true).start()
    proc.getOutputStream().close()
    proc.waitFor()
    return proc
  }

  def session_dir(s:Submission):File = {
    val ret = new File(session_base_dir, s.id.is.toString)
    ret.mkdir()
    return ret
  }
  def session_file(s:Submission, sp:String) = new File(session_dir(s), sp)

  def session_srcdir(s:Submission):File = {
    val ret = session_file(s, "Src")
    ret.mkdir()
    return ret
  }
  def session_srcfile(s:Submission, sp:String) = new File(session_srcdir(s), sp)

  def session_resultdir(s:Submission):File = {
    val ret = session_file(s, "Result")
    ret.mkdir()
    return ret
  }
  def session_resultfile(s:Submission, sp:String) = new File(session_resultdir(s), sp)

  def problem_dir(p:Problem) = new File(problem_base_dir,p.dirname.is)
  def problem_file(p:Problem, sp:String) = new File(problem_dir(p), sp)

  def problem_datalen(p:Problem):Int = {
    var i:Int = 0
    while(new File(problem_file(p, "data"), i.toString).isDirectory()) {
      i += 1
    }
    return i
  }

  def readAll(in:java.io.InputStream, limit:Int=1048576):String = {
    val isr = new java.io.InputStreamReader(in)

    val buf = new scala.collection.mutable.StringBuilder()
    val buf2 = new Array[Char](1024)
    var f = true
    while(f) {
      val numread = isr.read(buf2, 0, buf2.length)
      if(numread == -1) {
        f = false
      } else {
        buf.appendAll(buf2, 0, numread)
      }
      if(buf.length() > limit) f = false
    }
    isr.close()
    return buf.toString()
  }
  def writeAll(out:java.io.OutputStream, s:String):Unit = {
    val osw = new java.io.OutputStreamWriter(out)
    osw.write(s)
    osw.close()
  }
  def readFileAll(file:java.io.File, limit:Int=1048576):String = readAll(new java.io.FileInputStream(file), limit)
  def writeFileAll(file:java.io.File, s:String):Unit = writeAll(new java.io.FileOutputStream(file), s)

  val timedata_reg = "([.0-9]+) ([.0-9]+) ([0-9]+)\n".r
  def parse_timedata(s:String):(Int,Int) = {
    s match {
      case timedata_reg(x,y,z) => (((x.toDouble+y.toDouble)*1000).toInt, z.toInt)
      case _ => {
        System.out.println("********* Not Matched!!!!!!!!!!!! *******")
        (10000000, 10000000)
      }
    }
  }
}

