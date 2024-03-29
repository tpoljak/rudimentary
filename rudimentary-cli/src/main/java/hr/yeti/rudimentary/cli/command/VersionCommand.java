package hr.yeti.rudimentary.cli.command;

import hr.yeti.rudimentary.cli.Command;
import hr.yeti.rudimentary.cli.Rudy;
import java.util.Map;

public class VersionCommand implements Command {

  @Override
  public String name() {
    return "version";
  }

  @Override
  public String description() {
    return "Rudimentary cli tool version info.";
  }

  @Override
  public Map<String, String> options() {
    return Map.of();
  }

  @Override
  public void execute(Map<String, String> arguments) {
    System.console().printf(
        Rudy.CLI_INFO.getProperty("version")
        + System.lineSeparator()
    );
  }

}
