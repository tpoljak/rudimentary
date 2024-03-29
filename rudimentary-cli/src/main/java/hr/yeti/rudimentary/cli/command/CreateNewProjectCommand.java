package hr.yeti.rudimentary.cli.command;

import hr.yeti.rudimentary.cli.Command;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;

public class CreateNewProjectCommand implements Command {

  @Override
  public String name() {
    return "new";
  }

  @Override
  public String description() {
    return "Create new project.";
  }

  @Override
  public Map<String, String> options() {
    return Map.of(
        "location", "Set absolute path to location where the project will be created. If omitted, current directory will be used.",
        "name", "Set project name.",
        "package", "Create project's root package. If omitted, app root package will be created."
    );
  }

  @Override
  public void execute(Map<String, String> arguments) {
    if (!arguments.containsKey("name")) {
      System.out.println("Please set project name by using option -> --name.");
      return;
    }

    String location = arguments.getOrDefault("location", new File("").getAbsolutePath());
    String rootPackage = arguments.getOrDefault("package", "app");

    Path locationPath = Paths.get(location);

    if (!Files.exists(locationPath)) {
      try {
        Files.createDirectories(locationPath);
      } catch (IOException ex) {;
        ex.printStackTrace();
        return;
      }
    }

    try {
      Path projectDir = locationPath.resolve(arguments.get("name"));

      Files.createDirectory(projectDir);
      Files.write(projectDir.resolve("pom.xml"), pom(arguments.get("name"), rootPackage).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW);

      Files.createDirectories(projectDir.resolve("src").resolve("main").resolve("java"));
      Files.write(
          projectDir.resolve("src").resolve("main").resolve("java").resolve("module-info.java"), moduleInfo(rootPackage).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW
      );

      Path rootPackagePath = projectDir.resolve("src").resolve("main").resolve("java");
      if (rootPackage.contains(".")) {
        String[] paths = rootPackage.split("\\.");
        for (String path : paths) {
          rootPackagePath = rootPackagePath.resolve(path);
        }
      } else {
        rootPackagePath = rootPackagePath.resolve(rootPackage);
      }

      Files.createDirectories(rootPackagePath);
      Files.write(rootPackagePath.resolve("Application.java"), mainClass(rootPackage).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW);

      Path mainResourcesPath = projectDir.resolve("src").resolve("main").resolve("resources");
      Path servicesPath = mainResourcesPath.resolve("META-INF").resolve("services");

      Files.createDirectories(servicesPath);
      Files.createFile(servicesPath.resolve("hr.yeti.rudimentary.http.spi.HttpEndpoint"));
      Files.createFile(servicesPath.resolve("hr.yeti.rudimentary.context.spi.Instance"));

      Files.write(mainResourcesPath.resolve("config.properties"), config().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW);

      Files.createDirectories(projectDir.resolve("src").resolve("test").resolve("java"));
      Files.createDirectories(projectDir.resolve("src").resolve("test").resolve("resources"));

    } catch (IOException ex) {
      ex.printStackTrace();
    }

  }

  private String mainClass(String rootPackage) {
    return (rootPackage.length() > 0 ? ("package " + rootPackage + ";\n"
        + "\n") : "")
        + "import hr.yeti.rudimentary.server.Server;\n"
        + "import java.io.IOException;\n"
        + "\n"
        + "public class Application {\n"
        + "\n"
        + "  public static void main(String[] args) throws IOException {\n"
        + "    Server.start();\n"
        + "  }\n"
        + "}\n"
        + "";
  }

  private String moduleInfo(String rootPackage) {
    return "import hr.yeti.rudimentary.http.spi.HttpEndpoint;\n"
        + "\n"
        + "module " + rootPackage + " {\n"
        + "  requires hr.yeti.rudimentary.api;\n"
        + "  requires hr.yeti.rudimentary.server;\n"
        + "\n"
        + "  uses HttpEndpoint;\n"
        + "}\n"
        + "";
  }

  private String config() {
    return "# Server\n"
        + "server.port=8888\n"
        + "server.threadPoolSize=25\n"
        + "server.stopDelay=0";
  }

  private String pom(String projectName, String rootPackage) {
    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
        + "  <modelVersion>4.0.0</modelVersion>\n"
        + "  <parent>\n"
        + "    <groupId>hr.yeti.rudimentary</groupId>\n"
        + "    <artifactId>rudimentary</artifactId>\n"
        + "    <version>1.0-SNAPSHOT</version>\n"
        + "  </parent>\n"
        + "  <artifactId>" + projectName + "</artifactId>\n"
        + "  <version>1.0-SNAPSHOT</version>\n"
        + "  <packaging>jar</packaging>\n"
        + "  <dependencies>\n"
        + "    <dependency>\n"
        + "      <groupId>hr.yeti.rudimentary</groupId>\n"
        + "      <artifactId>rudimentary-api</artifactId>\n"
        + "      <version>1.0-SNAPSHOT</version>\n"
        + "    </dependency>\n"
        + "    <dependency>\n"
        + "      <groupId>hr.yeti.rudimentary</groupId>\n"
        + "      <artifactId>rudimentary-server</artifactId>\n"
        + "      <version>1.0-SNAPSHOT</version>\n"
        + "    </dependency>\n"
        + "    <dependency>\n"
        + "      <groupId>hr.yeti.rudimentary.exts</groupId>\n"
        + "      <artifactId>rudimentary-health-ext</artifactId>\n"
        + "      <version>1.0-SNAPSHOT</version>\n"
        + "    </dependency>\n"
        + "    <dependency>\n"
        + "      <groupId>hr.yeti.rudimentary.exts</groupId>\n"
        + "      <artifactId>rudimentary-apidocs-ext</artifactId>\n"
        + "      <version>1.0-SNAPSHOT</version>\n"
        + "    </dependency>\n"
        + "  </dependencies>\n"
        + "  <build>\n"
        + "    <plugins>\n"
        + "      <plugin>\n"
        + "        <groupId>org.apache.maven.plugins</groupId>\n"
        + "        <artifactId>maven-shade-plugin</artifactId>\n"
        + "        <version>3.2.1</version>\n"
        + "        <executions>\n"
        + "          <execution>\n"
        + "            <id>fatjar</id>\n"
        + "            <phase>package</phase>\n"
        + "            <goals>\n"
        + "              <goal>shade</goal>\n"
        + "            </goals>\n"
        + "          </execution>\n"
        + "        </executions>\n"
        + "        <configuration>\n"
        + "          <transformers>\n"
        + "            <transformer implementation=\"org.apache.maven.plugins.shade.resource.ManifestResourceTransformer\">\n"
        + "              <mainClass>" + rootPackage + ".Application</mainClass>\n"
        + "            </transformer>\n"
        + "            <transformer implementation=\"org.apache.maven.plugins.shade.resource.ServicesResourceTransformer\"/>\n"
        + "          </transformers>\n"
        + "        </configuration>\n"
        + "      </plugin>\n"
        + "      <plugin>\n"
        + "        <groupId>org.apache.maven.plugins</groupId>\n"
        + "        <artifactId>maven-compiler-plugin</artifactId>\n"
        + "        <version>3.8.0</version>\n"
        + "        <configuration>\n"
        + "          <source>10</source>\n"
        + "          <target>10</target>\n"
        + "        </configuration>\n"
        + "      </plugin> \n"
        + "    </plugins>\n"
        + "  </build>\n"
        + "</project>";
  }

}
