<#assign sectionName = 'admin'>

<#include '/includes/before_content.ftl'>

<script type="text/javascript">
    function deleteValueDefinition(valueDefinitionUid) {
        resourceUrl = '/admin/valueDefinitions/' + valueDefinitionUid + '?method=delete';
        resourceElem = $('Elem_' + valueDefinitionUid);
        resourceType = 'Value Definition';
        var deleteResource = new DeleteResource()
        deleteResource.deleteResource(resourceUrl, resourceElem, resourceType);
    }
</script>

<h1>Administration</h1>

<p><a href='/admin'>Admin</a> / 
   <a href='/admin/valueDefinitions'>Value Definitions</a></p>

<h2>Value Definitions</h2>
<p>
    <#assign pagerItemsLabel = 'value definitions'>
    <#assign pagerName = 'pagerTop'>
    <#include '/includes/pager.ftl'>
    <table>
        <tr>
            <th>Name</th>
            <th>Value Type</th>
            <th>Actions</th>
        </tr>
        <#list valueDefinitions as vd>
            <#if canViewEntity(vd)>
                <tr id='Elem_${vd.uid}'>
                    <td>${vd.name}</td>
                    <td>${vd.valueType}</td>
                    <td>
                        <a href='/admin/valueDefinitions/${vd.uid}'><img src="/images/icons/page_edit.png" title="Edit" alt="Edit" border="0"/></a>
                        <#if canDeleteEntity(vd)><input type="image" onClick="deleteValueDefinition('${vd.uid}'); return false;" src="/images/icons/page_delete.png" title="Delete" alt="Delete" border="0"/></#if>
                    </td>
                </tr>
            </#if>
        </#list>
    </table>
    <#assign pagerName = 'pagerBottom'>
    <#include '/includes/pager.ftl'>
</p>

<#if canCreate()>
    <p>
        <h2>Create Value Definition</h2>
        <p>
        <form action='/admin/valueDefinitions' method='POST' enctype='application/x-www-form-urlencoded'>
            Name: <input name='name' type='text' size='30'/><br/>
            Value Type: <select name='valueType'>
            <#list valueTypes?keys as key>
                <option value='${key}'>${valueTypes[key]}</option>
            </#list>
            </select><br/><br/>
            <input type='submit' value='Create'/>
        </form>
    </p>
</#if>

<#include '/includes/after_content.ftl'>