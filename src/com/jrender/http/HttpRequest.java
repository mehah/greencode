package com.jrender.http;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.regex.Pattern;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.websocket.Session;

import org.apache.tomcat.util.http.FastHttpDateFormat;
import org.apache.tomcat.util.http.MimeHeaders;

import com.jrender.exception.OperationNotAllowedException;
import com.jrender.http.enumeration.RequestMethod;
import com.jrender.http.security.UserPrincipal;
import com.jrender.kernel.WebSocketData;
import com.jrender.util.FileUtils;
import com.jrender.util.LogMessage;

public final class HttpRequest extends HttpServletRequestWrapper implements HttpServletRequest {
	private static final Pattern pattern = Pattern.compile("up.browser|up.link|windows ce|iphone|iemobile|mini|mmp|symbian|midp|wap|phone|pocket|mobile|pda|psp", Pattern.CASE_INSENSITIVE);
	private static final TimeZone GMT_ZONE = TimeZone.getTimeZone("GMT");
	private static final SimpleDateFormat FORMATS[] = {
        new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US),
        new SimpleDateFormat("EEEEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US),
        new SimpleDateFormat("EEE MMMM d HH:mm:ss yyyy", Locale.US)
    };
	
	static {
		for (SimpleDateFormat sdf : FORMATS) {
			sdf.setTimeZone(GMT_ZONE);
		}
	}
	
	private ViewSession viewSession;

	private Extension extension;
	private UserPrincipal userPrincipal;
	private Boolean isMobile = null;
	
	private final boolean isAjax, isIFrameHttpRequest;
	private final int cid, viewId, localPort;
	private final String methodType, remoteHost, requestURI, remoteAddr;
	
	private final Conversation conversation;
	private final ServletResponse response;
	private final HttpSession httpSession;
	private final Session webSocketSession;

	private final StringBuffer requestURL;
	private final MimeHeaders headers;
	
	final Map<String, String[]> params;
	final boolean contentIsHtml;

	public HttpRequest(HttpServletRequest request, HttpServletResponse response) {
		this(request, response, null);
	}

	public HttpRequest(HttpServletRequest request, ServletResponse response, WebSocketData wsData) {
		super(request);

		if (wsData != null) {
			this.webSocketSession = wsData.getSession();
			this.params = wsData.getParameters();
			this.httpSession = wsData.getHttpSession();
			this.isIFrameHttpRequest = false;
			this.isAjax = false;
			this.methodType = RequestMethod.POST.name(); // Método GET utiliza codificação de caracteres.
			this.remoteHost = wsData.getRemoteHost();
			this.requestURL = wsData.getRequestURL();
			this.requestURI = wsData.getRequestURI();
			this.localPort = wsData.getLocalPort();
			this.headers = wsData.getHeaders();
			this.remoteAddr = wsData.getRemoteAddr();
		} else {
			this.webSocketSession = null;
			this.params = new HashMap<String, String[]>(request.getParameterMap());
			this.httpSession = request.getSession();
			this.isIFrameHttpRequest = request.getParameterMap().containsKey("isIframe");
			this.isAjax = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
			this.methodType = request.getMethod();
			this.remoteHost = request.getRemoteHost();
			this.requestURL = request.getRequestURL();
			this.requestURI = request.getRequestURI();
			this.localPort = request.getLocalPort();
			this.remoteAddr = request.getRemoteAddr();
			this.headers = null;
		}
		
		this.response = response;

		contentIsHtml = Boolean.parseBoolean(getParameter("__contentIsHtml")) || isFirst();

		String v = getParameter("viewId");
		if (v != null && !v.isEmpty()) {
			viewId = Integer.parseInt(v);
			getViewSession().isNew = false;
		} else {
			Integer lastViewID = (Integer) this.httpSession.getAttribute("LAST_VIEW_ID");

			this.httpSession.setAttribute("LAST_VIEW_ID", viewId = (lastViewID == null ? 1 : ++lastViewID));
		}

		getViewSession().access();

		this.cid = (v = getParameter("cid")) == null ? 1 : Integer.parseInt(v);

		this.conversation = new Conversation(getViewSession(), cid);

		this.userPrincipal = (UserPrincipal) getSession().getAttribute("__USER_PRINCIPAL__");
	}
	
	public String getHeader(String name) {
		if(this.headers != null) {
			return this.headers.getHeader(name);
		}
		return super.getHeader(name);
	}
	
	public Enumeration<String> getHeaderNames() {
		if(this.headers != null) {
			return this.headers.names();
		}
		return super.getHeaderNames();
	}
	
	public Enumeration<String> getHeaders(String name) {
		if(this.headers != null) {
			return this.headers.values(name);
		}
		return super.getHeaders(name);
	}
	
