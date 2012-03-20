<#assign pageTitle = "Authentication">
<#include '/includes/before_content.ftl'>
<h1>Authentication</h1>
<#if activeUser?? && activeUser.username != 'guest'>
  <p>You are currently signed in as '${activeUser.username}'. You can <a href="/auth/signOut">Sign Out</a> or <a href="/auth/signIn?next=${next}">Sign In</a> again.</p></p>
<#else>
  <p>You are currently not signed in. To continue please <a href="/auth/signIn?next=${next}">Sign In</a>.</p>
</#if>
<#include '/includes/after_content.ftl'>