package hr.yeti.rudimentary.server.http.session;

import hr.yeti.rudimentary.context.spi.Instance;
import hr.yeti.rudimentary.http.session.Session;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HttpSessionManager implements Instance {

  private Map<String, Session> sessions;

  @Override
  public void destroy() {
  }

  @Override
  public void initialize() {
    sessions = new ConcurrentHashMap<>();
  }

  public Session create() {
    HttpSession newSession = new HttpSession();

    sessions.put(newSession.getRsid(), newSession);
    return newSession;
  }

  public Session get(String rsid) {
    if (sessions.containsKey(rsid)) {
      Session session = sessions.get(rsid);
      ((HttpSession) session).setLastAccessedTime(System.currentTimeMillis());
      return session;
    }
    throw new NoHttpSessionFoundException("No session found for RSID=" + rsid + ".");
  }

  public void remove(String rsid) {
    sessions.remove(rsid);
  }

}
