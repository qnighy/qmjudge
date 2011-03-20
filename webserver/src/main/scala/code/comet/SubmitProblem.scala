package code.comet

import _root_.net.liftweb.actor._
import _root_.net.liftweb.common._
import _root_.net.liftweb.http.SHtml._
import _root_.net.liftweb.http._
import _root_.net.liftweb.http.js.JsCmds._
import _root_.net.liftweb.http.js.JsCmd
import _root_.net.liftweb.util.Helpers._
import _root_.net.liftweb.util._
import _root_.net.liftweb.mapper._
import _root_.scala.actors.Actor._
import _root_.scala.actors._
import _root_.scala.xml.{Elem, NodeSeq}
import _root_.scala.collection.immutable._
import code.lib._
import code.model._

class SubmitProblem extends CometActor {
  val problem = S.location.get.currentValue.get.asInstanceOf[Problem]
  override def lowPriority = {
    case s:Submission => {
      partialUpdate(SubmissionList.reHtml)
    }
    case r:TestResult => {
      TesterResultArea.result = r
      partialUpdate(TesterResultArea.reHtml)
    }
    case (s:Submission,r:JudgeResult) => {
      partialUpdate(SubmissionList.reHtml & JudgeResultArea.reHtml)
    }
  }

  def newest_submission:Box[Submission] =
      Submission.find(
            By(Submission.user, User.currentUser),
            By(Submission.problem, problem),
            OrderBy(Submission.compile_time, Descending))
  def render =
    "#submit-form *" #> (SubmitForm.render(_:NodeSeq)) &
    SubmissionList.binder &
    SubmissionDetail.binder &
    JudgeForm.binder &
    JudgeResultArea.binder &
    TesterForm.binder &
    TesterResultArea.binder

  object SubmitForm {
    var s:SavedCode =
      SavedCode.find(
        By(SavedCode.problem, problem),
        By(SavedCode.user, User.currentUser),
        OrderBy(SavedCode.savetime, Descending)
      ) match {
        case Full(sn) => sn
        case _ => SavedCode.create.problem(problem).user(User.currentUser).lang(problem.langs.head).saveMe()
      }
    def langArea:NodeSeq =
      s.files.flatMap(f =>
        <h4>{f}</h4> ++
        textarea(s.findfile(f).code, s.findfile(f).code(_).save)
      )
    def langArea_withdefault:NodeSeq =
      s.files.flatMap(f =>
        <h4>{f}</h4> ++
        textarea(s.findDefault(f), s.findfile(f).code(_).save)
      )
    def changeLang(l:String):JsCmd = {
      if(!problem.langs.contains(l)) return _Noop
      s =
        SavedCode.find(
          By(SavedCode.problem, problem),
          By(SavedCode.user, User.currentUser),
          By(SavedCode.lang, l),
          OrderBy(SavedCode.savetime, Descending)
        ) match {
          case Full(sn) => sn
          case _ => SavedCode.create.problem(problem).user(User.currentUser).lang(l).saveMe()
        }
      SetHtml("submission-editor", langArea)
    }

    def reset_source():JsCmd = {
      SetHtml("submission-editor", langArea_withdefault)
    }

    def save():JsCmd = {
      s.savetime(new java.util.Date)
      s.save
      _Noop
    }

    def compile():JsCmd = {
      s.save
      val cs = Submission.create
      cs.problem(s.problem.is)
      cs.user(s.user.is)
      cs.lang(s.lang.is)
      cs.state("Compiling")
      cs.score(0.0)
      cs.save()
      s.files.foreach {f =>
        val sf = SourceFile.create.submission(cs).name(f)
        sf.code(s.findfile(f).code.is)
        sf.save()
      }
      QueryServer ! new CompileQuery(cs, SubmitProblem.this)
      partialUpdate(SetHtml("submission-list-area",SubmissionList.render()))
      _Noop
    }

