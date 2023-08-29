package com.example.camelseda

// basic dial command class which is a runnable
// Full solution has a class heirarch of different command types, executions, etc.
// Need this to demonstrate our 'logic' at runtime for our dynamic commands
class DIALCommand(val id: String, val commandName: String) : Runnable {
  
  fun isKillRoute() : Boolean {
    return this.commandName.equals("--dhKill")
  }

  override fun run() {
    //NOOP
  }
}