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
    var lang:String = defaultLang
    var upfile:scala.collection.mutable.Map[String,String] =
      scala.collection.mutable.Map()

    Submission.find(
      By(Submission.problem, problem),
      By(Submission.user, User.currentUser),
      OrderBy(Submission.datetime, Descending)
    ) match {
      case Full(s) =>
        if(files.isDefinedAt(s.lang.is)) lang = s.lang.is
        s.source_files.all.foreach(sf =>
          // if(files(lang)(sf.name)) upfile(sf.name) = sf.code
          upfile(sf.name) = sf.code
        )
      case Empty =>
      case Failure(s,e,c) =>
    }
    def langArea:NodeSeq =
      files(lang).toList.flatMap(f =>
        <h4>{f}</h4> ++
        // textarea(upfile.getOrElse(f,""), upfile(f) = _)
        textarea(upfile.getOrElse(f,""), { v =>
          System.out.println("value = '"+v+"'");
          upfile(f) = v
        })
      )
    def changeLang(l:String):JsCmd = {
      if(files.isDefinedAt(l)) {
        lang = l
        upfile = scala.collection.mutable.Map()
      }
      SetHtml("editor_area", langArea)
    }

    def save():JsCmd = {
      System.out.println("saving...")
      val submission = Submission.create
      submission.problem(problem)
      submission.user(User.currentUser)
      submission.lang(lang)
      submission.state("Saved")
      submission.save
      files(lang).foreach {f =>
        val sourcefile = SourceFile.create
        sourcefile.submission(submission)
        sourcefile.name(f)
        sourcefile.code(upfile(f))
        sourcefile.save
      }
      submission.validate match {
        case Nil => submission.save; S.notice("Saved!")
        case x => S.notice(x toString)
      }
      _Noop
    }

    "name=lang" #> ajaxSelect(
      files.keys.map(l => (l,SubmitProblem.langDescription(l))).toSeq,
      Full(lang),
      changeLang) &
    "#editor_area *" #> langArea &
    "name=save" #> ajaxSubmit("Save", save) &
    "name=compile" #> ajaxSubmit("Compile", () =>
      Alert("compile")
    )
  }
}

object SubmitProblem {
  val langDescription:Map[String,String] = Map("cpp"->"C++","java"->"Java")
}