	public long getDateHeader(String name) {
		if(this.headers != null) {
	        String value = getHeader(name);
	        if (value == null)
	            return (-1L);

	        long result = FastHttpDateFormat.parseDate(value, FORMATS);
	        if (result != (-1L)) {
	            return result;
	        }
	        throw new IllegalArgumentException(value);
		}
		return super.getDateHeader(name);
	}
	
	public int getIntHeader(String name) {
		if(this.headers != null) {
	        String value = getHeader(name);
	        if (value == null) {
	            return (-1);
	        }

	        return Integer.parseInt(value);
		}
		return super.getIntHeader(name);
	}
	
	public boolean isFirst() {
		return !(isAjax || isIFrameHttpRequest || isWebSocket());
	}
	
	public Session getWebSocketSession() {
		return webSocketSession;
	}
	
	public boolean isWebSocket() {
		return webSocketSession != null;
	}
	
	public String getRemoteHost() {
		return this.remoteHost;
	}
	
	public String getRequestURI() {
		return this.requestURI;
	}
	
	public StringBuffer getRequestURL() {
		return this.requestURL;
	}
	
	public int getLocalPort() {
		return this.localPort;
	}
	
	public Principal getUserPrincipal() {
		return getUserPrincipal(getSession());
	}

	public void setUserPrincipal(UserPrincipal user) {
		if (this.userPrincipal != null)
			throw new OperationNotAllowedException(LogMessage.getMessage("0039"));

		getSession().setAttribute("__USER_PRINCIPAL__", this.userPrincipal = user);
	}
	
	static boolean isMobile(HttpServletRequest request) {
		String userAgent = request.getHeader("user-agent");
		return userAgent != null && pattern.matcher(userAgent).find();
	}

	public boolean isMobile() {
		return isMobile == null ? isMobile = isMobile(this) : isMobile;
	}

	public boolean isMethod(RequestMethod methodType) {
		return getMethod().equals(methodType.name());
	}

	public boolean isAjax() {
		return isAjax;
	}

	public int getConversationId() {
		return cid;
	}

	public Extension getExtension() {
		return (extension != null ? extension = new Extension(this.getServletPath()) : extension);
	}

	public ViewSession getViewSession() {
		if (this.viewSession == null) {
			ViewSessionContext viewContext = new ViewSessionContext(getSession());

			if ((this.viewSession = viewContext.getViewSession(viewId)) == null) {
				if (response.isCommitted())
					throw new IllegalStateException("Cannot create a view session after the response has been committed");

				this.viewSession = new ViewSession(viewId, getSession(), viewContext);
			}
		}

		return this.viewSession;
	}

	public HttpSession getSession() {
		return this.httpSession;
	}

	public HttpSession getSession(boolean create) {
		return this.httpSession;
	}

	public String getMethod() {
		return this.methodType;
	}

	public Conversation getConversation() {
		return conversation;
	}

	public final class Extension {
		private final String ext;
		private final boolean isCss, isJS, isView;

		private Extension(String path) {
			ext = FileUtils.getExtension(path);
			isCss = ext.equals("css");
			isJS = ext.equals("js");
			isView = ext.equals("html") || ext.equals("xhtml") || ext.equals("jsp") || ext.equals("htm");
		}

		public String get() {
			return this.ext;
		}

		public boolean isCss() {
			return this.isCss;
		}

		public boolean isJS() {
			return this.isJS;
		}

		public boolean isView() {
			return this.isView;
		}
	}

	public String getParameter(String name) {
		String[] values = params.get(name);
		return values == null ? null : values[0];
	}

	public String[] getParameterValues(String name) {
		String[] value;
		int pos;
		if (isWebSocket() && (pos = name.indexOf("[]")) != -1) {
			value = params.get(name.substring(0, pos));
		} else {
			value = params.get(name);
		}

		return value;
	}
	
	public String[] getParameterValuesStartsWith(String name) {
		return getParameterValues(name, true);
	}
	
	public String[] getParameterValuesEndWith(String name) {
		return getParameterValues(name, false);
	}
	
	private String[] getParameterValues(String name, boolean start) {
		List<String> values = new ArrayList<String>();		
		for (Entry<String, String[]> param : params.entrySet()) {
			if(start && param.getKey().startsWith(name) || param.getKey().endsWith(name)) {
				for (String v : param.getValue()) {
					values.add(v);
				}
			}
		}
		
		if(values.size() == 0)
			return null;
		
		return values.toArray(new String[values.size()]);
	}

	public String getContextPath() {
		return com.jrender.kernel.$JRenderContext.getContextPath();
	}

	public boolean isIFrameHttpRequest() {
		return isIFrameHttpRequest;
	}

	public String getRemoteAddr() {
		return remoteAddr;
	}
	
	public static UserPrincipal getUserPrincipal(HttpSession session) {
		return (UserPrincipal) session.getAttribute("__USER_PRINCIPAL__");
	}
}
