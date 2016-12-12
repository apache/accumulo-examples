package org.apache.accumulo.examples.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

public class Help {
  @Parameter(names = {"-h", "-?", "--help", "-help"}, help = true)
  public boolean help = false;

  public void parseArgs(String programName, String[] args, Object... others) {
    JCommander commander = new JCommander();
    commander.addObject(this);
    for (Object other : others)
      commander.addObject(other);
    commander.setProgramName(programName);
    try {
      commander.parse(args);
    } catch (ParameterException ex) {
      commander.usage();
      exitWithError(ex.getMessage(), 1);
    }
    if (help) {
      commander.usage();
      exit(0);
    }
  }

  public void exit(int status) {
    System.exit(status);
  }

  public void exitWithError(String message, int status) {
    System.err.println(message);
    exit(status);
  }
}
