<#include '/dataCommon.ftl'>
<#include '/includes/before_content.ftl'>
<#include '/includes/furniture.ftl'>

<h1>Data Item Lookup</h1>

<p>
    <form action='${path}' method='GET' enctype='application/x-www-form-urlencoded'>
        Data Item UID: <input name='dataItemUid' value='${dataItemUid}' type='text' size='12'/>
        <input type='submit' value='Search'/>
    </form>
</p>

<#if !dataItem?? && dataItemUid != ''>

    <h2>Data Item Not Found</h2>

</#if>

<#if dataItem??>

    <h2>Data Item Details</h2>
    <p><#if dataItem.name != ''>Name: ${dataItem.name}<br/></#if>
        <#if dataItem.path != ''>Path: ${dataItem.path}<br/></#if>
        Start Date: ${startEndDate(dataItem.startDate)}<br/>
        End Date: <#if dataItem.endDate??>${startEndDate(dataItem.endDate)}<#else>None</#if><br/>
        Full Path: <a href="${basePath}">${basePath}</a><br/>
        Label: ${dataItem.label}<br/>
        Item Definition: ${dataItem.itemDefinition.name}<br/>
        UID: ${dataItem.uid}<br/>
        Created: ${dataItem.created?string.short}<br/>
        Modified: ${dataItem.modified?string.short}<br/>
    </p>

    <h2>Item Value Definitions & Values</h2>
    <p>
        <table>
            <tr>
                <th>Name</th>
                <th>Path</th>
                <th>Value Definition</th>
                <th>Data/Profile</th>
                <th>Value Type</th>
                <th>Value</th>
            </tr>
            <#list itemValueDefinitions as ivd>
                <tr>
                    <td>${ivd.name}</td>
                    <td>${ivd.path}</td>
                    <td>${ivd.valueDefinition.name}</td>
                    <td><#if ivd.fromData>D</#if> <#if ivd.fromProfile>P</#if></td>
                    <td>${ivd.valueDefinition.valueType}</td>
                    <td><#if itemValuesMap[ivd.path]??>${itemValuesMap[ivd.path][0].value}<#else>n/a</#if></td>
                </tr>
            </#list>
        </table>
    </p>

</#if>

<#include '/includes/after_content.ftl'>