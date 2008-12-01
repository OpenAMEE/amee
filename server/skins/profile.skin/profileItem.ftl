<#-- TODO: date formatting -->
<#-- TODO: check form fields match model -->

<#include 'profileCommon.ftl'>
<#include '/includes/furniture.ftl'>
<#include '/includes/before_content.ftl'>

<h1>Profile Item</h1>

<#include 'profileTrail.ftl'>

<h2>Profile Item Details</h2>
<p><#if profileItem.name != ''>Name: ${profileItem.name}<br/></#if>
    Amount: ${profileItem.amount}<br/>
    Unit: <#if profileItem.punit??>${profileItem.unit}<#else>None</#if><br/>
    Per Unit: <#if profileItem.perUnit??>${profileItem.perUnit}<#else>None</#if><br/>
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
                    (yyyymmddThhmm)<br/>
                    <#else>
                <td>${profileItem.startDate?datetime}</td>
            </#if>
        </tr>
        <tr>
            <td>End Date</td>
            <#if browser.profileItemActions.allowModify>
                <td><input name='endDate' value='<#if profileItem.endDate??>${startEndDate(profileItem.endDate)}</#if>' type='text' size='13'/>
                    (yyyymmddThhmm)<br/>
                    <#else>
                <td><#if profileItem.endDate??>${profileItem.endDate?datetime}<#else>None</#if></td>
            </#if>
        </tr>
        <tr>
            <td>End Marker</td>
            <#if browser.profileItemActions.allowModify>
                <td><select name='end'>
                    <option value='true'<#if profileItem.end> selected</#if>>Yes</option>
                    <option value='false'<#if !profileItem.end> selected</#if>>No</option>
                </select></td>
            <#else>
                <td>${profileItem.end}</td>
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
                            <#if iv.hasUnits()>
                                <input name='${iv.displayPath}Unit' value='${iv.unit}' type='text' size="30"/>
                            </#if>
                            <#if iv.hasPerUnits()>
                                <input name='${iv.displayPath}PerUnit' value='${iv.perUnit}' type='text' size="30"/>
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

<#include '/includes/after_content.ftl'>