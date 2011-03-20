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
      case CompileQuery(cs,returnee) => {
        assert(cs.state.is == "Compiling")
        cs.files.foreach {f =>
          writeFileAll(session_srcfile(cs, f), cs.findfile(f).code.is)
        }
        val cproc = run_qmjutil(cs, "build-program")
        cs.compile_result(readAll(cproc.getInputStream()))
        val exv = cproc.exitValue();
        cproc.destroy();

        if(exv==0) {
          cs.state("Compiled").save()
        } else {
          cs.state("Compile Error").save()
        }
        returnee ! cs
      }
      case TestQuery(ts,indata,returnee) => {
        assert(ts.state.is == "Compiled")

        writeFileAll(session_file(ts, "input.txt"), indata)

        val tproc = run_qmjutil(ts, "run-once")

        val outdata = readFileAll(session_resultfile(ts,"output.txt"))
        val errdata = readFileAll(session_resultfile(ts,"stderr.txt"))
        val timedata = parse_timedata(readFileAll(session_resultfile(ts,"time.txt")))
        val time = timedata._1
        val mem = timedata._2

        val tresult = readAll(tproc.getInputStream())
        val resultDsc:ResultDescription.Value =
          if(mem > ts.problem.obj.get.memlimit)
            ResultDescription.MemoryLimitExceeded
          else if(time > ts.problem.obj.get.timelimit)
            ResultDescription.TimeLimitExceeded
          else if(tresult containsSlice "RuntimeError")
            ResultDescription.RuntimeError
          else if(tproc.exitValue()==1)
            ResultDescription.SystemError
          else
            ResultDescription.SuccessfullyRun

        returnee ! new TestResult(new CaseResult(resultDsc, time, mem), indata, outdata, errdata)
      }
    }
  }
  this.start
}

case class CompileQuery(val s:Submission, val returnee:CometActor)

case class TestQuery(val s:Submission, val indata:String, val returnee:CometActor)

case class JudgeQuery(val s:Submission, val returnee:CometActor)

case class TestResult(result:CaseResult, indata:String, outdata:String, errdata:String)
