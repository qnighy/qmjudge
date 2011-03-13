package code.comet

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

class CometSubmissionList extends CometActor with CometListener {
  val problem = S.location.get.currentValue.get.asInstanceOf[Problem]
  def registerWith = SubmissionUpdateServer
  override def lowPriority = {
    case l:List[(User,Problem)] => {
      if(l.exists(_==(User.currentUser.get,problem))) {
        detailed_submission = newest_submission
        reRender()
      }
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

