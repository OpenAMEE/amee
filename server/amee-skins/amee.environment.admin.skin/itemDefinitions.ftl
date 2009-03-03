<#assign sectionName = 'environments'>
<#include '/includes/before_content.ftl'>
<script type="text/javascript">
function deleteItemDefinition(itemDefinitionUid) {
  resourceUrl = '/environments/${environment.uid}/itemDefinitions/' + itemDefinitionUid + '?method=delete';
  resourceElem = $('Elem_' + itemDefinitionUid);
  resourceType = 'Item Definition'; 
  var deleteResource = new DeleteResource()
  deleteResource.deleteResource(resourceUrl, resourceElem, resourceType);
}
</script>
<h1>Environment Administration</h1>
<p><a href='/environments'>Environments</a> / 
   <a href='/environments/${environment.uid}'>${environment.name}</a> / 
   <a href='/environments/${environment.uid}/itemDefinitions'>Item Definitions</a></p>
<h2>Item Definitions</h2>
<p>
<#assign pagerItemsLabel = 'item definitions'>
<#assign pagerName = 'pagerTop'>
<#include '/includes/pager.ftl'>
<table>
<tr>
  <th>Name</th>
  <th>Actions</th>
</tr>
<#list itemDefinitions as id>
  <tr id='Elem_${id.uid}'>
    <td>${id.name}</td>
    <td>
        <#if browser.itemDefinitionActions.allowView><a href='/environments/${environment.uid}/itemDefinitions/${id.uid}'><img src="/images/icons/page_edit.png" title="Edit" alt="Edit" border="0"/></a></#if>
        <#if browser.itemDefinitionActions.allowDelete><input type="image" onClick="deleteItemDefinition('${id.uid}'); return false;" src="/images/icons/page_delete.png" title="Delete" alt="Delete" border="0"/></#if>
    </td>
  </tr>
</#list>
</table>
<#assign pagerName = 'pagerBottom'>
<#include '/includes/pager.ftl'>
</p>
<#if browser.itemDefinitionActions.allowCreate>
<p>
  <h2>Create Item Definition</h2>
  <p>
    <form action='/environments/${environment.uid}/itemDefinitions' method='POST' enctype='application/x-www-form-urlencoded'>
    Name: <input name='name' type='text' size='30'/><br/><br/>
    <input type='submit' value='Create'/>
  </form>
  </p>
<p>
</#if>
<#include '/includes/after_content.ftl'>