// debug wrapper for firefox firebug console
var Log = {
    debug: function(msg) {
    },
    info: function(msg) {
    },
    warn: function(msg) {
    },
    error: function(msg) {
    },
    trace: function() {
    },
    profile: function() {
    },
    profileEnd: function() {
    },
    enable: function() {
        if ((typeof console != "undefined") && (typeof console.debug != "undefined")) {
            Log.debug = function(msg) {
                console.debug(msg);
            };
            Log.info = function(msg) {
                console.info(msg);
            };
            Log.warn = function(msg) {
                console.warn(msg);
            };
            Log.error = function(msg) {
                console.error(msg);
            };
            Log.trace = function(msg) {
                console.trace(msg);
            };
            Log.profile = function() {
                console.profile();
            };
            Log.profileEnd = function() {
                console.profileEnd();
            };
            Log.info("Started logging...");
        }
    },
    disable: function() {
        if (this.firebug) this.debug = false;
        Log.debug = function(msg) {
        };
        Log.info = function(msg) {
        };
        Log.warn = function(msg) {
        };
        Log.error = function(msg) {
        };
        Log.trace = function(msg) {
        };
        Log.profile = function() {
        };
        Log.profileEnd = function() {
        };
    }
}

// to disable logging comment out the line below
Log.enable();

// tests
/*
 Log.profile();
 Log.debug("debug");
 Log.info("info");
 Log.disable();
 Log.error("can't see me");
 Log.enable();
 Log.error("can see this error");
 Log.warn("warn");
 Log.trace();
 Log.profileEnd();
 */