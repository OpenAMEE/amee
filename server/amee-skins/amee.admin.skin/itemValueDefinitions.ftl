<#assign sectionName = 'admin'>

<#include '/includes/before_content.ftl'>

<script type="text/javascript">
    function deleteItemValueDefinition(itemValueDefinitionUid) {
        resourceUrl = '/admin/itemDefinitions/${itemDefinition.uid}/itemValueDefinitions/' + itemValueDefinitionUid + '?method=delete';
        resourceElem = $('Elem_' + itemValueDefinitionUid);
        resourceType = 'Item Value Definition';
        var deleteResource = new DeleteResource()
        deleteResource.deleteResource(resourceUrl, resourceElem, resourceType);
    }
</script>

<h1>Administration</h1>

<p><a href='/admin'>Admin</a> / 
   <a href='/admin/itemDefinitions'>Item Definitions</a> /
   <a href='/admin/itemDefinitions/${itemDefinition.uid}'>${itemDefinition.name}</a> /
   <a href='/admin/itemDefinitions/${itemDefinition.uid}/itemValueDefinitions'>Item Value Definitions</a></p>

<h2>Item Value Definitions</h2>
<p>
    <table>
    <tr>
        <th>Name</th>
        <th>Value Definition</th>
        <th>Value Type</th>
        <th>Actions</th>
    </tr>
    <#list itemValueDefinitions as ivd>
        <#if canViewEntity(ivd)>
            <tr id="Elem_${ivd.uid}">
                <td>${ivd.name}</td>
                <td><a href='/admin/valueDefinitions/${ivd.valueDefinition.uid}'>${ivd.valueDefinition.name}</a></td>
                <td>${ivd.valueDefinition.valueType}</td>
                <td>
                    <a href='/admin/itemDefinitions/${itemDefinition.uid}/itemValueDefinitions/${ivd.uid}'><img src="/images/icons/page_edit.png" title="Edit" alt="Edit" border="0"/></a>
                    <#if canDeleteEntity(ivd)><input type="image" onClick="deleteItemValueDefinition('${ivd.uid}'); return false;" src="/images/icons/page_delete.png" title="Delete" alt="Delete" border="0"/></#if>
                </td>
            </tr>
        </#if>
    </#list>
    </table>
</p>

<#if canCreate()>
    <h2>Create Item Value Definition</h2>
    <p>
        <#if valueDefinitions??>
            <form action='/admin/itemDefinitions/${itemDefinition.uid}/itemValueDefinitions' method='POST' enctype='application/x-www-form-urlencoded'>
                Name: <input name='name' type='text' size='50'/><br/>
                Path: <input name='path' type='text' size='50'/><br/>
                Type: <select name='valueDefinitionUid'>
                    <#list valueDefinitions as vd>
                        <option value='${vd.uid}'>${vd.name}</option>
                    </#list>
                </select><br/>
                Default value: <input name='value' type='text' size='10'/><br/>
                Choices: <input name='choices' type='text' size='50'/> (comma delimited name=value pairs)<br/>
                Get value from admin? <select name='fromData'><option value='false'>No</option><option value='true'>Yes</option></select><br/>
                Get value from user? <select name='fromProfile'><option value='false'>No</option><option value='true'>Yes</option></select><br/>
                Unit: <input name='unit' type='text' size='10'/><br/>
                PerUnit: <input name='perUnit' type='text' size='10'/><br/>
                API Version: <#list apiVersions as v>
                    ${v.version}<input type="checkbox" name="apiversion-${v.version}" value="true">
                </#list><br>
                Alias To: <select name='aliasedTo'>
                    <option value='' selected>None</option>
                    <#list itemValueDefinitions as ivd>
                    <option value='${ivd.uid}'>${ivd.name}</option>
                    </#list>
                </select><br/><br/>
                <input type='submit' value='Create'/>
            </form>
        <#else>
            No Value Definitions available.
        </#if>
    </p>
</#if>

<#include '/includes/after_content.ftl'>