<#assign sectionName = "environments">
<#include '/includes/before_content.ftl'>

<script type="text/javascript">
function deleteAttributeGroupDefinition(attributeGroupDefinitionUid) {
  resourceUrl = '/environments/${environment.uid}/attributeGroupDefinitions/' + attributeGroupDefinitionUid + '?method=delete';
  resourceElem = $('Elem_' + attributeGroupDefinitionUid);
  resourceType = 'Attribute Group Definition';
  var deleteResource = new DeleteResource()
  deleteResource.deleteResource(resourceUrl, resourceElem, resourceType);
}
</script>

<h1>Environment Administration</h1>

<p><a href='/environments'>Environments</a> /
   <a href='/environments/${environment.uid}'>${environment.name}</a> /
   <a href='/environments/${environment.uid}/attributeGroupDefinitions'>Attribute Group Definitions</a>
</p>
<#if browser.attributeGroupDefinitionActions.allowList>
<p>

<#assign pagerItemsLabel = 'attributeGroupDefinitions'>
<#assign pagerName = 'pagerTop'>
<#include '/includes/pager.ftl'>

<table>
<tr>
  <th>Name</th>
  <th></th>
</tr>
<#list attributeGroupDefinitions as agd>
  <tr id='Elem_${agd.uid}'>
    <td>${agd.name}</td>
    <td>
        <#if browser.attributeGroupDefinitionActions.allowView><a href='/environments/${environment.uid}/attributeGroupDefinitions/${agd.uid}'><img src="/images/icons/page_edit.png" title="Edit" alt="Edit" border="0"/></a></#if>
        <#if browser.attributeGroupDefinitionActions.allowDelete><input type="image" onClick="deleteAttributeGroupDefinition('${agd.uid}'); return false;" src="/images/icons/page_delete.png" title="Delete" alt="Delete" border="0"/></#if>
    </td>
  </tr>
</#list>
</table>

<#assign pagerName = 'pagerBottom'>
<#include '/includes/pager.ftl'>
    
</p>
</#if>
<#if browser.attributeGroupDefinitionActions.allowCreate>
  <h2>Create Attribute Group Definition</h2>
  <p>
  <form action='/environments/${environment.uid}/attributeGroupDefinitions' method='POST' enctype='application/x-www-form-urlencoded'>
  Name: <input name='name' type='text' size='30'/><br/><br/>
  <input type='submit' value="Create"/><br/>
  </form>
  </p>
</#if>
<#include '/includes/after_content.ftl'>