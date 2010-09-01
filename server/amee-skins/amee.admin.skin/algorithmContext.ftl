<#assign sectionName = 'admin'>

<#include '/includes/before_content.ftl'>

<h1>Administration</h1>

<p><a href='/admin'>Admin</a> /
   <a href='/admin/algorithmContexts'>Algorithm Contexts</a> /
   <a href='/admin/algorithmContexts/${algorithmContext.uid}'>${algorithmContext.name}</a></p>

<h2>Algorithm Context Details</h2>

<p>Name: ${algorithmContext.name}<br/>
   Created: ${algorithmContext.created?datetime}<br/>
   Modified: ${algorithmContext.modified?datetime}<br/>
</p>

<#if canModify()>
    <h2>Update Algorithm Context</h2>
    <p>
        <form action='/admin/algorithmContexts/${algorithmContext.uid}?method=put' method='POST' enctype='application/x-www-form-urlencoded'>
            Name: <input name='name' value='${algorithmContext.name}' type='text' size='30'/><br/>
            Content: <textarea name='content' rows='15' cols='60'>${algorithmContext.content}</textarea><br/><br/>
            <input type='submit' value='Update'/>
        </form>
    </p>
</#if>

<#include '/includes/after_content.ftl'>