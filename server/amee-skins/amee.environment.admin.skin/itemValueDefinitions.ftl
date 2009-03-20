<#assign sectionName = 'environments'>
<#include '/includes/before_content.ftl'>
<script type="text/javascript">
function deleteItemValueDefinition(itemValueDefinitionUid) {
  resourceUrl = '/environments/${environment.uid}/itemDefinitions/${itemDefinition.uid}/itemValueDefinitions/' + itemValueDefinitionUid + '?method=delete';
  resourceElem = $('Elem_' + itemValueDefinitionUid);
  resourceType = 'Item Value Definition'; 
  var deleteResource = new DeleteResource()
  deleteResource.deleteResource(resourceUrl, resourceElem, resourceType);
}
</script>
<h1>Environment Administration</h1>
<p><a href='/environments'>Environments</a> / 
   <a href='/environments/${environment.uid}'>${environment.name}</a> / 
   <a href='/environments/${environment.uid}/itemDefinitions'>Item Definitions</a> / 
   <a href='/environments/${environment.uid}/itemDefinitions/${itemDefinition.uid}'>${itemDefinition.name}</a> / 
   <a href='/environments/${environment.uid}/itemDefinitions/${itemDefinition.uid}/itemValueDefinitions'>Item Value Definitions</a></p>
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
  <tr id="Elem_${ivd.uid}">
    <td>${ivd.name}</td>
    <td>
      <#if browser.valueDefinitionActions.allowView>
        <a href='/environments/${environment.uid}/valueDefinitions/${ivd.valueDefinition.uid}'>${ivd.valueDefinition.name}</a>
      <#else>
        ${ivd.valueDefinition.name}
      </#if>
    </td>
    <td>${ivd.valueDefinition.valueType}</td>
    <td>
        <#if browser.itemValueDefinitionActions.allowView><a href='/environments/${environment.uid}/itemDefinitions/${itemDefinition.uid}/itemValueDefinitions/${ivd.uid}'><img src="/images/icons/page_edit.png" title="Edit" alt="Edit" border="0"/></a></#if>
        <#if browser.itemValueDefinitionActions.allowDelete><input type="image" onClick="deleteItemValueDefinition('${ivd.uid}'); return false;" src="/images/icons/page_delete.png" title="Delete" alt="Delete" border="0"/></#if>
    </td>
  </tr>
</#list>
</table>
</p>
<#if browser.itemValueDefinitionActions.allowCreate>
  <h2>Create Item Value Definition</h2>
  <p>
  <#if valueDefinitions??>
    <form action='/environments/${environment.uid}/itemDefinitions/${itemDefinition.uid}/itemValueDefinitions' method='POST' enctype='application/x-www-form-urlencoded'>
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
      Allowed roles: <input name='allowedRoles' type='text' size='50'/><br/>
      Unit: <input name='unit' type='text' size='10'/><br/>
      PerUnit: <input name='perUnit' type='text' size='10'/><br/>
      API Version:
      <#list apiVersions as v>
        ${v.version}<input type="checkbox" name="apiversion-${v.version}" value="true">
      </#list><br>
      Alias To: <select name='aliasedTo'>
        <option value='' selected>None</option>
        <#list itemValueDefinitions as ivd>
            <option value='${ivd.uid}'>${ivd.name}</option>
        </#list>
      </select>
      <br/><br/>
      <input type='submit' value='Create'/>
    </form>
  <#else>
    No Value Definitions available.
  </#if>
  </p>
</#if>
<#include '/includes/after_content.ftl'>