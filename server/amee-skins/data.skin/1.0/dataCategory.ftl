<#include '/dataCommon.ftl'>
<#include '/includes/before_content.ftl'>

<#if authorizationContext?? && authorizationContext.hasBeenChecked() && authorizationContext.isSuperUser()>
    <#assign allowPermissionEdit = true>
<#else>
    <#assign allowPermissionEdit = false>
</#if>

<script src="/scripts/amee/api_service.js" type="text/javascript"></script>
<script src="/scripts/amee/data_service.js" type="text/javascript"></script>

<script type="text/javascript">

    var PERMISSIONS_EDITOR = new PermissionsEditor();

    function openPermissionEditor(dataCategoryUid) {
        PERMISSIONS_EDITOR.open({entityUid: dataCategoryUid, entityType: 'DC'});
    }

    function deleteDataCategory(dataCategoryUid, dataCategoryPath) {
        var resourceUrl = dataCategoryPath + '?method=delete';
        var resourceElem = $('Elem_' + dataCategoryUid);
        var resourceType = 'Data Category';
        var deleteResource = new DeleteResource();
        deleteResource.deleteResource(resourceUrl, resourceElem, resourceType);
    }

    function deleteDataItem(uid, dataItemPath) {
        var resourceUrl = dataItemPath + '?method=delete';
        var resourceElem = $('Elem_' + uid);
        var resourceType = 'Data Item';
        var deleteResource = new DeleteResource();
        deleteResource.deleteResource(resourceUrl, resourceElem, resourceType);
    }

    function localeonchange() {
        var form = $('update');
        if ($('localeName')) {
            $('localeName').remove();
        }
        var locale = form['localeName_part'].value;
        var value = form['localeValue_part'].value;
        var locale_value = new Element('input', { id: "localeName", name: "name_" + locale , value: value});
        locale_value.hide()
        form.insert(locale_value);
    }

</script>

<h1>Data Category</h1>

<#include 'dataTrail.ftl'>

<h2>Data Category Details</h2>
<p>Name: ${dataCategory.name}<br/>
   <#if dataCategory.path != ''>Path: ${dataCategory.path}<br/></#if>
   Full Path: ${fullPath}<br/>
   <#if dataCategory.itemDefinition??>Item Definition: ${dataCategory.itemDefinition.name}<br/></#if>
   Environment: ${dataCategory.environment.name}<br/>
   UID: ${dataCategory.uid}<br/>
   Created: ${dataCategory.created?string.short}<br/>
   Modified: ${dataCategory.modified?string.short}<br/>
</p>

<#if 0 != dataCategories?size>
    <h2>Data Categories</h2>
    <p>
        <table>
            <tr>
                <th>Path</th>
                <th>Actions</th>
            </tr>
            <#list dataCategories as dc>
                <tr id='Elem_${dc.uid}'>
                    <td>${dc.name}</td>
                    <td>
                        <#if canViewEntity(dc)><a href='${basePath}${dc.fullPath}'><img src="/images/icons/folder_go.png" title="Go" alt="Go" border="0"/></a>
                        <#if canDeleteEntity(dc)><input type="image" onClick="deleteDataCategory('${dc.uid}', '${basePath}${dc.fullPath}'); return false;" src="/images/icons/folder_delete.png" title="Delete" alt="Delete" border="0"/></#if></#if>
                        <#if allowPermissionEdit><input type="image" onClick="openPermissionEditor('${dc.uid}'); return false;" src="/images/icons/lock_edit.png" title="Edit Permissions" alt="Edit Permissions" border="0"/></#if>
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
                <#assign ref = AMEEEntityReference.getInstance(ObjectType.DI, row.uid)>
                <tr id='Elem_${row.uid}'>
                    <td>${row.findCell('label')}</td>
                    <td>
                        <a href='${basePath}/${row.findCell('path')}'><img src="/images/icons/page_edit.png" title="Edit" alt="Edit" border="0"/></a>
                        <#if canDelete()><input type="image" onClick="deleteDataItem('${row.uid}', '${basePath}/${row.uid}'); return false;" src="/images/icons/page_delete.png" title="Delete" alt="Delete" border="0"/></#if>
                    </td>
                </tr>
            </#list>
        </table>
        <#assign pagerName = 'pagerBottom'>
        <#include '/includes/pager.ftl'>
    </p>
</#if>

<#if canModify()>
    <h2>Update Data Category</h2>
    <p>
        <form id="update" action='?method=put' method='POST' enctype='application/x-www-form-urlencoded'>
            <table>
                <tr>
                   <td>Name:</td>
                   <td colspan="2"><input name='name' value='${dataCategory.name}' type='text' size='30'/></td>
                </tr>
                <tr>
                   <td>Path:</td>
                   <td colspan="2"><input name='path' value='${dataCategory.path}' type='text' size='30'/></td>
                </tr>
                <tr>
                   <td>Item Definition:</td>
                   <td colspan="2">
                       <select name='itemDefinitionUid'>
                       <option value=''>(No Item Definition)</option>
                       <#list itemDefinitions as id>
                           <option value='${id.uid}'<#if dataCategory.itemDefinition?? && dataCategory.itemDefinition.uid == id.uid> selected</#if>>${id.name}</option>
                       </#list>
                       </select>
                   </td>
                </tr>
                <tr>
                   <td>Deprecated:</td>
                   <td colspan="2">
                    Yes <input type="radio" name="deprecated" value="true"<#if dataCategory.deprecated> checked</#if>/>
                    No <input type="radio" name="deprecated" value="false"<#if !dataCategory.deprecated> checked</#if>/>
                   </td>
                </tr>
            </table>
            <input type='submit' value='Update'/>
        </form>
    </p>
</#if>

<#if canCreate() && !dataCategory.aliasedCategory??>
    <h2>Create</h2>
    <p>
        <form action='${basePath}' method='POST' enctype='application/x-www-form-urlencoded'>
            Type: <select id='newObjectType' name='newObjectType'>
                <option value="DC">Data Category</option>
            <#if itemDefinition??>
                <option value="DI">Data Item (for ${itemDefinition.name})</option>
            </#if>
            </select><br/>
            Name: <input name='name' value='' type='text' size='30'/><br/>
            Path: <input name='path' value='' type='text' size='30'/><br/>
            Item Definition: <select name='itemDefinitionUid'>
            <option value=''>(No Item Definition)</option>
            <#list itemDefinitions as id>
                <option value='${id.uid}'>${id.name}</option>
            </#list>
            </select><br/><br/>
            <input type='submit' value='Create'/>
        </form>
    </p>
</#if>

<#include '/includes/after_content.ftl'>