    def render(xhtml:NodeSeq):NodeSeq = ajaxForm(renderVal(xhtml))
    def renderVal:(NodeSeq=>NodeSeq) =
      "#submission-language" #> ajaxSelect(
        problem.langs.map(l => (l,JudgeManager.langDescription(l))),
        Full(s.lang),
        changeLang) &
      "#submission-editor *" #> langArea &
      "#reset-submission" #> ajaxSubmit("Reset", reset_source) &
      "#save-submission" #> ajaxSubmit("Save", save) &
      "#compile-submission" #> ajaxSubmit("Compile", compile)
  }

  object SubmissionList extends ReloadableComponent {
    override def component_id = "submission-list-area"

    def render():NodeSeq =
      (
        ".submission-list-item" #> (renderItems(_:NodeSeq))
      )(cache)

    def renderItems(xhtml:NodeSeq):NodeSeq =
        Submission.findAll(
          By(Submission.user, User.currentUser),
          By(Submission.problem, problem),
          OrderBy(Submission.compile_time, Descending)).
        flatMap({ s => renderItem(s)(xhtml) })
    def renderItem(s:Submission)(xhtml:NodeSeq):NodeSeq =
      if(SubmissionDetail.target == Full(s))
        SubmissionDescription.render(s)(xhtml)
      else
        a(SubmissionDetail.setSubmission(s), SubmissionDescription.render(s)(xhtml))
  }
  object SubmissionDescription {
    def render(s:Submission) =
      ".submission-compile-time" #> s.compile_time.is.toString &
      ".submission-lang" #> s.langname &
      ".submission-state" #> s.state.is
  }

  object SubmissionDetail extends ReloadableComponent {
    override def component_id = "submission-detail"
    var target:Box[Submission] = newest_submission

    def render():NodeSeq = {
      target match {
        case Full(s) => renderIt(s)(cache)
        case _ => NodeSeq.Empty
      }
    }
    def setSubmission(s:Submission):()=>JsCmd = { () =>
      target = Full(s)
      TesterResultArea.result = TesterResultArea.default_result
      SubmissionList.reHtml & SubmissionDetail.reHtml & JudgeForm.reHtml & JudgeResultArea.reHtml & TesterForm.reHtml & TesterResultArea.reHtml
    }

    def renderIt(s:Submission) =
      SubmissionDescription.render(s) &
      ".submission-compile-result" #> <pre><samp>{s.compile_result}</samp></pre>
  }
  object JudgeForm extends ReloadableComponent {
    override def component_id = "judge-form"
    override def render():NodeSeq = <lift:form.ajax>{renderVal()}</lift:form.ajax>
    def renderVal():NodeSeq = {
      (
        "#run-judge" #> { xhtml:NodeSeq =>
          SubmissionDetail.target match {
            case Full(s) if s.judgeable => ajaxSubmit(
              xhtml match {
                case Seq(e:Elem) => e.attribute("value").getOrElse("_").toString
                case _ => "_"
              }, { () =>
                assert(s.judgeable)
                s.state("Queueing").save()
                QueryServer ! new JudgeQuery(s, SubmitProblem.this)
                SubmissionList.reHtml & SubmissionDetail.reHtml & JudgeResultArea.reHtml
            })
            case _ => NodeSeq.Empty
          }
        }
      )(cache)
    }
  }
  object JudgeResultArea extends ReloadableComponent {
    override def component_id = "judge-result-area"

    override def render():NodeSeq = SubmissionDetail.target match {
      case Full(s) =>
        if(s.state.is == "Queueing")
          <p>Queueing...</p>
        else if(s.state.is == "Judging" || s.state.is == "Judged")
          renderVal(s)
        else
          NodeSeq.Empty
      case _ => NodeSeq.Empty
    }

    def renderVal(s:Submission):NodeSeq =
      (
        ".case-result-area" #> (renderCaseResults(s)(_:NodeSeq))
      )(cache)

    def renderCaseResults(s:Submission)(xhtml:NodeSeq):NodeSeq =
      new JudgeResult(s.judge_result.is).l.zipWithIndex.flatMap({ case (cr,i) =>
        renderCaseResult(cr,i)(xhtml)
      })
    def renderCaseResult(cr:CaseResult, i:Int):(NodeSeq=>NodeSeq) =
      ".case-id" #> i.toString &
      ".case-result" #> cr.description
  }
  object TesterForm extends ReloadableComponent {
    override def component_id = "tester-form"
    private var test_input:String = ""

    // override def render():NodeSeq = ajaxForm(renderVal())
    override def render():NodeSeq = <lift:form.ajax>{renderVal()}</lift:form.ajax>
    def renderVal():NodeSeq = {
      (
        "#tester-input-area" #> textarea(test_input, test_input = _) &
        "#run-tester" #> { xhtml:NodeSeq =>
          SubmissionDetail.target match {
            case Full(s) if s.runnable => ajaxSubmit(
              xhtml match {
                case Seq(e:Elem) => e.attribute("value").getOrElse("_").toString
                case _ => "_"
              }, { () =>
              QueryServer ! new TestQuery(s, test_input, SubmitProblem.this)
              TesterResultArea.result = TesterResultArea.waiting_result
              TesterResultArea.reHtml
            })
            case _ => NodeSeq.Empty
          }
        }
      )(cache)
    }
  }
  object TesterResultArea extends ReloadableComponent {
    override def component_id = "test-result-area"
    val default_result = new TestResult(
      new CaseResult(ResultDescription.NotYet, 0, 0), "", "", "");
    val waiting_result = new TestResult(
      new CaseResult(ResultDescription.Waiting, 0, 0), "", "", "");
    var result:TestResult = default_result

    override def render():NodeSeq = {
      (
        ".tester-result" #> result.result.description &
        ".tester-indata" #> result.indata &
        ".tester-outdata" #> result.outdata &
        ".tester-errdata" #> result.errdata
      )(cache)
    }
  }
}

abstract class ReloadableComponent {
  protected var cache:NodeSeq = NodeSeq.Empty
  def component_id:String
  def render():NodeSeq
  def binder:CssBindFunc = ("#"+component_id+" *") #> { xhtml:NodeSeq => cache = xhtml; render() }
  def reHtml:JsCmd = SetHtml(component_id, render())
}
