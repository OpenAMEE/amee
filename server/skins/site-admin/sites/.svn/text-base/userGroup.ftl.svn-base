<#assign sectionName = "environments">
<#include '/includes/before_content.ftl'>
<h1>Environment Administration</h1>
<p><a href='/environments'>Environments</a> /
   <a href='/environments/${environment.uid}'>${environment.name}</a> /
   <a href='/environments/${environment.uid}/users'>Users</a> /
   <a href='/environments/${environment.uid}/users/${user.uid}'>${user.username}</a> /
   <a href='/environments/${environment.uid}/users/${user.uid}/groups'>Groups</a> /
   <a href='/environments/${environment.uid}/users/${user.uid}/groups/${group.uid}'>${group.name}</a></p>
<h2>Group Details</h2>
<p>
<#if browser.userActions.allowView>
Name: ${group.name}<br/>
</#if>
</p>
<#if browser.userActions.allowModify>
  <h2>Manage</h2>
  <p>
  <a href='/environments/${environment.uid}/users/${user.uid}/groups/${group.uid}/roles'>Roles</a><br/>
  </p>
</#if>
<#include '/includes/after_content.ftl'>