<#assign sectionName = "environments">
<#include '/includes/before_content.ftl'>
<h1>Environment Administration</h1>
<p><a href='/environments'>Environments</a> /
   <a href='/environments/${environment.uid}'>${environment.name}</a> /
   <a href='/environments/${environment.uid}/users'>Users</a> /
   <a href='/environments/${environment.uid}/users/${user.uid}'>${user.username}</a></p>
<h2>Details</h2>
<#if browser.userActions.allowView>
<p>
Name: ${user.name}<br/>
Username: ${user.username}<br/>
Email: ${user.email}<br/>
Created: ${user.created?datetime}<br/>
Modified: ${user.modified?datetime}<br/>
</p>
</#if>
<#if browser.userActions.allowModify>
  <h2>Manage</h2>
  <p>
  <a href='/environments/${environment.uid}/users/${user.uid}/groups'>Groups</a><br/>
  </p>
  <h2>Update User</h2>
  <p>
  <form action='/environments/${environment.uid}/users/${user.uid}?method=put' method='POST' enctype='application/x-www-form-urlencoded'>
  Name: <input name='name' value='${user.name}' type='text' size='30'/><br/>
  Username: <input name='username' value='${user.username}' type='text' size='30'/><br/>
  Email Address: <input name='email' value='${user.email}' type='text' size='30'/><br/>
  API Version: <select name='APIVersion'> <br/>
    <#list apiVersions as APIVersion>
        <option value='${APIVersion.version}' <#if user.APIVersion?? && APIVersion.version == user.APIVersion.version>selected='selected'</#if>>${APIVersion.version}</option>
    </#list>
  </select><br/>
  Type:  <select name='type'> <br/> 
    <option value='STANDARD' <#if user.type == 'STANDARD'>selected='selected'</#if>>STANDARD</option>
    <option value='SUPER' <#if user.type == 'SUPER'>selected='selected'</#if>>SUPER</option>
    <option value='GUEST' <#if user.type == 'GUEST'>selected='selected'</#if>>GUEST</option>
    <option value='ANONYMOUS' <#if user.type == 'ANONYMOUS'>selected='selected'</#if>>ANONYMOUS</option>
  </select><br/>
  Status:  <select name='status'> <br/>
    <option value='NEW' <#if user.status == 'NEW'>selected='selected'</#if>>NEW</option>
    <option value='ACTIVE' <#if user.status == 'ACTIVE'>selected='selected'</#if>>ACTIVE</option>
    <option value='CLOSED' <#if user.status == 'BLOCK'>selected='selected'</#if>>BLOCK</option>
  </select><br/>
  Locale: <select name='locale'> <br/>
        <option value='en_GB'>en_GB</option>
        <#list availableLocales as locale>
           <option value='${locale}' <#if user.locale?? && locale == user.locale>selected='selected'</#if>>${locale}</option>
       </#list>
     </select>
    <br/>
  <input type='submit' value="Update User"/><br/>
  </form>
  </p>
  <h2>Change Password</h2>
  <p>
  <form action='/environments/${environment.uid}/users/${user.uid}?method=put' method='POST' enctype='application/x-www-form-urlencoded'>
  Password: <input name='password' value='' type='password' size='30'/><br/><br/>
  <input type='submit' value="Change Password"/><br/>
  </form>
  </p>
</#if>
<#include '/includes/after_content.ftl'>