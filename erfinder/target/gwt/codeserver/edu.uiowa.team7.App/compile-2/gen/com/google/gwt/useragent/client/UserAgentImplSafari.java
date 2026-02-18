package com.google.gwt.useragent.client;

public class UserAgentImplSafari implements com.google.gwt.useragent.client.UserAgent {
  
  public native String getRuntimeValue() /*-{
    var ua = navigator.userAgent.toLowerCase();
    if ((function() { 
      return (ua.indexOf('webkit') != -1);
    })()) return 'safari';
    if ((function() { 
      return (ua.indexOf('gecko') != -1);
    })()) return 'gecko1_8';
    return 'unknown';
  }-*/;
  
  
  public String getCompileTimeValue() {
    return "safari";
  }
}
