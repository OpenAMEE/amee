<div id="modal" class="columnBlock paddingTopBottom clearfix" style="overflow: auto; text-align: left;"></div>

<#if activeUser?? && activeUser.APIVersion?? && activeUser.APIVersion.versionOne && node??>
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
                <td>kgCO2 pcm:</td>
                <td>${node.amount}</td>
            </tr>
        </#if>
        <tr>
            <td>Created:</td>
            <td>${node.created?string.short}</td>
        </tr>
        <tr>
            <td>Modified:</td>
            <td>${node.modified?string.short}</td>
        </tr>
    </table>
</#if>

<script type='text/javascript'>

    function showApiResult(message) {
        // prep element
        var modalElement = new Element('div', {id : 'modal', style : 'overflow: auto; text-align: left;'});
        modalElement.addClassName('columnBlock paddingTopBottom clearfix');
        $('modal').replace(modalElement);

        // set-up modal and open
        var modal = new Control.Modal($('modal'),{
             overlayOpacity: 0.75,
             className: 'modal',
             width: 600,
             height: 300,
             fade: true
         });

        modal.container.insert(message);
        modal.open();
    }

    function showJSON(successMethod, params) {
        var localParams;
        if (params) {
            localParams = params;
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

    function showATOM() {
        new Ajax.Request(window.location.href,
        {method: 'get', parameters: $('api').serialize(getHash=true), requestHeaders: ['Accept', 'application/atom+xml'], onSuccess: showATOMResponse});
    }

    function showATOMResponse(t) {
        showApiResult(t.responseText.escapeHTML());
    }

</script>

<form id='api' onSubmit="return false;">
<#if activeUser?? &&  activeUser.username != 'guest' && activeUser.apiVersion??>
    <h2>API ${activeUser.APIVersion.version}
    <br/>
</#if>        
</h2>
    <button id="showAPIJSON" name='showAPIJSON' type='button' onClick='showJSON(); return false;'>Show JSON</button>
    <br/><br/>
    <button id="showAPIXML" name='showAPIXML' type='button' onClick='showXML(); return false;'>Show XML</button>
    <br/><br/>
    <#if activeUser?? && !activeUser.APIVersion.versionOne>
        <button id="showAPIATOM" name='showAPIATOM' type='button' onClick='showATOM(); return false;'>Show ATOM</button>
    </#if>
</form>
