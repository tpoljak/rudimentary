package hr.yeti.rudimentary.cli.command;

import hr.yeti.rudimentary.cli.Command;
import hr.yeti.rudimentary.cli.Rudy;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class HelpCommand implements Command {

  private static final int DESCRIPTION_INDEX = 25;

  @Override
  public String name() {
    return "help";
  }

  @Override
  public String description() {
    return "Rudimentary cli tool usage info.";
  }

  @Override
  public Map<String, String> options() {
    return Map.of();
  }

  @Override
  public void execute(Map<String, String> options) {
    String commands = Rudy.COMMANDS.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .map(e -> e.getKey() + spaces(e.getKey()) + e.getValue().description() + System.lineSeparator() + formatOptions(e.getValue().options()))
        .collect(Collectors.joining(System.lineSeparator()));

    System.console().printf(
        "Usage: COMMAND [OPTIONS...]"
        + System.lineSeparator()
        + System.lineSeparator()
        + "Commands"
        + System.lineSeparator()
        + "--------"
        + System.lineSeparator()
        + commands
        + System.lineSeparator()
    );
  }

  public String spaces(String commandName) {
    return new String(new char[DESCRIPTION_INDEX - commandName.length()]).replaceAll("\0", " ");
  }

  public String formatOptions(Map<String, String> options) {
    if (Objects.isNull(options) || options.isEmpty()) {
      return "";
    }

    return options.entrySet().stream()
        .map(e -> " --" + e.getKey() + spaces(" --" + e.getKey()) + e.getValue())
        .collect(Collectors.joining(System.lineSeparator())) + System.lineSeparator();
  }

}
