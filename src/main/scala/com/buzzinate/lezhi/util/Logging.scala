package com.buzzinate.lezhi.util

trait Logging {

  /**
   * Logger for the type mixed into.
   */
  protected lazy val log = Logger(this.getClass)
}