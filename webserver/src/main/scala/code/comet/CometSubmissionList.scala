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
import _root_.scala.xml.NodeSeq
import code.lib._
import code.model._

class CometSubmissionList extends CometActor {
  val problem = S.location.get.currentValue.get.asInstanceOf[Problem]
  override def lowPriority = {
    case s:Submission => {
      // detailed_submission = s
      reRender()
    }
    case TestResult(result, outdata, errdata) => {
      test_result = result
      test_outdata = outdata
      test_errdata = errdata
      reRender()
    }
  }

  def newest_submission:Box[Submission] =
      Submission.find(
            By(Submission.user, User.currentUser),
            By(Submission.problem, problem),
            OrderBy(Submission.compile_time, Descending))

  private var detailed_submission:Box[Submission] = newest_submission
  private val detail_id = uniqueId+"_submission_detail"
  private var detail_xhtml:NodeSeq = List()
  def render = {
    ".submit-form" #> {xhtml:NodeSeq =>
      <lift:form.post>{
        renderSubmitForm(xhtml)
      }</lift:form.post>
    }&
    ".submission-list" #> (submission_list(_)) &
    ".submission-detail" #> {xhtml:NodeSeq =>
      detail_xhtml = xhtml
      detailed_submission match {
        case Full(s) => <div id={detail_id}>{renderSubmissionDetail(s)(xhtml)}</div>
        case _ => <div id={detail_id}></div>
      }
    } &
    ClearClearable
  }

  def renderSubmitForm = {
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
      SetHtml("editor_area", langArea)
    }

    def reset_source():JsCmd = {
      SetHtml("editor_area", langArea_withdefault)
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
        SavedFile.find(By(SavedFile.savedcode, s), By(SavedFile.name, f)) match {
          case Full(sff) => sf.code(sff.code.is)
          case _ => sf.code("")
        }
        sf.save()
      }
      QueryServer ! new CompileQuery(cs, this)
      _Noop
    }

    "name=lang" #> ajaxSelect(
      problem.langs.map(l => (l,JudgeManager.langDescription(l))),
      Full(s.lang),
      changeLang) &
    "#editor_area *" #> langArea &
    "name=reset" #> ajaxSubmit("Reset", reset_source) &
    "name=save" #> ajaxSubmit("Save", save) &
    "name=compile" #> ajaxSubmit("Compile", compile)
  }

  def submission_list(xhtml:NodeSeq):NodeSeq =
    <xml:group>{
      Submission.findAll(
            By(Submission.user, User.currentUser),
            By(Submission.problem, problem),
            OrderBy(Submission.compile_time, Descending)).
          flatMap({s =>
        a(updateSubmissionDetail(s), renderSubmission(s)(xhtml))
      })
    }</xml:group>

  def renderSubmission(s:Submission) =
    ".submission-compile-time" #> s.compile_time.is.toString &
    ".submission-lang" #> s.langname &
    ".submission-state" #> s.state.is

  def updateSubmissionDetail(s:Submission):()=>JsCmd = { () =>
    detailed_submission = Full(s)
    SetHtml(detail_id, renderSubmissionDetail(s)(detail_xhtml))
  }

  private var test_input:String = ""
  private var test_result:CaseResult = new CaseResult(ResultDescription.SuccessfullyRun, 0, 0)
  private var test_outdata:String = ""
  private var test_errdata:String = ""

  def renderSubmissionDetail(s:Submission) =
    renderSubmission(s) &
    ".submission-compile-result" #> <pre><samp>{s.compile_result}</samp></pre> &
    ".if-compile-succeeded" #> { xhtml:NodeSeq =>
      if(s.state=="Compiling" || s.state=="Compile Error") <span></span>
      else renderTesterForm(s)(xhtml)
    }

  def renderTesterForm(s:Submission) =
    "name=test-input" #> textarea(test_input, test_input = _) &
    ".test-result" #> test_result.description &
    ".test-outdata" #> test_outdata &
    ".test-errdata" #> test_errdata &
    "name=run-test" #> ajaxSubmit("", { () =>
      QueryServer ! new TestQuery(s, test_input, this)
      _Noop
    })
}

