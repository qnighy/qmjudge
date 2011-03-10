package code.lib

import _root_.scala.actors._
import _root_.scala.actors.Actor._
import _root_.net.liftweb.http._
import code.lib._
import code.model._

object QueryServer extends Actor {
  override def act = loop {
    react {
      case CompileQuery(cs) => {
        val iocs = new java.io.File("/home/qnighy/qmjudge/judgement/session/"+cs.id.is)
        val srcdir = new java.io.File(iocs, "Src")
        iocs.mkdir()
        srcdir.mkdir()
        cs.files.foreach {f =>
          val iosf = new java.io.File(srcdir, f);
          val iopw = new java.io.PrintWriter(new java.io.FileWriter(iosf))
          iopw.print(cs.findfile(f).code.is)
          iopw.close()
        }
        val cproc = new ProcessBuilder(
            "/home/qnighy/qmjudge/judgement/util/qmjutil",
            iocs.getAbsolutePath(),
            cs.problem.obj.get.dirname.is,
            cs.lang.is,
            "build-program"
          ).start();
        cproc.waitFor()

        val cresult_in = new java.io.BufferedReader(new java.io.InputStreamReader(cproc.getErrorStream()))
        val cresult = new StringBuffer()
        var line:String = null;
        while({line=cresult_in.readLine(); line!=null}) {
          cresult.append(line)
          cresult.append("\n")
        }
        cs.compile_result(cresult.toString)

        if(cproc.exitValue()==0) {
          cs.state("Compiled").save()
        } else {
          cs.state("Compile Error").save()
        }
        code.comet.SubmissionUpdateServer ! (cs.user.obj.get, cs.problem.obj.get)
      }
    }
  }
  this.start
}

case class CompileQuery(val s:Submission)

