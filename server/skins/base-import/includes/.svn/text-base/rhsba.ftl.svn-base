<!-- start rhs -->
<div id="rhs">
<h2>Meta</h2>
<table>
  <#if activeUser??>
    <tr>
      <td>User:</td><td>${activeUser.username}</td>
    </tr>
  </#if>
  <#if activeGroup??>
    <tr>
      <td>Group:</td><td>${activeGroup.name}</td>
    </tr>
  </#if>
</table>
<#if node??>
  <h2>Details</h2>
  <table>
    <#if node.displayName??>
      <tr>
        <td>Name:</td><td>${node.displayName}</td>
      </tr>
    </#if>
    <#if node.displayPath??>
      <tr>
        <td>Path:</td><td>${node.displayPath}</td>
      </tr>
    </#if>
    <tr>
      <td>UID:</td><td>${node.uid}</td>
    </tr>
    <#if node.amountPerMonth??>
      <tr>
        <td>kgCO2 pm:</td><td>${node.amountPerMonth}</td>
      </tr>
    </#if>
    <tr>
      <td>Created:</td><td>${node.created?string.short}</td>
    </tr>
    <tr>
      <td>Modified:</td><td>${node.modified?string.short}</td>
    </tr>
  </table>
</#if>
<script type='text/javascript'>

(function () {
    var INDENT = "&nbsp;&nbsp;";
    var NEWLINE = "<br />";
    var pPr = false;
    var indentLevel = 0;
    var indent = function(a) {
        if (!pPr) return a;
        for (var l=0; l<indentLevel; l++) {
            a[a.length] = INDENT;
        }
        return a;
    };

    var newline = function(a) {
        if (pPr) a[a.length] = NEWLINE;
        return a;
    };

    var m = {
            '\b': '\\b',
            '\t': '\\t',
            '\n': '\\n',
            '\f': '\\f',
            '\r': '\\r',
            '"' : '\\"',
            '\\': '\\\\'
        },
        s = {
            array: function (x) {
                var a = ['['], b, f, i, l = x.length, v;
                a = newline(a);
                indentLevel++;
                for (i = 0; i < l; i += 1) {
                    v = x[i];
                    f = s[typeof v];
                    if (f) {
                        v = f(v);
                        if (typeof v == 'string') {
                            if (b) {
                                a[a.length] = ',';
                                a = newline(a);
                            }
                            a = indent(a);
                            a[a.length] = v;
                            b = true;
                        }
                    }
                }
                indentLevel--;
                a = newline(a);
                a = indent(a);
                a[a.length] = ']';
                return a.join('');
            },
            'boolean': function (x) {
                return String(x);
            },
            'null': function (x) {
                return "null";
            },
            number: function (x) {
                return isFinite(x) ? String(x) : 'null';
            },
            object: function (x, formatedOutput) {
                if (x) {
                    if (x instanceof Array) {
                        return s.array(x);
                    }
                    var a = ['{'], b, f, i, v;
                    a = newline(a);
                    indentLevel++;
                    for (i in x) {
                        v = x[i];
                        f = s[typeof v];
                        if (f) {
                            v = f(v);
                            if (typeof v == 'string') {
                                if (b) {
                                    a[a.length] = ',';
                                    a = newline(a);
                                }
                                a = indent(a);
                                a.push(s.string(i), ((pPr) ? ' : ' : ':'), v);
                                b = true;
                            }
                        }
                    }
                    indentLevel--;
                    a = newline(a);
                    a = indent(a);
                    a[a.length] = '}';
                    return a.join('');
                }
                return 'null';
            },
            string: function (x) {
                if (/["\\\x00-\x1f]/.test(x)) {
                    x = x.replace(/([\x00-\x1f\\"])/g, function(a, b) {
                        var c = m[b];
                        if (c) {
                            return c;
                        }
                        c = b.charCodeAt();
                        return '\\u00' +
                            Math.floor(c / 16).toString(16) +
                            (c % 16).toString(16);
                    });
                }
                return '"' + x + '"';
            }
        };

    Object.prototype.toJSONString = function (prettyPrint) {
        pPr = prettyPrint;
        return s.object(this);
    };

})();

function showJSON() {
    new Ajax.Request(window.location.href,
        {method: 'get', requestHeaders: ['Accept', 'application/json'], onSuccess: showJSONResponse});
}

function showJSONResponse(t) {
    var js = eval('('+t.responseText+')');
    Dialog.alert('<div align="left">'+js.toJSONString(true)+'</div>',
      {windowParameters: {className: 'alphacube', width: 600, height: 500},
       okLabel: 'Close',
       top: 100});
}

function showXML() {
    new Ajax.Request(window.location.href,
        {method: 'get', requestHeaders: ['Accept', 'application/xml'], onSuccess: showXMLResponse});
}

function showXMLResponse(t) {
    Dialog.alert('<div align="left">'+t.responseText.escapeHTML().replace(/&gt;/g,"&gt;<br />")+'</div>',
      {windowParameters: {className: 'alphacube', width: 600, height: 500},
       okLabel: 'Close',
       top: 100});
}


</script>
<h2>API</h2>
<p>
<button name='showAPIJSON' type='button' onClick='showJSON()'>Show JSON</button>
<br/><br/>
<button name='showAPIXML' type='button' onClick='showXML()'>Show XML</button>
</p>
<h2>Filter</h2>
<script type="text/javascript">
<!--
function activate(filters){
   var redirect = location.href;
   var filts = '';
   for(i=0;i<filters.length;i++){
       if(filters[i].checked)filts+=filters[i].value+',' 
   }
   //strip out query string for now...TODO!
   redirect = redirect.replace(location.search,'');
   if(filts.length>0){
       redirect += '?filterBy='+filts;
       redirect += '&filterResults=true';
   }
   self.location.href=redirect;
}
-->
</script>
<form name="checks">
<input type="checkbox" name="filters" value="New" onClick="activate(this.form.filters)" <#if (filterBy!"")?contains("New")> checked="checked" </#if> /> New <br/>
<input type="checkbox" name="filters" value="Open" onClick="activate(this.form.filters)" <#if (filterBy!"")?contains("Open")> checked="checked" </#if> /> Open <br/>
<input type="checkbox" name="filters" value="Closed" onClick="activate(this.form.filters)" <#if (filterBy!"")?contains("Closed")> checked="checked" </#if> /> Closed <br/>
<input type="checkbox" name="filters" value="Hidden" onClick="activate(this.form.filters)" <#if (filterBy!"")?contains("Hidden")> checked="checked" </#if> /> Hidden <br/>
<input type="checkbox" name="filters" value="SMS" onClick="activate(this.form.filters)" <#if (filterBy!"")?contains("SMS")> checked="checked" </#if> /> SMS <br/>
</form>
</div>
<!-- end rhs -->

