<#assign sectionName = 'environments'>
<#include '/includes/before_content.ftl'>
<h1>Environment Administration</h1>
<p><a href='/environments'>Environments</a> / 
   <a href='/environments/${environment.uid}'>${environment.name}</a> / 
   <a href='/environments/${environment.uid}/itemDefinitions'>Item Definitions</a> / 
   <a href='/environments/${environment.uid}/itemDefinitions/${itemDefinition.uid}'>${itemDefinition.name}</a></p>
<h2>Item Definition Details</h2>
<p>Name: ${itemDefinition.name}<br/>
   Created: ${itemDefinition.created?string.short}<br/>
   Modified: ${itemDefinition.modified?string.short}<br/>
</p>
<h2>Manage</h2>
<p>
  <a href='/environments/${environment.uid}/itemDefinitions/${itemDefinition.uid}/algorithms'>Algorithms</a><br/>
  <a href='/environments/${environment.uid}/itemDefinitions/${itemDefinition.uid}/itemValueDefinitions'>Item Value Definitions</a><br/>
</p>
<#if browser.itemDefinitionActions.allowModify>
  <h2>Update Item Definition</h2>
  <p>
  <form action='/environments/${environment.uid}/itemDefinitions/${itemDefinition.uid}?method=put' method='POST' enctype='application/x-www-form-urlencoded'>
    Name: <input name='name' value='${itemDefinition.name}' type='text' size='30'/><br/>
    <#if itemDefinition.localeNames?size != 0>
      <#list itemDefinition.localeNames?keys as locale>
          Name (${locale}): <input name='name_${locale}' value='${itemDefinition.localeNames[locale].name}' type='text' size='30'/><br/>
      </#list>
    </#if>
    Skip Recalculation (Profile Items):
      Yes <input type="radio" name="skipRecalculation" value="true"<#if itemDefinition.skipRecalculation> checked</#if>/>
      No <input type="radio" name="skipRecalculation" value="false"<#if !itemDefinition.skipRecalculation> checked</#if>/><br/>
    Drill Down: <input name='drillDown' value='${itemDefinition.drillDown}' type='text' size='50'/><br/><br/>
    <input type='submit' value='Update'/>
  </form>
  </p>
</#if>
<#include '/includes/after_content.ftl'>