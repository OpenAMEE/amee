<#include 'dataCommon.ftl'>
<#include '/includes/before_content.ftl'>

<script src="/scripts/amee/api_service.js" type="text/javascript"></script>
<script src="/scripts/amee/data_service.js" type="text/javascript"></script>

<script type="text/javascript">
    
    function deleteDataCategory(dataCategoryUid, dataCategoryPath) {
        resourceUrl = dataCategoryPath + '?method=delete';
        resourceElem = $('Elem_' + dataCategoryUid);
        resourceType = 'Data Category';
        var deleteResource = new DeleteResource();
        deleteResource.deleteResource(resourceUrl, resourceElem, resourceType);
    }

    function deleteDataItem(uid, dataItemPath) {
        resourceUrl = dataItemPath + '?method=delete';
        resourceElem = $('Elem_' + uid);
        resourceType = 'Data Item';
        var deleteResource = new DeleteResource();
        deleteResource.deleteResource(resourceUrl, resourceElem, resourceType);
    }

</script>

<h1>Data Category</h1>

<#include 'dataTrail.ftl'>
<#assign children = browser.pathItem.findChildrenByType('DC')>

<h2>Data Category Details</h2>
<p>Name: ${dataCategory.name}<br/>
   <#if dataCategory.path != ''>Path: ${dataCategory.path}<br/></#if>
   Full Path: ${browser.fullPath}<br/>
   <#if dataCategory.itemDefinition??>Item Definition: ${dataCategory.itemDefinition.name}<br/></#if>
   Environment: ${dataCategory.environment.name}<br/>
   UID: ${dataCategory.uid}<br/>
   Created: ${dataCategory.created?string.short}<br/>
   Modified: ${dataCategory.modified?string.short}<br/>
</p>

<#if 0 != children?size>
    <h2>Data Categories</h2>
    <p>
    <table>
        <tr>
            <th>Path</th>
            <th>Actions</th>
        </tr>
        <#list children as pi>
            <tr id='Elem_${pi.uid}'>
                <td>${pi.name}</td>
                <td>
                <#if browser.dataCategoryActions.allowView><a href='${basePath}/${pi.path}'><img src="/images/icons/page_edit.png" title="Edit" alt="Edit" border="0"/></a></#if>
                <#if browser.dataCategoryActions.allowDelete><input type="image" onClick="deleteDataCategory('${pi.uid}', '${basePath}/${pi.path}'); return false;" src="/images/icons/page_delete.png" title="Delete" alt="Delete" border="0"/></#if>
                </td>
            </tr>
        </#list>
    </table>
    </p>
</#if>

<#if sheet?? && 0 != sheet.rows?size>
    <h2>Data Items</h2>
    <p>
    <#assign pagerItemsLabel = 'data items'>
    <#assign pagerName = 'pagerTop'>
    <#assign pagerUrl = basePath>
    <#include '/includes/pager.ftl'>
    <table>
        <tr>
            <th>Item</th>
            <th>Actions</th>
        </tr>
        <#list sheet.rows as row>
            <tr id='Elem_${row.uid}'>
                <td>${row.findCell('label')}</td>
                <td>
                <#if browser.dataItemActions.allowView><a href='${basePath}/${row.findCell('path')}'><img src="/images/icons/page_edit.png" title="Edit" alt="Edit" border="0"/></a></#if>
                <#if browser.dataItemActions.allowDelete><input type="image" onClick="deleteDataItem('${row.uid}', '${basePath}/${row.uid}'); return false;" src="/images/icons/page_delete.png" title="Delete" alt="Delete" border="0"/></#if>
                </td>
            </tr>
        </#list>
    </table>
    <#assign pagerName = 'pagerBottom'>
    <#include '/includes/pager.ftl'>
    </p>
</#if>

<#if browser.dataCategoryActions.allowModify>
    <h2>Update Data Category</h2>
    <p>
    <form action='${basePath}?method=put' method='POST' enctype='application/x-www-form-urlencoded'>
        Name: <input name='name' value='${dataCategory.name}' type='text' size='30'/><br/>
        Path: <input name='path' value='${dataCategory.path}' type='text' size='30'/><br/>
        Item Definition: <select name='itemDefinitionUid'>
        <option value=''>(No Item Definition)</option>
        <#list browser.itemDefinitions as id>
            <option value='${id.uid}'<#if dataCategory.itemDefinition?? && dataCategory.itemDefinition.uid == id.uid> selected</#if>>${id.name}</option>
        </#list>
        </select><br/><br/>
        <input type='submit' value='Update'/>
    </form>
    </p>
</#if>

<#if browser.dataCategoryActions.allowCreate || browser.dataItemActions.allowCreate>
    <h2>Create</h2>
    <p>
    <form action='${basePath}' method='POST' enctype='application/x-www-form-urlencoded'>
        Type: <select id='newObjectType' name='newObjectType'>
        <#if browser.dataCategoryActions.allowCreate>
            <option value="DC">Data Category</option>
        </#if>
        <#if itemDefinition?? && browser.dataItemActions.allowCreate>
            <option value="DI">Data Item (for ${itemDefinition.name})</option>
        </#if>
        </select><br/>
        Name: <input name='name' value='' type='text' size='30'/><br/>
        Path: <input name='path' value='' type='text' size='30'/><br/>
        Item Definition: <select name='itemDefinitionUid'>
        <option value=''>(No Item Definition)</option>
        <#list browser.itemDefinitions as id>
            <option value='${id.uid}'>${id.name}</option>
        </#list>
        </select><br/><br/>
        <input type='submit' value='Create'/>
    </form>
    </p>
</#if>

<#include '/includes/after_content.ftl'>