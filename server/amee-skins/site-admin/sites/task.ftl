<#assign sectionName = "environments">
<#include '/includes/before_content.ftl'>
<h1>Environment Administration</h1>
<p><a href='/environments'>Environments</a> /
   <a href='/environments/${environment.uid}'>${environment.name}</a> /
   <a href='/environments/${environment.uid}/tasks'>Tasks</a> /
   <a href='/environments/${environment.uid}/tasks/${scheduledTask.name}'>${scheduledTask.name}</a></p>
<h2>Details</h2>
<#if browser.scheduledTaskActions.allowModify>
<p>
Name: ${scheduledTask.name}<br/>
Created: ${scheduledTask.created?string.short}<br/>
Modified: ${scheduledTask.modified?string.short}<br/>
</p>
</#if>
<#if browser.scheduledTaskActions.allowModify>
  <h2>Update Task</h2>
  <p>
  <form action='/environments/${environment.uid}/tasks/${scheduledTask.uid}?method=put' method='POST' enctype='application/x-www-form-urlencoded'>
  Name: <input name='name' value='${scheduledTask.name}' type='text' size='30'/><br/>
  Component: <input name='component' value='${scheduledTask.component}' type='text' size='30'/><br/>
  Method: <input name='method' value='${scheduledTask.method}' type='text' size='30'/><br/>
  Cron: <input name='cron' value='${scheduledTask.cron}' type='text' size='30'/><br/>
  Duration: <input name='duration' value='${scheduledTask.duration?c}' type='text' size='30'/><br/>
  Servers: <input name='servers' value='${scheduledTask.servers}' type='text' size='60'/><br/>
  Run on shutdown:
    Yes <input type="radio" name="runOnShutdown" value="true"<#if scheduledTask.runOnShutdown> checked</#if>/>
    No <input type="radio" name="runOnShutdown" value="false"<#if !scheduledTask.runOnShutdown> checked</#if>/><br/>
  Enabled:
    Yes <input type="radio" name="enabled" value="true"<#if scheduledTask.enabled> checked</#if>/>
    No <input type="radio" name="enabled" value="false"<#if !scheduledTask.enabled> checked</#if>/><br/><br/>
  <input type='submit' value="Update Task"/><br/>
  </form>
  </p>
</#if>
<#include '/includes/after_content.ftl'>