package greencode.kernel;

import java.util.HashMap;

import javax.servlet.http.HttpSession;
import javax.websocket.Session;

import org.apache.tomcat.util.http.MimeHeaders;

public class WebSocketData {
	private HashMap<String, String[]> params;
	
	int eventId;
	
	String url;
	HttpSession httpSession;
	Session session;
	
	String remoteHost;
	StringBuffer requestURL;
	String requestURI;
	int localPort;
	MimeHeaders headers;
	
	public MimeHeaders getHeaders() {
		return headers;
	}
	
	public String getRemoteHost() {
		return remoteHost;
	}
	
	public StringBuffer getRequestURL() {
		return requestURL;
	}
	
	public String getRequestURI() {
		return requestURI;
	}
	
	public int getLocalPort() {
		return localPort;
	}
	
	public String getUrl() {
		return url;
	}

	public HashMap<String, String[]> getParameters() {
		return params;
	}

	public HttpSession getHttpSession() {
		return httpSession;
	}

	public Session getSession() {
		return session;
	}
}