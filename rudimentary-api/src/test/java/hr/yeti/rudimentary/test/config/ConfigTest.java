package hr.yeti.rudimentary.test.config;

import hr.yeti.rudimentary.config.ConfigProperty;
import hr.yeti.rudimentary.test.config.mock.ConfigMock;
import hr.yeti.rudimentary.test.context.mock.ContextMock;
import java.util.Map;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ConfigTest {

  // Mimics configuration instance which will be loaded via ServiceLoader.
  ConfigMock config;
  ContextMock ctx;

  @BeforeEach
  public void beforeEach() {
    config = new ConfigMock();
    ctx = new ContextMock(config);
  }

  @Test
  public void test_empty() {
    expect:
    assertTrue(config.isEmpty());
  }

  @Test
  public void test_default_sealed_value() {
    expect:
    assertFalse(config.isSealed());
  }

  @Test
  public void test_load_config_using_configProperty() {
    when:
    config.load(new ConfigProperty("server.port", "8888"));

    then:
    assertTrue(config.contains("server.port"));
    assertEquals("8888", config.value("server.port"));
    assertFalse(config.isSealed());
  }

  @Test
  public void test_load_config_using_Properties() {
    Properties properties;

    given:
    properties = new Properties();
    properties.put("server.port", "8888");

    when:
    config.load(properties);

    then:
    assertTrue(config.contains("server.port"));
    assertEquals("8888", config.value("server.port"));
    assertFalse(config.isSealed());
  }

  @Test
  @DisplayName("Should load multiple configs using Properties files")
  public void test_load_config_using_multiple_Properties() {
    Properties properties;
    Properties properties1;

    given:
    properties = new Properties();
    properties.put("server.port", "8888");
    properties.put("server.threadPoolSize", "25");

    properties1 = new Properties();
    properties1.put("server.port", "4444");

    when:
    config.load(properties, properties1);

    then:
    assertTrue(config.contains("server.port"));
    assertEquals("4444", config.value("server.port"));

    assertTrue(config.contains("server.threadPoolSize"));
    assertEquals("25", config.value("server.threadPoolSize"));

    assertFalse(config.isSealed());
  }

  @Test
  public void test_load_config_using_Map() {
    when:
    config.load(Map.of("server.port", "8888"));

    then:
    assertTrue(config.contains("server.port"));
    assertEquals("8888", config.value("server.port"));
    assertFalse(config.isSealed());
  }

  @Test
  public void test_load_config_using_Properties_file_path() {
    when:
    config.load(getClass().getClassLoader().getResource("config.properties").getPath());

    then:
    assertTrue(config.contains("server.port"));
    assertEquals("8888", config.value("server.port"));
    assertFalse(config.isSealed());
  }

  @Test
  @DisplayName("Should load multiple config using String paths files honorig order of loading")
  public void test_load_multiple_config_using_Properties_file_paths() {
    when:
    config.load(
        getClass().getClassLoader().getResource("config_1.properties").getPath(),
        getClass().getClassLoader().getResource("config.properties").getPath()
    );

    then:
    assertTrue(config.contains("server.port"));
    assertEquals("8888", config.value("server.port"));

    assertTrue(config.contains("server.threadPoolSize"));
    assertEquals("25", config.value("server.threadPoolSize"));

    assertFalse(config.isSealed());
  }

  @Test
  public void test_load_config_using_InputStream() {
    when:
    config.load(getClass().getClassLoader().getResourceAsStream("config.properties"));

    then:
    assertTrue(config.contains("server.port"));
    assertEquals("8888", config.value("server.port"));
    assertFalse(config.isSealed());
  }

  @Test
  @DisplayName("Should load multiple config using InputStreams honorig order of loading")
  public void test_load_multiple_config_using_InputStreams() {
    when:
    config.load(
        getClass().getClassLoader().getResourceAsStream("config_1.properties"),
        getClass().getClassLoader().getResourceAsStream("config.properties")
    );

    then:
    assertTrue(config.contains("server.port"));
    assertEquals("8888", config.value("server.port"));

    assertTrue(config.contains("server.threadPoolSize"));
    assertEquals("25", config.value("server.threadPoolSize"));

    assertFalse(config.isSealed());
  }

  @Test
  public void test_get_configProperty() {
    ConfigProperty property;

    given:
    config.load(new ConfigProperty("server.port", "8888"));

    when:
    property = config.property("server.port");

    then:
    assertNotNull(property);
    assertEquals(property.getName(), "server.port");
    assertEquals(property.value(), "8888");
  }

  @Test
  public void test_get_configProperty_with_default_value() {
    ConfigProperty property;

    when:
    property = config.property("server.port", "1234");

    then:
    assertNotNull(property);
    assertEquals(property.getName(), "server.port");
    assertEquals(property.value(), "1234");
  }

  @Test
  public void test() {
    RuntimeException e = assertThrows(RuntimeException.class, () -> {
      config.load("config.properties");
    });

    assertEquals("Failed to load configuration from config.properties", e.getMessage());
  }

  @Test
  public void test_seal() {
    when:
    config.seal();

    expect:
    assertTrue(config.isSealed());
  }

  @Test
  public void test_application_properties_is_cloned_instance_of_config_properties_values() {
    expect:
    config.applicationProperties().forEach((key, value) -> {
      assertEquals(config.property(key.toString()), value.toString());
    });
  }
}
