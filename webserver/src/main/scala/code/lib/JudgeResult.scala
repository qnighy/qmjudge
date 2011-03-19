package code.lib

case class JudgeResult(l:List[CaseResult]) {
  def this(s:String) = this(s.lines.map{l => new CaseResult(l)}.toList)
  override def toString() = l.mkString("")
}

case class CaseResult(rd:ResultDescription.Value, time:Int, mem:Int) {
  def this(a:String, b:String, c:String) = this(ResultDescription.withName(a), b.toInt, c.toInt)
  def this(s:String) =
    this(
      s match { case CaseResult.re(a,_,_) => a },
      s match { case CaseResult.re(_,b,_) => b },
      s match { case CaseResult.re(_,_,c) => c })
  override def toString() = "%s %s %s\n".format(rd, time, mem)

  def description:String = rd match{
    case ResultDescription.Waiting => "Waiting"
    case ResultDescription.NotYet => "Not Yet Run"
    case _ => "%s / %s / %s".format(
      rd,
      "%02d.%03dsec".format(time/1000,time%1000),
      if(mem<10000) "Lowmem" else "%dKB".format(mem)
    )
  }
}

object CaseResult {
  val re = "([a-zA-Z]+) ([0-9]+) ([0-9]+)".r
}

object ResultDescription extends Enumeration {
  val SuccessfullyRun = Value("SuccessfullyRun")
  val Waiting = Value("Waiting")
  val NotYet = Value("NotYet")
  val Accepted = Value("Accepted")
  val WrongAnswer = Value("WrongAnswer")
  val RuntimeError = Value("RuntimeError")
  val TimeLimitExceeded = Value("TimeLimitExceeded")
  val MemoryLimitExceeded = Value("MemoryLimitExceeded")
  val OutputLimitExceeded = Value("OutputLimitExceeded")
  val SystemError = Value("SystemError")
}
