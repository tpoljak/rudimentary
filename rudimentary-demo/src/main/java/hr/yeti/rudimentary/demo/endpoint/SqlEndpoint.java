package hr.yeti.rudimentary.demo.endpoint;

import hr.yeti.rudimentary.http.Request;
import hr.yeti.rudimentary.http.content.Json;
import hr.yeti.rudimentary.http.content.Text;
import hr.yeti.rudimentary.http.spi.HttpEndpoint;
import hr.yeti.rudimentary.sql.Sql;
import hr.yeti.rudimentary.sql.SqlQueryDef;
import java.net.URI;
import java.util.Map;

public class SqlEndpoint implements HttpEndpoint<Text, Json> {

  @Override
  public URI path() {
    return URI.create("/sql");
  }

  @Override
  public Json response(Request<Text> request) {
    Sql.query().row("select * from users where id=?;", 1);

    Sql.tx((sql) -> {
      sql.update("insert into users(id, name) values(1, 'M');");
      sql.update("insert into users(id, name) values(2, 'M');");
      sql.update("insert into users(id, name) values(3, 'M');");
      return sql.rows("select * from users;");
    });

    return new Json(Sql.query().rows("select * from users;"));
  }

  // Repository example :-)
  public static class USER {

    static SqlQueryDef<Map<String, Object>> getById(long id) {
      return (sql) -> {
        return sql.row("select * from users where id=?;", id);
      };
    }

  }
}
