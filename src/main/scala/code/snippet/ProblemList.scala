package code.snippet

import _root_.scala.xml.{NodeSeq, Text}
import _root_.net.liftweb.http.{StatefulSnippet, S}
import _root_.net.liftweb.http.SHtml._
import _root_.net.liftweb.util._
import _root_.net.liftweb.util.Helpers._
import _root_.net.liftweb.common._
import code.lib._
import code.model._

class ProblemList {

  def probRender(prob:Problem) =
    ".problem-title *" #> prob.title.is &
    ".problem-id *" #> prob.id.is &
    ".problem-link [href]" #> ("/edit-problem/" + prob.id.is.toString )

  def render(xhtml:NodeSeq):NodeSeq =
    <xml:Group>{
      Problem.findAll.flatMap { prob => probRender(prob)(xhtml) }
    }</xml:Group>

  def problemAdder =
    "type=submit" #> onSubmit(str => {
      val problem = Problem.create
      problem.validate match {
        case Nil => problem.save ; S.notice("Problem saved!")
        case x => S.error(x)
      }
    })
}


