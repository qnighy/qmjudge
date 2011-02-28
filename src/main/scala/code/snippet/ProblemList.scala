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
    ".problem-id *" #> prob.id.is

  def render(xhtml:NodeSeq):NodeSeq =
    <xml:Group>{
      Problem.findAll.flatMap { prob => probRender(prob)(xhtml) }
    }</xml:Group>
}


