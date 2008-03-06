<#assign sectionName = 'environments'>
<#include '/includes/before_content.ftl'>
<h1>Environment Administration</h1>
<p><a href='/environments'>Environments</a> / 
   <a href='/environments/${environment.uid}'>${environment.name}</a> / 
   <a href='/environments/${environment.uid}/itemDefinitions'>Item Definitions</a> / 
   <a href='/environments/${environment.uid}/itemDefinitions/${itemDefinition.uid}'>${itemDefinition.name}</a> / 
   <a href='/environments/${environment.uid}/itemDefinitions/${itemDefinition.uid}/algorithms'>Algorithms</a> / 
   <a href='/environments/${environment.uid}/itemDefinitions/${itemDefinition.uid}/algorithms/${algorithm.uid}'>${algorithm.name}</a></p>
<h2>Algorithm Details</h2>
<p>Name: ${algorithm.name}<br/>
   Created: ${algorithm.created?string.short}<br/>
   Modified: ${algorithm.modified?string.short}<br/>
</p>
<#if browser.algorithmActions.allowModify>
  <h2>Update Algorithm</h2>
  <p>
  <form action='/environments/${environment.uid}/itemDefinitions/${itemDefinition.uid}/algorithms/${algorithm.uid}?method=put' method='POST' enctype='application/x-www-form-urlencoded'>
    Name: <input name='name' value='${algorithm.name}' type='text' size='30'/><br/>
    Content: <textarea name='content' rows='15' cols='60'>${algorithm.content}</textarea><br/><br/>
    <input type='submit' value='Update'/>
  </form>
  </p>
</#if>
<#include '/includes/after_content.ftl'>