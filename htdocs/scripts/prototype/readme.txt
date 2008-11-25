This file came from: http://www.prototypejs.org/assets/2008/1/25/prototype-1.6.0.2.js

The current version is: 1.6.0.2

Taken patch from http://github.com/sstephenson/prototype/commit/1cd1075e81ffb29e53bfa52bb256f36a958a0b92 to fix issue with IE not firing dom:loaded at correct time

(function() {
  /* Support for the DOMContentLoaded event is based on work by Dan Webb,
     Matthias Miller, Dean Edwards, John Resig and Diego Perini. */

  var timer;

  function fireContentLoadedEvent() {
    if (document.loaded) return;
    if (timer) window.clearInterval(timer);
    document.loaded = true;
    document.fire("dom:loaded");
  }

  if (document.addEventListener) {
    document.addEventListener("DOMContentLoaded", function() {
      // Ensure all stylesheets are loaded, solves Opera issue
      if (Prototype.Browser.Opera &&
          $A(document.styleSheets).any(function(s) { return s.disabled }))
        return arguments.callee.defer();
      fireContentLoadedEvent();
    }, false);

  } else {
    document.attachEvent("onreadystatechange", function() {
      if (document.readyState == "complete") {
        document.detachEvent("onreadystatechange", arguments.callee);
        fireContentLoadedEvent();
      }
    });

    if (window == top) {
      timer = setInterval(function() {
        try {
          document.documentElement.doScroll("left");
        } catch(e) { return }
        fireContentLoadedEvent();
      }, 10);
    }
  }

  // Only WebKit nightly builds support DOMContentLoaded
  if (Prototype.Browser.WebKit) {
    timer = setInterval(function() {
      if (/loaded|complete/.test(document.readyState) &&
          document.styleSheets.length == $$('style, link[rel="stylesheet"]').length)
        fireContentLoadedEvent();
    }, 10);
  }

  // Worst case fallback...
  Event.observe(window, "load", fireContentLoadedEvent);
})();
