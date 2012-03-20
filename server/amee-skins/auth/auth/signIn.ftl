<#assign pageTitle = "Sign In">
<#include '/includes/before_content.ftl'>
<h1>Sign In</h1>
<#if activeUser?? && activeUser.username != 'guest'>
  <p>You are currently signed in as '${activeUser.username}'. You can sign in as another user below.</p>
<#else>
  <p>You are not currently signed in. Please use the form below to sign in.</p>
</#if>
<p>
<form action='/auth/signIn?method=put' method='POST' enctype='application/x-www-form-urlencoded'>
<input name='next' value='${next}' type='hidden'/>
Username: <input name='username' type='text' size='30'/><br/>
Password: <input name='password' type='password' size='30'/><br/><br/>
<input type='submit' value='Sign In'/><br/>
</form>
</p>
<#include '/includes/after_content.ftl'>