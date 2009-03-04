<#include 'dataCommon.ftl'>
<#include '/includes/before_content.ftl'>
<#include '/includes/furniture.ftl'>

<script src="/scripts/amee/api_service.js" type="text/javascript"></script>
<script src="/scripts/amee/data_service.js" type="text/javascript"></script>

<h1>Data Item</h1>

<#include 'dataTrail.ftl'>

<h2>Data Item Details</h2>
<p><#if dataItem.name != ''>Name: ${dataItem.name}<br/></#if>
    <#if dataItem.path != ''>Path: ${dataItem.path}<br/></#if>
    Start Date: ${startEndDate(dataItem.startDate)}<br/>
    End Date: <#if dataItem.endDate??>${startEndDate(dataItem.endDate)}<#else>None</#if><br/>
    Full Path: ${browser.fullPath}<br/>
    Label: ${dataItem.label}<br/>
    Item Definition: ${dataItem.itemDefinition.name}<br/>
    Environment: ${dataItem.environment.name}<br/>
    UID: ${dataItem.uid}<br/>
    Created: ${dataItem.created?string.short}<br/>
    Modified: ${dataItem.modified?string.short}<br/>
</p>

<h2>Item Values</h2>
<p>
    <table>
        <tr>                    
            <th>Name</th>
            <th>Value Definition</th>
            <th>Value Type</th>
            <th>Value</th>
            <th>Actions</th>
        </tr>
        <#list dataItem.itemValues as iv>
        <tr id="CV_${iv.uid}">
            <td>${iv.itemValueDefinition.name}</td>
            <td>${iv.itemValueDefinition.valueDefinition.name}</td>
            <td>${iv.itemValueDefinition.valueDefinition.valueType}</td>
            <td>${iv.value}</td>
            <td>
            <#if browser.dataItemActions.allowView><a href='${basePath}/${iv.displayPath}'><img src="/images/icons/page_edit.png" title="Edit" alt="Edit" border="0"/></a></#if>
            </td>
        </tr>
        </#list>
    </table>
</p>

<#if browser.dataItemActions.allowModify>
<h2>Update Data Item</h2>
<p>
    <form action='${basePath}?method=put' method='POST' enctype='application/x-www-form-urlencoded'>
        Name: <input name='name' value='${dataItem.name}' type='text' size='30' style="margin-left:25px"/><br/>
        Path: <input name='path' value='${dataItem.path}' type='text' size='30'style="margin-left:35px" /><br/>
        StartDate: <input name='startDate' value='${startEndDate(dataItem.startDate)}' type='text' size='20'/> (yyyy-MM-dd'T'HH:mm:ssZ)<br/>
        EndDate: <input name='endDate' value='<#if dataItem.endDate??>${startEndDate(dataItem.endDate)}</#if>' type='text' size='20' style="margin-left:6px"/> (yyyy-MM-dd'T'HH:mm:ssZ)<br/><br/>
        <input type='submit' value='Update'/>
    </form>
</p>
</#if>

<#include '/includes/after_content.ftl'>