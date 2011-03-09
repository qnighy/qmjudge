package code.snippet

import _root_.scala.collection.immutable._
import _root_.scala.xml.{NodeSeq, Text}
import _root_.net.liftweb.http.{StatefulSnippet, S}
import _root_.net.liftweb.http.SHtml._
import _root_.net.liftweb.http.js._
import _root_.net.liftweb.http.js.JsCmds._
import _root_.net.liftweb.util._
import _root_.net.liftweb.util.Helpers._
import _root_.net.liftweb.common._
import _root_.net.liftweb.mapper._
import code.lib._
import code.model._

class SubmitProblem(problem:code.model.Problem) extends StatefulSnippet {
  def dispatch = {case "render" => render}

  def render = {
    val s:Submission =
      Submission.find(
        By(Submission.problem, problem),
        By(Submission.user, User.currentUser),
        By(Submission.state, "Saved"),
        OrderBy(Submission.datetime, Descending)
      ) match {
        case Full(sn) => sn
        case _ => Submission.create.problem(problem).user(User.currentUser).lang(problem.langs.head).state("Saved").saveMe()
      }
    def langArea:NodeSeq =
      s.files.flatMap(f =>
        <h4>{f}</h4> ++
        textarea(s.findfile(f).code, s.findfile(f).code(_).save)
      )
    def changeLang(l:String):JsCmd = {
      if(problem.langs.contains(l)) {
        s.lang(l)
      }
      SetHtml("editor_area", langArea)
    }

    def save():JsCmd = {
      s.datetime(new java.util.Date())
      s.save
      _Noop
    }

    def compile():JsCmd = {
      save()
      s.datetime(new java.util.Date())
      s.save
      val cs = Submission.create
      cs.problem(s.problem.is)
      cs.user(s.user.is)
      cs.datetime(new java.util.Date())
      cs.lang(s.lang.is)
      cs.state("Compiling")
      cs.score(0.0)
      cs.save()
      s.files.foreach {f =>
        val sf = SourceFile.create.submission(cs).name(f)
        SourceFile.find(By(SourceFile.submission, s), By(SourceFile.name, f)) match {
          case Full(sff) => sf.code(sff.code.is)
          case _ => sf.code("")
        }
        sf.save()
      }
      QueryServer ! new CompileQuery(cs)
      code.comet.SubmissionUpdateServer ! (cs.user.obj.get, cs.problem.obj.get)
      _Noop
    }

    "name=lang" #> ajaxSelect(
      problem.langs.map(l => (l,SubmitProblem.langDescription(l))),
      Full(s.lang),
      changeLang) &
    "#editor_area *" #> langArea &
    "name=save" #> ajaxSubmit("Save", save) &
    "name=compile" #> ajaxSubmit("Compile", compile)
  }
}

object SubmitProblem {
  val langDescription:Map[String,String] = Map("cpp"->"C++","java"->"Java")
}

