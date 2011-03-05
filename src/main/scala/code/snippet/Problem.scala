package code.snippet

import _root_.scala.xml.{NodeSeq, Text}
import _root_.net.liftweb.http.S
import _root_.net.liftweb.http.SHtml._
import _root_.net.liftweb.util._
import _root_.net.liftweb.util.Helpers._
import _root_.net.liftweb.common._
import code.lib._

class Problem(problem:code.model.Problem) {
  def render =
    ".problem-id" #> problem.id.is &
    ".problem-title" #> problem.title.is &
    ".problem-statement-string" #> problem.statement.is &
    ".problem-edit-link [href]" #> ("/edit-problem/" + problem.id.is.toString ) &
    ".problem-link [href]" #> ("/problem/" + problem.id.is.toString )
}

