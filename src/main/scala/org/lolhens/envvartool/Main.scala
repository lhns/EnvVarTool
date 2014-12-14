package org.lolhens.envvartool


/**
 * Created by LolHens on 09.12.2014.
 */
object Main {
  val version = "1.0"
  val envVarPath = "SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Environment"

  def main(args: Array[String]): Unit = {
    val window = new EnvVarManager()
  }
}