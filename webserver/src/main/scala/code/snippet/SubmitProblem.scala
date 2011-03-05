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

  def files:Map[String,Set[String]] = Map(
    "cpp" -> Set("Main.cpp"),
    "java" -> Set("Main.java")
  )

  def defaultLang:String = files.keys.head


  def render = {
    val s:Submission =
      Submission.find(
        By(Submission.problem, problem),
        By(Submission.user, User.currentUser),
        By(Submission.state, "Saved"),
        OrderBy(Submission.datetime, Descending)
      ) match {
        case Full(sn) => sn
        case _ => Submission.create.problem(problem).user(User.currentUser).state("Saved")
      }
    def findfile(f:String) =
      SourceFile.find(By(SourceFile.submission, s), By(SourceFile.name, f)) match {
        case Full(sf) => sf
        case _ => {
          val sf = SourceFile.create.submission(s).name(f)
          sf.save
          sf
        }
      }
    def filenames:List[String] = files(s.lang).toList.sorted
    def langArea:NodeSeq =
      filenames.flatMap(f =>
        <h4>{f}</h4> ++
        textarea(findfile(f).code, findfile(f).code(_).save)
      )
    def changeLang(l:String):JsCmd = {
      if(files.isDefinedAt(l)) {
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
      s.datetime(new java.util.Date())
      s.save
      Alert("compile() not implemented; only saved"); // _Noop
    }

    "name=lang" #> ajaxSelect(
      files.keys.map(l => (l,SubmitProblem.langDescription(l))).toSeq,
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

