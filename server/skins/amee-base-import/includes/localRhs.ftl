<#if node??>
<h2>Details</h2>
<table>
    <#if node.displayName??>
        <tr>
            <td>Name:</td>
            <td>${node.displayName}</td>
        </tr>
    </#if>
    <#if node.displayPath??>
        <tr>
            <td>Path:</td>
            <td>${node.displayPath}</td>
        </tr>
    </#if>
        <tr>
            <td>UID:</td>
            <td>${node.uid}</td>
        </tr>
    <#if node.amount??>
        <tr>
            <td>Amount:</td>
            <td>${node.amount}</td>
        </tr>
    </#if>
    <tr>
        <td>Created:</td>
        <td>${node.created?datetime}</td>
    </tr>
    <tr>
        <td>Modified:</td>
        <td>${node.modified?datetime}</td>
    </tr>
</table>
</#if>

<script type='text/javascript'>

    function showApiResult(message) {
        var modal = new Control.Modal(false, {
            contents: '<div class="columnBlock paddingTopBottom clearfix" style="overflow: auto; text-align: left;">' + message + '</div>',
            width: 600,
            height: 300
        });
        
        modal.open();
    }


    function showJSON(successMethod, params) {
        var localParams;
        if (params) {
            localParams = params;
        } else {
            localParams = $('api').serialize(getHash=true);
        }

        if (!successMethod) {
            successMethod = showJSONResponse;
        }

        new Ajax.Request(window.location.href,
        {method: 'get', parameters: params, requestHeaders: ['Accept', 'application/json'], onSuccess: successMethod});
    }

    function showJSONResponse(t) {
        showApiResult(t.responseText.escapeHTML());
    }

    function showXML() {
        new Ajax.Request(window.location.href,
        {method: 'get', parameters: $('api').serialize(getHash=true), requestHeaders: ['Accept', 'application/xml'], onSuccess: showXMLResponse});
    }

    function showXMLResponse(t) {
        showApiResult(t.responseText.escapeHTML());
    }

</script>

<form id='api' onSubmit="return false;">
<h2>API
<!--
<#if apiVersions??>
    <select name='v'>
        <#list apiVersions as v>
          <option value='${v}'>${v}</option>
        </#list>
    </select>
</#if>
-->
</h2>
    <br/>
    <button name='showAPIJSON' type='button' onClick='showJSON(); return false;'>Show JSON</button>
    <br/><br/>
    <button name='showAPIXML' type='button' onClick='showXML(); return false;'>Show XML</button>
</form>
</p>
