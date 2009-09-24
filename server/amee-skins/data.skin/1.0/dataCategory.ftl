<#include '/dataCommon.ftl'>
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
<#assign children = pathItem.findChildrenByType('DC')>

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

<#if 0 != children?size>
    <h2>Data Categories</h2>
    <p>
    <table>
        <tr>
            <th>Path</th>
            <th>Actions</th>
        </tr>
        <#list children as pi>
            <#if canViewEntity(pi)>
                <tr id='Elem_${pi.uid}'>
                    <td>${pi.name}</td>
                    <td>
                    <a href='${basePath}/${pi.path}'><img src="/images/icons/page_edit.png" title="Edit" alt="Edit" border="0"/></a>
                    <#if canDeleteEntity(pi)><input type="image" onClick="deleteDataCategory('${pi.uid}', '${basePath}/${pi.path}'); return false;" src="/images/icons/page_delete.png" title="Delete" alt="Delete" border="0"/></#if>
                    </td>
                </tr>
            </#if>
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
            <#if canViewEntity(ref)>
                <tr id='Elem_${row.uid}'>
                    <td>${row.findCell('label')}</td>
                    <td>
                    <a href='${basePath}/${row.findCell('path')}'><img src="/images/icons/page_edit.png" title="Edit" alt="Edit" border="0"/></a>
                    <#if canDeleteEntity(ref)><input type="image" onClick="deleteDataItem('${row.uid}', '${basePath}/${row.uid}'); return false;" src="/images/icons/page_delete.png" title="Delete" alt="Delete" border="0"/></#if>
                    </td>
                </tr>
            </#if>
        </#list>
    </table>
    <#assign pagerName = 'pagerBottom'>
    <#include '/includes/pager.ftl'>
    </p>
</#if>

<#if canModifyEntity(dataCategory)>
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
            <#if dataCategory.localeNames?size != 0>
                <#list dataCategory.localeNames?keys as locale>
                <tr>
                    <td>Name: [${locale}]</td>
                    <td><input name='name_${locale}' value='${dataCategory.localeNames[locale].name}' type='text' size='30'/></td>
                    <td>Remove: <input type="checkbox" name="remove_name_${locale}"/> </td>
                </tr>
                </#list>
            </#if>
            <tr>
                <td>New Locale Name:</td>
                <td>
                    <select name='localeName_part' onchange='javascript:localeonchange();'> <br/>
                    <#list availableLocales as locale>
                        <option value='${locale}'>${locale}</option>
                    </#list>
                    </select>
                    <input name='localeValue_part' type='text' size='30' onchange='javascript:localeonchange();'/><br/>
                </td>
            </tr>
        </table>
        <input type='submit' value='Update'/>
    </form>
    </p>
</#if>

<#if canCreateEntity(dataCategory) && !dataCategory.aliasedCategory??>
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