<#include 'profileCommon.ftl'>
<#include '/includes/before_content.ftl'>
<script src="/scripts/amee/api_service.js" type="text/javascript"></script>
<script src="/scripts/amee/profile_service.js" type="text/javascript"></script>

<h1>Profile Item Value</h1>

<#include 'profileTrail.ftl'>

<#if activeUser.apiVersion.versionOne>
    <h2>Profile Item Value Details</h2>
    <p><#if profileItemValue.value != ''>Value: ${profileItemValue.value}<br/></#if>
        Full Path: ${browser.fullPath}<br/>
        Data Item Label: ${profileItemValue.item.dataItem.label}<br/>
        Item Value Definition: ${profileItemValue.itemValueDefinition.name}<br/>
        Value Definition: ${profileItemValue.itemValueDefinition.valueDefinition.name}<br/>
        Value Type: ${profileItemValue.itemValueDefinition.valueDefinition.valueType}<br/>
        Environment: ${profileItemValue.item.environment.name}<br/>
        UID: ${profileItemValue.uid}<br/>
        Created: ${profileItemValue.created?datetime}<br/>
        Modified: ${profileItemValue.modified?datetime}<br/>
    </p>

    <#if browser.profileItemValueActions.allowModify>
        <h2>Update Profile Item Value</h2>
        <p>
        <form action='${basePath}?method=put' method='POST' enctype='application/x-www-form-urlencoded'>
            Value:
            <#if profileItemValue.itemValueDefinition.choicesAvailable>
                <select name='value'>
                    <#list profileItemValue.itemValueDefinition.choiceList as choice>
                        <option value='${choice.value}'<#if profileItemValue.value == choice.value>
                                selected</#if>>${choice.name}</option>
                    </#list>
                </select>
            <#else>
                <input name='value' value='${profileItemValue.value}' type='text' size="30"/><br/>
                <#if profileItemValue.hasUnits()>
                    Unit: <input name='unit' value='${profileItemValue.unit}' type='text' size="30"/><br/>
                </#if>
                <#if profileItemValue.hasPerUnits()>
                    PerUnit: <input name='perUnit' value='${profileItemValue.perUnit}' type='text' size="30"/><br/>
                </#if>
            </#if><br/><br/>
            <input type='submit' value='Update'/>
        </form>
        </p>
    </#if>
<#else>
    <h2>Profile Item Value Details</h2>
    <p>
        <span id="value"></span>
        <span id="fullPath"></span>
        <span id="dataItemLabel"></span>
        <span id="itemValueDefinition"></span>
        <span id="valueDefinition"></span>
        <span id="valueType"></span>
        <span id="environment"></span>
        <span id="uid"></span>
        <span id="created"></span>
        <span id="modified"></span>
    </p>

    <h2>Update Profile Item Value</h2>
    <p>
        <form id="inputForm" action='#' method='POST' enctype='application/x-www-form-urlencoded'>
            <span id="inputValues"></span>
            <br/>
            <div id="inputSubmit"></div><div id="updateStatusSubmit"></div>
        </form>
    </p>
</#if>

<script type='text/javascript'>

    // api call
    <#if !activeUser.apiVersion.versionOne>
        var profileItemValueApiService = new ProfileItemValueApiService(
            {
                allowList : ${browser.profileItemActions.allowList?string},
                allowView : ${browser.profileItemActions.allowView?string},
                allowDelete : ${browser.profileItemActions.allowDelete?string},
                allowModify : ${browser.profileItemActions.allowModify?string}
            });
        profileItemValueApiService.apiRequest();
    </#if>
</script>

<#include '/includes/after_content.ftl'>