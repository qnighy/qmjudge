package code.lib

import code.model._
import _root_.scala.collection.JavaConversions._

object JudgeManager {
  val langDescription:Map[String,String] = Map("cpp"->"C++","java"->"Java")
  val qmjutil_path = "/home/qnighy/qmjudge/judgement/util/qmjutil"

  def run_qmjutil(s:Submission, args:String*):java.lang.Process = {
    val proc = new ProcessBuilder(
        qmjutil_path ::
        session_dir(s).getAbsolutePath() ::
        s.problem.obj.get.dirname.is ::
        s.lang.is ::
        args.toList).start()
    proc.waitFor()
    return proc
  }

  def session_dir(s:Submission):java.io.File = {
    val ret = new java.io.File("/home/qnighy/qmjudge/judgement/session/"+s.id.is)
    ret.mkdir()
    return ret
  }
  def session_file(s:Submission, sp:String) = new java.io.File(session_dir(s), sp)

  def session_srcdir(s:Submission):java.io.File = {
    val ret = session_file(s, "Src")
    ret.mkdir()
    return ret
  }
  def session_srcfile(s:Submission, sp:String) = new java.io.File(session_srcdir(s), sp)

  def session_resultdir(s:Submission):java.io.File = {
    val ret = session_file(s, "Result")
    ret.mkdir()
    return ret
  }
  def session_resultfile(s:Submission, sp:String) = new java.io.File(session_resultdir(s), sp)

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
}

