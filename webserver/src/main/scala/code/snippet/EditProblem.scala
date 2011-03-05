package code.snippet

import _root_.scala.xml.{NodeSeq, Text}
import _root_.net.liftweb.http.{StatefulSnippet, S}
import _root_.net.liftweb.http.SHtml._
import _root_.net.liftweb.util._
import _root_.net.liftweb.util.Helpers._
import _root_.net.liftweb.common._
import code.lib._

class EditProblem(problem:code.model.Problem) extends StatefulSnippet {
  def dispatch = {case "render" => render}

  def render =
    "name=title" #> problem.title.toForm &
    "name=statement" #> problem.statement.toForm &
    "name=published" #> problem.published.toForm &
    "name=dirname" #> problem.dirname.toForm &
    "type=submit" #> onSubmit(str =>
      problem.validate match {
        case Nil => problem.save ; S.notice("Problem saved!")
        case x => S.error(x)
      }
    )
}

