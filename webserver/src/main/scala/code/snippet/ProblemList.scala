package code.snippet

import _root_.scala.xml.{NodeSeq, Text}
import _root_.net.liftweb.http.{StatefulSnippet, S}
import _root_.net.liftweb.http.SHtml._
import _root_.net.liftweb.util._
import _root_.net.liftweb.util.Helpers._
import _root_.net.liftweb.common._
import code.lib._

class ProblemList {

  def render(xhtml:NodeSeq):NodeSeq =
    <xml:Group>{
      code.model.Problem.findAll.flatMap { prob => new Problem(prob).render(xhtml) }
    }</xml:Group>

  def problemAdder =
    "type=submit" #> onSubmit(str => {
      val problem = code.model.Problem.create
      problem.validate match {
        case Nil => problem.save ; S.notice("Problem saved!")
        case x => S.error(x)
      }
    })
}


