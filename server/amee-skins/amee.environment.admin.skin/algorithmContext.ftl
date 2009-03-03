<#assign sectionName = 'environments'>
<#include '/includes/before_content.ftl'>
<h1>Environment Administration</h1>
<p><a href='/environments'>Environments</a> /
   <a href='/environments/${environment.uid}'>${environment.name}</a> /
   <a href='/environments/${environment.uid}/algorithmContexts'>Algorithm Contexts</a> /
   <a href='/environments/${environment.uid}/algorithmContexts/${algorithmContext.uid}'>${algorithmContext.name}</a></p>
<h2>Algorithm Context Details</h2>
<p>Name: ${algorithmContext.name}<br/>
   Created: ${algorithmContext.created?datetime}<br/>
   Modified: ${algorithmContext.modified?datetime}<br/>
</p>
<#if browser.algorithmActions.allowModify>
  <h2>Update Algorithm</h2>
  <p>
  <form action='/environments/${environment.uid}/algorithmContexts/${algorithmContext.uid}?method=put' method='POST' enctype='application/x-www-form-urlencoded'>
    Name: <input name='name' value='${algorithmContext.name}' type='text' size='30'/><br/>
    Content: <textarea name='content' rows='15' cols='60'>${algorithmContext.content}</textarea><br/><br/>
    <input type='submit' value='Update'/>
  </form>
  </p>
</#if>
<#include '/includes/after_content.ftl'>