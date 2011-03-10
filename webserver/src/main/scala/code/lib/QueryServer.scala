package code.lib

import _root_.scala.actors._
import _root_.scala.actors.Actor._
import _root_.net.liftweb.http._
import code.lib._
import code.model._
import JudgeManager._

object QueryServer extends Actor {
  override def act = loop {
    react {
      case CompileQuery(cs) => {
        assert(cs.state.is == "Compiling")
        cs.files.foreach {f =>
          writeFileAll(session_srcfile(cs, f), cs.findfile(f).code.is)
        }
        val cproc = run_qmjutil(cs, "build-program")

        cs.compile_result(readAll(cproc.getErrorStream()))

        if(cproc.exitValue()==0) {
          cs.state("Compiled").save()
        } else {
          cs.state("Compile Error").save()
        }
        code.comet.SubmissionUpdateServer ! (cs.user.obj.get, cs.problem.obj.get)
      }
      case TestQuery(ts,indata,returnee) => {
        assert(ts.state.is == "Compiled")

        writeFileAll(session_file(ts, "input.txt"), indata)

        val tproc = run_qmjutil(ts, "run-once")

        val tresult = readAll(tproc.getInputStream())
        val resultString:String =
          if(tresult containsSlice "RuntimeError")
            "Runtime Error"
          else if(tproc.exitValue()==1)
            "System Error"
          else
            "Succesfully Run"

        val outdata = readFileAll(session_resultfile(ts,"output.txt"))
        val errdata = readFileAll(session_resultfile(ts,"stderr.txt"))
        val timedata = readFileAll(session_resultfile(ts,"time.txt"))

        returnee ! new TestResult(resultString, outdata, errdata, timedata)
      }
    }
  }
  this.start
}

case class CompileQuery(val s:Submission)

case class TestQuery(val s:Submission, val indata:String, val returnee:CometActor)

case class TestResult(resultString:String, outdata:String, errdata:String, timedata:String)
