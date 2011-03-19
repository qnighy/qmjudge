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
      TesterResultArea.setTestResult(r)
      partialUpdate(TesterResultArea.reHtml)
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
    private var item_cache:NodeSeq = NodeSeq.Empty

    def render():NodeSeq =
      (
        ".submission-list-item" #> (renderItems(_:NodeSeq))
      )(cache)
    def renderItems(xhtml:NodeSeq):NodeSeq = { item_cache = xhtml; renderItems() }
    def renderItems():NodeSeq =
      <xml:group>{
        Submission.findAll(
          By(Submission.user, User.currentUser),
          By(Submission.problem, problem),
          OrderBy(Submission.compile_time, Descending)).
        flatMap({s =>
          a(SubmissionDetail.setSubmission(s), SubmissionDescription.render(s)(item_cache))
               })
      }</xml:group>
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
      SubmissionDetail.reHtml & TesterForm.reHtml
    }

    def renderIt(s:Submission) =
      SubmissionDescription.render(s) &
      ".submission-compile-result" #> <pre><samp>{s.compile_result}</samp></pre>
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
            case Full(s) if s.state.is == "Compiled" => ajaxSubmit(
              xhtml match {
                case Seq(e:Elem) => e.attribute("value").getOrElse("_").toString
                case _ => "_"
              }, { () =>
              QueryServer ! new TestQuery(s, test_input, SubmitProblem.this)
              TesterResultArea.setTestResult(
                new TestResult(
                  new CaseResult(ResultDescription.Waiting, 0, 0), "", ""))
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
    private var test_input:String = ""
    private var test_result:CaseResult = new CaseResult(ResultDescription.NotYet, 0, 0)
    private var test_outdata:String = ""
    private var test_errdata:String = ""

    def setTestResult(r:TestResult):Unit = r match {
      case TestResult(result, outdata, errdata) => {
        test_result = result
        test_outdata = outdata
        test_errdata = errdata
      }
    }

    override def render():NodeSeq = {
      (
        ".tester-result" #> test_result.description &
        ".tester-outdata" #> test_outdata &
        ".tester-errdata" #> test_errdata
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
