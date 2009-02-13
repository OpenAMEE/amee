<#-- TODO: date formatting -->
<#-- TODO: check form fields match model -->

<#include 'profileCommon.ftl'>
<#include '/includes/furniture.ftl'>
<#include '/includes/before_content.ftl'>
<script src="/scripts/amee/api_service.js" type="text/javascript"></script>
<script src="/scripts/amee/profile_service.js" type="text/javascript"></script>
<h1>Profile Item</h1>

<#include 'profileTrail.ftl'>
<#if activeUser.apiVersion.versionOne>
    <h2>Profile Item Details</h2>
    <p><#if profileItem.name != ''>Name: ${profileItem.name}<br/></#if>
        Amount: ${profileItem.amount}<br/>
        Start Date: ${startEndDate(profileItem.startDate)}<br/>
        End Date: <#if profileItem.endDate??>${startEndDate(profileItem.endDate)}<#else>None</#if><br/>
        Full Path: ${browser.fullPath}<br/>
        Data Item Label: ${profileItem.dataItem.label}<br/>
        Item Definition: ${profileItem.itemDefinition.name}<br/>
        Environment: ${profileItem.environment.name}<br/>
        UID: ${profileItem.uid}<br/>
        Created: ${profileItem.created?datetime}<br/>
        Modified: ${profileItem.modified?datetime}<br/>
    </p>

    <h2>Profile Item Values</h2>
    <p>
    <form action='${basePath}?method=put' method='POST' enctype='application/x-www-form-urlencoded'>
        <table>
            <tr>
                <th>Name</th>
                <th>Value</th>
            </tr>
            <tr>
                <td>Name</td>
                <#if browser.profileItemActions.allowModify>
                    <td><input name='name' value='${profileItem.name}' type='text' size='30'/><br/>
                        <#else>
                    <td>${profileItem.name}</td>
                </#if>
            </tr>
            <tr>
                <td>Start Date</td>
                <#if browser.profileItemActions.allowModify>
                    <td><input name='startDate' value='${startEndDate(profileItem.startDate)}' type='text' size='13'/>
                        (${getDateFormat(activeUser.apiVersion.version)})<br/>
                        <#else>
                    <td>${profileItem.startDate?datetime}</td>
                </#if>
            </tr>
            <tr>
                <td>End Date</td>
                <#if browser.profileItemActions.allowModify>
                    <td><input name='endDate' value='<#if profileItem.endDate??>${startEndDate(profileItem.endDate)}</#if>' type='text' size='13'/>
                        (${getDateFormat(activeUser.apiVersion.version)})<br/>
                        <#else>
                    <td><#if profileItem.endDate??>${profileItem.endDate?datetime}<#else>None</#if></td>
                </#if>
            </tr>
            <#if 0 != profileItem.itemValues?size>
                <#list profileItem.itemValues as iv>
                <tr>
                    <#if browser.profileItemActions.allowModify>
                        <td><a href='${basePath}/${iv.displayPath}'>${iv.displayName}</a></td>
                        <td>
                            <#if iv.itemValueDefinition.choicesAvailable>
                                <select name='${iv.displayPath}'>
                                    <#list iv.itemValueDefinition.choiceList as choice>
                                        <option value='${choice.value}' <#if iv.value == choice.value>selected</#if>>${choice.name}</option>
                                    </#list>
                                </select>
                            <#else>
                                <input name='${iv.displayPath}' value='${iv.value}' type='text' size="30"/>
                                <#if !activeUser.apiVersion.versionOne>
                                    <#if iv.hasUnit()>
                                        <input name='${iv.displayPath}Unit' value='${iv.unit}' type='text' size="30"/>
                                    </#if>
                                    <#if iv.hasPerUnit()>
                                        <input name='${iv.displayPath}PerUnit' value='${iv.perUnit}' type='text' size="30"/>
                                    </#if>
                                <#else>
                                    (${iv.unit} per ${iv.perUnit})
                                </#if>
                            </#if>
                        </td>
                    <#else>
                        <td>${iv.displayName}</td>
                        <td>${iv.value}</td>
                    </#if>
                </tr>
                </#list>
            </#if>
        </table>
        <#if browser.profileItemActions.allowModify>
            <br/>
            <input type='submit' value='Update'/>
        </#if>
    </form>
    </p>
<#else>
    <h2>Profile Item Details</h2>
    <p>
        <span id="name"></span>
        <span id="amount"></span>
        <span id="startDate"></span>
        <span id="endDate"></span>
        <span id="fullPath"></span>
        <span id="dataItemLabel"></span>
        <span id="itemDefinition"></span>
        <span id="environment"></span>
        <span id="uid"></span>
        <span id="created"></span>
        <span id="modified"></span>
    </p>

    <h2>Profile Item Values</h2>
    <p>
        <form id="inputForm" action='#' method='POST' enctype='application/x-www-form-urlencoded'>
            <table id="inputTable">
            </table>
            <br/>
            <div id="inputSubmit"></div><div id="updateStatusSubmit"></div>
        </form>
    </p>
</#if>
<script type='text/javascript'>

    // api call
    <#if !activeUser.apiVersion.versionOne>
        var profileItemApiService = new ProfileItemApiService();
        profileItemApiService.apiRequest();
    </#if>
</script>


<#include '/includes/after_content.ftl'>
