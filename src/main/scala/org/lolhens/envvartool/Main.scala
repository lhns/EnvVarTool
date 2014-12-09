package org.lolhens.envvartool

import at.jta.Regor

/**
 * Created by LolHens on 09.12.2014.
 */
object Main {
  val version = "1.0"
  val envVarPath = "HKLM\\SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Environment"
  val regor = new Regor()

  def main(args: Array[String]): Unit = {
    val window = new EnvVarEditor("Path")
  }
}