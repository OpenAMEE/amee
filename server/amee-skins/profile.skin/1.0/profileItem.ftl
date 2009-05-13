<#-- TODO: date formatting -->
<#-- TODO: check form fields match model -->

<#include '/profileCommon.ftl'>
<#include '/includes/furniture.ftl'>
<#include '/includes/before_content.ftl'>

<script src="/scripts/amee/api_service.js" type="text/javascript"></script>
<script src="/scripts/amee/profile_service.js" type="text/javascript"></script>

<h1>Profile Item</h1>

<#include 'profileTrail.ftl'>

<h2>Profile Item Details</h2>
<p>
    <#if profileItem.name != ''>Name: ${profileItem.name}<br/></#if>
    <#if !profileItem.end>
        kgCO2 pcm: ${amountPerMonth}<br/>
        Valid From: ${profileItem.startDate?string(getDateFormatV1())}<br/>
        End: No<br/>
    <#else>
        End: Yes<br/>
    </#if>
    Full Path: ${basePath}<br/>
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
            <td>Valid From</td>
            <#if browser.profileItemActions.allowModify>
                <td><input name='validFrom' value='${profileItem.startDate?string(getDateFormatV1())}' type='text' size='13'/>
                    (${getDateFormatV1()})<br/>
                    <#else>
                <td>${profileItem.startDate?datetime}</td>
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
                            <#if iv.hasUnit() && iv.hasPerUnit()>
                                (${iv.unit} per ${iv.perUnit})
                            <#elseif iv.hasUnit()>
                                (${iv.unit})
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
