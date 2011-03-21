package code.lib

import _root_.scala.actors._
import _root_.scala.actors.Actor._
import _root_.scala.collection.immutable._
import _root_.scala.util.Random
import _root_.net.liftweb.http._
import code.lib._
import code.model._
import JudgeManager._

object QueryServer {
  val server_list:List[String] = List("local")
  val servers:Map[String,QueryServer] =
    Map( server_list.map { s => (s -> new QueryServer()) } : _*)

  def select_server():String = server_list(Random.nextInt(server_list.size))
}

class QueryServer extends Actor {
  override def act = loop {
    react {
      case CompileQuery(cs,returnee) => {
        assert(cs.state.is == "Compiling")

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
        assert(ts.runnable)

        writeFileAll(session_file(ts, "input.txt"), indata)

        val tproc = run_qmjutil(ts, "run-once")

        val outdata = readFileAll(session_resultfile(ts,"output.txt"))
        val errdata = readFileAll(session_resultfile(ts,"stderr.txt"))
        val timedata = parse_timedata(readFileAll(session_resultfile(ts,"time.txt")))
        val time = timedata._1
        val mem = timedata._2

        val tresult = readAll(tproc.getInputStream())
        val description:String =
          if(mem > ts.problem.obj.get.memlimit)
            "MemoryLimitExceeded"
          else if(time > ts.problem.obj.get.timelimit)
            "TimeLimitExceeded"
          else if(tresult containsSlice "RuntimeError")
            "RuntimeError"
          else if(tproc.exitValue()==1)
            "SystemError"
          else
            "SuccessfullyRun"

        returnee ! new TestResult(description, time, mem, indata, outdata, errdata)
      }
      case JudgeQuery(js,returnee) => {
        assert(js.state.is == "Queueing")

        val n = problem_datalen(js.problem.obj.get)
        for(i <- 0 until n) {
          js.case_result(i)
        }
        returnee ! new PartialJudgeResult(js)
        for(i <- 0 until n) {
          val cr = js.case_result(i)
          val jproc = run_qmjutil(js, "judge-once", i.toString)

          val timedata = parse_timedata(readFileAll(session_resultfile(js,"time.txt")))
          val time = timedata._1
          val mem = timedata._2

          val jresult = readAll(jproc.getInputStream())
          val description:String =
            if(mem > js.problem.obj.get.memlimit)
              "MemoryLimitExceeded"
            else if(time > js.problem.obj.get.timelimit)
              "TimeLimitExceeded"
            else if(jresult containsSlice "RuntimeError")
              "RuntimeError"
            else if(jproc.exitValue()==1)
              "SystemError"
            else if(jresult containsSlice "Wrong")
              "WrongAnswer"
            else if(jresult containsSlice "Correct")
              "Accepted"
            else
              "SystemError"

          cr.description(description).time(time).mem(mem).save()
          returnee ! new PartialJudgeResult(js)
        }
        js.state("Judged").save()
        returnee ! new PartialJudgeResult(js)
      }
    }
  }
  this.start
}

case class CompileQuery(val s:Submission, val returnee:CometActor)

case class TestQuery(val s:Submission, val indata:String, val returnee:CometActor)

case class JudgeQuery(val s:Submission, val returnee:CometActor)

case class TestResult(description:String, time:Int, mem:Int, indata:String, outdata:String, errdata:String) {
  def description_tm:String =
    JudgeManager.description_tm(description, time, mem)
}

case class PartialJudgeResult(val s:Submission)
