package code.comet

import _root_.net.liftweb.actor._
import _root_.net.liftweb.common._
import _root_.net.liftweb.http._
import _root_.scala.actors.Actor._
import _root_.scala.actors._
import code.lib._
import code.model._

object SubmissionUpdateServer extends LiftActor with ListenerManager{
  private var updates:List[(User,Problem)] = Nil
  def createUpdate = updates
  override def lowPriority = {
    case up:(User,Problem) => {
      updates ::= up
      updateListeners()
      updates = Nil
    }
  }
}


