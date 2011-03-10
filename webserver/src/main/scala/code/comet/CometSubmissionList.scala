package code.comet

import _root_.net.liftweb.common._
import _root_.net.liftweb.http.SHtml._
import _root_.net.liftweb.http._
import _root_.net.liftweb.http.js.JsCmds._
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
        reRender()
      }
    }
  }

  def render = {
    ".submission-list" #> (submission_list(_)) &
    ClearClearable
  }

  def submission_list(xhtml:NodeSeq):NodeSeq =
    <xml:group>{
      Submission.findAll(
            By(Submission.user, User.currentUser),
            By(Submission.problem, problem)).
          flatMap({s =>
        renderSubmission(s)(xhtml)
      })
    }</xml:group>

  def renderSubmission(s:Submission) =
    ".submission-compile-time" #> s.compile_time.is &
    ".submission-lang" #> s.lang.is &
    ".submission-state" #> s.state.is
}

